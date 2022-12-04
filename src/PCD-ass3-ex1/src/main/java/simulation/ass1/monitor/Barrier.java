package simulation.ass1.monitor;

public interface Barrier {

    void hitAndWaitAll() throws InterruptedException;

    void setFinalBarrier();
}
