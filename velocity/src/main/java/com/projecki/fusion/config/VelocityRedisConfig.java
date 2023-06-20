package com.projecki.fusion.config;

import com.projecki.fusion.FusionVelocity;
import com.projecki.fusion.config.redis.HermesConfig;
import com.projecki.fusion.config.redis.RedisConfig;
import com.projecki.fusion.config.serialize.Serializer;

/**
 * Deprecated in favor of {@link HermesConfig}. This has almost no use outside the realm of hermes' distribution.
 */
@Deprecated
public class VelocityRedisConfig<T> extends RedisConfig<T> {

    public VelocityRedisConfig(Serializer<T> deserializer, String pluginName, String configName) {
        super(deserializer, pluginName + ":" + configName, FusionVelocity.getMessageClient(), FusionVelocity.getRedisCommands());
    }
}
