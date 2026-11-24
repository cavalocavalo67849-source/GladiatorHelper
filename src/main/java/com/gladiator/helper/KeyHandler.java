
package com.gladiator.helper;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class KeyHandler {

    private static KeyBinding activateKey;

    public static void register() {
        activateKey = new KeyBinding(
            "key.gladiatorhelper.activate",
            Keyboard.KEY_H,
            "Gladiator Helper"
        );

        ClientRegistry.registerKeyBinding(activateKey);

        FMLCommonHandler.instance().bus().register(
            new KeyHandler()
        );
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (activateKey.isPressed()) {
            BuildHandler.build();
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            BuildHandler.tick();
        }
    }
}
