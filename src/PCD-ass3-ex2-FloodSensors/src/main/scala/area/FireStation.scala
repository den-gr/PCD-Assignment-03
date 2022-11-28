package area

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import area.AreaUtils.*
import area.AreaUtils.MSG.*
import area.Sensor.SensorMsg.Setup

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

object FireStation:

  def apply(area: Area): Behavior[MSG] = Behaviors.setup(new FireStationActor(_,area))


  private class FireStationActor(context: ActorContext[MSG], val area: Area) extends AbstractBehavior[MSG](context):

    override def onMessage(msg: MSG): Behavior[MSG] = msg match
      case ALARM =>
        println(s"Alarm in area $area")
        val sensor  = context.spawn(Sensor(), "Sensor1")
        sensor ! Setup
        this
      case OK =>
        println("OK")
        this
