package io.pusteblume;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

public class ManipulationOfThreadsTest {


    @Test
    public void threadLifeCycle() {
        Thread t = new Thread(()-> {
            IntStream.range(1, 10000).forEach(v->{});
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Assertions.assertEquals(t.getState(), Thread.State.NEW);

        t.start();
        Assertions.assertEquals(t.getState(), Thread.State.RUNNABLE);
        while (t.getState().equals(Thread.State.RUNNABLE)){}

        Assertions.assertEquals(t.getState(), Thread.State.TIMED_WAITING);
        while (t.getState().equals(Thread.State.TIMED_WAITING)){}
        while (t.getState().equals(Thread.State.RUNNABLE)){}

        Assertions.assertEquals(t.getState(), Thread.State.TERMINATED);
    }

    @Test
    public void waitingForThreadsToFinish() throws InterruptedException {
        Thread t = new Thread(()-> IntStream.range(1, 10000).forEach(v->{}));
        t.start();
        Assertions.assertEquals(t.getState(), Thread.State.RUNNABLE);
        t.join();
        Assertions.assertEquals(t.getState(), Thread.State.TERMINATED);
    }
}



