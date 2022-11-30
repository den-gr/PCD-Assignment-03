package area
import akka.actor.typed.receptionist.Receptionist
import area.Message

object AreaUtils:
  type Area = Int
  type WaterLevel = Int
  type ID = Int

  sealed trait State
  case class Alarm(data: WaterLevel) extends State with Message
  case class Ok(sensorId: ID, area: Area, data: WaterLevel) extends State with Message

  type FireStationMsg = State | Receptionist.Listing