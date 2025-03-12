package com.radar.norest.compat;

import net.minecraftforge.fml.ModList;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

//TODO I don't think I needed this file tbh.
public class NoRestZombieMixinPlugin implements IMixinConfigPlugin {

    private Logger LOGGER = LoggerFactory.getLogger(NoRestZombieMixinPlugin.class);

    private static final String[] CONFLICTING_MODS = {
            "hordes",
    };

    private static final String[] SUN_IMMUNITY_MIXINS = {
            "ZombieSunImmunityMixin"
    };

    private boolean hasConflictingMods() {
        try {
            if (ModList.get() == null) {
                LOGGER.info("[No Rest for the Wicked] ModList not available yet, assuming no conflicts");
                return false;
            }

            for (String modId : CONFLICTING_MODS) {
                if (ModList.get().isLoaded(modId)) {
                    LOGGER.info("[No Rest for the Wicked] Detected compatible mod: {}, disabling sun immunity mixins", modId);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            LOGGER.error("[No Rest for the Wicked] Error checking for conflicting mods: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        for (String mixinName : SUN_IMMUNITY_MIXINS) {
            if (mixinClassName.endsWith(mixinName)) {
                return !hasConflictingMods();
            }
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
