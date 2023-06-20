package com.projecki.fusion.util.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/**
 * An executor {@link Runnable} that periodically calls
 * the {@link #execute(long)} when used as the task passed
 * to a {@link Thread} constructor such as {@link Thread(Runnable)}.
 * <p>
 *     The implementation is intended to search for
 *     {@link RefreshTask RefreshTasks} that are present and
 *     {@link RefreshTask#shouldRun(long) should run} and execute them.
 * </p>
 * <p>
 *     In this way, a {@link RefreshTask} is not needed to be
 *     scheduled anywhere and instead can be passive while the
 *     {@link Thread} executing this task is active.
 * </p>
 *
 * @since May 31, 2022
 * @author Andavin
 */
public abstract class RefreshTaskExecutor implements Runnable {

    protected static final long ONE_MILLIS = 1000000;
    private volatile boolean running = true;
    private final long period;
    private final Object refreshLock = new Object();

    /**
     * Create a new task executor.
     *
     * @param period The period of time (in nanoseconds) that should
     *               elapse before the loop should iterate again.
     */
    protected RefreshTaskExecutor(long period) {
        this.period = period;
    }

    @Override
    public final void run() {

        while (running) {

            long currentTime = System.nanoTime();
            try {
                this.execute(currentTime);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            // Wait the time minus the elapsed time
            LongSupplier wait = () -> period - (System.nanoTime() - currentTime);
            if (wait.getAsLong() >= ONE_MILLIS) { // Avoid synchronization if we don't have to

                synchronized (refreshLock) {
                    do {
                        long waitMs = TimeUnit.NANOSECONDS.toMillis(wait.getAsLong());
                        if (waitMs > 0) {

                            try {
                                refreshLock.wait(waitMs);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            break;
                        }
                    } while (wait.getAsLong() >= ONE_MILLIS);
                }
            }
        }
    }

    /**
     * Execute the task for this refresh action.
     * <p>
     *     This is called once every {@link #period}; never shorter,
     *     but, depending on how long the tasks require to execute,
     *     could be longer.
     * </p>
     *
     * @param currentTime The current time in
     *                    {@link System#nanoTime() nanoseconds}.
     */
    protected abstract void execute(long currentTime);

    /**
     * Cease execution of this task on start of the
     * next iteration.
     */
    public final void shutdown() {
        this.running = false;
    }
}
