package com.projecki.fusion.currency.pubsub;

import com.projecki.fusion.message.MessageClient;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record CurrencyUpdateMessage(@NotNull UUID uuid, @NotNull String currencyId) implements MessageClient.Message { }
