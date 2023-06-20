package com.projecki.fusion.statistic;

import java.util.Objects;
import java.util.UUID;

public record Statistic(UUID parentId, StatisticType type, long value) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Statistic statistic = (Statistic) o;
        return parentId.equals(statistic.parentId) && type.equals(statistic.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentId, type);
    }
}
