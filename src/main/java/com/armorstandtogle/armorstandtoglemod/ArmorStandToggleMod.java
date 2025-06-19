package com.armorstandtogle.armorstandtoglemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.util.*;

@Mod(
    modid = ArmorStandToggleMod.MODID,
    name = "ArmorStandToggle",
    version = "1.0",
    guiFactory = "com.armorstandtogle.armorstandtoglemod.ArmorStandToggleMod$ModGuiFactory",
    acceptedMinecraftVersions = "[1.12.2]"
)
public class ArmorStandToggleMod {
    
    public static final String MODID = "armorstandtogle";
    
    // 配置参数
    @Config.Comment("启用/禁用整个模组功能")
    public static boolean enabled = true;
    
    @Config.Comment("关键词列表（用逗号分隔）")
    public static String[] keywords = {"[HIDE]", "SECRET"};
    
    @Config.Comment("白名单模式：仅显示包含关键词的名称")
    public static boolean whitelistMode = false;
    
    @Config.Comment("调试模式：在游戏中显示过滤信息")
    public static boolean debugMode = false;
    
    @Config.Comment("是否隐藏空名称标签")
    public static boolean hideEmptyNames = false;
    
    // 配置文件实例
    private static Configuration config;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        File configDir = event.getModConfigurationDirectory();
        File configFile = new File(configDir, MODID + ".cfg");
        config = new Configuration(configFile);
        syncConfig(true); // 初始加载配置
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // 注册事件处理器 - 先注册事件处理器再注册自身（配置监听）
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    private static void syncConfig(boolean loadFromConfigFile) {
        if (config == null) return;

        if (loadFromConfigFile) {
            config.load();
        }

        enabled = config.get("general", "enabled", enabled, "启用/禁用整个模组").getBoolean();
        keywords = config.get("general", "keywords", keywords, "关键词列表（用逗号分隔）").getStringList();
        whitelistMode = config.get("general", "whitelistMode", whitelistMode, "白名单模式：仅显示包含关键词的名称").getBoolean();
        debugMode = config.get("general", "debugMode", debugMode, "调试模式：在游戏中显示过滤信息").getBoolean();
        hideEmptyNames = config.get("general", "hideEmptyNames", hideEmptyNames, "是否隐藏空名称标签").getBoolean();
    }
    
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(MODID)) {
            syncConfig(false); // 不从文件加载，直接读取内存值
            
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
    
    // 事件处理内部类
    public static class EventHandler {
        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public void onRenderLiving(RenderLivingEvent.Pre<?> event) {
            // 只在客户端运行，且模组启用时处理
            if (!enabled) return;
            
            // 修复：使用基类EntityLivingBase，然后检查实体类型
            EntityLivingBase entity = event.getEntity();
            
            // 确保实体是盔甲架
            if (!(entity instanceof EntityArmorStand)) {
                return;
            }
            
            EntityArmorStand armorStand = (EntityArmorStand) entity;
            String name = armorStand.getCustomNameTag();
            
            // 处理空名称
            if (name == null || name.isEmpty()) {
                if (hideEmptyNames) {
                    event.setCanceled(true);
                    logDebug("空名称盔甲架: §c隐藏");
                }
                return;
            }
            
            // 检查名称是否符合过滤条件
            boolean shouldHide = shouldHideNameTag(name);
            
            logDebug(String.format("盔甲架 '%s': %s", name, shouldHide ? "§c隐藏" : "§a显示"));
            
            // 隐藏盔甲架实体和名称标签
            if (shouldHide) {
                event.setCanceled(true);
            }
        }
        
        // 安全的调试日志输出
        @SideOnly(Side.CLIENT)
        private void logDebug(String message) {
            if (!debugMode) return;
            
            EntityPlayer player = Minecraft.getMinecraft().player;
            if (player != null) {
                player.sendMessage(new TextComponentString(message));
            }
        }
        
        // 判断是否应该隐藏名称标签
        private boolean shouldHideNameTag(String name) {
            if (keywords.length == 0) {
                return whitelistMode; // 白名单模式无关键词意味着全部隐藏，黑名单模式无关键词意味着全部显示
            }
            
            boolean foundKeyword = false;
            for (String keyword : keywords) {
                if (keyword == null || keyword.isEmpty()) continue;
                
                if (name.contains(keyword)) {
                    foundKeyword = true;
                    break;
                }
            }
            
            return whitelistMode ? !foundKeyword : foundKeyword;
        }
    }
    
    // 配置界面工厂
    public static class ModGuiFactory implements IModGuiFactory {
        @Override public void initialize(Minecraft minecraftInstance) {}
        @Override public boolean hasConfigGui() { return true; }
        @Override public GuiScreen createConfigGui(GuiScreen parentScreen) {
            return new ModConfigGui(parentScreen);
        }
        @Override public Set<IModGuiFactory.RuntimeOptionCategoryElement> runtimeGuiCategories() {
            return Collections.emptySet();
        }
    }
    
    // 配置界面
    public static class ModConfigGui extends GuiConfig {
        public ModConfigGui(GuiScreen parent) {
            super(parent, getConfigElements(), 
                  MODID, "config", false, false, "盔甲架名称标签过滤设置");
        }

        @Override
        public void onGuiClosed() {
            super.onGuiClosed();
            
            // 同步配置并保存
            syncConfig(false);
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
    
    // 获取配置元素
    private static List<IConfigElement> getConfigElements() {
        if (config == null) return new ArrayList<>();
        
        return new ArrayList<>(new ConfigElement(config.getCategory("general")).getChildElements());
    }
}