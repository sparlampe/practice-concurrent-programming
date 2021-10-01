package io.pusteblume;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;


public class LocksTest {
    private static int counter = 0;
    private static Lock lock = new ReentrantLock();

    public static void increment(){
        lock.lock();
        try {
            IntStream.rangeClosed(1, 100).forEach(b -> counter++);
        }
        finally {
          lock.unlock();
        }
    }

    @Test
    public void reentrantLocks() throws InterruptedException {
        Thread t1 = new Thread(LocksTest::increment);
        Thread t2 = new Thread(LocksTest::increment);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Counter is " + counter);
    }
}



