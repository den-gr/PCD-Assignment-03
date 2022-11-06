package simulation;

import simulation.monitor.ConcurrentSimulator;
import simulation.task.TaskSimulator;
import simulation.monitor.SimpleWaitMonitor;
import simulation.monitor.SimpleWaitMonitorImpl;
import utils.Chrono;
import simulation.gui.SimulationView;
import simulation.basic.Simulator;

import java.util.LinkedList;
import java.util.List;
import java.util.OptionalDouble;

public class SimulationExecutor {
    final static int WINDOW_SIZE = 700;
    final static int SIMULATION_SIZE = 3;
    private final List<Integer> nBodies;
    private final List<Integer> nSteps;
    private final List<Integer> nThreads;
    private final SimulationView viewer;
    private final SimpleWaitMonitor monitor = new SimpleWaitMonitorImpl();
    private final Chrono ch;
    private final boolean useGui;


    public SimulationExecutor(boolean USE_GUI, List<Integer> nBodies, List<Integer> nSteps, List<Integer> nThreads){
        this.nBodies = nBodies;
        this.nSteps = nSteps;
        this.nThreads = nThreads;
        this.useGui = USE_GUI;
        if(USE_GUI){
            this.viewer = new SimulationView(WINDOW_SIZE,WINDOW_SIZE);
            this.viewer.getFrame().setStartHandler((a) -> {
                monitor.simpleNotify();
                this.viewer.getFrame().setFocusOnSimulation();
            });
        }else{
            viewer = null;
        }
        this.ch = new Chrono();
    }

    public void run(){
       do{
            Simulator sim = new ActorSimulator(viewer, nBodies.get(0), SIMULATION_SIZE);
            ch.start();
            sim.execute(nSteps.get(0));
            ch.stop();
            System.out.println("Executor execution, time " + ch.getTime()/1000.0);
            if(useGui) monitor.simpleWait();
        } while (useGui);
    }

    /**
     * Method for calculate performance of program
     */
    public void runMultiple(int numIteration){
        Simulator sim;
        for (Integer n_thread : nThreads){
            for (Integer nBody : nBodies) {
                for(Integer nStep: nSteps){
                    List<Long> times = new LinkedList<>();
                    System.out.println("nBody: " + nBody + " |nStep " + nStep + " |nThread: " + n_thread);
                    for(int i = 0; i < numIteration; i++) {
                        sim = new ConcurrentSimulator(viewer, nBody, SIMULATION_SIZE, n_thread);
                        ch.start();
                        sim.execute(nStep);
                        ch.stop();
                        times.add(ch.getTime());
//                        System.out.println(">>>>> NumIteration: " + (i + 1) +  " | Execution time: " + ch.getTime() / 1000.0 + " sec");
                    }
                    OptionalDouble optAverage = times.stream().mapToLong(m -> m).average();
                    if(optAverage.isPresent()){
                        System.out.println(">>>>>@@@@ Average time "  + optAverage.getAsDouble() / 1000.0 + " sec");
                    }else{
                        System.err.println("List of times is empty");
                    }
                }
            }
        }
    }

    /**
     * Run serial version of program
     */
    public void runSerial(){
        Chrono ch = new Chrono();
        Simulator sim = new SerialSimulator(viewer, nBodies.get(0), SIMULATION_SIZE);
        ch.start();
        sim.execute(nSteps.get(0));
        ch.stop();
        System.out.println("SERIAL: nBody: " + nBodies.get(0) + " |nStep " + nSteps.get(0));
        System.out.println("Executor execution, time " + ch.getTime()/1000.0);
    }
}
