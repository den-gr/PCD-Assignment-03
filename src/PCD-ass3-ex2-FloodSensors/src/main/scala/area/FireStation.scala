package area

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import area.AreaUtils.*
import scala.language.postfixOps
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import gui.SimpleGUI
import area.AreaUtils.*
import area.Sensor.{RemoveWater, SensorMsg}

object FireStation:
  val Service: ServiceKey[FireStationMsg] = ServiceKey[FireStationMsg]("FireStation")
  var sensorService: ServiceKey[SensorMsg] = _

  def apply(area: Area): Behavior[FireStationMsg] = Behaviors.setup(ctx =>
    sensorService = ServiceKey[SensorMsg]("Sensor" + area)
    ctx.spawnAnonymous[Receptionist.Listing](
      Behaviors.setup(internal =>
        internal.system.receptionist ! Receptionist.Subscribe(sensorService, internal.self);
        Behaviors.receiveMessage(msg =>
          ctx.self ! msg;
          Behaviors.same
        )
      )
    );

    ctx.system.receptionist ! Receptionist.Register(Service, ctx.self)
    ctx.self ! Clean()
    new FireStationActor(ctx, area)
  )

  private class FireStationActor(context: ActorContext[FireStationMsg], val area: Area)
      extends AbstractBehavior[FireStationMsg](context):
    private val gui = SimpleGUI(areaWidthUnit*5, areaHeightUnit * 5, area)
    gui.addActionListenerForButton(_ => removeWater())
    private var activeSensors: Set[Int] = Set.empty
    var sensorsRefs: List[ActorRef[SensorMsg]] = List.empty

    def removeWater(): Unit =
      sensorsRefs.foreach(_ ! RemoveWater())

    override def onMessage(msg: FireStationMsg): Behavior[FireStationMsg] = msg match
      case Alarm(sensorId, sensorArea, data) =>
        activeSensors = activeSensors + sensorId
        gui.updateWaterLevel(sensorId, data, true)
        this
      case Ok(sensorId, sensorArea, data) =>
        activeSensors = activeSensors + sensorId
        gui.updateWaterLevel(sensorId, data)
//        println(s"OK $sensorId => $data")
        this
      case Hello(sensorId, sensorArea, (x, y)) =>
        gui.activeSensors += sensorId // speed up visualisation
        activeSensors += sensorId
        gui.addNewSensor(sensorId, x, y, sensorArea)
        this
      case Clean() =>
        gui.setActiveSensors(activeSensors)
        activeSensors = Set.empty
        clean()
      case m: Receptionist.Listing =>
        m match
          case FireStation.sensorService.Listing(listings) => sensorsRefs = listings.toList
        this

    private def clean(): Behavior[FireStationMsg] = {
      Behaviors.withTimers[FireStationMsg] { timers =>
        timers.startSingleTimer("Cleaning", Clean(), FiniteDuration(5, TimeUnit.SECONDS))
        this
      }
    }
