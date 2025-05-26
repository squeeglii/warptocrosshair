package me.cg360.mod.warptocrosshair.compat.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.cg360.mod.warptocrosshair.config.ConfigUIBuilder;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> ConfigUIBuilder.buildConfig().generateScreen(screen);
    }

}
