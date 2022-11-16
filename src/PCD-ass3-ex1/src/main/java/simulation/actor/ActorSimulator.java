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

    public ActorSimulator(SimulationView viewer, int nBodies, int dimSimulation, int nWorkers) {
        super(viewer, nBodies, dimSimulation);
        this.nWorkers = nWorkers;
    }

    @Override
    public void execute(long nSteps) {

        System.out.println("Execute");
        final ActorSystem<CoordinatorMsg> actorSystem = ActorSystem.create(CoordinatorActor.create(this.viewer, this.bodies, this.bounds, nSteps, nWorkers), "Master");

        SimpleWaitMonitor monitor = new SimpleWaitMonitorImpl();
        actorSystem.whenTerminated().onComplete(c -> {
            System.out.println("Actor system terminated");
            monitor.simpleNotify();
            return c.get();
        }, ExecutionContext.fromExecutor(actorSystem.executionContext()));

        //start simulation
        actorSystem.tell(new CoordinatorActor.SetupCoordinatorActorMsg());

        //wait simulation termination
        monitor.simpleWait();
    }
}
