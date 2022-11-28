package area

import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import area.AreaUtils.*
import area.AreaUtils.FireStationMsg.*
import area.Sensor.SensorMsg.Setup
import scala.language.postfixOps
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

object FireStation:

  val Service = ServiceKey[FireStationMsg]("FireStationService")
  def apply(area: Area): Behavior[FireStationMsg] = Behaviors.setup(ctx =>
    ctx.system.receptionist ! Receptionist.Register(Service, ctx.self)
    new FireStationActor(ctx, area)
  )

  private class FireStationActor(context: ActorContext[FireStationMsg], val area: Area)
      extends AbstractBehavior[FireStationMsg](context):

    override def onMessage(msg: FireStationMsg): Behavior[FireStationMsg] = msg match
      case ALARM(d) =>
        println(s"Alarm in area $area")
        this
      case OK(d) =>
        println(s"OK => $d")
        this
