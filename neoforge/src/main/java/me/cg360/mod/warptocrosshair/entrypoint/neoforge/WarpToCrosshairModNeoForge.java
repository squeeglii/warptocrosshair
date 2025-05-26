package me.cg360.mod.warptocrosshair.entrypoint.neoforge;

import me.cg360.mod.warptocrosshair.WTCKeyMappings;
import me.cg360.mod.warptocrosshair.WarpToCrosshairMod;
import me.cg360.mod.warptocrosshair.config.ConfigUIBuilder;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = WarpToCrosshairMod.MOD_ID, dist = Dist.CLIENT)
public class WarpToCrosshairModNeoForge {

    public WarpToCrosshairModNeoForge(IEventBus modEventBus) {
        modEventBus.addListener(this::init);
        modEventBus.addListener(this::registerBindings);
        modEventBus.addListener(this::clientTick);
    }


    public void init(FMLClientSetupEvent event) {
        WarpToCrosshairMod.init(); // loads config
        ModLoadingContext.get().registerExtensionPoint(
                IConfigScreenFactory.class,
                () -> (client, parent) -> ConfigUIBuilder.buildConfig().generateScreen(parent)
        );
    }

    public void clientTick(ClientTickEvent event) {
        WarpToCrosshairMod.tickClient(Minecraft.getInstance());
    }


    public void registerBindings(RegisterKeyMappingsEvent event) {
        WTCKeyMappings.forEachKeybindingDo(event::register);
    }

}
