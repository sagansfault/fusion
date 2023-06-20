package com.projecki.fusion.game.state;

import com.google.common.base.Preconditions;

/**
 * An extension of {@link TickingGameState} which groups a number of ticks into a frame and calls
 * {@link FramedTickingGameState#onTickFrame()} when the amount of ticks makes a frame.
 *
 * This allows you to specify the amount of ticks before triggering the {@link #onTickFrame()} method.
 */
public abstract class FramedTickingGameState extends TickingGameState {

	private final int ticksPerFrame;
	private int ticks = 0;

	/**
	 * A ticking state which only calls {@link #onTickFrame()} after a certain amount of ticks
	 *
	 * @param ticksPerFrame the amount of ticks before calling the method
	 * @throws IllegalArgumentException when ticksPerFrame < 1
	 */
	public FramedTickingGameState(int ticksPerFrame) {
		Preconditions.checkArgument(ticksPerFrame >= 1, "Cannot be less than 1 tick in a frame!");
		this.ticksPerFrame = ticksPerFrame;
	}

	/**
	 * A default of 20 ticks per frame to create a frame of ~1 second
	 */
	public FramedTickingGameState() {
		this(20);
	}

	@Override
	public final void onTick() {
		if (ticks++ >= ticksPerFrame) {
			onTickFrame();
			ticks = 0;
		}
	}

	/**
	 * Called when the specified amount of ticks has passed on a recurring basis, until the state is stopped.
	 */
	public abstract void onTickFrame();

}
