package simulation;

import java.util.List;

public class Main {
    final static boolean USE_GUI = true;
    final static boolean SINGLE_EXECUTION = true;
    final static int NUM_ITERATION = 3;

    public static void main(String[] args) {
        List<Integer> n_bodiesList = List.of(5000);
        List<Integer> n_stepsList = List.of(1000);
        List<Integer> n_threadsList =  List.of(8); //IntStream.rangeClosed(1, 16).boxed().collect(Collectors.toList());
        SimulationExecutor ex = new SimulationExecutor(USE_GUI, n_bodiesList, n_stepsList, n_threadsList);
        if(SINGLE_EXECUTION){
            ex.run();
        }else{ // multiple execution for performance tests
            ex.runMultiple(NUM_ITERATION);
        }
    }
}
