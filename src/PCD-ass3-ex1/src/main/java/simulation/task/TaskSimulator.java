package simulation.task;

import simulation.basic.AbstractSimulator;
import simulation.gui.SimulationView;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class TaskSimulator extends AbstractSimulator {
    private final int nThreads;
    private volatile boolean isStopped = false;

    public TaskSimulator(SimulationView viewer, int nBodies, int dimSimulation, int nThreads) {
        super(viewer, nBodies, dimSimulation);
        this.nThreads = nThreads;
    }
    @Override
    public void execute(long nSteps) {
        ExecutorService exec =  Executors.newFixedThreadPool(nThreads);

        int myStart;
        int myEnd;
        List<AbstractTask> callableVelocity = new LinkedList<>();
        List<AbstractTask> callablesPosition = new LinkedList<>();
        if(viewer != null){ // set stop button handler
            SimulationView.VisualiserFrame.setStopHandler((a) -> {
                callablesPosition.forEach(AbstractTask::cancel);
                callableVelocity.forEach(AbstractTask::cancel);
                isStopped = true;
            });
        }
        for(int i = 0; i < nThreads; i++){
            myStart = bodies.size() * i / nThreads;
            myEnd = bodies.size() * (i+1) / nThreads;
            callableVelocity.add(new TaskUpdateVelocity(myStart, myEnd, bodies));
            callablesPosition.add(new TaskUpdatePosition(myStart, myEnd, bodies, bounds));
        }
        //Run simulation
        for(int k = 0; k <= nSteps && !isStopped; k++){
            submitAllAndWait(exec, callableVelocity);
            submitAllAndWait(exec, callablesPosition);
            if(viewer != null && !isStopped) viewer.display(k*0.001, k);
        }
        exec.shutdownNow();
        try {
            boolean safeTermination = exec.awaitTermination(5, TimeUnit.SECONDS);
            if(!safeTermination) System.err.println("Not safe termination");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void submitAllAndWait(ExecutorService exec, List<AbstractTask> callables){
        try {
            for(Future<Void> f : exec.invokeAll(callables)){
                f.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
