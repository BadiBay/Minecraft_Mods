package com.dualfishing;

import net.minecraft.entity.projectile.FishingBobberEntity;

public interface DualFishingPlayer {
    FishingBobberEntity getOffHandFishHook();
    void setOffHandFishHook(FishingBobberEntity fishHook);
    boolean isCastingOffhand();
    void setCastingOffhand(boolean castingOffhand);
}
