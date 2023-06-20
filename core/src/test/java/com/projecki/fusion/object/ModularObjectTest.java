package com.projecki.fusion.object;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @since May 25, 2022
 * @author Andavin
 */
public class ModularObjectTest {

    @Test
    public void testModulePresence() {
        TestObject object = new TestObject();
        assertNotNull(object.get(SubModule.class));
        assertNotNull(object.get(RunnableModule.class));
        assertNotNull(object.get(TransitiveModule.class));
        assertNotNull(object.get(Runnable.class));
    }

    @Test
    public void testInitialization() {
        new TestObject().initialize();
    }

    @Test
    public void testAdd() {
        OtherTestObject object = new OtherTestObject();
        assertNull(object.get(RunnableModule.class));
        object.add(RunnableModule.class);
        assertNotNull(object.get(RunnableModule.class));
    }

    @Test
    public void testRemove() {
        TestObject object = new TestObject();
        assertNotNull(object.get(RunnableModule.class));
        object.remove(RunnableModule.class);
        assertNull(object.get(RunnableModule.class));
    }
}
