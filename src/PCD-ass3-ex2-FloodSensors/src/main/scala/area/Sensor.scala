package area

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import area.AreaUtils.*
import area.AreaUtils.FireStationMsg.*

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

object Sensor:
  type SenMsg = SensorMsg | Receptionist.Listing

  def apply(): Behavior[SenMsg] =
    Behaviors.setup(ctx =>
      Behaviors.withTimers(timers =>
        timers.startSingleTimer("Measuring", SensorMsg.Measuring, getTimeout)
        new SensorBehaviour(ctx)
      )
     )

  enum SensorMsg extends Message:
    case HeartBeatRequest
    case HeartBeat
    case Measurement(data: Int)
    case Measuring
    case Setup

  private class SensorBehaviour(context: ActorContext[SenMsg])
      extends AbstractBehavior[SenMsg](context):
    import SensorMsg.*
    var fireStationRef: ActorRef[FireStationMsg] = _

    override def onMessage(msg: SenMsg): Behavior[SenMsg] = msg match
      case m: Receptionist.Listing =>
          context.log.info("Register FireStation")
          fireStationRef = m.serviceInstances(FireStation.Service).head
          this
      case Measuring =>
        val data = Random.nextInt(100)
        context.log.info(s"New measuring ${data}")
        if(fireStationRef != null) fireStationRef ! OK(data)
        Behaviors.withTimers(timers =>
          timers.startSingleTimer("Measuring", Measuring, getTimeout)
          this
        )
      case m =>
        context.log.info(s"Not supported message type $m")
        this



  def getTimeout: FiniteDuration = FiniteDuration(3000 + Random.nextInt(500), TimeUnit.MILLISECONDS)
