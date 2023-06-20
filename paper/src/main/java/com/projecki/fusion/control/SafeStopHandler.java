package com.projecki.fusion.control;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Allows registering of handlers which
 * dictate if it is safe to stop the server
 */
public class SafeStopHandler {

    private final Set<Supplier<Boolean>> safeHandlers = new HashSet<>();

    /**
     * Checks all the registered handlers and returns if
     * they all believe the server is safe to stop
     *
     * @return if the server is safe to stop
     */
    boolean safeToStop() {
        return safeHandlers.stream()
                .allMatch(Supplier::get);
    }

    /**
     * Register a handler that checks if the server is
     * currently able to safely stop
     */
    public void registerHandler(Supplier<Boolean> isSafe) {
       safeHandlers.add(isSafe);
    }

}
