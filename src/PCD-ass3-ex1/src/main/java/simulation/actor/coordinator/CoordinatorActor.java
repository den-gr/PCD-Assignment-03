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

    public record VelocityUpdateResult(List<Body> bodies) implements CoordinatorMsg {}

    public record PositionUpdateResult(List<Body> bodies) implements CoordinatorMsg {}

    public record ViewUpdateResult() implements CoordinatorMsg {}

    private ArrayList<Body> bodies;
    private double vt = 0;
    private final double DT = 0.001;
    private long iter = 0;

    private final long nSteps;
    private final int nWorkers;

    private int calculatedPartitions;

    private final List<List<Body>> buffer;

    //TODO debito tecnico
    private final List<ActorRef<WorkerMsg>> refs = new LinkedList<>();

    private final ActorRef<ViewActor.UpdateViewMsg> viewerRef;

    private boolean isViewed = true;
    private boolean areBodyUpdated = true;

    public CoordinatorActor(ActorContext<CoordinatorMsg> context, SimulationView viewer, ArrayList<Body> bodies, Boundary bounds, long nSteps, int nWorkers) {
        super(context);
        this.bodies = bodies;
        this.nSteps = nSteps;
        this.nWorkers = nWorkers;
        this.calculatedPartitions = nWorkers - 1;
        this.buffer = new LinkedList<>();
        if(viewer != null){
            viewerRef = getContext().spawn(ViewActor.create(viewer, getContext().getSelf()), "viewer");
        }else{
            viewerRef = null;
        }
        for(int i = 0; i < nWorkers; i++){
            int myStart = bodies.size() * i / nWorkers;
            int myEnd = bodies.size() * (i+1) / nWorkers;
            refs.add(getContext()
                    .spawn(WorkerActor.create( myStart, myEnd, bounds, DT, getContext().getSelf()), "executor"+i));
        }
    }

    public static Behavior<CoordinatorMsg> create(SimulationView viewer, ArrayList<Body> bodies, Boundary bounds, long nSteps, int nWorkers) {
        return Behaviors.setup(ctx -> new CoordinatorActor(ctx, viewer, bodies, bounds, nSteps, nWorkers));
    }

    @Override
    public Receive<CoordinatorMsg> createReceive() {
        var receiverBuilder = newReceiveBuilder();
        if(this.viewerRef != null){
            receiverBuilder.onMessage(ViewUpdateResult.class, this::onViewUpdateFeedback);
        }

        return receiverBuilder
                .onMessage(VelocityUpdateResult.class, this::onVelocityUpdateFeedback)
                .onMessage(PositionUpdateResult.class, this::onPositionUpdateFeedback)
                .build();
    }

    private Behavior<CoordinatorMsg> onViewUpdateFeedback(ViewUpdateResult message) {
        isViewed = true;
        if(iter >= nSteps) { //terminate simulation with viewer
            getContext().getSystem().terminate();
            return Behaviors.stopped();
        }else if(areBodyUpdated) {
            startIteration();
        }
        return this;
    }

    private Behavior<CoordinatorMsg> onVelocityUpdateFeedback(VelocityUpdateResult msg){
        addBodies(msg.bodies);
        if(this.calculatedPartitions == nWorkers){
//            getContext().getLog().info("Velocity Update feedback complete");
            updateBodies();
            refs.forEach(e -> e.tell(new WorkerActor.UpdatePositionMsg(this.bodies)));
            resetBuffer();
        }
        return this;
    }

    private Behavior<CoordinatorMsg> onPositionUpdateFeedback(PositionUpdateResult msg){
        addBodies(msg.bodies);
        if(this.calculatedPartitions == nWorkers) {
            updateBodies();
            resetBuffer();
            iter++;
            if(viewerRef != null){// update view with a Viewer actor
                vt = vt + DT;
                var positions = this.bodies.stream()
                        .map(Body::getPos)
                        .collect(Collectors.toList());
                this.viewerRef.tell(new ViewActor.UpdateViewMsg(positions, vt, iter));
                areBodyUpdated = true;
                if(isViewed){
                    startIteration();
                }
            }else if(iter >= nSteps) { // terminate simulation without viewer
                getContext().getSystem().terminate();
                return Behaviors.stopped();
            }else{ // start new iteration
                startIteration();
            }
        }
        return this;
    }

    /**
     * Start a new simulation iteration
     */
    private void startIteration(){
        this.isViewed = false;
        this.areBodyUpdated = false;
        refs.forEach(e -> e.tell(new WorkerActor.UpdateVelocityMsg(this.bodies)));
    }

    /**
     * Unite partial lists of updated bodies
     */
    private void updateBodies(){
        this.bodies = this.buffer.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Add a part of updated bodies to the actor buffer
     * @param bodies partition of updated bodies
     */
    private void addBodies(List<Body> bodies){
        this.calculatedPartitions++;
        this.buffer.add(bodies);

    }

    private void resetBuffer(){
        this.calculatedPartitions = 0;
        this.buffer.clear();
    }
}
