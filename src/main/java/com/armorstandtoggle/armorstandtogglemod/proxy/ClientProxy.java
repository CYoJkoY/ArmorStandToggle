package com.armorstandtoggle.armorstandtogglemod.proxy;

import com.armorstandtoggle.armorstandtogglemod.ArmorStandRenderHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        MinecraftForge.EVENT_BUS.register(new ArmorStandRenderHandler());
    }
}