package simulation.monitor;

import simulation.monitor.SimpleWaitMonitor;

public class SimpleWaitMonitorImpl implements SimpleWaitMonitor {

    @Override
    public synchronized void simpleWait(){
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void simpleNotify(){
        notify();
    }
}
