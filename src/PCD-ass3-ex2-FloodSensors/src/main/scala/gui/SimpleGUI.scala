package gui

import area.AreaUtils.Area
import area.AreaUtils.*
import area.FireStation

import javax.swing.{JFrame, JPanel, SwingUtilities}
import java.awt.{BorderLayout, Canvas, Color, Component, Dimension, FlowLayout, Graphics, LayoutManager}

class SimpleGUI(val width: Int, val height: Int, idArea: Area):
  self => // self-types, used to take the val reference inside the inner class
  private final val stationPanelSize: Int = 250
  private val elementWidth = width/40 + height/40
  private val frame = JFrame()
  private val canvas = Environment()
  private val stationPanel = FireStationJPanel(stationPanelSize,200, idArea)
  frame.setTitle(s"Fire Station GUI of area $idArea")
  frame.setSize(width+stationPanelSize, height)
  frame.setVisible(true)
  canvas.setVisible(true)
  stationPanel.setVisible(true)
  if idArea == 1 then
    frame.setLocation(elementWidth, areaHeightUnit)
  else
    frame.setLocation(width + stationPanelSize, areaHeightUnit)

  canvas.setSize(width, height)
  stationPanel.setSize(200, height)
  frame.getContentPane.setLayout(BorderLayout(0,0))
  frame.getContentPane.add(canvas, BorderLayout.CENTER)
  frame.getContentPane.add(stationPanel, BorderLayout.EAST)
  val hunit: Int = areaHeightUnit
  val wunit: Int = areaWidthUnit

  export stationPanel.printWaterLevel

  def drawNewSensor(sensorId: Int, x: Int, y: Int): Unit = SwingUtilities.invokeLater { () =>
    canvas.sensors = canvas.sensors + (sensorId -> (x, y))
    canvas.sensors = Map(canvas.sensors.toSeq.sortWith(_._1 < _._1): _*)
    canvas.invalidate()
    canvas.repaint()
  }

  def setActiveSensors(set: Set[Int]): Unit =SwingUtilities.invokeLater { () =>
    canvas.activeSensors = set
    canvas.invalidate()
    canvas.repaint()
  }


  private class Environment() extends JPanel:
    val BLUE: Color = Color(109,109, 182)
    val RED: Color = Color(213,161, 161)
    val GREEN: Color = Color(166,213, 160)
    val YELLOW: Color = Color(234,243, 68)
    var sensors: Map[Int, (Int, Int)] = Map()
    var activeSensors: Set[Int] = Set.empty
    override def getPreferredSize = new Dimension(self.width, self.height)
    override def paintComponent(graphics: Graphics): Unit =

      graphics.clearRect(0, 0, self.width, self.height)

      fillZone(BLUE, 0,0, wunit*3, self.height)
      fillZone(GREEN, wunit*3, 0, wunit * 2, self.height)


      drawSensor(Color.WHITE,  wunit * 4, hunit * 4, 9)

      graphics.setColor(Color.BLACK)
      sensors.filter(e => activeSensors.contains(e._1)).foreach({ case (id->(x,y)) => drawSensor(Color.WHITE, x, y, id)})

      def fillZone(c: Color, x: Int, y: Int, w: Int, h: Int): Unit =
        graphics.setColor(c)
        graphics.fillRect(x,y, w, h)

      def drawSensor(c: Color = Color.WHITE, x: Int, y: Int, id: Int): Unit =
        graphics.setColor(c)
        graphics.fillOval(x, y, elementWidth, elementWidth)
        graphics.setColor(Color.BLACK)
        graphics.drawOval(x, y, elementWidth, elementWidth)
        graphics.drawString(id.toString, x+elementWidth/3, y+elementWidth/4 *3)

