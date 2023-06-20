package com.projecki.fusion.control;

public enum ControlAction {
    /**
     * Safely stop a server based on registered conditions
     */
    SAFE_STOP,
    /**
     * Stop a server regardless of conditions
     */
    SHUTDOWN,
    /**
     * Mark a server for stopping soon
     */
    MARK_STOPPING,
    /**
     * Kill the server process, assuming that control
     * handling is still running and active
     */
    KILL
}
