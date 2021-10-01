package io.pusteblume;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

public class ForkJoinTest {

    static class  SimpleRecursiveAction extends RecursiveAction {
        private int simulatedWork;

        public SimpleRecursiveAction(int simulatedWork) {
            this.simulatedWork = simulatedWork;
        }

        @Override
        protected void compute() {
            if(simulatedWork > 100){
                System.out.println("Parallel execution and split task ..." + simulatedWork);
                SimpleRecursiveAction simpleRecursiveAction1 = new SimpleRecursiveAction(simulatedWork/2);
                SimpleRecursiveAction simpleRecursiveAction2 = new SimpleRecursiveAction(simulatedWork/2);
                simpleRecursiveAction1.fork();
                simpleRecursiveAction2.fork();

            } else {

                System.out.println("No need in forking..." + simulatedWork);
            }
        }
    }

    @Test
    public void recursiveAction() {
            ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
            SimpleRecursiveAction simpleRecursiveAction = new SimpleRecursiveAction(200);
            pool.invoke(simpleRecursiveAction);

    }

    static class  SimpleRecursiveTask extends RecursiveTask<Integer> {
        private int simulatedWork;

        public SimpleRecursiveTask(int simulatedWork) {
            this.simulatedWork = simulatedWork;
        }

        @Override
        protected Integer compute() {
            if(simulatedWork > 100){
                System.out.println("Parallel execution and split task ..." + simulatedWork);
                SimpleRecursiveTask simpleRecursiveTask1 = new SimpleRecursiveTask(simulatedWork/2);
                SimpleRecursiveTask simpleRecursiveTask2 = new SimpleRecursiveTask(simulatedWork/2);
                simpleRecursiveTask1.fork();
                simpleRecursiveTask2.fork();
                int solution = 0;
                solution += simpleRecursiveTask1.join();
                solution += simpleRecursiveTask2.join();

                return solution;
            } else {
                System.out.println("No need in forking..." + simulatedWork);
                return 2*simulatedWork;
            }
        }
    }

    @Test
    public void recursiveTask() {
        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        SimpleRecursiveTask simpleRecursiveTask = new SimpleRecursiveTask(200);
        System.out.println(pool.invoke(simpleRecursiveTask));
    }
}



