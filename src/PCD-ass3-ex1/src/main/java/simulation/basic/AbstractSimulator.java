package simulation.basic;

import simulation.gui.SimulationView;

import java.util.ArrayList;

public abstract class AbstractSimulator implements Simulator {
    protected final SimulationView viewer;

    /* bodies in the field */
    protected ArrayList<Body> bodies;

    /* boundary of the field */
    protected Boundary bounds;

    /* virtual time */
    protected double vt;

    /* virtual time step */
    protected double dt;

    public AbstractSimulator(SimulationView viewer, int nBodies, int dimSimulation) {
        this.viewer = viewer;
        /* initializing boundary and bodies */
        bounds = WorldGenerator.createBoundary(dimSimulation);
        bodies = WorldGenerator.createBodies(nBodies, bounds);
        if(this.viewer != null){
            viewer.setBodies(bodies);
            viewer.setBounds(bounds);
        }

    }
}
