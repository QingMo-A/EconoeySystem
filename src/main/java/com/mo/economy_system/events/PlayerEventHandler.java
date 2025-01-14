package com.mo.economy_system.events;

import com.mo.economy_system.EconomySystem;
import com.mo.economy_system.territory.Territory;
import com.mo.economy_system.territory.TerritoryManager;
import com.mo.economy_system.utils.ServerMessageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = EconomySystem.MODID)
public class PlayerEventHandler {

    private static final Map<UUID, Territory> playerCurrentTerritory = new WeakHashMap<>();
    private static final Map<UUID, BlockPos> lastPositions = new WeakHashMap<>();
    private static final Map<UUID, Long> lastCheckTime = new WeakHashMap<>();
    private static long CHECK_INTERVAL = 200L; // 检测间隔，默认 200ms

    @SubscribeEvent
    public static void onPlayerMove(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        UUID playerUUID = player.getUUID();
        BlockPos playerPos = player.blockPosition();
        long currentTime = System.currentTimeMillis();
        long lastTime = lastCheckTime.getOrDefault(playerUUID, 0L);

        // 跳过未到达检测间隔的玩家
        if (currentTime - lastTime < CHECK_INTERVAL) {
            return;
        }
        lastCheckTime.put(playerUUID, currentTime);

        // 检查位置是否发生变化
        BlockPos lastPosition = lastPositions.get(playerUUID);
        if (lastPosition != null && lastPosition.getX() == playerPos.getX() && lastPosition.getZ() == playerPos.getZ()) {
            return;
        }

        // ServerMessageUtil.sendDebugMessage("玩家正在移动");
        lastPositions.put(playerUUID, playerPos);

        // 查询当前所在领地
        Territory currentTerritory = TerritoryManager.getTerritoryAtIgnoreY(playerPos.getX(), playerPos.getZ());
        Territory previousTerritory = playerCurrentTerritory.get(playerUUID);

        // 处理领地进入和离开事件
        if (!Objects.equals(previousTerritory, currentTerritory)) {
            if (previousTerritory != null) {
                // ServerMessageUtil.sendDebugMessage("玩家离开领地");
                MinecraftForge.EVENT_BUS.post(new PlayerLeaveTerritoryEvent(player, previousTerritory));
            }
            if (currentTerritory != null) {
                // ServerMessageUtil.sendDebugMessage("玩家进入领地");
                MinecraftForge.EVENT_BUS.post(new PlayerEnterTerritoryEvent(player, currentTerritory));
            }
        }

        // 更新当前领地状态
        playerCurrentTerritory.put(playerUUID, currentTerritory);
    }

    public static void setCheckInterval(long interval) {
        CHECK_INTERVAL = interval;
    }
}
