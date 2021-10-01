package io.pusteblume;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SemaphoresMutexesTest {

    enum Downloader {
        INSTANCE;
        public Semaphore semaphore = new Semaphore(3, true);
        public void downloadData(){
            try{
                semaphore.acquire();
                download();
            } catch (InterruptedException e){
                e.printStackTrace();
            } finally {
                semaphore.release();
            }
        }
        private void download(){
            System.out.println("Downloading data from the web..." +Thread.currentThread().getName());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void semaphores() throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();

        for(int i=0; i < 12; i++){
            executorService.execute(Downloader.INSTANCE::downloadData);
        }

        Thread.sleep(100);
        Assertions.assertEquals(Downloader.INSTANCE.semaphore.getQueueLength(), 9);

    }
}



