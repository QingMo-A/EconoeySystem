package com.mo.economy_system.events;

import com.mo.economy_system.EconomySystem;
import com.mo.economy_system.commands.*;
import com.mo.economy_system.system.economy_system.market.MarketItem;
import com.mo.economy_system.system.economy_system.market.MarketManager;
import com.mo.economy_system.system.economy_system.market.MarketSavedData;
import com.mo.economy_system.system.economy_system.red_packet.RedPacketManager;
import com.mo.economy_system.system.economy_system.reward.RewardManager;
import com.mo.economy_system.system.economy_system.shop.ShopManager;
import com.mo.economy_system.system.economy_system.EconomySavedData;
import com.mo.economy_system.system.territory_system.TerritoryManager;
import com.mo.economy_system.update_checker.UpdateChecker;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = EconomySystem.MODID)
public class MainEventHandler {

    private static ShopManager shopManager;

    private static final Random RANDOM = new Random();

    private static final String SHOP_REFRESH_MESSAGE_KEY = "message.shop.shop_refresh";
    private static final String MOB_REWARD_MESSAGE_KEY = "message.mob_reward";

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        // 注册指令
        EconomyCommand.register(event.getServer().getCommands().getDispatcher());
        TpaCommand.register(event.getServer().getCommands().getDispatcher());
        RedPacketCommand.register(event.getServer().getCommands().getDispatcher());
        TerritoryClaimCommand.register(event.getServer().getCommands().getDispatcher());
        TerritoryCommand.register(event.getServer().getCommands().getDispatcher());
        InfoCommand.register(event.getServer().getCommands().getDispatcher());
        // StarterKitCommand.register(event.getServer().getCommands().getDispatcher());

        // 获取服务器主世界
        ServerLevel overworld = event.getServer().overworld();

        EconomySavedData.getInstance(overworld);
        // 初始化 ShopManager
        shopManager = new ShopManager();
        MarketSavedData marketData = MarketSavedData.getInstance(overworld);
        MarketManager.setMarketItems(marketData.getMarketItems()); // 将数据加载到 MarketManager
        TerritoryManager.reset();
        // 初始化领地数据
        TerritoryManager.initialize(overworld);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        // 服务器停止时，Forge 会自动保存 SavedData
        System.out.println("Saving economy data...");
        MarketSavedData marketData = MarketSavedData.getInstance(event.getServer().overworld());
        marketData.clearMarketItems(); // 清空原有数据
        for (MarketItem item : MarketManager.getMarketItems()) {
            marketData.addMarketItem(item); // 保存当前市场商品
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return; // 确保只在每个 tick 的开始阶段执行

        ServerLevel overworld = event.getServer().overworld(); // 获取主世界
        long dayTime = overworld.getDayTime() % 24000; // 获取当前世界的时间（一天的 tick 范围 0-23999）

        // 如果是中午（6000 tick）
        if (dayTime == 6000 || dayTime == 18000) {
            if (shopManager != null) {
                shopManager.adjustPrices(); // 调整价格

                // 遍历所有在线玩家并发送消息
                for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                    player.sendSystemMessage(Component.translatable(SHOP_REFRESH_MESSAGE_KEY));
                }
            }
        }

        // 定时检查红包
        if (dayTime % 100 == 0) {
            RedPacketManager.checkAndExpireRedPackets();
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            UpdateChecker.checkForUpdates(serverPlayer);

            ServerLevel serverLevel = serverPlayer.serverLevel();
            EconomySavedData savedData = EconomySavedData.getInstance(serverLevel);

            // 获取离线消息并发送给玩家
            List<String> offlineMessages = savedData.getOfflineMessages(serverPlayer.getUUID());
            for (String message : offlineMessages) {
                serverPlayer.sendSystemMessage(Component.literal(message));
            }
        }
    }

    @SubscribeEvent
    public static void onMobDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;

        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) return;

        RewardManager.RewardEntry rewardEntry = EconomySystem.REWARD_MANAGER
                .getRewardForEntity(mob.getType().builtInRegistryHolder().key().location())
                .orElse(null);

        if (rewardEntry != null && RANDOM.nextDouble() < rewardEntry.dropChance) {
            int reward = RANDOM.nextInt(rewardEntry.dropMax - rewardEntry.dropMin + 1) + rewardEntry.dropMin;
            EconomySavedData economy = EconomySavedData.getInstance(player.serverLevel());
            economy.addBalance(player.getUUID(), reward);
            player.sendSystemMessage(Component.translatable(MOB_REWARD_MESSAGE_KEY, event.getEntity().getName().getString(), reward));
        }
    }

}
