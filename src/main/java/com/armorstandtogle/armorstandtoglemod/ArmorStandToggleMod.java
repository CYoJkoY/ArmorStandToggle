package com.armorstandtogle.armorstandtoglemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.item.EntityArmorStand;
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
    
    // 配置参数（可通过配置文件修改）
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

        // 创建配置文件
        File configFile = new File(configDir, MODID + ".cfg");
        config = new Configuration(configFile);
        
        // 同步配置
        syncConfig(true);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // 注册事件处理器
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.register(this); // 注册配置变更监听
    }
    
    // 同步配置 - 仅从文件加载值到静态变量，不更新内存配置
    private static void syncConfig(boolean loadFromConfigFile) {
        if (config == null) {
            return;
        }

        // 只有在preInit或配置变更时从文件加载
        if (loadFromConfigFile) {
            config.load();
        }

        // 更新静态变量
        enabled = config.get("general", "enabled", enabled).getBoolean();
        keywords = config.get("general", "keywords", keywords).getStringList();
        whitelistMode = config.get("general", "whitelistMode", whitelistMode).getBoolean();
        debugMode = config.get("general", "debugMode", debugMode).getBoolean();
        hideEmptyNames = config.get("general", "hideEmptyNames", hideEmptyNames).getBoolean();

        // 不再在syncConfig中保存配置
    }
    
    // 配置变更监听
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(MODID)) {
            System.out.println("Config changed event triggered!");
            
            // 直接更新静态变量而不重新加载文件
            enabled = config.get("general", "enabled", enabled).getBoolean();
            keywords = config.get("general", "keywords", keywords).getStringList();
            whitelistMode = config.get("general", "whitelistMode", whitelistMode).getBoolean();
            debugMode = config.get("general", "debugMode", debugMode).getBoolean();
            hideEmptyNames = config.get("general", "hideEmptyNames", hideEmptyNames).getBoolean();
            
            // 保存配置
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
    
    // 事件处理内部类
    public static class EventHandler {
        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public void onRenderNameTag(RenderLivingEvent.Specials.Pre<?> event) {
            // 如果模组禁用，不执行任何操作
            if (!enabled) return;
            
            // 仅处理盔甲架实体
            if (event.getEntity() instanceof EntityArmorStand) {
                EntityArmorStand armorStand = (EntityArmorStand) event.getEntity();
                String name = armorStand.getCustomNameTag();
                
                // 处理空名称
                if (name == null || name.isEmpty()) {
                    if (hideEmptyNames) {
                        event.setCanceled(true);
                    }
                    return;
                }
                
                // 检查名称是否符合过滤条件
                boolean shouldHide = shouldHideNameTag(name);
                
                // 调试输出
                if (debugMode) {
                    String status = shouldHide ? "§c隐藏" : "§a显示";
                    Minecraft.getMinecraft().player.sendChatMessage(
                        String.format("盔甲架 '%s': %s", name, status)
                    );
                }
                
                // 根据需要隐藏名称标签
                if (shouldHide) {
                    event.setCanceled(true);
                }
            }
        }
        
        // 判断是否应该隐藏名称标签
        private boolean shouldHideNameTag(String name) {
            // 修复逻辑：处理当keywords为空时的特殊情况
            if (keywords.length == 0) {
                return whitelistMode; // 白名单模式无关键词意味着全部隐藏，黑名单模式无关键词意味着全部显示
            }
            
            for (String keyword : keywords) {
                // 跳过空关键词
                if (keyword == null || keyword.isEmpty()) continue;
                
                // 黑名单模式：名称包含关键词 -> 隐藏
                if (!whitelistMode && name.contains(keyword)) {
                    return true;
                }
                
                // 白名单模式：名称不包含关键词 -> 隐藏
                if (whitelistMode && !name.contains(keyword)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    // 配置界面工厂
    public static class ModGuiFactory implements IModGuiFactory {

        @Override
        public void initialize(Minecraft minecraftInstance) {}

        @Override
        public boolean hasConfigGui() {
            return true;
        }

        @Override
        public GuiScreen createConfigGui(GuiScreen parentScreen) {
            return new ModConfigGui(parentScreen);
        }

        @Override
        public Set<IModGuiFactory.RuntimeOptionCategoryElement> runtimeGuiCategories() {
            return Collections.emptySet();
        }
    }
    
    // 配置界面
    public static class ModConfigGui extends GuiConfig {
        public ModConfigGui(GuiScreen parent) {
            super(parent, getConfigElements(), 
                  MODID, "config", false, false, "盔甲架名称标签过滤设置");
        }

        // 修复配置保存逻辑
        @Override
        public void onGuiClosed() {
            super.onGuiClosed();
            
            // 从内存配置更新静态变量
            enabled = config.get("general", "enabled", enabled).getBoolean();
            keywords = config.get("general", "keywords", keywords).getStringList();
            whitelistMode = config.get("general", "whitelistMode", whitelistMode).getBoolean();
            debugMode = config.get("general", "debugMode", debugMode).getBoolean();
            hideEmptyNames = config.get("general", "hideEmptyNames", hideEmptyNames).getBoolean();
            
            // 保存配置到文件
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
    
    // 获取配置元素
    private static List<IConfigElement> getConfigElements() {
        if (config == null) {
            return new ArrayList<>();
        }
        
        List<IConfigElement> list = new ArrayList<>();
        
        // 获取general类别的属性
        list.addAll(new ConfigElement(config.getCategory("general")).getChildElements());
        
        return list;
    }
}