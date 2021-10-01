package io.pusteblume;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class ExampleSummingUpTest {

    static class SequentialSum {

        public int sum(int[] nums){
            int total = 0;

            for(int i=0; i<nums.length; ++i){
                total = total + nums[i];
            }
            return total;
        }

    }

    @Test
    public void sequential() {
        Random random = new Random();
        int[] nums = new int[30];
        for (int j = 0; j < nums.length; j++){
            nums[j] = random.nextInt(1000) -500;
        }
        SequentialSum sequentialSum = new SequentialSum();
        int seqSum = sequentialSum.sum(nums);
        int expectedSeqSum = IntStream.of(nums).sum();
        Assertions.assertEquals(expectedSeqSum, seqSum);
    }


    static class ParallelWorker extends Thread {

        private int[] nums;
        private int low;
        private int high;


        private int partialSum;

        public ParallelWorker(int[] nums, int low, int high) {
            this.nums = nums;
            this.low = low;
            this.high = high;
        }

        public int getPartialSum() {
            return partialSum;
        }

        @Override
        public void run(){
            partialSum =0;
            for(int i=low; i<high; i++){
                partialSum = partialSum + nums[i];
            }
        }
    }
    static class ParallelSum {
        private ParallelWorker[] sums;
        private int numOfThreads;

        public ParallelSum(int numOfThreads){
            this.numOfThreads = numOfThreads;
            this.sums = new ParallelWorker[numOfThreads];
        }

        public int sum(int[] nums){
            int steps = (int) Math.ceil(nums.length * 1.0 / numOfThreads);
            for(int i=0; i<numOfThreads; i++){
                sums[i] = new ParallelWorker(nums, i*steps, Math.min((i+1)*steps, nums.length));
                sums[i].start();
            }

            try {
                for(ParallelWorker worker: sums)
                    worker.join();
            } catch (InterruptedException e) {
                    e.printStackTrace();
            }

            int total = 0;
            for(ParallelWorker worker: sums)
                total += worker.getPartialSum();

            return total;
        }
    }


    @Test
    public void parallel() {
        Random random = new Random();
        ParallelSum parallelSum = new ParallelSum(Runtime.getRuntime().availableProcessors());
        int[] nums = new int[30];
        for (int j = 0; j < nums.length; j++){
            nums[j] = random.nextInt(1000) -500;
        }

        int parSum = parallelSum.sum(nums);
        int expectedSeqSum = IntStream.of(nums).sum();
        Assertions.assertEquals(expectedSeqSum, parSum);
    }
}



