import akka.actor.typed.{ActorSystem, Behavior}
import area.{FireStation, Sensor}
import area.AreaUtils.State
import com.typesafe.config.ConfigFactory
import gui.SimpleGUI

import java.awt.Dimension
import java.awt.Toolkit
import area.AreaUtils.*

val AREA_1 = 1
val AREA_2 = 2

/** Area 1
  */
@main def runFireStation1(): Unit =
  startupWithRole("FireStation", 2550)(FireStation(AREA_1))

@main def runSensor1Area1(): Unit =
  startupWithRole("Sensor", 2551)(Sensor(1, AREA_1, (areaWidthUnit / 4 * 3, areaHeightUnit * 1)))

@main def runSensor2Area1(): Unit =
  startupWithRole("Sensor", 2552)(Sensor(2, AREA_1, (areaWidthUnit * 1, areaHeightUnit * 2)))

@main def runSensor3Area1(): Unit =
  startupWithRole("Sensor", 2554)(Sensor(3, AREA_1, (areaWidthUnit * 2, areaHeightUnit * 3)))

/** Area 2
  */
@main def runFireStation2(): Unit =
  startupWithRole("FireStation", 2560)(FireStation(AREA_2))

@main def runSensor4Area2(): Unit =
  startupWithRole("Sensor", 2561)(Sensor(4, AREA_2, (areaWidthUnit * 4, areaHeightUnit * 2)))


def startupWithRole[X](role: String, port: Int)(root: => Behavior[X]): ActorSystem[X] =
  val config = ConfigFactory
    .parseString(s"""
      akka.remote.artery.canonical.port=$port
      akka.cluster.roles = [$role]
      """)
    .withFallback(ConfigFactory.load("application"))

  // Create an Akka system
  ActorSystem(root, "ClusterSystem", config)

