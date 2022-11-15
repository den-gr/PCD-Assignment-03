package simulation.actor;

import akka.actor.typed.ActorSystem;
import scala.concurrent.ExecutionContext;
import simulation.actor.coordinator.CoordinatorActor;
import simulation.actor.coordinator.CoordinatorMsg;
import simulation.basic.AbstractSimulator;
import simulation.gui.SimulationView;
import utils.SimpleWaitMonitor;
import utils.SimpleWaitMonitorImpl;

public class ActorSimulator extends AbstractSimulator {
    private final int nWorkers;

    private final SimpleWaitMonitor monitor = new SimpleWaitMonitorImpl();

    public ActorSimulator(SimulationView viewer, int nBodies, int dimSimulation, int nWorkers) {
        super(viewer, nBodies, dimSimulation);
        this.nWorkers = nWorkers;
    }

    @Override
    public void execute(long nSteps) {

        System.out.println("Execute");
        final ActorSystem<CoordinatorMsg> actorSystem = ActorSystem.create(CoordinatorActor.create(this.viewer, this.bodies, this.bounds, nSteps, nWorkers), "Master");
        actorSystem.whenTerminated().onComplete(c -> {
            System.out.println("Actor system terminated");
            monitor.simpleNotify();
            return c.get();
        }, ExecutionContext.fromExecutor(actorSystem.executionContext()));


        actorSystem.tell(new CoordinatorActor.PositionUpdateFeedback(this.bodies));

        if(viewer != null) viewer.getFrame().setStopHandler(h -> actorSystem.terminate());

        monitor.simpleWait();
    }
}
