package com.projecki.fusion.config;

import com.projecki.fusion.FusionVelocity;
import com.projecki.fusion.config.redis.HermesConfig;
import com.projecki.fusion.config.serialize.Serializer;

public class VelocityHermesConfig<T> extends HermesConfig<T> {

    public VelocityHermesConfig(Serializer<T> deserializer, String pluginName, String configName) {
        super(deserializer, FusionVelocity.getOrganization(), pluginName, configName, FusionVelocity.getMessageClient(), FusionVelocity.getRedisCommands());
    }
}
