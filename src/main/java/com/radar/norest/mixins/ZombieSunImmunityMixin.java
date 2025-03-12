package com.radar.norest.mixins;

import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public class ZombieSunImmunityMixin {
    @Inject(method = "isSunSensitive", at = @At("HEAD"), cancellable = true)
    private void preventSunSensitivity(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
