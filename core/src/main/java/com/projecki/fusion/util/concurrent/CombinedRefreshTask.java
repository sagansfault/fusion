package com.projecki.fusion.util.concurrent;

/**
 * @author Andavin
 * @since May 31, 2022
 */
public class CombinedRefreshTask extends RefreshTask {

    private final RefreshTask task1, task2;

    public CombinedRefreshTask(RefreshTask task1, RefreshTask task2) {
        this.task1 = task1;
        this.task2 = task2;
    }

    @Override
    public boolean shouldRun(long currentTime) {
        return task1.shouldRun(currentTime) ||
               task2.shouldRun(currentTime);
    }

    @Override
    public void execute(long currentTime) {

        if (task1.shouldRun(currentTime)) {
            task1.execute(currentTime);
        }

        if (task2.shouldRun(currentTime)) {
            task2.execute(currentTime);
        }
    }
}
