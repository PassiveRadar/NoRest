package com.radar.norest;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles Forge game events
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventBus {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForgeEventBus.class);

    @SubscribeEvent
    public static void onPositionCheck(MobSpawnEvent.PositionCheck event) {
        if (event.getEntity() instanceof Zombie && Config.isDaylightSpawningEnabled()) {
            BlockPos pos = new BlockPos((int) event.getX(), (int) event.getY(), (int) event.getZ());
            ServerLevelAccessor level = event.getLevel();

            int skyLight = level.getBrightness(LightLayer.SKY, pos);
            boolean isDaylight = skyLight >= 8;
            
            if (isDaylight) {
                // Handle daylight spawning with rate limits
                boolean canSpawn = ZombieManager.canSpawnMoreZombies(level);
                if (canSpawn) {
                    Zombie zombie = (Zombie) event.getEntity();
                    zombie.addTag("day");
                    event.setResult(Event.Result.ALLOW);
                    LOGGER.debug("Allowing zombie spawn in daylight (within limits)");
                } else {
                    // Explicitly DENY when at cap or in cooldown
                    event.setResult(Event.Result.DENY);
                    LOGGER.debug("Denied zombie spawn due to rate limits or cooldown");
                }
            } else {
                // Normal night spawning rules
                event.setResult(Event.Result.DEFAULT);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        // Only process deaths on the server side to avoid double counting
        if (event.getEntity() instanceof Zombie && !event.getEntity().level().isClientSide()) {
            LOGGER.debug("Processing zombie death on server side");
            Zombie zombie = (Zombie) event.getEntity();
            if (zombie.getTags().contains("day") && event.getEntity().level() instanceof ServerLevel serverLevel) {
                ZombieManager.onZombieDeath(serverLevel);
            }
        }
    }

    @SubscribeEvent
    public static void onEntitySpawn(MobSpawnEvent.FinalizeSpawn event) {
        // Only process spawns on the server side
        if (event.getEntity() instanceof Zombie && !event.getEntity().level().isClientSide()) {
            LOGGER.debug("Processing zombie spawn on server side");
            if (event.getEntity().getTags().contains("day") && event.getEntity().level() instanceof ServerLevel serverLevel) {
                ZombieManager.recordZombieSpawn(serverLevel);
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandManager.registerCommands(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Zombie && !event.getLevel().isClientSide()) {
            LOGGER.debug("Zombie unloaded from chunk/level, removing from count");
            if (event.getEntity().getTags().contains("day") && event.getLevel() instanceof ServerLevel serverLevel) {
                ZombieManager.onZombieUnloaded(serverLevel);
            }
        }
    }
    
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Zombie && !event.getLevel().isClientSide()) {
            LOGGER.debug("Zombie loaded into chunk/level, adding to count");
            if (event.getEntity().getTags().contains("day") && event.getLevel() instanceof ServerLevel serverLevel) {
                ZombieManager.onZombieLoaded(serverLevel);
            }
        }
    }
}
