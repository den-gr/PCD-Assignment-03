package simulation.actor.executor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import simulation.actor.coordinator.CoordinatorActor;
import simulation.actor.coordinator.CoordinatorMsg;
import simulation.basic.Body;
import simulation.basic.Boundary;
import simulation.basic.Physics;
import simulation.basic.V2d;

import java.util.ArrayList;

public class ExecutorActor extends AbstractBehavior<ExecutorMsg> {

    public record UpdateVelocityMsg(ArrayList<Body> bodies) implements ExecutorMsg {
    }

    public record UpdatePositionMsg(ArrayList<Body> bodies) implements ExecutorMsg {
    }

    private final int myStart;
    private final int myEnd;
    private final Boundary bounds;
    private final double dt;

    private final ActorRef<CoordinatorMsg> myCoordinator;


    public static Behavior<ExecutorMsg> create(int start, int end,  Boundary bounds, double dt, ActorRef<CoordinatorMsg> myCoordinator){
        return Behaviors.setup(ctx -> new ExecutorActor(ctx, start, end, bounds, dt, myCoordinator));
    }

    public ExecutorActor(ActorContext<ExecutorMsg> context, int start, int end, Boundary bounds, double dt, ActorRef<CoordinatorMsg> myCoordinator) {
        super(context);
        this.myStart = start;
        this.myEnd = end;
        this.bounds = bounds;
        this.dt = dt;
        this.myCoordinator = myCoordinator;
    }

    @Override
    public Receive<ExecutorMsg> createReceive() {
        return newReceiveBuilder()
                    .onMessage(UpdateVelocityMsg.class, this::onUpdateVelocityMsg)
                    .onMessage(UpdatePositionMsg.class, this::onUpdatePositionMsg)
                    .build();
    }

    private Behavior<ExecutorMsg> onUpdateVelocityMsg(UpdateVelocityMsg message){
        ArrayList<Body> bodies = message.bodies;

        for (int i = 0; i < bodies.size(); i++) {
            Body b = bodies.get(i);

            /* compute total force on bodies */
            V2d totalForce = Physics.computeTotalForceOnBody(b, bodies);

            /* compute instant acceleration */
            V2d acc = new V2d(totalForce).scalarMul(1.0 / b.getMass());

            /* update velocity */
            b.updateVelocity(acc, dt);
        }

        /* update virtual time */
//        getContext().getLog().info("Send feedback");
        this.myCoordinator.tell(new CoordinatorActor.VelocityUpdateFeedback(bodies));
        return this;
    }

    private Behavior<ExecutorMsg> onUpdatePositionMsg(UpdatePositionMsg message){
        ArrayList<Body> bodies = message.bodies;

        /* compute bodies new pos */

        for (Body b : bodies) {
            b.updatePos(dt);
        }

        /* check collisions with boundaries */

        for (Body b : bodies) {
            b.checkAndSolveBoundaryCollision(bounds);
        }

//        getContext().getLog().info("Send feedback");
        this.myCoordinator.tell(new CoordinatorActor.PositionUpdateFeedback(bodies));
        return this;
    }
}
