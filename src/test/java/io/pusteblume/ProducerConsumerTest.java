package io.pusteblume;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class ProducerConsumerTest {

   static class Process {
        private List<Integer> list = new ArrayList<>();
        private static final int UPPER_LIMIT = 5;
        private static final int LOWER_LIMIT = 0;
        private final Object lock = new Object();
        private int value = 0;

        void produce() throws  InterruptedException{
            synchronized (lock) {
                while (true) {
                    if(list.size() == UPPER_LIMIT) {
                        System.out.println("Waiting for items to be removed");
                       lock.wait();
                    } else {
                        System.out.println("Adding item" + value);
                        list.add(value);
                        value++;
                        lock.notify();
                    }
                    Thread.sleep(500);
                }
            }
        }
       void consume() throws  InterruptedException{
           synchronized (lock) {
               while (true) {
                   if(list.size() == LOWER_LIMIT) {
                       System.out.println("Waiting for items to be added");
                       lock.wait();
                   } else {
                       System.out.println("Removing item" + list.remove(list.size()-1));
                       lock.notify();
                   }
                   Thread.sleep(500);
               }
           }
       }
   }

    @Test
    public void waitNotifyOnObject() throws InterruptedException {
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
        consumer.start();
        producer.join();
        consumer.join();
    }



    static class ProcessWithLock {
        private final Lock lock = new ReentrantLock();
        private final Condition condition = lock.newCondition();
        private int value = 0;

        void produce() throws  InterruptedException {
            lock.lock();
            System.out.println("Producer method...");
            condition.await();
            System.out.println("Producer again...");
            lock.unlock();
        }
        void consume() throws  InterruptedException{
            lock.lock();
            Thread.sleep(2000);
            System.out.println("Consumer method ");
            condition.signal();
            lock.unlock();
        }
    }

    @Test
    public void usingCondition() throws InterruptedException {
        ProcessWithLock p = new ProcessWithLock();
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
        consumer.start();
        producer.join();
        consumer.join();
    }

}



