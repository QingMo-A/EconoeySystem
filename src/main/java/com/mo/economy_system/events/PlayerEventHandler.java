package com.mo.economy_system.events;

import com.mo.economy_system.EconomySystem;
import com.mo.economy_system.territory.Territory;
import com.mo.economy_system.territory.TerritoryManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = EconomySystem.MODID)
public class PlayerEventHandler {

    // 用于存储玩家当前所在的领地
    private static final Map<UUID, Territory> playerCurrentTerritory = new HashMap<>();

    // 记录上次检测时间，防止频繁触发
    private static final Map<UUID, Long> lastCheckTime = new HashMap<>();

    // 检测间隔（毫秒）
    private static final long CHECK_INTERVAL = 200L; // 每 200 毫秒检测一次

    @SubscribeEvent
    public static void onPlayerMove(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        UUID playerUUID = player.getUUID();
        long currentTime = System.currentTimeMillis();
        long lastTime = lastCheckTime.getOrDefault(playerUUID, 0L);

        // 如果未达到检测间隔，直接返回
        if (currentTime - lastTime < CHECK_INTERVAL) {
            return;
        }
        lastCheckTime.put(playerUUID, currentTime);

        // 获取玩家当前位置（忽略 Y 轴）
        BlockPos playerPos = player.blockPosition();
        Territory currentTerritory = TerritoryManager.getTerritoryAtIgnoreY(playerPos.getX(), playerPos.getZ());

        Territory previousTerritory = playerCurrentTerritory.get(playerUUID);

        // 如果玩家进入了新的领地
        if (currentTerritory != null && !Objects.equals(previousTerritory, currentTerritory)) {
            MinecraftForge.EVENT_BUS.post(new PlayerEnterTerritoryEvent(player, currentTerritory));
        }

        // 如果玩家离开了之前的领地
        if (currentTerritory == null && previousTerritory != null) {
            MinecraftForge.EVENT_BUS.post(new PlayerLeaveTerritoryEvent(player, previousTerritory));
        }

        // 更新玩家当前的领地状态
        playerCurrentTerritory.put(playerUUID, currentTerritory);
    }
}
