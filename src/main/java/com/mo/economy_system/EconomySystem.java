package com.mo.economy_system;

import com.mo.economy_system.item.ModItems;
import com.mo.economy_system.system.economy_system.reward.RewardConfigWatcher;
import com.mo.economy_system.system.economy_system.reward.RewardManager;
import com.mo.economy_system.system.economy_system.shop.ConfigWatcher;
import com.mo.economy_system.system.economy_system.shop.ShopManager;
import com.mo.economy_system.network.EconomyNetwork;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(EconomySystem.MODID)
public class EconomySystem {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "economy_system";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ShopManager SHOP_MANAGER = new ShopManager();
    public static final RewardManager REWARD_MANAGER = new RewardManager();

    public EconomySystem(FMLJavaModLoadingContext context) {
        // 获取 mod 事件总线
        IEventBus modEventBus = context.getModEventBus();

        // 注册客户端事件
        modEventBus.addListener(this::onClientSetup);
        // 注册物品
        ModItems.ITEMS.register(modEventBus);

        EconomyNetwork.register();

        // 启动文件监听器
        new ConfigWatcher(SHOP_MANAGER).watchConfigFile();
        new RewardConfigWatcher(REWARD_MANAGER).watchConfigFile();

        // 日志信息
        LOGGER.info("Economy System Mod Initialized!");
    }

    public EconomySystem() {
        // 获取 mod 事件总线
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册客户端事件
        modEventBus.addListener(this::onClientSetup);
        // 注册物品
        ModItems.ITEMS.register(modEventBus);

        EconomyNetwork.register();

        // 启动文件监听器
        new ConfigWatcher(SHOP_MANAGER).watchConfigFile();
        new RewardConfigWatcher(REWARD_MANAGER).watchConfigFile();

        // 日志信息
        LOGGER.info("Economy System Mod Initialized!");
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        // 注册按键绑定的事件监听
        LOGGER.info("Registering Keybinds...");
    }
}
