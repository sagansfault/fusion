package com.projecki.fusion.object;

import org.jetbrains.annotations.Nullable;

/**
 * @since May 25, 2022
 * @author Andavin
 */
@DependsOnAll(TestModule.class)
public class TestObject extends ModularObject<TestModule> {

    @Override
    public boolean equals(@Nullable Object o) {
        return o == this;
    }

    @Override
    public String toString() {
        return "test";
    }
}
