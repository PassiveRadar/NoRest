package com.radar.norest.events;

import com.radar.norest.Config;
import com.radar.norest.util.ZombieManager;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBus {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModEventBus.class);

    @SubscribeEvent
    public static void onSpawnPlacementRegister(SpawnPlacementRegisterEvent event) {
        if (Config.isDaylightSpawningEnabled()) {
            LOGGER.info("Registering custom zombie spawn placement rules");
            event.register(
                    EntityType.ZOMBIE,
                    ZombieManager::canZombieSpawnInDaylight,
                    SpawnPlacementRegisterEvent.Operation.OR
            );
        }
    }
}
