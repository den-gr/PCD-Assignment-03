package simulation.monitor;

public class BarrierImpl implements Barrier {
    private final int nParticipants;
    private int nHits;
    private int nGone;
    private boolean isBarrierFinal = false;
    private final StopSimulation simulationStopper;

    public BarrierImpl(int nParticipants, StopSimulation simulationStopper) {
        this.nParticipants = nParticipants;
        this.nHits = 0;
        this.nGone = 0;
        this.simulationStopper = simulationStopper;
    }

    public synchronized void hitAndWaitAll() throws InterruptedException {
        nHits++;
        while (nHits < nParticipants) {
            wait();
        }
        nGone++;
        if(nGone == 1)notifyAll();
        if(isBarrierFinal && nGone == 1){
            //if all threads are blocked on barrier and it's time to stop execution
            System.out.println("Final barrier encounter");
            simulationStopper.stop();
        }

        if (nGone == nHits) {
            nGone = 0;
            nHits = 0;
        }
    }

    @Override
    public synchronized void setFinalBarrier() {
        isBarrierFinal = true;
    }
}
