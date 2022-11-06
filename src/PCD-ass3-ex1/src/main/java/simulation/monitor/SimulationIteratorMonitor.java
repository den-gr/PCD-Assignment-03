package simulation.monitor;

import simulation.basic.Body;

import java.util.ArrayList;

public interface SimulationIteratorMonitor extends StopSimulation {
    void incrementCounter();

    boolean isSimulationNotOver();

    void stop();

    void setBodies(ArrayList<Body> bodies);
}
