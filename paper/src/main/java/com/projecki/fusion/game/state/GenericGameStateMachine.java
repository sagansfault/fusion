package com.projecki.fusion.game.state;

import com.google.common.base.Preconditions;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

/**
 * Represents the highest extendable form of a state machine.
 *
 * @param <T> The type of state this state machine handles
 */
public class GenericGameStateMachine<T extends GameState> {

    private final JavaPlugin plugin;
    private T currentState;

    public GenericGameStateMachine(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * Shifts the state to the next one given. This calls the complete function on the previous
     * state, sets the current one to the new one and starts it.
     *
     * @param next The state to change to
     */
    public final void changeState(T next) {
        Preconditions.checkNotNull(next, "Next game state cannot be null");
        if (this.currentState != null) {
            this.currentState.onComplete();
        }
        this.currentState = next;
        next.attachParentMachine(this);
        this.currentState.onStart();
    }

    /**
     * Returns the current state set in this machine.
     *
     * @return The current state set in this machine.
     */
    public final T getCurrentState() {
        return currentState;
    }

    /**
     * Returns the current state of this state machine as the given type if it IS the given type.
     * An empty optional is returned if the current state is not of the type given.
     *
     * @param type The type to ask of the current state
     * @param <K> Some sub-type of State
     * @return An optional containing the safely casted current class or empty if it was not of this type.
     */
    @SuppressWarnings("unchecked")
    public final <K extends T> Optional<K> getCurrentStateAs(Class<K> type) {
        if (type.isInstance(currentState)) {
            return Optional.of((K) currentState); // safe to cast, ignore this
        }
        return Optional.empty();
    }

    /**
     * Returns whether the current state of this state machine is of the type given. This is sort of like if states
     * were enums, and you wanted to check the current state with a double equals. Since states are not enums, this
     * method exists to replace similar functionality with {@code Class#isInstance(...)}
     *
     * @param currentStateType The type of state check for.
     * @return Whether the current state is an instance of the one provided.
     */
    public boolean isCurrentState(Class<? extends T> currentStateType) {
        return this.currentState.getClass().equals(currentStateType);
    }

    /**
     * "Completes" this state machine. This signifies the end of a state series or the end of the
     * state machine's use. Use this preferably only when you're going to finish your last state.
     */
    public final void complete() {
        if (this.currentState != null) {
            this.currentState.onComplete();
        }
        this.currentState = null;
    }
}
