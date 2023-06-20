package com.projecki.fusion.sql.converter;

import com.projecki.fusion.util.UUIDUtil;
import org.jetbrains.annotations.NotNull;
import org.jooq.Converter;

import java.util.UUID;

/**
 * @since March 17, 2022
 * @author Andavin
 */
public class UUIDConverter implements Converter<byte[], UUID> {

    @Override
    public UUID from(byte[] bytes) {
        return UUIDUtil.toUuid(bytes);
    }

    @Override
    public byte[] to(UUID uuid) {
        return UUIDUtil.toBytes(uuid);
    }

    @Override
    public @NotNull Class<byte[]> fromType() {
        return byte[].class;
    }

    @Override
    public @NotNull Class<UUID> toType() {
        return UUID.class;
    }
}
