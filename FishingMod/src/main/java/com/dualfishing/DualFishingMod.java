package com.dualfishing;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DualFishingMod implements ModInitializer {
    public static final String MOD_ID = "dualfishing";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final TrackedData<Integer> OFF_HAND_FISH_HOOK_ID = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Dual Fishing Mod!");
    }
}
