package com.projecki.fusion.util;

import java.util.LinkedList;
import java.util.Queue;

public class RateLimiter {

    private final long cooldown;
    private final int maxCharges;

    private final Queue<Long> lastUsed = new LinkedList<>();

    public RateLimiter(long cooldown, int maxCharges) {
        this.cooldown = cooldown;
        this.maxCharges = maxCharges;
    }

    public boolean isLimited() {
        cleanTop();
        return lastUsed.size() >= maxCharges;
    }

    public void use() {
        lastUsed.add(System.currentTimeMillis());
        cleanTop();
    }

    private void cleanTop() {
        if (lastUsed.size() == 0) return;

        if (lastUsed.peek() + cooldown < System.currentTimeMillis()) {
            lastUsed.poll();
            cleanTop();
        }
    }

}
