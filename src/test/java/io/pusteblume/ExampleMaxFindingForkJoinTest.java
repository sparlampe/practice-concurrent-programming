package io.pusteblume;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ExampleMaxFindingForkJoinTest {

    static final int SIZE = Integer.MAX_VALUE/2;
    static final int THRESHOLD = SIZE/Runtime.getRuntime().availableProcessors();

    static class SequentialMaxFind{

        public int sequentialMaxFind(int[] nums){
            int max = nums[0];
            for (int i=1; i<nums.length; i++){
                if(nums[i] > max){
                    max = nums[i];
                }
            }
            return  max;
        }
    }

    static class ParallelMaxFind extends RecursiveTask<Integer> {

        private int[] nums;
        private int lowIndex;
        private int highIndex;

        public ParallelMaxFind(int[] nums, int lowIndex, int highIndex) {
            this.nums = nums;
            this.lowIndex = lowIndex;
            this.highIndex = highIndex;
        }

        private int sequentialMaxFind(){
            int max = nums[lowIndex];
            for (int i=lowIndex+1; i<highIndex; i++){
                if(nums[i] > max){
                    max = nums[i];
                }
            }
            return  max;
        }

        @Override
        protected Integer compute() {
            if(highIndex - lowIndex < THRESHOLD){
                return sequentialMaxFind();
            } else {
                int middleIndex = (lowIndex+highIndex)/2;
                ParallelMaxFind task1 = new ParallelMaxFind(nums, lowIndex, middleIndex+1);
                ParallelMaxFind task2 = new ParallelMaxFind(nums, middleIndex+1, highIndex);
                invokeAll(task1, task2);
                return Math.max(task1.join(), task2.join());
            }
        }
    }

    public static int[] createRandomArray(int n) {
        Random random = new Random();
        int[] a = new int[n];

        for (int i = 0; i < n; i++) {
            a[i] = random.nextInt(10000);
        }

        return a;
    }

    @Test
    public void findMax() {
        int[] nums = createRandomArray(SIZE);
        System.out.println("Max: " + Arrays.stream(nums).max().getAsInt());
        SequentialMaxFind sequentialMaxFind = new SequentialMaxFind();
        long start = System.currentTimeMillis();
        System.out.println("Max: " + sequentialMaxFind.sequentialMaxFind(nums));
        System.out.println("Time taken: " + (System.currentTimeMillis() - start) + "ms");

        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        ParallelMaxFind parallelMaxFind = new ParallelMaxFind(nums, 0, nums.length);
        start = System.currentTimeMillis();
        System.out.println("Max: " + pool.invoke(parallelMaxFind));
        System.out.println("Time taken: " + (System.currentTimeMillis() - start) + "ms");
    }

}



