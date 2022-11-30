import akka.actor.typed.{ActorSystem, Behavior}
import area.{FireStation, Sensor}
import area.AreaUtils.State
import com.typesafe.config.ConfigFactory
import gui.SimpleGUI

import java.awt.Dimension
import java.awt.Toolkit

@main def runFireStation1(): Unit =
  startupWithRole("FireStation", 2551)(FireStation(1))

@main def runSensor1Area1(): Unit =
  startupWithRole("Sensor", 2552)(Sensor(1, 1))

@main def runSensor2Area1(): Unit =
  startupWithRole("Sensor", 2553)(Sensor(2, 1))
  

@main def runFireStation2(): Unit =
  startupWithRole("FireStation", 2561)(FireStation(2))



@main def mainGui(): Unit =
  runGui(1)
  def runGui(idArea: Int): Unit =
    val screenSize = Toolkit.getDefaultToolkit.getScreenSize
    val frontendGui = SimpleGUI(screenSize.getWidth.toInt / 3, screenSize.getHeight.toInt / 5 * 3, idArea) // init the gui
    var list = List[(Int, Int)]()
    for i <- 1 to 100 by 10 do list = list :+ (i, i)
    frontendGui.render(list)

def startupWithRole[X](role: String, port: Int)(root: => Behavior[X]): ActorSystem[X] =
  val config = ConfigFactory
    .parseString(s"""
      akka.remote.artery.canonical.port=$port
      akka.cluster.roles = [$role]
      """)
    .withFallback(ConfigFactory.load("application"))

  // Create an Akka system
  ActorSystem(root, "ClusterSystem", config)