package io.pusteblume;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class ConcurrentDataStructuresTest {

    @Test
    public void nonSynchronizedCollections() throws InterruptedException {
        List<Integer> nums = new ArrayList<>();
        Thread t1 = new Thread(()-> IntStream.rangeClosed(1, 1000).forEach(nums::add));
        Thread t2 = new Thread(()-> IntStream.rangeClosed(1, 1000).forEach(nums::add));
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // produces exception   ArrayIndexOutOfBoundsException : Index 53 out of bounds for length 49
    }

    @Test
    public void synchronizedCollections() throws InterruptedException {
        //Not efficient because of the intrinsic lock
        List<Integer> nums = Collections.synchronizedList(new ArrayList<>());
        Thread t1 = new Thread(()-> IntStream.rangeClosed(1, 1000).forEach(nums::add));
        Thread t2 = new Thread(()-> IntStream.rangeClosed(1, 1000).forEach(nums::add));
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

     static class WorkerCountdownLatch implements Runnable {
        private  int id;
        private CountDownLatch countDownLatch;

        public WorkerCountdownLatch(int id, CountDownLatch countDownLatch){
            this.id = id;
            this.countDownLatch = countDownLatch;
        }

         @Override
         public void run() {
            doWork();
            countDownLatch.countDown();
         }

         private void doWork(){
             System.out.println("Thread with id " + id + " starts working");
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
     }

    @Test
    public void countDownLatch() throws InterruptedException {
       ExecutorService service = Executors.newSingleThreadExecutor();
       CountDownLatch latch = new CountDownLatch(5);
       for (int i=0; i<5; i++){
           service.execute(new WorkerCountdownLatch(i+1, latch));
       }

       latch.await();
       System.out.println("All the prerequisites are done");
       service.shutdown();
    }

    static class WorkCyclicBarrier implements Runnable {
        private  int id;
        private CyclicBarrier cyclicBarrier;

        public WorkCyclicBarrier(int id, CyclicBarrier cyclicBarrier){
            this.id = id;
            this.cyclicBarrier = cyclicBarrier;
        }

        @Override
        public void run() {
            doWork();
            try {
                cyclicBarrier.await();
                System.out.println("After await " + id);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }

        private void doWork(){
            System.out.println("Thread with id " + id + " starts working");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void cyclicBarrier() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(5);
        CyclicBarrier barrier = new CyclicBarrier(5, ()-> System.out.println("All tasks are finished"));
        for (int i=0; i<5; i++){
            service.execute(new WorkCyclicBarrier(i+1, barrier));
        }

        service.shutdown();
        service.awaitTermination(10, TimeUnit.SECONDS);
    }


    static class Producer implements Runnable {
        private BlockingQueue<Integer> blockingQueue;
        public Producer(BlockingQueue blockingQueue){
            this.blockingQueue = blockingQueue;
        }
        @Override
        public void run() {

            int counter = 0;
            while (true) {
                try {
                    blockingQueue.put(counter);
                    System.out.println("Put item to queue " + counter);
                    counter++;
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class Consumer implements Runnable {
        private BlockingQueue<Integer> blockingQueue;
        public Consumer(BlockingQueue blockingQueue){
            this.blockingQueue = blockingQueue;
        }
        @Override
        public void run() {
            while (true) {
                try {
                    System.out.println("Taken item from queue" + blockingQueue.take());
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void blockingQueue() throws InterruptedException {
        BlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<>(10);
        Producer firstWorker = new Producer(blockingQueue);
        Consumer secondWorker = new Consumer(blockingQueue);
        Thread t1 = new Thread(firstWorker);
        Thread t2 = new Thread(secondWorker);

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }


    static class WorkerDelayed implements Delayed {
        private long duration;
        private String message;

        public WorkerDelayed(long duration, String message){
            this.duration = System.currentTimeMillis() + duration;
            this.message = message;
        }


        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(duration - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            if(this.duration < ((WorkerDelayed) o).getDuration()){
                return -1;
            }
            if(this.duration > ((WorkerDelayed) o).getDuration()){
                return 1;
            }
            return 0;
        }

        public long getDuration(){
            return duration;
        }
    }

    @Test
    public void delayedQueue() throws InterruptedException {
        BlockingQueue<WorkerDelayed> delayQueue = new DelayQueue<>();
        try {
            delayQueue.put(new WorkerDelayed(100, "This is message 1"));
            delayQueue.put(new WorkerDelayed(500, "This is message 2"));
            delayQueue.put(new WorkerDelayed(400, "This is message 3"));
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        Thread.sleep(2000);
        Assertions.assertEquals(delayQueue.take().message, "This is message 1");
        Assertions.assertEquals(delayQueue.take().message, "This is message 3");
        Assertions.assertEquals(delayQueue.take().message, "This is message 2");
    }

    @Test
    public void priorityBlockingQueue() throws InterruptedException {
        BlockingQueue<String> queue = new PriorityBlockingQueue<>();
        new Thread(()-> {
            queue.add("D");
            queue.add("C");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            queue.add("A");
        }).start();
        Thread.sleep(100);
        Assertions.assertEquals(queue.take(), "C");
        Assertions.assertEquals(queue.take(), "D");
        Assertions.assertEquals(queue.take(), "A");
    }

    @Test
    public void synchronizedMapInefficientIntrinsicLock() throws InterruptedException {
        Map<String, Integer> map = Collections.synchronizedMap(new HashMap<>());
        Thread t1 = new Thread(() -> IntStream.rangeClosed(1,1000).forEach(n-> map.put(String.valueOf(n), n)));
        Thread t2 = new Thread(() -> IntStream.rangeClosed(1001,2000).forEach(n-> map.put(String.valueOf(n), n)));

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    @Test
    public void concurrentMap() throws InterruptedException {
        ConcurrentMap<String, Integer> map = new ConcurrentHashMap<>();
        Thread t1 = new Thread(() -> IntStream.rangeClosed(1,1000).forEach(n-> map.put(String.valueOf(n), n)));
        Thread t2 = new Thread(() -> IntStream.rangeClosed(1001,2000).forEach(n-> map.put(String.valueOf(n), n)));

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    static class FirstExchanger implements Runnable {
        private int counter;
        private Exchanger<Integer> exchanger;

        public FirstExchanger(Exchanger<Integer> exchanger){
            this.exchanger = exchanger;
        }

        @Override
        public void run() {
            while (true){
                counter ++;
                System.out.println("First thread incremented " + counter);
                try {
                    counter = exchanger.exchange(counter);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    static class SecondExchanger implements Runnable {
        private int counter;
        private Exchanger<Integer> exchanger;

        public SecondExchanger(Exchanger<Integer> exchanger){
            this.exchanger = exchanger;
        }

        @Override
        public void run() {
            while (true){
                counter --;
                System.out.println("Second thread decremented " + counter);
                try {
                    counter = exchanger.exchange(counter);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Test
    public void exchanger() throws InterruptedException {
        Exchanger<Integer> exchanger = new Exchanger<>();
        Thread t1 = new Thread(new FirstExchanger(exchanger));
        Thread t2 = new Thread(new SecondExchanger(exchanger));
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    @Test
    public void copyOnWriteArray() throws InterruptedException {
        List<Integer> list  = new CopyOnWriteArrayList<>();
        list.addAll(List.of(0,0,0,0,0,0,0,0,0,0));
        Thread t1 = new Thread(() -> {
            Random random = new Random();
            while (true) {
                list.set(random.nextInt(list.size()), random.nextInt(10));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread t2 = new Thread(() -> {
            while (true) {
                System.out.println(list);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

}



