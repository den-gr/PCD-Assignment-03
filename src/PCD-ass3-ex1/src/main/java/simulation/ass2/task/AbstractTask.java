package simulation.ass2.task;

import simulation.basic.Body;

import java.util.List;
import java.util.concurrent.Callable;

public abstract class AbstractTask implements Callable<Void> {
    protected final static Double DT = 0.001;
    protected final int start;
    protected final int end;
    protected final List<Body> bodies;
    protected volatile Boolean isStopped = false;

    public AbstractTask(int start, int end, List<Body> bodies){
        this.start = start;
        this.end = end;
        this.bodies = bodies;
    }

    public void cancel(){
        isStopped = true;
    }
}
