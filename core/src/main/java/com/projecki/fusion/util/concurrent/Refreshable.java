package com.projecki.fusion.util.concurrent;

import org.jetbrains.annotations.Nullable;

/**
 * @since April 08, 2022
 * @author Andavin
 */
public interface Refreshable {

    /**
     * Get the {@link Runnable} task that should
     * be executed refreshing.
     *
     * @return The refresh task.
     */
    @Nullable
    RefreshTask refreshTask();
}
