package org.graphstream.boids.test;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class BoidGraph {
    @Test
    public void demo() throws InterruptedException {
        System.setProperty("org.graphstream.ui", "javafx");

        final CountDownLatch lock = new CountDownLatch(1);

        new Thread(() -> {
            System.out.println("Launch demo in a new Thread.");
            org.graphstream.boids.BoidGraph ctx = new org.graphstream.boids.BoidGraph();

            try {
                ctx.loadDGSConfiguration("configExample.dgs");
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            ctx.display(false);
            ctx.loop();
        }).start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                lock.countDown();
                System.out.println("Unlocking...");
            }
        });
        System.out.println("Locking...");
        lock.await();
        System.out.println("Done.");
        org.junit.Assert.assertTrue(true);

    }
}
