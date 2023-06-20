package com.projecki.fusion.reward;

/**
 * Represents a generic 'reward'-eqsue feature in which a simple single function is run to award a generic target type.
 * In most cases (spigot) this target type will be a player and such is exemplary in implementations of this interface.
 *
 * @param <T> The target type to reward.
 */
public interface GenericReward<T> {

    /**
     * The function to run when the reward is to be issued.
     *
     * @param target The target of the reward, see implementations of this interface for more info.
     */
    void reward(T target);
}
