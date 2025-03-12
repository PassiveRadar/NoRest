package com.radar.norest.util;

import com.radar.norest.NoRest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZombieData extends SavedData {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombieData.class);
    private static final String DATA_NAME = NoRest.MODID + "_zombie_data";

    private int zombieCount = 0;
    private long lastCapReachedTick = 0;
    private boolean capWasReached = false;
    
    public ZombieData() {
        // Big chilling
    }

    public static ZombieData load(CompoundTag tag) {
        ZombieData data = new ZombieData();
        
        data.zombieCount = tag.getInt("zombieCount");
        data.lastCapReachedTick = tag.getLong("lastCapReachedTick");
        data.capWasReached = tag.getBoolean("capWasReached");
        
        LOGGER.info("Loaded ZombieData from save: count={}, capReached={}, lastCapTick={}",
                data.zombieCount, data.capWasReached, data.lastCapReachedTick);
        
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("zombieCount", zombieCount);
        tag.putLong("lastCapReachedTick", lastCapReachedTick);
        tag.putBoolean("capWasReached", capWasReached);
        
        LOGGER.info("Saved ZombieData to disk: count={}, capReached={}, lastCapTick={}",
                zombieCount, capWasReached, lastCapReachedTick);
        
        return tag;
    }

    public static ZombieData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();

        return overworld.getDataStorage().computeIfAbsent(
                ZombieData::load,
                ZombieData::new,
                DATA_NAME
        );
    }
    
    public int getZombieCount() {
        return zombieCount;
    }
    
    public long getLastCapReachedTick() {
        return lastCapReachedTick;
    }
    
    public boolean wasCapReached() {
        return capWasReached;
    }
    
    public void incrementZombieCount() {
        zombieCount++;
        this.setDirty();
    }
    
    public void decrementZombieCount() {
        zombieCount--;
        if (zombieCount < 0) {
            zombieCount = 0;
        }
        this.setDirty();
    }
    
    public void setCapReached(long gameTick, boolean reached) {
        this.lastCapReachedTick = gameTick;
        this.capWasReached = reached;
        this.setDirty();
    }
    
    public void resetZombieCount() {
        this.zombieCount = 0;
        this.capWasReached = false;
        this.setDirty();
    }
}
