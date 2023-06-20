package com.projecki.fusion.game.state;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class GameState implements Listener {

    // status of this current state
    private Status status = Status.CREATED;

    private GenericGameStateMachine<? extends GameState> parentMachine;

    <T extends GameState> void attachParentMachine(GenericGameStateMachine<T> parentMachine) {
        this.parentMachine = parentMachine;
    }

    public <T extends GameState> GenericGameStateMachine<T> getParentMachine() {
        return (GenericGameStateMachine<T>) parentMachine; // java generics go brrr
    }

    final void onComplete() {
        HandlerList.unregisterAll(this);
        onFinish();
        status = Status.FINISHED;
    }

    final void onStart() {
        JavaPlugin plugin = parentMachine.getPlugin();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.onBegin();
        status = Status.ACTIVE;
    }

    /**
     * Get the current {@link Status} of this {@link GameState}.
     *
     * @return the status of this game state
     */
    public final Status getStatus () {
        return status;
    }

    /**
     * The function that runs when the current state in the parent machine is set to this state.
     * (ie. The function called when this state starts)
     */
    public abstract void onBegin();

    /**
     * The function that runs when the current state is this state and {@code setState(...)}
     * is called. (ie. when the machine switches to another state)
     */
    public abstract void onFinish();

    public enum Status {
        CREATED,
        ACTIVE,
        FINISHED
    }
}
