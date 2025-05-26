package me.cg360.mod.warptocrosshair.entrypoint.fabric;

import me.cg360.mod.warptocrosshair.WTCKeyMappings;
import me.cg360.mod.warptocrosshair.WarpToCrosshairMod;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public class WarpToCrosshairModFabric {

    public void init() {
        WTCKeyMappings.forEachKeybindingDo(KeyBindingHelper::registerKeyBinding);
        WarpToCrosshairMod.init();

        ClientTickEvents.END_CLIENT_TICK.register(WarpToCrosshairMod::tickClient);
    }

}
