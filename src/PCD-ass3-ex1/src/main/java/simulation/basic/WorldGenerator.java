package simulation.basic;

import simulation.basic.Body;
import simulation.basic.Boundary;
import simulation.basic.P2d;
import simulation.basic.V2d;

import java.util.ArrayList;
import java.util.Random;

public class WorldGenerator {
    public static Boundary createBoundary(int dim){
        if(dim > 100){
            throw new IllegalStateException("Too big");
        }
        return new Boundary(-dim, -dim, dim, dim);
    }

    public static ArrayList<Body> createBodies(int nBodies, Boundary bounds) {
//        Random rand = new Random(System.currentTimeMillis());
        Random rand = new Random(999);
        ArrayList<Body> bodies = new ArrayList<Body>();
        for (int i = 0; i < nBodies; i++) {
            double x = bounds.getX0()*0.25 + rand.nextDouble() * (bounds.getX1() - bounds.getX0()) * 0.25;
            double y = bounds.getY0()*0.25 + rand.nextDouble() * (bounds.getY1() - bounds.getY0()) * 0.25;
            Body b = new Body(i, new P2d(x, y), new V2d(0, 0), 10);
            bodies.add(b);
        }
        return bodies;
    }


    /// Deprecated
//    private void testBodySet1_two_bodies() {
//        bounds = new Boundary(-4.0, -4.0, 4.0, 4.0);
//        bodies = new ArrayList<Body>();
//        bodies.add(new Body(0, new P2d(-0.1, 0), new V2d(0,0), 1));
//        bodies.add(new Body(1, new P2d(0.1, 0), new V2d(0,0), 2));
//    }
//
//    private void testBodySet2_three_bodies() {
//        bounds = new Boundary(-1.0, -1.0, 1.0, 1.0);
//        bodies = new ArrayList<Body>();
//        bodies.add(new Body(0, new P2d(0, 0), new V2d(0,0), 10));
//        bodies.add(new Body(1, new P2d(0.2, 0), new V2d(0,0), 1));
//        bodies.add(new Body(2, new P2d(-0.2, 0), new V2d(0,0), 1));
//    }
//
//    private void testBodySet3_some_bodies() {
//        bounds = new Boundary(-4.0, -4.0, 4.0, 4.0);
//        int nBodies = 100;
//        Random rand = new Random(System.currentTimeMillis());
//        bodies = new ArrayList<Body>();
//        for (int i = 0; i < nBodies; i++) {
//            double x = bounds.getX0()*0.25 + rand.nextDouble() * (bounds.getX1() - bounds.getX0()) * 0.25;
//            double y = bounds.getY0()*0.25 + rand.nextDouble() * (bounds.getY1() - bounds.getY0()) * 0.25;
//            Body b = new Body(i, new P2d(x, y), new V2d(0, 0), 10);
//            bodies.add(b);
//        }
//    }
//
//    private void testBodySet4_many_bodies() {
//        bounds = new Boundary(-6.0, -6.0, 6.0, 6.0);
//        int nBodies = 1000;
//        Random rand = new Random(System.currentTimeMillis());
//        bodies = new ArrayList<Body>();
//        for (int i = 0; i < nBodies; i++) {
//            double x = bounds.getX0()*0.25 + rand.nextDouble() * (bounds.getX1() - bounds.getX0()) * 0.25;
//            double y = bounds.getY0()*0.25 + rand.nextDouble() * (bounds.getY1() - bounds.getY0()) * 0.25;
//            Body b = new Body(i, new P2d(x, y), new V2d(0, 0), 10);
//            bodies.add(b);
//        }
//    }
}
