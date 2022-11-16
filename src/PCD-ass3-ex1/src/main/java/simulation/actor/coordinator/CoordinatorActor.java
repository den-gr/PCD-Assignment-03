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

    public record SetupCoordinatorActorMsg() implements CoordinatorMsg {}

    public record VelocityUpdateResult(List<Body> bodies) implements CoordinatorMsg {}

    public record PositionUpdateResult(List<Body> bodies) implements CoordinatorMsg {}

    public record ViewUpdateResult() implements CoordinatorMsg {}

    private ArrayList<Body> bodies;
    private double vt = 0;
    private final double DT = 0.001;
    private long iter = 0;

    private final long nSteps;
    private final int nWorkers;

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
        refs.forEach(e -> e.tell(new WorkerActor.SetupWorkerMsg()));
    }

    public static Behavior<CoordinatorMsg> create(SimulationView viewer, ArrayList<Body> bodies, Boundary bounds, long nSteps, int nWorkers) {
        return Behaviors.setup(ctx -> new CoordinatorActor(ctx, viewer, bodies, bounds, nSteps, nWorkers));
    }

    @Override
    public Receive<CoordinatorMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(SetupCoordinatorActorMsg.class, msg -> {
                    startIteration();
                    return new UpdateVelocityCoordinatorBehaviour(getContext());
                })
                .build();
    }


    private void reactToViewUpdate(){
        isViewed = true;
        if(iter >= nSteps) { //terminate simulation with viewer
            getContext().getSystem().terminate();
        }else if(areBodyUpdated) {
            startIteration();
        }
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
    private void updateBodies(List<List<Body>> buffer){
        this.bodies = buffer.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    class UpdateVelocityCoordinatorBehaviour extends AbstractBehavior<CoordinatorMsg> {
        private int calculatedPartitions = 0;
        private final List<List<Body>> buffer = new LinkedList<>();

        private UpdateVelocityCoordinatorBehaviour(ActorContext<CoordinatorMsg> context) {
            super(context);
        }

        @Override
        public Receive<CoordinatorMsg> createReceive() {
            var receiverBuilder = newReceiveBuilder();
            if(viewerRef != null){
                receiverBuilder.onMessage(ViewUpdateResult.class, msg -> {
                    reactToViewUpdate();
                    return this;
                });
            }
            return receiverBuilder
                    .onMessage(VelocityUpdateResult.class, this::onVelocityUpdateResult)
                    .build();
        }


        private Behavior<CoordinatorMsg> onVelocityUpdateResult(VelocityUpdateResult msg){
            this.calculatedPartitions++;
            this.buffer.add(msg.bodies);

            if(calculatedPartitions == nWorkers){
                updateBodies(this.buffer);
                refs.forEach(e -> e.tell(new WorkerActor.UpdatePositionMsg(bodies)));
                return new UpdatePositionCoordinatorBehaviour(getContext());
            }
            return this;
        }
    }


    class UpdatePositionCoordinatorBehaviour extends AbstractBehavior<CoordinatorMsg> {
        private int calculatedPartitions = 0;
        private final List<List<Body>> buffer = new LinkedList<>();

        private UpdatePositionCoordinatorBehaviour(ActorContext<CoordinatorMsg> context) {
            super(context);
        }

        @Override
        public Receive<CoordinatorMsg> createReceive() {
            var receiverBuilder = newReceiveBuilder();
            if(viewerRef != null){
                receiverBuilder.onMessage(ViewUpdateResult.class, msg -> {
                    reactToViewUpdate();
                    return this;
                });
            }
            return receiverBuilder
                    .onMessage(PositionUpdateResult.class, this::onPositionUpdateResult)
                    .build();
        }

        private Behavior<CoordinatorMsg> onPositionUpdateResult(PositionUpdateResult msg){
            this.calculatedPartitions++;
            this.buffer.add(msg.bodies);

            if(calculatedPartitions == nWorkers) {
                updateBodies(this.buffer);
                iter++;
                if(viewerRef != null){// update view with a Viewer actor
                    vt = vt + DT;
                    var positions = bodies.stream()
                            .map(Body::getPos)
                            .collect(Collectors.toList());
                    viewerRef.tell(new ViewActor.UpdateViewMsg(positions, vt, iter));
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
                return new UpdateVelocityCoordinatorBehaviour(getContext());
            }
            return this;
        }
    }
}
