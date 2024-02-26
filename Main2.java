import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

//Deadlock and starvation free
public class Main2 {
    private static final Random random = new Random();
    private static final int time = 1000;
    //private static final Semaphore cleaners = new Semaphore();
    //private static final Semaphore polishers = new Semaphore();
    //private static final ReentrantLock lock = new ReentrantLock(true);
    private static final Semaphore maxWorkers = new Semaphore(10, true);
    private static final Lightswitch room = new Lightswitch();


    public static void main(String[] args) {
        Stopwatch watch = new Stopwatch();
        //int maxWorkers = 10;
        int totalWorkers = 20;
        //int workers = 0;
        List<Thread> list = new ArrayList<>();

        for (int i = 0; i < totalWorkers; i++) {
            Random random = new Random();
            int number = random.nextInt(2) + 1;

            if (number == 1 ) { //Cleaner
                Cleaner cleaner = new Cleaner();
                cleaner.start();
                list.add(cleaner);
            }
            else if (number == 2) { //Polisher
                Polisher polisher = new Polisher();
                polisher.start();
                list.add(polisher);
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
    }

    private static class Cleaner extends Thread {

        @Override
        public void run() {
            Stopwatch watch2 = new Stopwatch();
            maxWorkers.acquireUninterruptibly();
            double time = watch2.elapsedTime();
            System.out.println("CLEANER THREAD" + " " + this.getId() + " WAIT TIME - " + time);

            room.lockRoom(true);
            try {
                Stopwatch watch3 = new Stopwatch();
                System.out.println("CLEANER THREAD" + " " + this.getId() + " STARTED");
                Thread.sleep(random.nextInt(10000) + 1);
                double time2 = watch3.elapsedTime();
                System.out.println("CLEANER THREAD" + " " + this.getId() + " FINISHED TIME -  " + time2);
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
            Stopwatch watch4 = new Stopwatch();
            maxWorkers.acquireUninterruptibly();
            double time3 = watch4.elapsedTime();
            System.out.println("POLISHER THREAD" + " " + this.getId() + " WAIT TIME - " + time3);
            room.lockRoom(false);
            try {
                Stopwatch watch5 = new Stopwatch();

                System.out.println("POLISHER THREAD" + " " + this.getId() + " STARTED");
                Thread.sleep(random.nextInt(100) + 1);
                double time4 = watch5.elapsedTime();
                System.out.println("POLISHER THREAD" + " " + this.getId() + " FINISHED TIME -  " + time4);
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
        private final Semaphore mutex = new Semaphore(1, true);
        private final Semaphore turnstile = new Semaphore(1, true);

        public synchronized void lockRoom(Boolean check) {
            try {
                turnstile.acquireUninterruptibly();
                if (check) {
                    if (counterCleaner == 0) {
                        mutex.acquireUninterruptibly();
                    }
                    counterCleaner++;
                } else {
                    if (counterPolisher == 0) {
                        mutex.acquireUninterruptibly();
                    }
                    counterPolisher++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                turnstile.release();
            }
        }
        public synchronized void unlockRoom(Boolean check) {
            try {
                turnstile.acquireUninterruptibly();
                if (check) {
                    if (counterCleaner == 0) {
                        mutex.acquireUninterruptibly();
                    }
                    counterCleaner++;
                } else {
                    if (counterPolisher == 0) {
                        mutex.acquireUninterruptibly();
                    }
                    counterPolisher++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                turnstile.release();
            }
        }
    }
}