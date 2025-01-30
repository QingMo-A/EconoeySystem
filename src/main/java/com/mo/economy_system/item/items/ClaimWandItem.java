package com.mo.economy_system.item.items;

import com.mo.economy_system.system.territory_system.Territory;
import com.mo.economy_system.system.territory_system.TerritoryManager;
import com.mo.economy_system.utils.MessageKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClaimWandItem extends Item {

    private static final Map<UUID, BlockPos> firstPositions = new HashMap<>();
    private static final Map<UUID, BlockPos> secondPositions = new HashMap<>();
    private static final Map<UUID, ScheduledExecutorService> particleTasks = new HashMap<>(); // 记录每个玩家的粒子任务
    private static final Map<UUID, ScheduledExecutorService> timeoutTasks = new HashMap<>(); // 记录每个玩家的超时任务

    public ClaimWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.FAIL; // 仅支持服务端
        }

        UUID playerUUID = player.getUUID();
        BlockPos clickedPos = context.getClickedPos();

        if (!firstPositions.containsKey(playerUUID)) {
            // 玩家未选定第一个点
            firstPositions.put(playerUUID, clickedPos);
            player.sendSystemMessage(Component.translatable(MessageKeys.CLAIM_WAND_FIRST_POSITION_SET, clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
            startTimeoutTask(player); // 开始倒计时任务
        } else if (!secondPositions.containsKey(playerUUID)) {
            // 玩家未选定第二个点
            secondPositions.put(playerUUID, clickedPos);
            player.sendSystemMessage(Component.translatable(MessageKeys.CLAIM_WAND_SECOND_POSITION_SET, clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));

            // 检查新圈地是否包含已有领地
            BlockPos firstPos = firstPositions.get(playerUUID);
            if (isOverlappingExistingTerritory(player, firstPos, clickedPos)) {
                player.sendSystemMessage(Component.translatable(MessageKeys.CLAIM_WAND_OVERLAP_ERROR));
                firstPositions.remove(playerUUID);
                secondPositions.remove(playerUUID);
                return InteractionResult.FAIL;
            }

            // 计算范围和价格
            int volume = calculateVolume(firstPos, clickedPos);
            int price = volume * 20;

            player.sendSystemMessage(Component.translatable(MessageKeys.CLAIM_WAND_VOLUME, volume));
            player.sendSystemMessage(Component.translatable(MessageKeys.CLAIM_WAND_PRICE, price));
            player.sendSystemMessage(Component.translatable(MessageKeys.CLAIM_WAND_INSTRUCTION));

            // 显示粒子效果（仅显示边缘）
            showParticleEffect((ServerLevel) player.level(), firstPos, clickedPos, player);
        } else {
            // 第三次右键，取消圈地
            firstPositions.remove(playerUUID);
            secondPositions.remove(playerUUID);

            // 停止粒子效果
            stopParticleEffect(playerUUID);
            stopTimeoutTask(playerUUID);

            player.sendSystemMessage(Component.translatable(MessageKeys.CLAIM_WAND_CANCEL));
        }

        return InteractionResult.SUCCESS;
    }


    private int calculateVolume(BlockPos pos1, BlockPos pos2) {
        int xSize = Math.abs(pos2.getX() - pos1.getX()) + 1;
        int ySize = Math.abs(pos2.getY() - pos1.getY()) + 1;
        int zSize = Math.abs(pos2.getZ() - pos1.getZ()) + 1;
        return xSize * ySize * zSize; // 计算体积
    }

    private void showParticleEffect(ServerLevel level, BlockPos pos1, BlockPos pos2, ServerPlayer player) {
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
                level.sendParticles(ParticleTypes.END_ROD, x + 0.5, minY + 1.5, minZ + 0.5, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.END_ROD, x + 0.5, minY + 1.5, maxZ + 0.5, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.END_ROD, x + 0.5, maxY + 1.5, minZ + 0.5, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.END_ROD, x + 0.5, maxY + 1.5, maxZ + 0.5, 1, 0, 0, 0, 0);
            }
            for (int z = minZ; z <= maxZ; z++) {
                level.sendParticles(ParticleTypes.END_ROD, minX + 0.5, minY + 1.5, z + 0.5, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.END_ROD, maxX + 0.5, minY + 1.5, z + 0.5, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.END_ROD, minX + 0.5, maxY + 1.5, z + 0.5, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.END_ROD, maxX + 0.5, maxY + 1.5, z + 0.5, 1, 0, 0, 0, 0);
            }
            for (int y = minY; y <= maxY; y++) {
                level.sendParticles(ParticleTypes.END_ROD, minX + 0.5, y + 1.5, minZ + 0.5, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.END_ROD, maxX + 0.5, y + 1.5, minZ + 0.5, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.END_ROD, minX + 0.5, y + 1.5, maxZ + 0.5, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.END_ROD, maxX + 0.5, y + 1.5, maxZ + 0.5, 1, 0, 0, 0, 0);
            }
        }, 0, 2, TimeUnit.SECONDS); // 每秒生成一次粒子效果

        particleTasks.put(playerUUID, executorService);
    }

    private void stopParticleEffect(UUID playerUUID) {
        ScheduledExecutorService executorService = particleTasks.remove(playerUUID);
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    private void startTimeoutTask(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        executorService.schedule(() -> {
            // 如果玩家的圈地状态仍然有效，则自动取消
            if (firstPositions.containsKey(playerUUID) || secondPositions.containsKey(playerUUID)) {
                firstPositions.remove(playerUUID);
                secondPositions.remove(playerUUID);
                stopParticleEffect(playerUUID);
                player.sendSystemMessage(Component.translatable(MessageKeys.CLAIM_WAND_TIMEOUT));
            }
        }, 60, TimeUnit.SECONDS); // 60秒后执行

        timeoutTasks.put(playerUUID, executorService);
    }

    private static void stopTimeoutTask(UUID playerUUID) {
        ScheduledExecutorService executorService = timeoutTasks.remove(playerUUID);
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    public static BlockPos getFirstPosition(UUID playerUUID) {
        return firstPositions.get(playerUUID);
    }

    public static BlockPos getSecondPosition(UUID playerUUID) {
        return secondPositions.get(playerUUID);
    }

    public static void clearPositions(UUID playerUUID) {
        firstPositions.remove(playerUUID);
        secondPositions.remove(playerUUID);

        ScheduledExecutorService executorService = particleTasks.remove(playerUUID);
        if (executorService != null) {
            executorService.shutdownNow();
        }

        // 停止超时任务
        stopTimeoutTask(playerUUID);
    }

    private boolean isOverlappingExistingTerritory(ServerPlayer player, BlockPos pos1, BlockPos pos2) {
        // 计算新领地的范围
        int minX = Math.min(pos1.getX(), pos2.getX());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        // 遍历所有现有领地
        for (Territory territory : TerritoryManager.getAllTerritories()) {
            // 如果玩家是领地所有者，不检测此领地
            /*if (territory.isOwner(playerUUID)) {
                continue;
            }*/

            // 获取现有领地的 X-Z 范围
            int existingMinX = Math.min(territory.getPos1().getX(), territory.getPos2().getX());
            int existingMaxX = Math.max(territory.getPos1().getX(), territory.getPos2().getX());
            int existingMinZ = Math.min(territory.getPos1().getZ(), territory.getPos2().getZ());
            int existingMaxZ = Math.max(territory.getPos1().getZ(), territory.getPos2().getZ());

            // 检测范围是否重叠
            if (maxX >= existingMinX && minX <= existingMaxX && maxZ >= existingMinZ && minZ <= existingMaxZ && player.serverLevel().dimension().equals(territory.getDimension())) {
                return true; // 存在重叠
            }
        }

        return false; // 没有重叠
    }

}
