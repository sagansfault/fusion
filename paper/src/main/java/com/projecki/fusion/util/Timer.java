package com.projecki.fusion.util;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;

public class Timer {

    @NotNull
    private Duration duration;

    @NotNull
    private Instant startTimestamp;

    @NotNull
    private State state = State.PAUSED;

    /**
     * Construct a new {@link Timer} for the specified {@link Duration}
     * The created {@link Timer} doesn't start automatically, use {@link #start()} to start the timer.
     *
     * @param duration the duration of the timer
     */
    public Timer(@NotNull Duration duration) {
        this.startTimestamp = Instant.now();
        this.duration = duration;
    }


    /**
     * Get the amount of time remaining on this {@link Timer}.
     * If the timer is paused, this time will stay constant
     *
     * @return the time remaining on this timer
     */
    public Duration getTimeRemaining () {

        // if the timer is paused, we can just return the duration
        if (state == State.PAUSED) return duration;

        // update the state of the timer, to make sure it's not finished
        updateState();

        // if the timer is finished, just return a duration of 0
        if (state == State.FINISHED) {
            return Duration.ofSeconds(0);
        }

        // return the remaining duration
        return Duration.between(Instant.now(), startTimestamp.plus(duration));
    }

    /**
     * Add the specified amount of time to the timer's duration.
     *
     * @param addedDuration the duration to add to the timer
     *
     * @return the new {@link Duration} of the timer
     */
    public Duration add (@NotNull Duration addedDuration) {
        this.duration = notNegative(duration.plus(addedDuration));
        return duration;
    }

    /**
     * Remove the specified amount of time from the timer.
     *
     * @param subtractedDuration the time to remove from the timer
     * @return
     */
    public Duration subtract (@NotNull Duration subtractedDuration) {
        return add(subtractedDuration.negated());
    }

    /**
     * Pause the timer, this will freeze the time remaining.
     */
    public void pause() {
        if (state == State.PAUSED) return;

        state = State.PAUSED;
        duration = duration.minus(Duration.between(startTimestamp, Instant.now()));
    }

    /**
     * Start the timer
     */
    public void start() {
        if (state != State.PAUSED) return;

        state = State.RUNNING;
        startTimestamp = Instant.now();
    }

    /**
     * Update the state of the timer
     */
    private void updateState() {
        if (state == State.PAUSED) return;

        if (startTimestamp.plus(duration).isBefore(Instant.now())) {
            state = State.FINISHED;
        }
    }

    /**
     * Make sure this {@link Timer} doesn't get a negative duration.
     * By simply setting it to a {@link Duration} of {@code 0}
     */
    private Duration notNegative (@NotNull Duration value) {
        return value.toNanos() < 0 ? Duration.ofSeconds(0) : value;
    }

    /**
     * Get whether the {@link Timer} is finished
     *
     * @return {@code true} if the timer is finished or else {@code false}
     */
    public boolean isFinished() {
        updateState();
        return state == State.FINISHED;
    }

    /**
     * Get whether the {@link Timer} is currently running!
     *
     * @return {@code true} if the timer is running or else {@code false}
     */
    public boolean isRunning() {
        updateState();
        return state == State.RUNNING;
    }

    /**
     * Get the {@link Duration} of this timer.
     *
     * @return the duration of this timer
     */
    @NotNull
    public Duration getDuration() {
        return duration;
    }

    enum State {
        RUNNING, PAUSED, FINISHED
    }
}