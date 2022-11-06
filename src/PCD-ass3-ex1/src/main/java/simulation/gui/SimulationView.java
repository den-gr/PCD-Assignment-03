package simulation.gui;

import simulation.basic.Body;
import simulation.basic.Boundary;
import simulation.basic.P2d;
import simulation.basic.V2d;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;

/**
 * Simulation view
 *
 * @author aricci
 *
 */
public class SimulationView {
        
	private final VisualiserFrame frame;
	private Boundary bounds = null;

    /**
     * Creates a view of the specified size (in pixels)
     * 
     * @param w
     * @param h
     */
    public SimulationView(int w, int h){
    	frame = new VisualiserFrame(w,h);
    }
        
    public void display(List<P2d> coordinates, double vt, long iter){
		if(bounds == null){
			throw new IllegalStateException("Bounds or bodies are not set");
		}
		frame.display(coordinates, vt, iter, bounds);
    }

	public void setBounds(Boundary bounds){
		this.bounds = bounds;
	}

	public VisualiserFrame getFrame(){
		return this.frame;
	}
}
