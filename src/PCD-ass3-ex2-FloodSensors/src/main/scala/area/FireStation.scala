package area

import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import area.AreaUtils.*

import scala.language.postfixOps
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import gui.SimpleGUI
import area.AreaUtils.*
object FireStation:


  val Service: ServiceKey[FireStationMsg] = ServiceKey[FireStationMsg]("FireStation")

  def apply(area: Area): Behavior[FireStationMsg] = Behaviors.setup(ctx =>
    ctx.system.receptionist ! Receptionist.Register(Service, ctx.self)
    ctx.self ! Clean()
    new FireStationActor(ctx, area)
  )

  private class FireStationActor(context: ActorContext[FireStationMsg], val area: Area)
      extends AbstractBehavior[FireStationMsg](context):
    private val gui = SimpleGUI(areaWidthUnit*5, areaHeightUnit * 5, area)
    private var activeSensors: Set[Int] = Set.empty

    override def onMessage(msg: FireStationMsg): Behavior[FireStationMsg] = msg match
      case Alarm(d) =>
        println(s"Alarm in area $area")
        this
      case Ok(sensorId, sensorArea, data) =>
        if sensorArea == area then
          activeSensors = activeSensors + sensorId
        gui.printWaterLevel(sensorId, data, activeSensors)
        println(s"OK => $data")
        this
      case Hello(sensorId, sensorArea, (x, y)) =>
        gui.drawNewSensor(sensorId, x, y)
        this
      case Clean() =>
        println("cleaning")
        gui.setActiveSensors(activeSensors)
        activeSensors = Set.empty
        clean()


    private def clean(): Behavior[FireStationMsg] = {
      Behaviors.withTimers[FireStationMsg] { timers =>
        timers.startSingleTimer("Cleaning", Clean(), FiniteDuration(5, TimeUnit.SECONDS))
        this
      }
    }
