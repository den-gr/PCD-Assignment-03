package area

import akka.actor.Terminated
import akka.actor.typed.internal.Terminate
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import area.AreaUtils.*
import area.Sensor.SensorMsg
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

object Sensor:
  type SensorMsg = SensorState | Receptionist.Listing
  var Service: ServiceKey[SensorMsg] = _

  def apply(id: ID, area: Area, coordinates: (Int, Int)): Behavior[SensorMsg] = Behaviors.setup(ctx =>
    Service = ServiceKey[SensorMsg]("Sensor" + area);

    ctx.spawnAnonymous[Receptionist.Listing](
      Behaviors.setup(internal =>
        internal.system.receptionist ! Receptionist.Subscribe(FireStation.Service, internal.self);
        internal.system.receptionist ! Receptionist.Subscribe(Service, internal.self);
        Behaviors.receiveMessage(msg =>
          ctx.self ! msg;
          Behaviors.same
        )
      )
    );
    ctx.system.receptionist ! Receptionist.Register(Service, ctx.self);
    Behaviors.withTimers(timers =>
      timers.startSingleTimer("Measuring", Measuring(), getTimeout)
      new SensorBehaviour(ctx, id, area, coordinates)
    )
  )

  sealed trait SensorState
  // activate measuring of water level
  case class Measuring() extends SensorState with Message

  // sensor candidate itself as a leader
  case class LeaderCandidate(candidate: Leader) extends SensorState with Message

  // sensor inform leader if it enter or exit in local alarm state
  case class SensorAlarm(sensorId: ID, active: Boolean) extends SensorState with Message

  // leader send to all nodes for activate their area alarm
  case class AreaAlarm() extends SensorState with Message

  // remove water and disable area alarm
  case class RemoveWater() extends SensorState with Message

  // disable area alarm
  case class DisableAlarm() extends SensorState with Message

  case class Leader(id: ID, leaderRef: ActorRef[SensorMsg])

  private class SensorBehaviour(
      context: ActorContext[SensorMsg],
      val id: ID,
      val area: Area,
      val coordinates: (Int, Int)
  ) extends AbstractBehavior[SensorMsg](context):
    var fireStationRef: List[ActorRef[State]] = List.empty
    var sensorsRefs: List[ActorRef[SensorMsg]] = List.empty
    private var isAreaAlarm: Boolean = false
    private var isLocalAlarm: Boolean = false
    private var leader: Leader = Leader(id, context.self)
    private var currentWaterLevel: WaterLevel = 50
    private var sensorsInAlarm: Set[ID] = Set.empty


    override def onMessage(msg: SensorMsg): Behavior[SensorMsg] = msg match
      case m: Receptionist.Listing =>
        m match
          case FireStation.Service.Listing(listings) => // The number of fire stations is changed
            if fireStationRef.isEmpty && listings.nonEmpty then
              listings.foreach(_ ! Hello(id, area, coordinates))
              context.self ! Measuring()
            else if listings.size > fireStationRef.size then listings.last ! Hello(id, area, coordinates)
            fireStationRef = listings.toList
          case Service.Listing(listings) => // The number of sensors in area is changed
            if listings.toList.size > sensorsRefs.size && leader.id == id then
              listings.toList
                .filter(!sensorsRefs.contains(_))
                .foreach(_ ! LeaderCandidate(Leader(id, context.self)))
              if isAreaAlarm then
                // leader must control if there are a majority nodes with local alarm
                context.self ! SensorAlarm(id, isLocalAlarm)
            else if listings.toList.size < sensorsRefs.size then
              if leader.id == id then
                sensorsInAlarm = Set.empty
                listings.toList.foreach(_ ! DisableAlarm()) // restart alarm voting
              else if !listings.contains(leader.leaderRef) then // leader is failed
                isLocalAlarm = false
                isAreaAlarm = false
                leader = Leader(id, context.self)
                sensorsRefs.foreach(_ ! LeaderCandidate(leader)) // start new election
            sensorsRefs = listings.toList;
        this
      case Measuring() =>
        val waterMeasurement = detectWaterLevel()
        val tooMatchWater = waterMeasurement > MAX_NORMAL_WATER_LEVEL

        if tooMatchWater && !isLocalAlarm then
          isLocalAlarm = true
          leader.leaderRef ! SensorAlarm(id, true)
        if tooMatchWater && !isAreaAlarm then leader.leaderRef ! SensorAlarm(id, true)
        else if !tooMatchWater && isLocalAlarm then
          isLocalAlarm = false
          leader.leaderRef ! SensorAlarm(id, false)

        println(s"Water: $waterMeasurement | leader: ${leader.id} | locAl: $isLocalAlarm | arAl: $isAreaAlarm")
        val msg: State with Message =
          if isAreaAlarm then Alarm(id, area, waterMeasurement) else Ok(id, area, waterMeasurement)
        if fireStationRef.nonEmpty then fireStationRef.foreach(_ ! msg)

        Behaviors.withTimers(timers =>
          timers.startSingleTimer("Measuring", Measuring(), getTimeout)
          this
        )
      case LeaderCandidate(candidate) =>
        if candidate.id <= leader.id then leader = candidate
        else if id == leader.id then candidate.leaderRef ! LeaderCandidate(leader)
        this
      case SensorAlarm(sensorId, active) =>
        if leader.id != id then // I am not a leader so redirect msg
          isLocalAlarm = false
          isAreaAlarm = false
          leader.leaderRef ! SensorAlarm(sensorId, active)
        else
          if active then sensorsInAlarm += sensorId
          else sensorsInAlarm -= sensorId

          if sensorsRefs.size / 2 + 1 <= sensorsInAlarm.size then
            // we have a majority
            sensorsRefs.foreach(_ ! AreaAlarm())
          else if isAreaAlarm && sensorsRefs.size / 2 + 1 > sensorsInAlarm.size then
            // there are not a majority anymore
            sensorsRefs.foreach(_ ! DisableAlarm())
        this
      case AreaAlarm() =>
        isAreaAlarm = true
        context.self ! Measuring() //speed up gui update
        this
      case RemoveWater() =>
        currentWaterLevel -= 30 + Random.nextInt(5)
        if currentWaterLevel < 0 then currentWaterLevel = 0
        context.self ! DisableAlarm()
        this
      case DisableAlarm() =>
        isAreaAlarm = false
        context.self ! Measuring() //speed up gui update
        this

    def detectWaterLevel(): WaterLevel =
      if currentWaterLevel < 100 then currentWaterLevel += (id % 3) + 1 + Random.nextInt(4)
      else if currentWaterLevel < 110 then currentWaterLevel += 1
      currentWaterLevel

  def getTimeout: FiniteDuration = FiniteDuration(3000 + Random.nextInt(500), TimeUnit.MILLISECONDS)
