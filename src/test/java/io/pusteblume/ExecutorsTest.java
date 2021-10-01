package io.pusteblume;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorsTest {

    static class  Task implements Runnable {
        private int id;
        public Task(int id){
            this.id = id;
        }

        @Override
        public void run() {
            System.out.println("Task with id " + id + " is in work - thread id: " + Thread.currentThread().getName());
            long duration = (long) Math.random() * 5;
            try {
                 TimeUnit.SECONDS.sleep(duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void singleThreadExecutor() throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        for(int i = 0; i<5; i++){
            executor.execute(new Task(i));
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test
    public void fixedExecutor() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for(int i = 0; i<100; i++){
            executor.execute(new Task(i));
        }
        executor.shutdown();
        if(!executor.awaitTermination(2, TimeUnit.SECONDS)){
            executor.shutdownNow();
        }
    }

    @Test
    public void scheduledExecutor() throws InterruptedException {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(new Task(1), 1000, 2000, TimeUnit.MILLISECONDS);
        TimeUnit.SECONDS.sleep(10);
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }
}



