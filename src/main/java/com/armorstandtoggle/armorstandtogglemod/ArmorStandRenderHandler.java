package com.armorstandtoggle.armorstandtogglemod;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ArmorStandRenderHandler {
    
    // 使用泛型类型确保类型安全
    @SubscribeEvent(priority = EventPriority.HIGH)
    @SideOnly(Side.CLIENT)
    public void onRenderArmorStand(RenderLivingEvent.Pre<EntityLivingBase> event) {
        EntityLivingBase entity = event.getEntity();
        
        // 只处理盔甲架
        if (!(entity instanceof EntityArmorStand)) {
            return;
        }
        
        EntityArmorStand armorStand = (EntityArmorStand) entity;
        
        // 检查盔甲架是否隐形且设置了不渲染
        if (armorStand.isInvisible() && !ArmorStandRenderToggleMod.shouldRender) {
            event.setCanceled(true);
        }
    }
}