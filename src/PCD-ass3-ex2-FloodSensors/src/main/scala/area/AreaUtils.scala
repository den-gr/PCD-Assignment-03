package area
import  area.Message

object AreaUtils:
  type Area = Int;

  enum MSG extends Message:
    case ALARM, OK
