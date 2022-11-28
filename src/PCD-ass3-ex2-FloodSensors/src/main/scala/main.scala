import akka.actor.typed.{ActorSystem, Behavior}
import area.{FireStation, Sensor}
import area.AreaUtils.FireStationMsg
import com.typesafe.config.ConfigFactory
import gui.SimpleGUI

import java.awt.Dimension
import java.awt.Toolkit

@main def main(): Unit =
//  runGui()


  startupWithRole("FireStation", 2551)(FireStation(1))
  startupWithRole("Sensor", 2551)(Sensor())
//  ActorSystem[FireStationMsg](FireStation(1), "FireStation")

@main def run(): Unit =
  println("wow")


def startupWithRole[X](role: String, port: Int)(root: => Behavior[X]): ActorSystem[X] =
  val config = ConfigFactory
    .parseString(s"""
      akka.remote.artery.canonical.port=$port
      akka.cluster.roles = [$role]
      """)
    .withFallback(ConfigFactory.load("application"))

  // Create an Akka system
  ActorSystem(root, "ClusterSystem", config)

def runGui(): Unit =
  val screenSize = Toolkit.getDefaultToolkit.getScreenSize
  val frontendGui = SimpleGUI(screenSize.getWidth.toInt / 2, screenSize.getHeight.toInt / 5 * 3) // init the gui
  var list = List[(Int, Int)]()
  for i <- 1 to 100 by 10 do list = list :+ (i, i)
  frontendGui.render(list)