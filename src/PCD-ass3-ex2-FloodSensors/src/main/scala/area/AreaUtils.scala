package area
import akka.actor.typed.receptionist.Receptionist
import area.Message

import java.awt.Toolkit

object AreaUtils:
  type Area = Int
  type WaterLevel = Int
  type ID = Int

  sealed trait State
  case class Alarm(data: WaterLevel) extends State with Message
  case class Ok(sensorId: ID, area: Area, data: WaterLevel) extends State with Message
  case class Hello(sensorId: Int, area: Area, coordinates: (Int,Int)) extends  State with Message

  type FireStationMsg = State | Receptionist.Listing

  private val screenSize = Toolkit.getDefaultToolkit.getScreenSize
  val areaWidthUnit: Int = screenSize.getWidth.toInt / 15
  val areaHeightUnit: Int = screenSize.getHeight.toInt / 25 * 3