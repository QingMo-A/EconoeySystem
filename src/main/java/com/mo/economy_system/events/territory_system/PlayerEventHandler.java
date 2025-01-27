package com.mo.economy_system.events.territory_system;

import com.mo.economy_system.EconomySystem;
import com.mo.economy_system.territory.Territory;
import com.mo.economy_system.territory.TerritoryManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Mod.EventBusSubscriber(modid = EconomySystem.MODID)
public class PlayerEventHandler {

    private static final Map<UUID, Territory> playerCurrentTerritory = new WeakHashMap<>();
    private static final Map<UUID, BlockPos> lastPositions = new WeakHashMap<>();
    private static final Map<UUID, Long> lastCheckTime = new WeakHashMap<>();
    private static final Map<UUID, ScheduledExecutorService> particleTasks = new HashMap<>(); // 记录每个玩家的粒子任务
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
            if (previousTerritory != null && player.serverLevel().dimension().equals(previousTerritory.getDimension())) {
                // ServerMessageUtil.sendDebugMessage("玩家离开领地");
                MinecraftForge.EVENT_BUS.post(new PlayerLeaveTerritoryEvent(player, previousTerritory));
                stopParticleEffect(playerUUID);
            }
            if (currentTerritory != null && player.serverLevel().dimension().equals(currentTerritory.getDimension())) {
                // ServerMessageUtil.sendDebugMessage("玩家进入领地");
                MinecraftForge.EVENT_BUS.post(new PlayerEnterTerritoryEvent(player, currentTerritory));
                showParticleEffect(player.serverLevel(), currentTerritory.getPos1(), currentTerritory.getPos2(), player);
            }
        }

        // 更新当前领地状态
        playerCurrentTerritory.put(playerUUID, currentTerritory);
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        UUID playerUUID = player.getUUID();
        // stopParticleEffect(playerUUID);
    }

    public static void setCheckInterval(long interval) {
        CHECK_INTERVAL = interval;
    }

    private static void showParticleEffect(ServerLevel level, BlockPos pos1, BlockPos pos2, ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        executorService.scheduleAtFixedRate(() -> {
            // 计算边界范围
            int minX = Math.min(pos1.getX(), pos2.getX());
            int maxX = Math.max(pos1.getX(), pos2.getX());
            int minY = Math.min(pos1.getY(), pos2.getY());
            int maxY = Math.max(pos1.getY(), pos2.getY());
            int minZ = Math.min(pos1.getZ(), pos2.getZ());
            int maxZ = Math.max(pos1.getZ(), pos2.getZ());

            // 在X-Z平面四条边上生成粒子
            for (int x = minX; x <= maxX; x++) {
                level.sendParticles(ParticleTypes.END_ROD, x + 0.5, minY + 2.5, minZ + 0.5, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.END_ROD, x + 0.5, minY + 2.5, maxZ + 0.5, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.END_ROD, x + 0.5, maxY + 2.5, minZ + 0.5, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.END_ROD, x + 0.5, maxY + 2.5, maxZ + 0.5, 1, 0, 0, 0, 0);
            }
            for (int z = minZ; z <= maxZ; z++) {
                level.sendParticles(ParticleTypes.END_ROD, minX + 0.5, minY + 2.5, z + 0.5, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.END_ROD, maxX + 0.5, minY + 2.5, z + 0.5, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.END_ROD, minX + 0.5, maxY + 2.5, z + 0.5, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.END_ROD, maxX + 0.5, maxY + 2.5, z + 0.5, 1, 0, 0, 0, 0);
            }
            for (int y = minY; y <= maxY; y++) {
                level.sendParticles(ParticleTypes.END_ROD, minX + 0.5, y + 2.5, minZ + 0.5, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.END_ROD, maxX + 0.5, y + 2.5, minZ + 0.5, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.END_ROD, minX + 0.5, y + 2.5, maxZ + 0.5, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.END_ROD, maxX + 0.5, y + 2.5, maxZ + 0.5, 1, 0, 0, 0, 0);
            }
            stopParticleEffect(playerUUID);
        }, 0, 2, TimeUnit.SECONDS); // 每秒生成一次粒子效果

        particleTasks.put(playerUUID, executorService);
    }

    public static void stopParticleEffect(UUID playerUUID) {
        ScheduledExecutorService executorService = particleTasks.remove(playerUUID);
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}
