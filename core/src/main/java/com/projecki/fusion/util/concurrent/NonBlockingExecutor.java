package com.projecki.fusion.util.concurrent;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * A simple implementation of {@link ExecutorService} for
 * executing {@link Runnable tasks}.
 * <p>
 * This implementation is completely non-blocking when adding
 * tasks. {@link ConcurrentLinkedQueue} is used as the underlying
 * queue; the queue is only checked for tasks on a periodic timer.
 * <p>
 * <i>Tasks are, in no way, guaranteed to be executed immediately.</i>
 * <p>
 * All tasks in the queue will be executed and, once empty,
 * the thread will go into a waiting state for a period
 * of milliseconds. It will then check if any more tasks are available.
 * If no tasks are available, the thread will wait again, check again
 * and so on.
 *
 * @since April 11, 2022
 * @author Andavin
 */
public class NonBlockingExecutor extends AbstractExecutorService {

    private volatile boolean running = true;
    private final long period;
    private final List<Thread> threads;
    private final Object lock = new Object();
    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();

    /**
     * Create a new executor with a single thread
     * to execute tasks with.
     *
     * @param name The name of the executor thread.
     * @param period The period of milliseconds to will wait after
     *               completing all available tasks before checking
     *               for more tasks.
     */
    public NonBlockingExecutor(String name, long period) {
        checkArgument(period > 0, "invalid period: %s", period);
        this.period = period;
        this.threads = List.of(this.createThread(r -> new Thread(r, name), 0));
    }

    /**
     * Create a new executor with a single thread
     * to execute tasks with.
     *
     * @param factory The {@link ThreadFactory} to use when
     *                creating the {@link Thread}.
     * @param period The period of milliseconds to will wait after
     *               completing all available tasks before checking
     *               for more tasks.
     */
    public NonBlockingExecutor(ThreadFactory factory, long period) {
        checkArgument(period > 0, "invalid period: %s", period);
        this.period = period;
        this.threads = List.of(this.createThread(factory, 0));
    }

    /**
     * Create a new executor with a fixed amount of
     * threads to execute tasks with.
     *
     * @param name The name prefix to use when naming each thread.
     * @param period The period of milliseconds each thread will wait after
     *               completing all available tasks before checking
     *               for more tasks.<br>
     *               With multiple threads, the effective period will be roughly:
     *               <pre>Math.max(period / threads, 1)</pre>
     * @param threadCount The amount of threads to use to execute tasks.<br>
     *                    Threads will start on an interval split as evenly
     *                    as possible using {@code Math.max(period / threads, 1)}
     *                    for the offset of each subsequent thread.
     */
    public NonBlockingExecutor(String name, long period, int threadCount) {

        checkArgument(period > 0, "invalid period: %s", period);
        checkArgument(threadCount > 0, "invalid thread count: %s", threadCount);
        Thread[] threads = new Thread[threadCount];
        long offset = Math.max(period / threadCount, 1);
        for (int i = 0; i < threadCount; i++) {
            String threadName = name + " - " + i;
            threads[i] = this.createThread(r -> new Thread(r, threadName), offset * i);
        }

        this.period = period;
        this.threads = List.of(threads);
    }

    /**
     * Create a new executor with a fixed amount of
     * threads to execute tasks with.
     *
     * @param factory The {@link ThreadFactory} to use when
     *                creating {@link Thread Threads}.
     * @param period The period of milliseconds each thread will wait after
     *               completing all available tasks before checking
     *               for more tasks.<br>
     *               With multiple threads, the effective period will be roughly:
     *               <pre>Math.max(period / threads, 1)</pre>
     * @param threadCount The amount of threads to use to execute tasks.
     *                    Threads will start on an interval split as evenly
     *                    as possible using {@code Math.max(period / threads, 1)}
     *                    for the offset of each subsequent thread.
     */
    public NonBlockingExecutor(ThreadFactory factory, long period, int threadCount) {

        checkArgument(period > 0, "invalid period: %s", period);
        checkArgument(threadCount > 0, "invalid thread count: %s", threadCount);
        Thread[] threads = new Thread[threadCount];
        long offset = Math.max(period / threadCount, 1);
        for (int i = 0; i < threadCount; i++) {
            threads[i] = this.createThread(factory, offset * i);
        }

        this.period = period;
        this.threads = List.of(threads);
    }

    @Override
    public void execute(@NotNull Runnable command) {
        this.queue.add(command);
    }

    @Override
    public void shutdown() {

        this.running = false;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    @NotNull
    @Override
    public List<Runnable> shutdownNow() {

        if (!running) {
            return List.of();
        }

        this.shutdown();
        for (Thread thread : threads) {
            thread.interrupt();
        }

        if (queue.isEmpty()) {
            return List.of();
        }

        List<Runnable> tasks = new ArrayList<>();
        Runnable task;
        while ((task = queue.poll()) != null) {
            tasks.add(task);
        }

        return List.copyOf(tasks);
    }

    @Override
    public boolean isShutdown() {
        return !running;
    }

    @Override
    public boolean isTerminated() {
        return !running && queue.isEmpty();
    }

    @Override
    public boolean awaitTermination(long timeout, @NotNull TimeUnit unit) throws InterruptedException {

        checkState(isShutdown(), "not shutdown");
        long start = System.nanoTime();
        long nanos = unit.toNanos(timeout);
        if (!queue.isEmpty()) {

            long leftover = nanos;
            for (Thread thread : threads) {

                thread.join(TimeUnit.NANOSECONDS.toMillis(leftover), (int) (leftover % 1000000));
                leftover = nanos - (System.nanoTime() - start);
                if (leftover <= 0) {
                    return false;
                }
            }
        }

        return true;
    }

    private Thread createThread(ThreadFactory factory, long startupDelay) {
        Thread thread = factory.newThread(() -> {

            if (startupDelay > 0) {
                try {
                    Thread.sleep(startupDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            while (running) {
                this.executeTasks();
            }
        });
        thread.start();
        return thread;
    }

    private void executeTasks() {

        if (queue.isEmpty()) {
            synchronized (lock) {
                try {
                    lock.wait(period);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        Runnable action;
        while ((action = queue.poll()) != null) {
            try {
                action.run();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
