package area

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import area.AreaUtils.*
import area.AreaUtils.MSG.*

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

object Sensor:
  def apply(): Behavior[SensorMsg] = Behaviors.setup(ctx => new SensorBehaviour(ctx))

  enum SensorMsg extends Message:
    case HeartBeatRequest
    case HeartBeat
    case Measurement(data: Int)
    case Measuring
    case Setup

  private class SensorBehaviour(context: ActorContext[SensorMsg]) extends AbstractBehavior[SensorMsg](context):
    import SensorMsg.*
    override def onMessage(msg: SensorMsg): Behavior[SensorMsg] = msg match
      case Setup =>
        Behaviors.withTimers(timers =>
          timers.startSingleTimer("Measuring", Measuring, FiniteDuration(3, TimeUnit.SECONDS))
          activeBehaviour()
        )
      case m =>
        context.log.info(s"Not supported message type $m")
        this

    def activeBehaviour(): Behavior[SensorMsg] = Behaviors.receive[SensorMsg] { (ctx, msg) =>
      msg match
        case Measuring =>
          ctx.log.info(s"New measuring ${Random.nextInt(100)}")
          Behaviors.withTimers(timers =>
            timers.startSingleTimer("Measuring", Measuring, FiniteDuration(3, TimeUnit.SECONDS))
            activeBehaviour()
          )
        case m =>
          context.log.info(s"Not supported message type $m")
          activeBehaviour()
    }
