package com.projecki.fusion.user;

import com.projecki.fusion.util.concurrent.RefreshTask;
import com.projecki.fusion.util.concurrent.RefreshTaskExecutor;
import com.projecki.fusion.util.concurrent.Refreshable;

/**
 * @since May 31, 2022
 * @author Andavin
 */
public final class UserTaskExecutor extends RefreshTaskExecutor {

    private static final long PERIOD = ONE_MILLIS * 50;
    private final Users<?, ?> users;

    public UserTaskExecutor(Users<?, ?> users) {
        super(PERIOD);
        this.users = users;
    }

    @Override
    protected void execute(long currentTime) {

        for (User<?> user : users.getOnline()) {

            if (user instanceof Refreshable refreshable) {

                RefreshTask task = refreshable.refreshTask();
                if (task != null && task.shouldRun(currentTime)) {
                    task.execute(currentTime);
                }
            }

            for (Refreshable refreshable : user.getAll(Refreshable.class)) {

                RefreshTask task = refreshable.refreshTask();
                if (task != null && task.shouldRun(currentTime)) {
                    task.execute(currentTime);
                }
            }
        }
    }
}
