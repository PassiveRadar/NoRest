package com.radar.norest;

import com.mojang.logging.LogUtils;
import com.radar.norest.util.ZombieData;
import com.radar.norest.util.ZombieManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(NoRest.MODID)
public class NoRest {

    public static final String MODID = "norest";
    private static final Logger LOGGER = LogUtils.getLogger();

    public NoRest() {
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        LOGGER.info("No Rest for the Wicked has been initialized");
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        ZombieData data = ZombieData.get(overworld);
        ZombieManager.resetZombieCount(overworld);
        overworld.getAllEntities().forEach(e -> {
            if (e instanceof Zombie zombie) {
                if (zombie.getTags().contains("day")) {
                    data.incrementZombieCount();
            }
            }
        });
        LOGGER.info("Loaded ZombieData on server start: {} active zombies", data.getZombieCount());
    }
}
