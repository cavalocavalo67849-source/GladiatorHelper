
package com.gladiator.helper;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = GladiatorHelper.MODID,
    name = GladiatorHelper.NAME,
    version = GladiatorHelper.VERSION,
    acceptedMinecraftVersions = "[1.8.9]"
)
public class GladiatorHelper {

    public static final String MODID = "gladiatorhelper";
    public static final String NAME = "Gladiator Helper";
    public static final String VERSION = "1.0.0";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Preparação do mod.
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        KeyHandler.register();
    }
}
