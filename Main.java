import java.awt.geom.Point2D;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

//Deadlock free but can starve
//Removed fairness
public class Main {
    private static final Random random = new Random();
    private static final int time = 1000;
    private static final int totalThreads = 100;
    private static final AtomicInteger threads = new AtomicInteger();
    private static int cleaners = 0;
    private static int polishers = 0;
    //private static final Semaphore cleaners = new Semaphore();
    //private static final Semaphore polishers = new Semaphore();
    private static final Lightswitch room = new Lightswitch();
    //private static final Semaphore mutex = new Semaphore(1, true);
    private static final Semaphore maxWorkers = new Semaphore(25);


    public static void main(String[] args) {
        Stopwatch watch = new Stopwatch();
        //int maxWorkers = 10;
        //int workers = 0;
        List<Thread> list = new ArrayList<>();

        while (threads.get() < totalThreads) {
            try {
                Thread.sleep(time);
                Random random = new Random();
                int number = random.nextInt(2) + 1;

                if (number == 1) { //Cleaner
                    Cleaner cleaner = new Cleaner();
                    cleaner.start();
                    list.add(cleaner);
                } else if (number == 2) { //Polisher
                    Polisher polisher = new Polisher();
                    polisher.start();
                    list.add(polisher);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                threads.incrementAndGet();
            }
        }

        for (int i = 0; i < list.size(); i++) { //Loop to wait for each thread to finish and begin next
            try {
                list.get(i).join();
            } catch (InterruptedException ex) {
                System.out.println("Something is not working");
            }
        }
        System.out.printf("Program took %f seconds\n", watch.elapsedTime());
        System.out.println("Total threads created " + threads.get());
    }

    private static class Cleaner extends Thread {

        @Override
        public void run() {
            Stopwatch watch6 = new Stopwatch();
            maxWorkers.acquireUninterruptibly();
            double time6 = watch6.elapsedTime();
            System.out.println("CLEANER THREAD" + " " + this.threadId() + " WAIT TIME - " + time6);
            room.lockRoom(true);
            try {
                Stopwatch watch7 = new Stopwatch();
                System.out.println("CLEANER THREAD" + " " + this.threadId() + " STARTED");
                Thread.sleep(random.nextInt(10000) + 1);
                double time7 = watch7.elapsedTime();
                System.out.println("CLEANER THREAD" + " " + this.threadId() + " FINISHED TIME -  " + time7);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                room.unlockRoom(true);
                maxWorkers.release();
            }
        }
    }

    private static class Polisher extends Thread {

        @Override
        public void run() {
            Stopwatch watch8 = new Stopwatch();
            maxWorkers.acquireUninterruptibly();
            double time8 = watch8.elapsedTime();
            System.out.println("POLISHER THREAD" + " " + this.threadId() + " WAIT TIME - " + time8);
            room.lockRoom(false);
            try {
                Stopwatch watch9 = new Stopwatch();
                System.out.println("POLISHER THREAD" + " " + this.threadId() + " STARTED");
                Thread.sleep(random.nextInt(10000) + 1);
                double time9 = watch9.elapsedTime();
                System.out.println("CLEANER THREAD" + " " + this.threadId() + " FINISHED TIME -  " + time9);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                room.unlockRoom(false);
                maxWorkers.release();
            }
        }
    }

    private static class Lightswitch {
        private int counterCleaner = 0;
        private int counterPolisher = 0;
        private final Semaphore cleanerMutex = new Semaphore(1);
        private final Semaphore polisherMutex = new Semaphore(1);

        public synchronized void lockRoom(Boolean check) {
            if (check) {
                counterCleaner++;
                if (counterCleaner == 1) {
                    polisherMutex.acquireUninterruptibly();
                }
            } else {
                counterPolisher++;
                if (counterPolisher == 1) {
                    cleanerMutex.acquireUninterruptibly();
                }
            }
        }

        public synchronized void unlockRoom(Boolean check) {
            if (check) {
                counterCleaner--;
                if (counterCleaner == 0) {
                    polisherMutex.release();
                }
            } else {
                counterPolisher--;
                if (counterPolisher == 0) {
                    cleanerMutex.release();
                }
            }
        }
    }
}
