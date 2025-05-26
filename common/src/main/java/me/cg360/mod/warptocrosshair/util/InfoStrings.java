package me.cg360.mod.warptocrosshair.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class InfoStrings {

    public static final Component WARP_MISS = Component.translatable("notif.warptocrosshair.warp.miss").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
    public static final Component WARP_TOO_CLOSE = Component.translatable("notif.warptocrosshair.warp.too_close").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
    public static final Component WARP_NO_PERMISSION = Component.translatable("notif.warptocrosshair.warp.no_permission").withStyle(ChatFormatting.ITALIC, ChatFormatting.RED);
    public static final Component WARP = Component.translatable("notif.warptocrosshair.warp.success").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);

}
