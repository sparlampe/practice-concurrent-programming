package io.pusteblume;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;


public class CommunicationOfThreadsTest {


    int counter = 0;
    void increment() {
        counter = counter+1;
    }
    @Test
    public void race() {
        counter = 0;
        Thread t1 = new Thread(() -> IntStream.rangeClosed(1,100000).forEach(v->increment()));
        Thread t2 = new Thread(() -> IntStream.rangeClosed(1,100000).forEach(v->increment()));
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(counter);
        Assertions.assertNotEquals(counter, 200000);
    }

    synchronized void syncIncrement() {
        counter = counter+1;
    }
    @Test
    public void synchronization() {
        counter = 0;
        Thread t1 = new Thread(() -> IntStream.rangeClosed(1,100000).forEach(v->syncIncrement()));
        Thread t2 = new Thread(() -> IntStream.rangeClosed(1,100000).forEach(v->syncIncrement()));
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(counter);
        Assertions.assertEquals(counter, 200000);
    }

    synchronized void syncIncrement1() {
        counter = counter+1;
    }
    synchronized void syncIncrement2() {
        counter = counter+1;
    }
    @Test
    public void synchronizationKeyWordUsesSameMonitor() {
        counter = 0;
        Thread t1 = new Thread(() -> IntStream.rangeClosed(1,100000).forEach(v->syncIncrement1()));
        Thread t2 = new Thread(() -> IntStream.rangeClosed(1,100000).forEach(v->syncIncrement2()));
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(counter);
        Assertions.assertEquals(counter, 200000);
    }

    final Object lock1 = new Object();
    int counter1 = 0;
    void syncIncrement11() {
        synchronized (lock1) {
            counter1 = counter1 + 1;
        }
    }
    final Object lock2 = new Object();
    int counter2 = 0;
    void syncIncrement21() {
        synchronized (lock2) {
            counter2 = counter2 + 1;
        }
    }
    @Test
    public void independentSynchronisation() {
        counter1 = 0;
        counter2 = 0;
        Thread t1 = new Thread(() -> IntStream.rangeClosed(1,100000).forEach(v->syncIncrement11()));
        Thread t12 = new Thread(() -> IntStream.rangeClosed(1,100000).forEach(v->syncIncrement11()));
        Thread t2 = new Thread(() -> IntStream.rangeClosed(1,100000).forEach(v->syncIncrement21()));
        Thread t22 = new Thread(() -> IntStream.rangeClosed(1,100000).forEach(v->syncIncrement21()));
        t1.start();
        t2.start();
        t12.start();
        t22.start();
        try {
            t1.join();
            t2.join();
            t12.join();
            t22.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(counter1);
        Assertions.assertEquals(counter1, 200000);
        System.out.println(counter2);
        Assertions.assertEquals(counter2, 200000);
    }

   static class Process {
        void produce() throws  InterruptedException{
            synchronized (this) {
                System.out.println("Produce method");
                wait();
                System.out.println("Again produce method");
            }
           System.out.println("Produce exit");
        }
       void consume() throws  InterruptedException{
            synchronized (this) {
                System.out.println("Consume method");
                notify();
                Thread.sleep(100);
            }
           System.out.println("Consume exit");
       }
   }

    @Test
    public void waitNotify() throws InterruptedException {
        Process p = new Process();
        Thread producer = new Thread(()->{
            try {
                p.produce();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread consumer = new Thread(()->{
            try {
                p.consume();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        producer.start();
        while (producer.getState().equals(Thread.State.RUNNABLE)){}
        Assertions.assertEquals(producer.getState(), Thread.State.WAITING);

        consumer.start();
        while (consumer.getState().equals(Thread.State.RUNNABLE)){}
        Assertions.assertEquals(consumer.getState(), Thread.State.TIMED_WAITING);
        Assertions.assertEquals(producer.getState(), Thread.State.BLOCKED);

        producer.join();
        consumer.join();
    }
}



