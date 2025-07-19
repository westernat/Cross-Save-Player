package org.mesdag.cross_save_player;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(CrossSavePlayer.MODID)
public final class CrossSavePlayer {
    public static final String MODID = "cross_save_player";
    public static final Logger LOGGER = LoggerFactory.getLogger("CrossSavePlayer");

    public CrossSavePlayer(ModContainer modContainer) {
        if (FMLEnvironment.dist.isClient()) {
            modContainer.registerConfig(ModConfig.Type.CLIENT, Configs.SPEC);
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        } else {
            LOGGER.warn("This mod is client-only and not server-sided!");
        }
    }
}
