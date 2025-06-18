package com.armorstandtoggle.armorstandtogglemod;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@Mod(modid = ArmorStandRenderToggleMod.MODID, version = "1.0", clientSideOnly = true)
public class ArmorStandRenderToggleMod {
    public static final String MODID = "armorstandtoggle";
    public static KeyBinding toggleKey;
    public static boolean shouldRender = true;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        toggleKey = new KeyBinding("key.armorstandtoggle.toggle", Keyboard.KEY_K, "Armor Stand Toggle");
        ClientRegistry.registerKeyBinding(toggleKey);
        MinecraftForge.EVENT_BUS.register(new ArmorStandRenderHandler());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (toggleKey.isPressed()) {
            shouldRender = !shouldRender;
        }
    }
}