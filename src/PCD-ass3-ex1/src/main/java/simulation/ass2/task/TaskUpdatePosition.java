package simulation.ass2.task;

import simulation.basic.Body;
import simulation.basic.Boundary;

import java.util.List;

public class TaskUpdatePosition extends AbstractTask{
    private final Boundary bounds;

    public TaskUpdatePosition(int start, int end, List<Body> bodies, Boundary bounds){
        super(start, end, bodies);
        this.bounds = bounds;
    }

    @Override
    public Void call() {
        for (int i = start; i < end && !isStopped; i++) {
            Body b = bodies.get(i);
            b.updatePos(DT);
            b.checkAndSolveBoundaryCollision(bounds);
        }
        return null;
    }
}
