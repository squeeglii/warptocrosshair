package me.cg360.mod.warptocrosshair;

import dev.isxander.yacl3.platform.YACLPlatform;
import me.cg360.mod.warptocrosshair.config.WTCConfig;
import me.cg360.mod.warptocrosshair.util.InfoStrings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class WarpToCrosshairMod {

    public static final String MOD_ID = "warptocrosshair";

    private static boolean wasWarpMappingProcessed = false;

    // Environment -- simple platform indepentent way of tracking incompatible mods.
    private static Set<String> compatibilityNeededMods = new HashSet<>();

    // Not quite incompatible, but some config defaults need to be changed when a mod is detected.
    public static void noteIncompatibleMod(String modId) {
        compatibilityNeededMods.add(modId.toLowerCase());
    }

    public static void init() {
        WTCConfig.HANDLER.load();
    }

    public static void tickClient(Minecraft client) {
        //if(!WTCKeyMappings.WARP_ACTION.consumeClick()) return;

        if(!WTCKeyMappings.WARP_ACTION.isDown()) {
            wasWarpMappingProcessed = false;
            return;
        }

        if(wasWarpMappingProcessed)
            return;

        wasWarpMappingProcessed = true;

        if(client == null) return;
        if(client.player == null) return;

        WarpToCrosshairMod.warpPlayer(client.player, client);
    }


    public static void warpPlayer(LocalPlayer player, Minecraft client) {
        int limit = 16 * (1 + client.options.renderDistance().get());
        HitResult hit = player.pick(limit, 1.0f, false);
        HitResult.Type type = hit.getType();

        double maxRange = Math.max(player.blockInteractionRange(), player.entityInteractionRange());
        double totalLength = Math.sqrt(hit.distanceTo(player));

        // Block range so do not warp.
        // TODO: Minimum range setting.
        if(totalLength < maxRange) {
            return;
        }

        double bufferRange = WarpToCrosshairMod.getConfig().getBufferDistance();

        // teleports with a bit of distance so it is comfortable.
        if(totalLength <= bufferRange) {
            client.gui.setOverlayMessage(InfoStrings.WARP_TOO_CLOSE, false);

            if(WarpToCrosshairMod.getConfig().shouldPlaySounds())
                player.playNotifySound(SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.PLAYERS, 1.0f, 1.6f);

            return;
        }

        // raycast missed (teleported into infinity?)
        if(type == HitResult.Type.MISS) {
            client.gui.setOverlayMessage(InfoStrings.WARP_MISS, false);

            if(WarpToCrosshairMod.getConfig().shouldPlaySounds())
                player.playNotifySound(SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.PLAYERS, 1.0f, 1.6f);

            return;
        }

        double clippedLength = totalLength - bufferRange;
        Vec3 clipped = player.position()
                .lerp(hit.getLocation(), clippedLength / totalLength)
                .subtract(0, player.getEyeHeight(), 0);

        // Does player have teleport permissions?
        if (!player.hasPermissions(2)) {
            client.gui.setOverlayMessage(InfoStrings.WARP_NO_PERMISSION, false);

            if(WarpToCrosshairMod.getConfig().shouldPlaySounds())
                player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS,  1.0f, 0.5f);
            return;
        }

        // Same way gamemode switcher works
        player.connection.sendUnsignedCommand("tp @s %.2f %.2f %.2f".formatted(
                clipped.x, clipped.y, clipped.z
        ));
        client.gui.setOverlayMessage(InfoStrings.WARP, false);

        if(WarpToCrosshairMod.getConfig().shouldPlaySounds())
            player.playNotifySound(SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.2f, 1.5f);
    }



    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(WarpToCrosshairMod.MOD_ID, name);
    }

    public static WTCConfig getConfig() {
        return WTCConfig.HANDLER.instance();
    }

    public static Logger getLogger() {
        return LoggerFactory.getLogger(WarpToCrosshairMod.class);
    }

    public static Path getDefaultConfigPath() {
        return YACLPlatform.getConfigDir();
    }
}
