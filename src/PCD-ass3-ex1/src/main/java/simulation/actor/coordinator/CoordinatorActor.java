package simulation.actor.coordinator;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import simulation.actor.executor.ExecutorActor;
import simulation.actor.executor.ExecutorMsg;
import simulation.basic.Body;
import simulation.basic.Boundary;
import simulation.gui.SimulationView;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class CoordinatorActor extends AbstractBehavior<CoordinatorMsg> {
    public record Ciao(String name) implements CoordinatorMsg {
    }

    public record VelocityUpdateFeedback(ArrayList<Body> bodies) implements CoordinatorMsg {
    }

    public record PositionUpdateFeedback(ArrayList<Body> bodies) implements CoordinatorMsg {
    }

    private final  SimulationView viewer;
    private final ArrayList<Body> bodies;
    private final Boundary bounds;
    private double vt = 0;
    private final double DT = 0.001;
    private long iter = 0;

    private ActorRef<ExecutorMsg> ref;

    private final long nSteps;

    public CoordinatorActor(ActorContext<CoordinatorMsg> context, SimulationView viewer, ArrayList<Body> bodies, Boundary bounds, long nSteps) {
        super(context);
        this.viewer = viewer;
        this.bodies = bodies;
        this.bounds = bounds;
        this.nSteps = nSteps;
    }

    public static Behavior<CoordinatorMsg> create(SimulationView viewer, ArrayList<Body> bodies, Boundary bounds, long nSteps) {
        return Behaviors.setup(ctx -> new CoordinatorActor(ctx, viewer, bodies, bounds, nSteps));
    }

    @Override
    public Receive<CoordinatorMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(Ciao.class, this::onSayHello)
                .onMessage(VelocityUpdateFeedback.class, this::onVelocityUpdateFeedback)
                .onMessage(PositionUpdateFeedback.class, this::onPositionUpdateFeedback)
                .build();
    }

    private Behavior<CoordinatorMsg> onSayHello(Ciao command) {
        getContext().getLog().info("Spawn an executor actor");
        ref = getContext().spawn(ExecutorActor.create( 0, bodies.size()-1, bounds, DT, getContext().getSelf()), "executor");
        getContext().getSelf().tell(new PositionUpdateFeedback(this.bodies));
        return this;
    }

    private Behavior<CoordinatorMsg> onVelocityUpdateFeedback(VelocityUpdateFeedback msg){

//        getContext().getLog().info("Velocity updated");
        ref.tell(new ExecutorActor.UpdatePositionMsg(msg.bodies));
        return this;
    }

    private Behavior<CoordinatorMsg> onPositionUpdateFeedback(PositionUpdateFeedback msg){

        if(iter < nSteps) {
//            getContext().getLog().info("Position updated, iteration {}", iter);
            vt = vt + DT;
            iter++;
            if(viewer != null) viewer.display(msg.bodies.stream().map(Body::getPos).collect(Collectors.toList()), vt, iter);
            ref.tell(new ExecutorActor.UpdateVelocityMsg(msg.bodies));
            return this;
        }
        return Behaviors.stopped();
    }
}
