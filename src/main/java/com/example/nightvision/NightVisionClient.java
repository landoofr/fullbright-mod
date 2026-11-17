package com.example.nightvision;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Client-side night vision toggle.
 *
 * The key shows up in Options > Controls > Key Binds under the "Night Vision"
 * category, so it can be rebound like any vanilla key. Default key: N.
 */
public class NightVisionClient implements ClientModInitializer {
    private static final String MOD_ID = "nightvision";

    // 78 = GLFW_KEY_N
    private static final int DEFAULT_KEY_N = 78;

    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));

    private static KeyMapping toggleKey;
    private static boolean enabled = false;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key." + MOD_ID + ".toggle",
                DEFAULT_KEY_N,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(NightVisionClient::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }

        // consumeClick() fires once per key press, so holding the key does not flicker it.
        while (toggleKey.consumeClick()) {
            enabled = !enabled;
            if (!enabled) {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
        }

        // Keep the effect applied while enabled. Re-checking every tick means it comes back
        // after respawning or changing dimension, when the game clears effects.
        if (enabled && !player.hasEffect(MobEffects.NIGHT_VISION)) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION,
                    MobEffectInstance.INFINITE_DURATION,
                    0,
                    false, // ambient
                    false, // show particles
                    false  // show HUD icon
            ));
        }
    }
}
