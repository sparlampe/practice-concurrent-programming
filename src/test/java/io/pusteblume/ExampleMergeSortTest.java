package io.pusteblume;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ExampleMergeSortTest {

    static class MergeSort{

        private int[] nums;
        private int[] tempArray;
        public MergeSort(int[] nums){
            this.nums = nums;
            this.tempArray = new int[nums.length];
        }

        public Thread mergeSortParallel(int low, int high, int numThread){
            return new Thread(){
                @Override public void run(){
                    parallelMergeSort(low, high, numThread/2);
                }
            };
        }

        private void parallelMergeSort(int low, int high, int numOfThreads) {
            if(numOfThreads<=1){
                mergeSort(low, high);
                return;
            }
            int middle = (low+high)/2;
            Thread leftSorter = mergeSortParallel(low, middle, numOfThreads);
            Thread rightSorter = mergeSortParallel(middle+1, high, numOfThreads);
            leftSorter.start();
            rightSorter.start();
            try {
                leftSorter.join();
                rightSorter.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            merge(low, middle, high);
        }

        public void mergeSort(int low, int high){
            if(low >= high){
                return;
            }
            int middle = (low + high)/ 2;
            mergeSort(low, middle);
            mergeSort(middle+1, high);
            merge(low, middle, high);
        }
        private void merge(int low, int middle, int high){
            for(int i= low; i <= high; i++){
                tempArray[i] = nums[i];
            }
            int i = low;
            int j = middle +1;
            int k = low;
            while (( i<= middle) && ( j<= high)){
                if(tempArray[i] <= tempArray[j]){
                    nums[k] = tempArray[i];
                    i++;
                } else {
                    nums[k] = tempArray[j];
                    j++;
                }
                k++;
            }

            while(i <= middle){
                nums[k] = tempArray[i];
                k++;
                i++;
            }
            while(j <= high){
                nums[k] = tempArray[j];
                k++;
                j++;
            }
        }
        public int[] getNums() {
            return nums;
        }
        public static int[] createRandomArray(int n) {
            Random random = new Random();
            int[] a = new int[n];

            for (int i = 0; i < n; i++) {
                a[i] = random.nextInt(10000);
            }

            return a;
        }
    }

    @Test
    public void sequential() {
        Random random = new Random();
        int[] nums = new int[30];
        for (int j = 0; j < nums.length; j++){
            nums[j] = random.nextInt(1000) -500;
        }

        MergeSort mergeSort = new MergeSort(nums);
        mergeSort.mergeSort(0, nums.length-1);

        int[] cloneNums = Arrays.copyOf(nums, nums.length);
        Arrays.sort(cloneNums);

        System.out.println(Arrays.toString(mergeSort.getNums()));
        System.out.println(Arrays.toString(cloneNums));
        Assertions.assertArrayEquals(mergeSort.getNums(), cloneNums);
    }
    @Test
    public void parallel() {
        int numOfCpus = Runtime.getRuntime().availableProcessors();
        int[] nums = {4,2,6,5,44,78,-4,0,1};

        int[] cloneNums = Arrays.copyOf(nums, nums.length);
        Arrays.sort(cloneNums);

        MergeSort mergeSort = new MergeSort(nums);
        mergeSort.parallelMergeSort(0, nums.length-1, numOfCpus);

        System.out.println(Arrays.toString(mergeSort.getNums()));
        System.out.println(Arrays.toString(cloneNums));
        Assertions.assertArrayEquals(mergeSort.getNums(), cloneNums);
    }

    @Test
    public void seqVsParallel(){

            int numOfThreads  = Runtime.getRuntime().availableProcessors();

            System.out.println("Number of threads/cores: " + numOfThreads);
            System.out.println("");

            int[] numbers1 = MergeSort.createRandomArray(1000000);
            int[] numbers2 =  Arrays.copyOf(numbers1, numbers1.length);

            for(int i=0;i<numbers1.length;i++)
                numbers2[i] = numbers1[i];

            MergeSort parallelSorter = new MergeSort(numbers1);

            // run the algorithm and time how long it takes
            long startTime1 = System.currentTimeMillis();
            parallelSorter.parallelMergeSort(0, numbers1.length-1, numOfThreads);
            long endTime1 = System.currentTimeMillis();

            System.out.printf("Time taken for 10 000 000 elements parallel =>  %6d ms \n", endTime1 - startTime1);
            System.out.println("");

            startTime1 = System.currentTimeMillis();
            MergeSort sequentisalSorted = new MergeSort(numbers2);
            sequentisalSorted.mergeSort(0,numbers2.length-1);
            endTime1 = System.currentTimeMillis();

            System.out.printf("Time taken for 10 000 000 elements sequential =>  %6d ms \n", endTime1 - startTime1);
            System.out.println("");

    }
}



