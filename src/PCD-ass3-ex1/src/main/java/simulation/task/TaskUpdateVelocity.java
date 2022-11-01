package simulation.task;

import simulation.basic.Body;
import simulation.basic.Physics;
import simulation.basic.V2d;

import java.util.List;

public class TaskUpdateVelocity extends AbstractTask {

    public TaskUpdateVelocity(int start, int end, List<Body> bodies){
        super(start, end, bodies);
    }

    @Override
    public Void call() {
        for (int i = start; i < end && !isStopped; i++) {
            Body b = bodies.get(i);
            /* compute total force on bodies */
            V2d totalForce = Physics.computeTotalForceOnBody(b, bodies);

            /* compute instant acceleration */
            V2d acc = new V2d(totalForce).scalarMul(1.0 / b.getMass());

            /* update velocity */
            b.updateVelocity(acc, DT);
        }
        return null;
    }
}
