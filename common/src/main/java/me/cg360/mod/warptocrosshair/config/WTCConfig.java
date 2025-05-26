package me.cg360.mod.warptocrosshair.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import me.cg360.mod.warptocrosshair.WarpToCrosshairMod;
import me.cg360.mod.warptocrosshair.config.helper.*;

public class WTCConfig extends DefaultValueTracker {

    public static ConfigClassHandler<WTCConfig> HANDLER = ConfigClassHandler.createBuilder(WTCConfig.class)
            .id(WarpToCrosshairMod.id("main"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(WarpToCrosshairMod.getDefaultConfigPath().resolve(WarpToCrosshairMod.MOD_ID + ".json"))
                    .setJson5(false)
                    .build())
            .build();

    public WTCConfig() {
        this.saveDefaults(); // This should be run before /any/ saving or loading occurs.
    }

    @Category("main") @SerialEntry
    @ContinuousRange(min = 0.0f, max = 32.0f, sliderStep = 0.5f, formatTranslationKey = "config.cg.value.distance")
    private float bufferDistance = 8.0f;

    @Category("main") @SerialEntry
    private boolean playSounds = true;


    public float getBufferDistance() {
        return this.bufferDistance;
    }

    public boolean shouldPlaySounds() {
        return this.playSounds;
    }
}
