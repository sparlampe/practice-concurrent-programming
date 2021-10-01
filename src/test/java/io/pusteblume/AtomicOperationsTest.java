package io.pusteblume;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class AtomicOperationsTest {

    static class  NonAtomicInteger {
        static int counter = 0;
        static void increment(){
            IntStream.rangeClosed(1,100000).forEach(v -> counter++);
        }
    }

    @Test
    public void nonAtomicInteger() throws InterruptedException {
        Thread t1 =  new Thread(NonAtomicInteger::increment, "thread1");
        Thread t2 =  new Thread(NonAtomicInteger::increment, "thread2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        Assertions.assertEquals(200000, NonAtomicInteger.counter);
    }


    static AtomicInteger counter = new AtomicInteger(0);
    static void increment(){
            IntStream.rangeClosed(1,100000).forEach(v -> counter.incrementAndGet());
    }

    @Test
    public void atomicInteger() throws InterruptedException {
        Thread t1 =  new Thread(AtomicOperationsTest::increment, "thread1");
        Thread t2 =  new Thread(AtomicOperationsTest::increment, "thread2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        Assertions.assertEquals(200000, counter.get());
    }
}



