package io.pusteblume;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CallableInterfaceTest {

    static class  Processor implements Callable<String> {
        private int id;
        public Processor(int id){
            this.id = id;
        }

        @Override
        public String call() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Id: " + this.id;
        }
    }

    @Test
    public void returningValue() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<String>> list = new ArrayList<>();

        for(int i = 0; i<5; i++){
            Future<String> future = executor.submit(new Processor(i+1));
            list.add(future);
        }

        for (Future<String> future: list){
            System.out.println(future.get());
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }
}



