import akka.actor.typed.ActorSystem
import area.FireStation
import area.AreaUtils.MSG
import gui.SimpleGUI

import java.awt.Dimension
import java.awt.Toolkit

@main def main(): Unit =
  println("Hello world!")

  val screenSize = Toolkit.getDefaultToolkit.getScreenSize
  val frontendGui = SimpleGUI(screenSize.getWidth.toInt / 2, screenSize.getHeight.toInt / 5 * 3) // init the gui
  var list = List[(Int, Int)]()
  for i <- 1 to 100 by 10 do list = list :+ (i, i)
  frontendGui.render(list)

  val system = ActorSystem[MSG](FireStation(1), "FireStation")
  system ! MSG.ALARM

@main def run(): Unit =
  println("wow")
