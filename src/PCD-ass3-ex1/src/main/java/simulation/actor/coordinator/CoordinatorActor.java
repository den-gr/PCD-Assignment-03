package simulation.actor.coordinator;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import simulation.actor.view.ViewActor;
import simulation.actor.worker.WorkerActor;
import simulation.actor.worker.WorkerMsg;
import simulation.basic.Body;
import simulation.basic.Boundary;
import simulation.gui.SimulationView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CoordinatorActor extends AbstractBehavior<CoordinatorMsg> {
    public record Ciao(String name) implements CoordinatorMsg {
    }

    public record VelocityUpdateFeedback(List<Body> bodies) implements CoordinatorMsg {
    }

    public record PositionUpdateFeedback(List<Body> bodies) implements CoordinatorMsg {
    }

    private ArrayList<Body> bodies;
    private final Boundary bounds;
    private double vt = 0;
    private final double DT = 0.001;
    private long iter = 0;

    private final long nSteps;
    private final int nWorkers;

    private int calculatedPartitions = 0;

    private final List<List<Body>> buffer;

    //TODO debito tecnico
    private final List<ActorRef<WorkerMsg>> refs = new LinkedList<>();

    private final ActorRef<ViewActor.UpdateViewMsg> viewerRef;


    public CoordinatorActor(ActorContext<CoordinatorMsg> context, SimulationView viewer, ArrayList<Body> bodies, Boundary bounds, long nSteps, int nWorkers) {
        super(context);
        this.bodies = bodies;
        this.bounds = bounds;
        this.nSteps = nSteps;
        this.nWorkers = nWorkers;
        this.buffer = new LinkedList<>();
        if(viewer != null){
            viewerRef = getContext().spawn(ViewActor.create(viewer), "viewer");
        }else{
            viewerRef = null;
        }


    }

    public static Behavior<CoordinatorMsg> create(SimulationView viewer, ArrayList<Body> bodies, Boundary bounds, long nSteps, int nWorkers) {
        return Behaviors.setup(ctx -> new CoordinatorActor(ctx, viewer, bodies, bounds, nSteps, nWorkers));
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
        for(int i = 0; i < nWorkers; i++){
            int myStart = bodies.size() * i / nWorkers;
            int myEnd = bodies.size() * (i+1) / nWorkers;
            refs.add(getContext()
                    .spawn(WorkerActor.create( myStart, myEnd, bounds, DT, getContext().getSelf()), "executor"+i));
        }

        getContext().getSelf().tell(new PositionUpdateFeedback(this.bodies));
        this.calculatedPartitions = nWorkers - 1;
        return this;
    }

    private Behavior<CoordinatorMsg> onVelocityUpdateFeedback(VelocityUpdateFeedback msg){
        addBodies(msg.bodies);
        if(this.calculatedPartitions == nWorkers){
//            getContext().getLog().info("Velocity Update feedback complete");
            updateBodies();
            refs.forEach(e -> e.tell(new WorkerActor.UpdatePositionMsg(this.bodies)));
            resetBuffer();
        }else{
//            getContext().getLog().info("Velocity Update feedback incomplete");
        }
        return this;
    }

    private Behavior<CoordinatorMsg> onPositionUpdateFeedback(PositionUpdateFeedback msg){
        addBodies(msg.bodies);
        if(this.calculatedPartitions == nWorkers) {
//            getContext().getLog().info("Position Update feedback complete");
            updateBodies();
            iter++;
            if(viewerRef != null){
                vt = vt + DT;
                var positions = this.bodies.stream()
                        .map(Body::getPos)
                        .collect(Collectors.toList());
                this.viewerRef.tell(new ViewActor.UpdateViewMsg(positions, vt, iter));
            }
            refs.forEach(e -> e.tell(new WorkerActor.UpdateVelocityMsg(this.bodies)));
            resetBuffer();
            if(iter >= nSteps) {
                getContext().getSystem().terminate();
                return Behaviors.stopped();
            }
        }
//        getContext().getLog().info("Position Update feedback  incomplete");
        return this;
    }

    private void updateBodies(){
        this.bodies = this.buffer.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void addBodies(List<Body> bodies){
        this.calculatedPartitions++;
        this.buffer.add(bodies);

    }

    private void resetBuffer(){
        this.calculatedPartitions = 0;
        this.buffer.clear();
    }
}
