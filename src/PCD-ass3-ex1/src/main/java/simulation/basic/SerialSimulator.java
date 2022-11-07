package simulation.basic;

import simulation.basic.Physics;
import simulation.basic.AbstractSimulator;
import simulation.basic.Body;
import simulation.gui.SimulationView;
import simulation.basic.V2d;

import java.util.stream.Collectors;

public class SerialSimulator extends AbstractSimulator {

	public SerialSimulator(SimulationView viewer, int nBodies, int dimSimulation) {
		super(viewer, nBodies, dimSimulation);
	}
	
	@Override
	public void execute(long nSteps) {

		/* init virtual time */

		vt = 0;
		dt = 0.001;

		long iter = 0;

		/* simulation loop */

		while (iter < nSteps) {

			/* update bodies velocity */

			for (int i = 0; i < bodies.size(); i++) {
				Body b = bodies.get(i);

				/* compute total force on bodies */
				V2d totalForce = Physics.computeTotalForceOnBody(b, bodies);

				/* compute instant acceleration */
				V2d acc = new V2d(totalForce).scalarMul(1.0 / b.getMass());

				/* update velocity */
				b.updateVelocity(acc, dt);
			}

			/* compute bodies new pos */

			for (Body b : bodies) {
				b.updatePos(dt);
			}

			/* check collisions with boundaries */

			for (Body b : bodies) {
				b.checkAndSolveBoundaryCollision(bounds);
			}

			/* update virtual time */

			vt = vt + dt;
			iter++;

			/* display current stage */

			viewer.display(bodies.stream().map(Body::getPos).collect(Collectors.toList()), vt, iter);

		}
	}
}
