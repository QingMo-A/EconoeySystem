package com.mo.economy_system.system.economy_system.shop;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ShopPriceAdjuster {
    private static final int TICKS_PER_ADJUSTMENT = 24000; // 每 24000 tick 调整一次价格（1 天）
    private static int tickCounter = 0;

    private static ShopManager shopManager;

    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event) {
        // 初始化 ShopManager
        shopManager = new ShopManager();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        tickCounter++;

        // 如果达到调整价格的时间间隔
        if (tickCounter >= TICKS_PER_ADJUSTMENT) {
            if (shopManager != null) {
                shopManager.adjustPrices(); // 调整价格
                System.out.println("Shop prices have been adjusted!");
            }
            tickCounter = 0; // 重置计数器
        }
    }
}
