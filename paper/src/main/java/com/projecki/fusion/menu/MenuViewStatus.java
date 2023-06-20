package com.projecki.fusion.menu;

import java.util.Objects;

public record MenuViewStatus(AbstractMenu menu, int pageIndex) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuViewStatus that = (MenuViewStatus) o;
        return pageIndex == that.pageIndex && menu.equals(that.menu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menu, pageIndex);
    }
}
