package io.pusteblume;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ExampleDiningPhilosophersProblemTest {

    static class Chopstick {
        private int id;
        private Lock lock;

        public Chopstick(int id){
            this.id = id;
            this.lock = new ReentrantLock();
        }

        public boolean pickUp(Philosopher philosopher, State state) throws InterruptedException {
            if(lock.tryLock(10, TimeUnit.MILLISECONDS)){
                System.out.println(philosopher + " picked up " + state.toString() + " " + this);
                return true;
            }
            return false;
        }

        public void putDown(Philosopher philosopher, State state){
            lock.unlock();
            System.out.println(philosopher + " puts down "+state.toString()+ " " + this);
        }

        public String toString(){
            return  "Chopstick " + id;
        }
    }

    static class Philosopher implements Runnable {
        private int id;
        private volatile boolean isFull;
        private Chopstick leftChopstick;
        private Chopstick rightChopstick;
        private Random random;


        private int eatingCounter;

        public Philosopher(int id, Chopstick leftChopstick, Chopstick rightChopstick){
            this.id = id;
            this.leftChopstick = leftChopstick;
            this.rightChopstick = rightChopstick;
            this.random = new Random();
        }

        @Override
        public void run() {
            try{
                while (!this.isFull){
                    think();
                    if(leftChopstick.pickUp(this, State.LEFT)){
                        if(rightChopstick.pickUp(this, State.RIGHT)){
                            eat();
                            rightChopstick.putDown(this, State.RIGHT);
                        }
                        leftChopstick.putDown(this, State.LEFT);

                    }
                }
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        private void think() throws InterruptedException {
            System.out.println(this + " is thinking...");
            Thread.sleep(random.nextInt(1000));
        }

        private void eat() throws InterruptedException {
            System.out.println(this + " is eating...");
            eatingCounter++;
            Thread.sleep(random.nextInt(1000));
        }

        public void setFull(boolean full){
            this.isFull = full;
        }

        public boolean isFull(){
            return this.isFull;
        }

        @Override
        public String toString(){
            return "Philosopher " +id;
        }


        public int getEatingCounter() {
            return eatingCounter;
        }
    }

    static class Constants {
        private Constants(){}
        public static final int NUMBER_OF_PHILOSOPHERS = 5;
        public static final int NUMBER_OF_CHOPSTICKS = 5;
        public static final int SIMULATION_RUNNING_TIME = 5*1000;
    }

    enum State {
        LEFT, RIGHT
    }

    @Test
    public void problem() throws InterruptedException {
        ExecutorService executorService = null;
        Philosopher[] philosophers = null;
        Chopstick[] chopsticks = null;
        try {
            philosophers = new Philosopher[Constants.NUMBER_OF_PHILOSOPHERS];
            chopsticks = new Chopstick[Constants.NUMBER_OF_CHOPSTICKS];
            for (int i=0; i<Constants.NUMBER_OF_CHOPSTICKS; i++) {
                chopsticks[i] = new Chopstick(i);
            }
            executorService = Executors.newFixedThreadPool(Constants.NUMBER_OF_PHILOSOPHERS);
            for(int i=0; i<Constants.NUMBER_OF_PHILOSOPHERS; ++i){
                philosophers[i] =  new Philosopher(i, chopsticks[i], chopsticks[(i+1)%Constants.NUMBER_OF_PHILOSOPHERS]);
                executorService.execute(philosophers[i]);
            }
            Thread.sleep(Constants.SIMULATION_RUNNING_TIME);
            for(Philosopher philosopher: philosophers){
                philosopher.setFull(true);
            }
        } finally {
            executorService.shutdown();
            if(!executorService.isTerminated()){
                while (!executorService.isTerminated()){
                    Thread.sleep(1000);
                }
                for (Philosopher philosopher : philosophers)
                    System.out.println(philosopher + " ate #" + philosopher.getEatingCounter() + " times");
            }
        }
    }
}



