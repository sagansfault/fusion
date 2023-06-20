package com.projecki.fusion.game.state;

import org.bukkit.scheduler.BukkitRunnable;

public abstract class TickingGameState extends GameState {
    
    @Override
    public final void onBegin() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (getStatus() == Status.CREATED) return;
                if (getStatus() == Status.FINISHED) {
                    cancel();
                    return;
                }

                onTick();
            }
        }.runTaskTimer(getParentMachine().getPlugin(),0, 1);

        onBeginTicking();
    }

    /**
     * Called whenever this state starts
     */
    public abstract void onBeginTicking();

    /**
     * Called every server tick
     */
    public abstract void onTick ();

}
