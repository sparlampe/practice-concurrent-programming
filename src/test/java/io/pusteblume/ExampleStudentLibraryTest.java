package io.pusteblume;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ExampleStudentLibraryTest {

    static class Book{
        private int id;
        private Lock lock;

        public Book(int id ){
            this.id = id;
            this.lock = new ReentrantLock();
        }
        public void read(Student student) throws InterruptedException {
            if(lock.tryLock(10, TimeUnit.MILLISECONDS)) {
                System.out.println(student + " starts reading " + this);
                Thread.sleep(2000);
                lock.unlock();
                System.out.println(student + " finished reading " + this);
            }
        }

        @Override
        public String toString() {
            return "Book{" +
                    "id=" + id +
                    '}';
        }
    }
    static class Student implements Runnable{
        private int id;
        private Book[] books;

        public Student(int id, Book[] books) {
            this.id = id;
            this.books = books;
        }

        @Override
        public void run() {
            Random random = new Random();
            while (true){
                int bookId = random.nextInt(Constants.NUMBER_OF_BOOKS);
                try {
                    books[bookId].read(this);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }

        @Override
        public String toString() {
            return "Student{" +
                    "id=" + id +
                    '}';
        }
    }
    static class Constants{
        private Constants() {}
        public static final int NUMBER_OF_STUDENTS = 5;
        public static final int NUMBER_OF_BOOKS = 5;
    }

    @Test
    public void problem() throws InterruptedException {
        Student[] students;
        Book[] books;
        ExecutorService executorService = Executors.newFixedThreadPool(Constants.NUMBER_OF_STUDENTS);
        try{
          books = new Book[Constants.NUMBER_OF_BOOKS];
          students = new Student[Constants.NUMBER_OF_STUDENTS];

          for(int i=0; i < Constants.NUMBER_OF_BOOKS; i++){
              books[i] = new Book(i);
          }
          for(int i=0; i < Constants.NUMBER_OF_STUDENTS; i++){
              students[i] = new Student(i, books);
              executorService.execute(students[i]);
          }
        } finally {
            executorService.shutdown();
        }

        Thread.sleep(1000*60);
    }
}



