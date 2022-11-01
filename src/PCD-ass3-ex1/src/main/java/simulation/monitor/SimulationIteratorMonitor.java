package simulation.monitor;

public interface SimulationIteratorMonitor extends StopSimulation {
    void incrementCounter();

    boolean isSimulationNotOver();

    void stop();
}
