package dev.example.hello;

import dev.turboism.sdk.plugin.TurboismPlugin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class HelloPluginTest {

    @Test
    void implementsTurboismPluginAndPreservesEventPayload() {
        HelloPlugin plugin = new HelloPlugin();
        assertInstanceOf(TurboismPlugin.class, plugin);

        HelloPlugin.HelloEvent event = new HelloPlugin.HelloEvent("Hello action invoked");
        assertEquals("Hello action invoked", event.message());
    }
}
