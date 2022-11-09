package simulation.actor;

import akka.actor.typed.ActorSystem;
import simulation.actor.coordinator.CoordinatorActor;
import simulation.actor.coordinator.CoordinatorMsg;
import simulation.basic.AbstractSimulator;
import simulation.gui.SimulationView;

public class ActorSimulator extends AbstractSimulator {
    private final int nWorkers;

    public ActorSimulator(SimulationView viewer, int nBodies, int dimSimulation, int nWorkers) {
        super(viewer, nBodies, dimSimulation);
        this.nWorkers = nWorkers;
    }

    @Override
    public void execute(long nSteps) {


        final ActorSystem<CoordinatorMsg> mainActor = ActorSystem.create(CoordinatorActor.create(this.viewer, this.bodies, this.bounds, nSteps, nWorkers), "Master");

        mainActor.tell(new CoordinatorActor.Ciao("Ciaone"));
        if(viewer != null){
            viewer.getFrame().setStopHandler(h -> {
                mainActor.terminate();
            });
        }



    }
}
