package me.cg360.mod.warptocrosshair;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.function.Consumer;

public class WTCKeyMappings {

    private static final ArrayList<KeyMapping> KEY_MAPPINGS = new ArrayList<>();


    public static final KeyMapping WARP_ACTION = defineMapping(new KeyMapping("key.warptocrosshair.warp", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_COMMA, "key.categories.warptocrosshair"));


    private static KeyMapping defineMapping(KeyMapping k) {
        KEY_MAPPINGS.add(k);
        return k;
    }

    public static void forEachKeybindingDo(Consumer<KeyMapping> keyMappingConsumer) {
        for(KeyMapping mapping: WTCKeyMappings.KEY_MAPPINGS) {
            keyMappingConsumer.accept(mapping);
        }
    }
}
