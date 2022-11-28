package area
import  area.Message

object AreaUtils:
  type Area = Int
  type WaterLevel = Int

  enum FireStationMsg extends Message:
    case ALARM(data: WaterLevel)
    case OK(data: WaterLevel)
