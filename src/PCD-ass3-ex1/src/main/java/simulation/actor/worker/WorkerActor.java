package simulation.actor.worker;

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

import java.util.LinkedList;
import java.util.List;

/**
 * Worker actor have two cyclic behaviors: update velocity and update position
 */
public class WorkerActor extends AbstractBehavior<WorkerMsg> {
    public record SetupWorkerMsg() implements WorkerMsg{}
    public record UpdateVelocityMsg(List<Body> bodies) implements WorkerMsg {}
    public record UpdatePositionMsg(List<Body> bodies) implements WorkerMsg {}
    private final int myStart;
    private final int myEnd;
    private final Boundary bounds;
    private final double dt;
    private final ActorRef<CoordinatorMsg> myCoordinator;

    public static Behavior<WorkerMsg> create(int start, int end, Boundary bounds, double dt, ActorRef<CoordinatorMsg> myCoordinator){
        return Behaviors.setup(ctx -> new WorkerActor(ctx, start, end, bounds, dt, myCoordinator));
    }

    private WorkerActor(ActorContext<WorkerMsg> context, int start, int end, Boundary bounds, double dt, ActorRef<CoordinatorMsg> myCoordinator) {
        super(context);
        this.myStart = start;
        this.myEnd = end;
        this.bounds = bounds;
        this.dt = dt;
        this.myCoordinator = myCoordinator;
    }

    @Override
    public Receive<WorkerMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(SetupWorkerMsg.class, msg ->
                        new VelocityUpdateWorkerBehaviour(getContext()))
                .build();
    }

    /**
     * Behaviour that update velocity
     */
    class VelocityUpdateWorkerBehaviour extends AbstractBehavior<WorkerMsg>{

        private VelocityUpdateWorkerBehaviour(ActorContext<WorkerMsg> context) {
            super(context);
        }

        @Override
        public Receive<WorkerMsg> createReceive() {
            return newReceiveBuilder()
                    .onMessage(UpdateVelocityMsg.class, this::onUpdateVelocityMsg)
                    .build();
        }

        private Behavior<WorkerMsg> onUpdateVelocityMsg(UpdateVelocityMsg message){
            List<Body> updatedBodies = new LinkedList<>();

            for (int i = myStart; i < myEnd; i++) {
                Body b = message.bodies.get(i);

                /* compute total force on bodies */
                V2d totalForce = Physics.computeTotalForceOnBody(b, message.bodies);

                /* compute instant acceleration */
                V2d acc = new V2d(totalForce).scalarMul(1.0 / b.getMass());

                /* update velocity */
                b.updateVelocity(acc, dt);
                updatedBodies.add(b);
            }

            myCoordinator.tell(new CoordinatorActor.VelocityUpdateResult(updatedBodies));
            return new PositionUpdateWorkerBehaviour(getContext());
        }
    }

    /**
     * Behaviour that update position
     */
    class PositionUpdateWorkerBehaviour extends AbstractBehavior<WorkerMsg>{

        private PositionUpdateWorkerBehaviour(ActorContext<WorkerMsg> context) {
            super(context);
        }

        @Override
        public Receive<WorkerMsg> createReceive() {
            return newReceiveBuilder()
                    .onMessage(UpdatePositionMsg.class, this::onUpdatePositionMsg)
                    .build();
        }

        private Behavior<WorkerMsg> onUpdatePositionMsg(UpdatePositionMsg message){
            List<Body> updatedBodies = new LinkedList<>();

            for (int i = myStart; i < myEnd; i++){
                Body b = message.bodies.get(i);
                b.updatePos(dt);
                b.checkAndSolveBoundaryCollision(bounds);
                updatedBodies.add(b);
            }

            myCoordinator.tell(new CoordinatorActor.PositionUpdateResult(updatedBodies));
            return new VelocityUpdateWorkerBehaviour(getContext());
        }
    }
}
