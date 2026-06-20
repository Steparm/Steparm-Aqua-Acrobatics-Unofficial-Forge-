package sheg1_steparm.aquaacrobaticsunofficial.util;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;
import sheg1_steparm.aquaacrobaticsunofficial.config.ConfigHandler;

public class Keybindings {
    public static KeyBinding forceCrawling = null;

    public static void register() {
        if (ConfigHandler.MOVEMENT_CONFIG.enableToggleCrawling) {
            forceCrawling = new KeyBinding("key.aquaacrobaticsunofficial.toggle_crawling", Keyboard.KEY_C, "key.aquaacrobaticsunofficial.category");
            ClientRegistry.registerKeyBinding(forceCrawling);
        }
    }
}