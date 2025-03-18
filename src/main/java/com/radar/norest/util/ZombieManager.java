package com.radar.norest.util;

import com.radar.norest.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZombieManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombieManager.class);

    public static boolean canZombieSpawnInDaylight(EntityType<Zombie> type, ServerLevelAccessor level,
                                                   MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }

        BlockPos blockBelow = pos.below();
        BlockState state = level.getBlockState(blockBelow);
        if (!state.isFaceSturdy(level, blockBelow, Direction.UP)) {
            return false;
        }

        int maxLightLevel = Config.getMaxSpawnLightLevel();
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        if (blockLight > maxLightLevel) {
            return false;
        }

        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        if (skyLight < 8) {
            return false;
        }

        if (!canSpawnMoreZombies(level)) {
            LOGGER.debug("Blocking zombie spawn: at cap or in cooldown period");
            return false;
        }

        double spawnChance = Config.getDaylightSpawnChance();
        return !(random.nextDouble() >= spawnChance);
    }

    public static boolean canSpawnMoreZombies(ServerLevelAccessor level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            LOGGER.warn("Level is not a ServerLevel, cannot access persistent data");
            return false;
        }
        
        ZombieData data = ZombieData.get(serverLevel);
        int currentCount = data.getZombieCount();
        int maxZombies = Config.getMinDaylightZombies();

        LOGGER.debug("Current zombie count: {}/{}", currentCount, maxZombies);
        
        // Check if we're in a cooldown period first
        if (data.wasCapReached()) {
            long currentTick = level.getLevel().getGameTime();
            long ticksSinceCap = currentTick - data.getLastCapReachedTick();
            int cooldownTicks = Config.getSpawnCooldownSeconds() * 20;
            boolean cooldownPassed = ticksSinceCap >= cooldownTicks;

            LOGGER.debug("In cooldown period for {} ticks, Cooldown: {} ticks, Cooldown passed: {}", ticksSinceCap, cooldownTicks, cooldownPassed);
            
            if (cooldownPassed) {
                LOGGER.debug("Cooldown period has passed, resetting cooldown state");
                data.setCapReached(0, false);
            } else {
                LOGGER.debug("Still in cooldown period, blocking spawns");
                return false;
            }
        }

        if (currentCount < maxZombies) {
            return true;
        }

        if (!data.wasCapReached()) {
            data.setCapReached(level.getLevel().getGameTime(), true);
            LOGGER.debug("Cap reached! Setting cooldown at tick: {}", data.getLastCapReachedTick());
        }

        return false;

        // How did we get here?
    }

    public static void onZombieDeath(ServerLevel level) {
        ZombieData data = ZombieData.get(level);
        data.decrementZombieCount();
        LOGGER.debug("Zombie died, active count now: {}", data.getZombieCount());
    }

    public static void recordZombieSpawn(ServerLevel level) {
        ZombieData data = ZombieData.get(level);
        data.incrementZombieCount();
        LOGGER.debug("Zombie spawn recorded. Active zombies: {}", data.getZombieCount());
    }

    public static void resetZombieCount(ServerLevel level) {
        ZombieData data = ZombieData.get(level);
        data.resetZombieCount();
        LOGGER.debug("Zombie count reset to 0");
    }

    public static String getStatusReport(ServerLevelAccessor level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return "Cannot get status: Not a server level";
        }
        
        ZombieData data = ZombieData.get(serverLevel);
        int zombieCount = data.getZombieCount();
        int maxZombies = getZombieCap(serverLevel);
        
        long ticksSinceCap = 0;
        if (data.wasCapReached()) {
            long currentTick = level.getLevel().getGameTime();
            ticksSinceCap = currentTick - data.getLastCapReachedTick();
        }
        
        int cooldownTicks = Config.getSpawnCooldownSeconds() * 20;
        
        return String.format(
            "Zombie Status:\n" +
            "- Current count: %d/%d\n" +
            "- Cap reached: %b\n" +
            (data.wasCapReached() ? "- Time since cap: %d ticks (%d seconds)\n" : "") +
            "- Cooldown: %d ticks (%d seconds)\n" +
            "- Ready for spawn: %b",
            zombieCount, maxZombies,
            data.wasCapReached(),
            (data.wasCapReached() ? ticksSinceCap : 0), (data.wasCapReached() ? ticksSinceCap/20 : 0),
            cooldownTicks, cooldownTicks/20,
            (zombieCount < maxZombies || (data.wasCapReached() && ticksSinceCap >= cooldownTicks))
        );
    }

    public static void onZombieUnloaded(ServerLevel level) {
        ZombieData data = ZombieData.get(level);
        data.decrementZombieCount();
        LOGGER.debug("Zombie unloaded, active count now: {}", data.getZombieCount());
    }

    public static void onZombieLoaded(ServerLevel level) {
        ZombieData data = ZombieData.get(level);
        data.incrementZombieCount();
        LOGGER.debug("Zombie loaded, active count now: {}", data.getZombieCount());
    }

    private static int getZombieCap(ServerLevel level) {
        int playerAddedCount = Config.getPerPlayerAdditionalZombies() * level.players().size() + Config.getMinDaylightZombies();
        return playerAddedCount > Config.getMinDaylightZombies() ? Config.getMaxDaylightZombies() : playerAddedCount;
    }
} 