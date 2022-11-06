package simulation.gui;

import simulation.basic.Boundary;
import simulation.basic.P2d;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.*;
import java.util.List;

public class VisualiserFrame extends JFrame {

    private static VisualiserPanel panel;
    private static JButton start;
    private static JButton stop;

    public VisualiserFrame(int w, int h){
        setTitle("Bodies Simulation");
        setSize(w,h);
        setResizable(false);
        panel = new VisualiserPanel(w,h);
        getContentPane().setLayout(new BorderLayout(0,0));
        JPanel panel2 = new JPanel();
        start = new JButton("Start");
        stop = new JButton("Stop");
        panel2.setLayout(new FlowLayout());
        panel2.add(start);
        panel2.add(stop);
        getContentPane().add(panel2, BorderLayout.SOUTH);

        getContentPane().add(panel, BorderLayout.CENTER);
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent ev){
                System.exit(0);
            }
            public void windowClosed(WindowEvent ev){
                System.exit(0);
            }
        });
        this.setVisible(true);
    }

    public void setFocusOnSimulation(){
        panel.requestFocusInWindow();
    }

    public void setStartHandler(ActionListener al){
        start.addActionListener(al);
    }

    public  void setStopHandler(ActionListener al){
        stop.addActionListener(al);
    }


    public void display(List<P2d> coordinates, double vt, long iter, Boundary bounds){
        try {
            SwingUtilities.invokeAndWait(() -> {
                panel.display(coordinates, vt, iter, bounds);
                repaint();
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    };

    public void updateScale(double k) {
        panel.updateScale(k);
    }
}
