package com.radar.norest;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NoRest.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    public static final ForgeConfigSpec SPEC;
    // General settings
    public static final ForgeConfigSpec.BooleanValue ENABLE_DAYLIGHT_SPAWNING;
    // Spawn settings
    public static final ForgeConfigSpec.DoubleValue DAYLIGHT_SPAWN_CHANCE;
    public static final ForgeConfigSpec.IntValue MAX_SPAWN_LIGHT_LEVEL;
    // Spawn rate settings
    public static final ForgeConfigSpec.IntValue MAX_DAYLIGHT_ZOMBIES;
    public static final ForgeConfigSpec.IntValue SPAWN_COOLDOWN_SECONDS;

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    static {
        BUILDER.comment("Daylight Zombies Configuration");
        BUILDER.push("general");

        ENABLE_DAYLIGHT_SPAWNING = BUILDER
                .comment("Enable zombies to spawn during daylight hours")
                .define("enableDaylightSpawning", true);

        BUILDER.pop();

        BUILDER.push("spawning");

        DAYLIGHT_SPAWN_CHANCE = BUILDER
                .comment("Chance multiplier for zombies to spawn in daylight (0.0-1.0), doesn't do much so I wouldn't touch it.")
                .defineInRange("daylightSpawnChance", 0.5, 0.0, 1.0);

        MAX_SPAWN_LIGHT_LEVEL = BUILDER
                .comment("Maximum light level where zombies can spawn.")
                .defineInRange("maxSpawnLightLevel", 15, 0, 15);

        MAX_DAYLIGHT_ZOMBIES = BUILDER
                .comment("Maximum number of zombies that can spawn during daylight, the max is weird because we are doing spawning via how normal spawns work.")
                .defineInRange("maxDaylightZombies", 30, 0, 100);

        SPAWN_COOLDOWN_SECONDS = BUILDER
                .comment("Cooldown in seconds between zombie spawn attempts, keep this high so that you don't get constant spawning, think flat world in minecraft with slimes.")
                .defineInRange("spawnCooldownSeconds", 10, 0, 3600);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    // there has to be a better way for this, i hate this so much...
    public static boolean isDaylightSpawningEnabled() {
        try {
            return ENABLE_DAYLIGHT_SPAWNING.get();
        } catch (IllegalStateException e) {
            return true;
        }
    }

    public static double getDaylightSpawnChance() {
        try {
            return DAYLIGHT_SPAWN_CHANCE.get();
        } catch (IllegalStateException e) {
            return 0.5;
        }
    }

    public static int getMaxSpawnLightLevel() {
        try {
            return MAX_SPAWN_LIGHT_LEVEL.get();
        } catch (IllegalStateException e) {
            return 15;
        }
    }

    public static int getMaxDaylightZombies() {
        try {
            return MAX_DAYLIGHT_ZOMBIES.get();
        } catch (IllegalStateException e) {
            return 30;
        }
    }

    public static int getSpawnCooldownSeconds() {
        try {
            return SPAWN_COOLDOWN_SECONDS.get();
        } catch (IllegalStateException e) {
            return 10;
        }
    }
}
