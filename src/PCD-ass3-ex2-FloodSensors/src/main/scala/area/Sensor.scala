package area

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

  def apply(): Behavior[SensorMsg] = Behaviors.setup(ctx =>
    ctx.system.receptionist ! Receptionist.Subscribe(FireStation.Service, ctx.self);
//    ctx.system.receptionist ! Receptionist.Register(Service, ctx.self);
    Behaviors.withTimers(timers =>
      timers.startSingleTimer("Measuring", Measuring(), getTimeout)
      new SensorBehaviour(ctx)
    )
  )

  sealed trait SensorState
  case class Measuring() extends SensorState with Message
  case class Setup() extends SensorState with Message

  private class SensorBehaviour(context: ActorContext[SensorMsg]) extends AbstractBehavior[SensorMsg](context):
    var fireStationRef: List[ActorRef[State]] = List.empty

    override def onMessage(msg: SensorMsg): Behavior[SensorMsg] = msg match
      case m: Receptionist.Listing =>
//        if m.servicesWereAddedOrRemoved then context.log.info("added or removed")
        if m.serviceInstances(FireStation.Service).toList != fireStationRef then
          context.log.info("Register FireStation")
          fireStationRef = m.serviceInstances(FireStation.Service).toList
        else context.log.info(s"Empty list $fireStationRef")
        this
      case Measuring() =>
        val data = Random.nextInt(100)
        context.log.info(s"New measuring $data")
        if fireStationRef.nonEmpty then fireStationRef.head ! Ok(data)
        Behaviors.withTimers(timers =>
          timers.startSingleTimer("Measuring", Measuring(), getTimeout)
          this
        )
      case m =>
        context.log.info(s"Not supported message type $m")
        this

  def getTimeout: FiniteDuration = FiniteDuration(3000 + Random.nextInt(500), TimeUnit.MILLISECONDS)
