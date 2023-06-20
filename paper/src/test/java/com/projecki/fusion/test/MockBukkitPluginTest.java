package com.projecki.fusion.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MockBukkitPluginTest<P extends JavaPlugin> extends MockBukkitTest{

    /**
     * plugin class used to initialise the plugin instance
     */
    private final Class<P> pluginClass;

    /**
     * the mocked plugin instance
     */
    protected P plugin;

    /**
     * Construct a new {@link MockBukkitTest}.
     * This is required to get the plugin class instance.
     *
     * @param pluginClass the class of type {@link P}
     */
    public MockBukkitPluginTest (Class<P> pluginClass) {
        this.pluginClass = pluginClass;
    }

    @BeforeAll
    public void initPlugin () {
        plugin = MockBukkit.load(pluginClass);
    }
}
