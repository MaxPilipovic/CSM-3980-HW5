import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

//Deadlock and starvation free
public class Main2 {
    private static final Random random = new Random();
    private static final int time = 100;
    private static final AtomicInteger threads = new AtomicInteger();
    private static final int totalThreads = 625;
    //private static final Semaphore cleaners = new Semaphore();
    //private static final Semaphore polishers = new Semaphore();
    //private static final ReentrantLock lock = new ReentrantLock(true);
    private static final Semaphore maxWorkers = new Semaphore(125, true);
    private static final Lightswitch room = new Lightswitch();


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
            Stopwatch watch2 = new Stopwatch();
            maxWorkers.acquireUninterruptibly();
            double time = watch2.elapsedTime();
            System.out.println("CLEANER THREAD" + " " + this.threadId() + " WAIT TIME - " + time);

            room.lockRoom(true);
            try {
                Stopwatch watch3 = new Stopwatch();
                System.out.println("CLEANER THREAD" + " " + this.threadId() + " STARTED");
                Thread.sleep(random.nextInt(10000) + 1);
                double time2 = watch3.elapsedTime();
                System.out.println("CLEANER THREAD" + " " + this.threadId() + " FINISHED TIME -  " + time2);
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
            System.out.println("POLISHER THREAD" + " " + this.threadId() + " WAIT TIME - " + time3);
            room.lockRoom(false);
            try {
                Stopwatch watch5 = new Stopwatch();

                System.out.println("POLISHER THREAD" + " " + this.threadId() + " STARTED");
                Thread.sleep(random.nextInt(100) + 1);
                double time4 = watch5.elapsedTime();
                System.out.println("POLISHER THREAD" + " " + this.threadId() + " FINISHED TIME -  " + time4);
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
        private final Semaphore cleanerMutex = new Semaphore(1, true);
        private final Semaphore polisherMutex= new Semaphore(1, true);
        private final ReentrantLock turnstile = new ReentrantLock(true);

        public synchronized void lockRoom(Boolean check) {
            try {
                turnstile.lock();
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
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                turnstile.unlock();
            }
        }

        public synchronized void unlockRoom(Boolean check) {
            try {
                turnstile.lock();
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
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                turnstile.unlock();
            }
        }
    }
}