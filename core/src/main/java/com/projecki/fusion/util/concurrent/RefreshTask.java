package com.projecki.fusion.util.concurrent;

/**
 * @author Andavin
 * @since April 08, 2022
 */
public class RefreshTask {

    private long next;
    private final long period;
    private final Runnable task;

    /**
     * Create a new refresh task that runs a task every
     * period of nanoseconds.
     *
     * @param period The amount of nanoseconds to wait
     *               between runs of the task.
     * @param task The {@link Runnable} task to run.
     */
    public RefreshTask(long period, Runnable task) {
        this.task = task;
        this.period = period;
        this.next = System.nanoTime() + period;
    }

    /**
     * Create a new refresh task that runs as the same
     * time as the given refresh task and with the same
     * period, but run a separate task.
     *
     * @param refreshTask The refresh task to copy from.
     * @param task The {@link Runnable} task to run.
     */
    public RefreshTask(RefreshTask refreshTask, Runnable task) {
        this.next = refreshTask.next;
        this.period = refreshTask.period;
        this.task = task;
    }

    RefreshTask() {
        this.next = 0;
        this.period = 0;
        this.task = null;
    }

    /**
     * Determine whether this task should run.
     *
     * @param currentTime The current time (in nanoseconds).
     * @return If this task should run.
     */
    public boolean shouldRun(long currentTime) {
        return currentTime >= next;
    }

    /**
     * Execute this task and set the next time that it
     * should execute.
     *
     * @param currentTime The current time (in nanoseconds).
     */
    public void execute(long currentTime) {
        this.next = currentTime + period;
        this.task.run();
    }
}
