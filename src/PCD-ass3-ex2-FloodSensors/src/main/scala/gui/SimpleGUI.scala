package gui

import javax.swing.{JFrame, JPanel, SwingUtilities}
import java.awt.{Canvas, Color, Dimension, Graphics}

class SimpleGUI(val width: Int, val height: Int):
  self => // self-types, used to take the val reference inside the inner class
  private val elementWidth = width/30
  private val frame = JFrame()
  private val canvas = Environment()
  frame.setSize(width, height)
  frame.setVisible(true)
  canvas.setVisible(true)
  frame.setLocationRelativeTo(null)
  canvas.setSize(width, height)
  frame.add(canvas)
  def render(elements: List[(Int, Int)]): Unit = SwingUtilities.invokeLater { () =>
    canvas.elements = elements
    canvas.invalidate()
    canvas.repaint()
  }

  private class Environment() extends JPanel:
    val BLUE: Color = Color(109,109, 182)
    val RED: Color = Color(213,161, 161)
    val GREEN: Color = Color(166,213, 160)
    val YELLOW: Color = Color(234,243, 68)
    var elements: List[(Int, Int)] = List.empty
    override def getPreferredSize = new Dimension(self.width, self.height)
    override def paintComponent(graphics: Graphics): Unit =
      val hunit = self.height/5
      val wunit = self.width/5

      graphics.clearRect(0, 0, self.width, self.height)

      fillZone(BLUE, 0,0, wunit*3, hunit*3)
      fillZone(GREEN, 0, hunit*3, wunit*3, hunit*2)
      fillZone(RED, wunit*3, 0, wunit * 2, hunit * 3)
      fillZone(YELLOW, wunit * 3, hunit * 3, wunit * 2, hunit * 2)


      drawSensor(Color.WHITE,  wunit * 4, hunit * 4)

      graphics.setColor(Color.BLACK)
      elements.foreach((x, y) => graphics.drawOval(x, y, elementWidth, elementWidth))

      def fillZone(c: Color, x: Int, y: Int, w: Int, h: Int): Unit =
        graphics.setColor(c)
        graphics.fillRect(x,y, w, h)

      def drawSensor(c: Color, x: Int, y: Int): Unit =
        graphics.setColor(c)
        graphics.fillOval(x, y, elementWidth, elementWidth)
        graphics.setColor(Color.BLACK)
        graphics.drawOval(x, y, elementWidth, elementWidth)