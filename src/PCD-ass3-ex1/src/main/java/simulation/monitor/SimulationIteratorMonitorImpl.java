package simulation.monitor;

import simulation.gui.SimulationView;

public class SimulationIteratorMonitorImpl implements SimulationIteratorMonitor {
    private long iter = 0;
    private long nStep;
    private long partialIter = 0 ;
    private final long nThreads;
    private final SimulationView viewer;
    private boolean isNotOver = true;

    public SimulationIteratorMonitorImpl(long nStep, int nThread, SimulationView viewer){
        this.nStep = nStep;
        this.nThreads = nThread;
        this.viewer = viewer;
    }

    @Override
    public synchronized void incrementCounter(){
        partialIter++;

        if(partialIter % nThreads == 0){
            iter++;
            partialIter = 0;
            if(viewer != null){
                viewer.display(iter / 1000.0, iter);
            }
        }
    }

    @Override
    public synchronized boolean isSimulationNotOver(){
        if((iter >= nStep && isNotOver)){
            isNotOver = false;
        }
        return isNotOver;
    }

    @Override
    public synchronized void stop() {
        nStep = iter;
    }
}
