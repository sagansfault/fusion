package com.projecki.fusion.currency;

import org.jetbrains.annotations.NotNull;

public record CurrencyPair(@NotNull String id, @NotNull long balance) { }
