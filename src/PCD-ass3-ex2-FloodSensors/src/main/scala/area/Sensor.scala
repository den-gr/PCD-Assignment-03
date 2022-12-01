package area

import akka.actor.Terminated
import akka.actor.typed.internal.Terminate
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import area.AreaUtils.*

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

object Sensor:
  type SensorMsg = SensorState | Receptionist.Listing

  val Service: ServiceKey[SensorMsg] = ServiceKey[SensorMsg]("Sensor")
  def apply(id: ID, area: Area, coordinates: (Int, Int)): Behavior[SensorMsg] = Behaviors.setup(ctx =>

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
      new SensorBehaviour(ctx, id, area, (coordinates))
    )
  )

  sealed trait SensorState
  case class Measuring() extends SensorState with Message
  case class Setup(d: String) extends SensorState with Message
  case class Leave(idSensor: Int, leader: Int) extends SensorState with Message


  private class SensorBehaviour(context: ActorContext[SensorMsg], val id: ID, val area: Area,val coordinates: (Int,Int))
      extends AbstractBehavior[SensorMsg](context):
    var fireStationRef: List[ActorRef[State]] = List.empty
    var sensorsRefs: List[ActorRef[SensorMsg]] = List.empty

    override def onMessage(msg: SensorMsg): Behavior[SensorMsg] = msg match
      case m: Receptionist.Listing =>
        m match
          case FireStation.Service.Listing(listings) =>
            println(s"listing fs $listings")
            if fireStationRef.isEmpty && listings.nonEmpty then
              listings.foreach(_ ! Hello(id, area, coordinates))
            else if listings.size > fireStationRef.size then
              listings.last ! Hello(id, area, coordinates)
            fireStationRef = listings.toList
          case Service.Listing(listings) =>
            sensorsRefs = listings.toList
        this
      case Measuring() =>
        val data = Random.nextInt(100)
        println(s"New measuring: $data")
//        sensorsRefs.foreach(e => if e != context.self then e ! Setup(data.toString))
        if fireStationRef.nonEmpty then fireStationRef.foreach(_ ! Ok(id, area, data))
        Behaviors.withTimers(timers =>
          timers.startSingleTimer("Measuring", Measuring(), getTimeout)
          this
        )
      case Setup(d) =>
        println(s"Setup $d")
        this

  def getTimeout: FiniteDuration = FiniteDuration(3000 + Random.nextInt(500), TimeUnit.MILLISECONDS)
