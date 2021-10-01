package io.pusteblume;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadLockingTest {

    static class Deadlock {

        private Lock lock1= new ReentrantLock(true);
        private Lock lock2= new ReentrantLock(true);

        public void worker1(){
            lock1.lock();
            System.out.println("Worker1 acquired lock1");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lock2.lock();
            System.out.println("Worker1 acquired lock2");
            lock1.unlock();
            lock2.unlock();
        }
        public void worker2(){
            lock2.lock();
            System.out.println("Worker2 acquired lock2");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lock1.lock();
            System.out.println("Worker2 acquired lock1");
            lock1.unlock();
            lock2.unlock();
        }

    }

    @Test
    public void deadlock() throws InterruptedException {
        Deadlock deadlock = new Deadlock();
        Thread t1 = new Thread(deadlock::worker1);
        Thread t2 = new Thread(deadlock::worker2);

        t1.start();
        t2.start();
        Thread.sleep(1000);
        Assertions.assertEquals(t1.getState(), Thread.State.WAITING);
        Assertions.assertEquals(t2.getState(), Thread.State.WAITING);
    }


    static class Spoon {
        private volatile Diner owner;
        public Spoon(Diner d) { owner = d; }
        public Diner getOwner() { return owner; }
        public void setOwner(Diner d) { owner = d; }
        public synchronized void use() {
            System.out.printf("%s has eaten!", owner.name);
        }
    }

    static class Diner {
        private String name;
        private boolean isHungry;

        public Diner(String n) { name = n; isHungry = true; }
        public String getName() { return name; }
        public boolean isHungry() { return isHungry; }

        public void eatWith(Spoon spoon, Diner spouse) {
            while (isHungry) {
                // Don't have the spoon, so wait patiently for spouse.
                if (spoon.owner != this) {
                    try { Thread.sleep(1); }
                    catch(InterruptedException e) { continue; }
                    continue;
                }

                // If spouse is hungry, insist upon passing the spoon.
                if (spouse.isHungry()) {
                    System.out.printf(
                            "%s: You eat first my darling %s!%n",
                            name, spouse.getName());
                    spoon.setOwner(spouse);
                    continue;
                }

                // Spouse wasn't hungry, so finally eat
                spoon.use();
                isHungry = false;
                System.out.printf(
                        "%s: I am stuffed, my darling %s!%n",
                        name, spouse.getName());
                spoon.setOwner(spouse);
            }
        }
    }

    @Test
    public void liveLock() throws InterruptedException {
        //https://stackoverflow.com/a/8863671
        final Diner husband = new Diner("Bob");
        final Diner wife = new Diner("Alice");
        final Spoon s = new Spoon(husband);
        Thread t1 = new Thread(() -> husband.eatWith(s, wife));
        Thread t2 = new Thread(()->  wife.eatWith(s, husband));

        t1.start();
        t2.start();
        Thread.sleep(100);
        Assertions.assertTrue(List.of(Thread.State.RUNNABLE, Thread.State.TIMED_WAITING).contains(t1.getState()));
        Assertions.assertTrue(List.of(Thread.State.RUNNABLE, Thread.State.TIMED_WAITING).contains(t2.getState()));
    }

}



