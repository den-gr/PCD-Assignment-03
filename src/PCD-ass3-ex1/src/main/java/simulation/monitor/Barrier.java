package simulation.monitor;

public interface Barrier {

    void hitAndWaitAll() throws InterruptedException;

    void setFinalBarrier();
}
