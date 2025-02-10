package com.mo.economy_system.events.territory_system;

import com.mo.economy_system.EconomySystem;
import com.mo.economy_system.system.territory_system.Territory;
import com.mo.economy_system.system.territory_system.TerritoryManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * EventHandler_Player 类用于处理与领地系统相关的玩家事件。
 */
@Mod.EventBusSubscriber(modid = EconomySystem.MODID)
public class EventHandler_Player {

    /**
     * 记录每个玩家当前所在的领地，使用 WeakHashMap 避免内存泄漏。
     */
    private static final Map<UUID, Territory> playerCurrentTerritory = new WeakHashMap<>();

    /**
     * 记录每个玩家上次的位置，用于检测玩家是否移动。
     */
    private static final Map<UUID, BlockPos> lastPositions = new WeakHashMap<>();

    /**
     * 记录每个玩家上次检查的时间，用于控制检测频率。
     */
    private static final Map<UUID, Long> lastCheckTime = new WeakHashMap<>();

    /**
     * 记录每个玩家的粒子任务，用于在进入领地时生成粒子效果。
     */
    private static final Map<UUID, ScheduledExecutorService> particleTasks = new HashMap<>();

    /**
     * 检测间隔，默认 200ms。
     */
    private static long CHECK_INTERVAL = 200L;

    /**
     * 处理玩家移动事件，检测玩家是否进入了新的领地或离开了当前领地。
     *
     * @param event 玩家 tick 事件
     */
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

        lastPositions.put(playerUUID, playerPos);

        // 查询当前所在领地
        Territory currentTerritory = TerritoryManager.getTerritoryAtIgnoreY(playerPos.getX(), playerPos.getZ());
        Territory previousTerritory = playerCurrentTerritory.get(playerUUID);

        // 处理领地进入和离开事件
        if (!Objects.equals(previousTerritory, currentTerritory)) {
            if (previousTerritory != null && player.serverLevel().dimension().equals(previousTerritory.getDimension())) {
                MinecraftForge.EVENT_BUS.post(new Event_PlayerLeaveTerritory(player, previousTerritory));
                stopParticleEffect(playerUUID);
            }
            if (currentTerritory != null && player.serverLevel().dimension().equals(currentTerritory.getDimension())) {
                MinecraftForge.EVENT_BUS.post(new Event_PlayerEnterTerritory(player, currentTerritory));
                showParticleEffect(player.serverLevel(), currentTerritory.getPos1(), currentTerritory.getPos2(), player);
            }
        }

        // 更新当前领地状态
        playerCurrentTerritory.put(playerUUID, currentTerritory);
    }

    /**
     * 处理玩家放置方块事件，检查玩家是否有权限在当前位置放置方块。
     *
     * @param event 方块放置事件
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        BlockPos pos = event.getPos();
        if (!hasPermission(player, pos)) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§c你没有权限在此领地放置方块！"));
        }
    }

    /**
     * 处理玩家破坏方块事件，检查玩家是否有权限在当前位置破坏方块。
     *
     * @param event 方块破坏事件
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (!(player instanceof ServerPlayer serverPlayer)) return; // 检查是否为 ServerPlayer

        BlockPos pos = event.getPos();
        if (!hasPermission(serverPlayer, pos)) {
            event.setCanceled(true);
            serverPlayer.sendSystemMessage(Component.literal("§c你没有权限在此领地破坏方块！"));
        }
    }

    /**
     * 处理玩家右键使用物品事件，检查玩家是否有权限在当前位置使用物品。
     *
     * @param event 物品使用事件
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onItemUse(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        BlockPos pos = serverPlayer.blockPosition();
        if (!hasPermission(serverPlayer, pos)) {
            event.setCanceled(true);
            serverPlayer.sendSystemMessage(Component.literal("§c你没有权限在此领地使用物品！"));
        }
    }

    /**
     * 处理玩家右键操作方块事件，检查玩家是否有权限在当前位置右键操作方块。
     *
     * @param event 方块右键点击事件
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        BlockPos pos = event.getPos(); // 获取右键点击的方块位置
        if (!hasPermission(serverPlayer, pos)) {
            event.setCanceled(true);
            serverPlayer.sendSystemMessage(Component.literal("§c你没有权限在此领地右键操作方块！"));
        }
    }

    /**
     * 检测玩家是否有在指定位置操作的权限。
     *
     * @param player 玩家实例
     * @param pos    方块位置
     * @return 如果有权限返回 true，否则返回 false
     */
    private static boolean hasPermission(ServerPlayer player, BlockPos pos) {
        Territory territory = TerritoryManager.getTerritoryAtIgnoreY(pos.getX(), pos.getZ());
        if (territory == null || !player.serverLevel().dimension().equals(territory.getDimension())) return true; // 如果不在领地内，允许操作

        // 检查是否是领地所有者或被授权的玩家
        return territory.isOwner(player.getUUID()) || territory.hasPermission(player.getUUID()) || player.hasPermissions(2);
    }

    /**
     * 设置检测间隔时间（毫秒）。
     *
     * @param interval 新的检测间隔时间
     */
    public static void setCheckInterval(long interval) {
        CHECK_INTERVAL = interval;
    }

    /**
     * 在领地边界显示粒子效果。
     *
     * @param level   服务器世界实例
     * @param pos1    领地边界点1
     * @param pos2    领地边界点2
     * @param player  玩家实例
     */
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

    /**
     * 停止指定玩家的粒子效果任务。
     *
     * @param playerUUID 玩家 UUID
     */
    public static void stopParticleEffect(UUID playerUUID) {
        ScheduledExecutorService executorService = particleTasks.remove(playerUUID);
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}
