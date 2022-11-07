package simulation.actor;

import akka.actor.typed.ActorSystem;
import simulation.actor.coordinator.CoordinatorActor;
import simulation.actor.coordinator.CoordinatorMsg;
import simulation.basic.AbstractSimulator;
import simulation.gui.SimulationView;

public class ActorSimulator extends AbstractSimulator {
    public ActorSimulator(SimulationView viewer, int nBodies, int dimSimulation) {
        super(viewer, nBodies, dimSimulation);
    }

    @Override
    public void execute(long nSteps) {
        final ActorSystem<CoordinatorMsg> mainActor = ActorSystem.create(CoordinatorActor.create(this.viewer, this.bodies, this.bounds, nSteps), "helloakka");

        mainActor.tell(new CoordinatorActor.Ciao("Ciaone"));

    }
}
