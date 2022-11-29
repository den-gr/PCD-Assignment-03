package area

import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import area.AreaUtils.*

import scala.language.postfixOps
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import gui.SimpleGUI

import java.awt.Toolkit
object FireStation:


  val Service: ServiceKey[FireStationMsg] = ServiceKey[FireStationMsg]("FireStation")

  def apply(area: Area): Behavior[FireStationMsg] = Behaviors.setup(ctx =>
    ctx.system.receptionist ! Receptionist.Register(Service, ctx.self)
    new FireStationActor(ctx, area)
  )

  private class FireStationActor(context: ActorContext[FireStationMsg], val area: Area)
      extends AbstractBehavior[FireStationMsg](context):
    private val screenSize = Toolkit.getDefaultToolkit.getScreenSize
    private val gui = SimpleGUI(screenSize.getWidth.toInt / 3, screenSize.getHeight.toInt / 5 * 3, 1)

    override def onMessage(msg: FireStationMsg): Behavior[FireStationMsg] = msg match
      case Alarm(d) =>
        println(s"Alarm in area $area")
        this
      case Ok(d) =>
        gui.printWaterLevel(1, d)
        println(s"OK => $d")
        this