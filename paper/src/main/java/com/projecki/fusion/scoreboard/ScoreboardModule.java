package com.projecki.fusion.scoreboard;

import com.projecki.fusion.user.PaperUserModule;
import com.projecki.fusion.util.concurrent.CombinedRefreshTask;
import com.projecki.fusion.util.concurrent.RefreshTask;
import com.projecki.fusion.util.concurrent.Refreshable;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @since May 24, 2022
 * @author Andavin
 */
public class ScoreboardModule extends PaperUserModule implements Refreshable {

    protected static final AtomicInteger OBJ_IDS = new AtomicInteger();
    protected RefreshTask refreshTask;

    @Override
    public @Nullable RefreshTask refreshTask() {
        return refreshTask;
    }

    /**
     * Set the specified {@link RefreshTask} combining it with
     * the current {@link #refreshTask()}, if one is present.
     *
     * @param refreshTask The {@link RefreshTask} to set.
     */
    protected void refreshTask(RefreshTask refreshTask) {
        this.refreshTask = this.refreshTask != null ?
                new CombinedRefreshTask(this.refreshTask, refreshTask) :
                refreshTask;
    }

    /**
     * Clear the {@link RefreshTask} if one is currently set
     * on this module.
     */
    public void clearAutoRefresh() {
        this.refreshTask = null;
    }
}
