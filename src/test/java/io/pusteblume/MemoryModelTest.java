package io.pusteblume;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MemoryModelTest {

    static class WorkerCached implements Runnable {
        private boolean terminatedCached; //might be cached
        @Override
        public void run() {
            System.out.println("Cached worker is running...");
            while(!terminatedCached){}
            System.out.println("Cached worker is terminating...");
        }
        public void setTerminatedCached(boolean terminatedCached){
            this.terminatedCached = terminatedCached;
        }

    }

    static class WorkerVolatile implements Runnable {
        private volatile boolean terminatedVolatile; // guaranteed to stay in RAM
        @Override
        public void run() {
            System.out.println("Volatile worker is running...");
            while(!terminatedVolatile){}
            System.out.println("Volatile worker is terminating...");
        }

        public void setTerminatedVolatile(boolean terminatedVolatile) {
            this.terminatedVolatile = terminatedVolatile;
        }
    }

    @Test
    public void volatileKeyWord() throws InterruptedException {
        WorkerCached workerCached = new WorkerCached();
        Thread tCached = new Thread(workerCached);

        WorkerVolatile workerVolatile = new WorkerVolatile();
        Thread tVolatile = new Thread(workerVolatile);

        tCached.start();
        tVolatile.start();


        Thread.sleep(500);

        workerCached.setTerminatedCached(true);
        System.out.println("Cached value might not be visible inside the thread.");
        Thread.sleep(100);
        Assertions.assertEquals(tCached.getState(), Thread.State.RUNNABLE);

        workerVolatile.setTerminatedVolatile(true);
        System.out.println("Volatile value is visible inside the thread.");
        Thread.sleep(100);
        Assertions.assertEquals(tVolatile.getState(), Thread.State.TERMINATED);
    }

}



