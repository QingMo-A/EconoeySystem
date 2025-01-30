package com.mo.economy_system.events.territory_system;

import com.mo.economy_system.system.territory_system.Territory;
import com.mo.economy_system.system.territory_system.TerritoryManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class TerritoryPermissionHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // ServerMessageUtil.sendDebugMessage("玩家正在放置方块");

        BlockPos pos = event.getPos();
        if (!hasPermission(player, pos)) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§c你没有权限在此领地放置方块！"));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (!(player instanceof ServerPlayer serverPlayer)) return; // 检查是否为 ServerPlayer

        // ServerMessageUtil.sendDebugMessage("玩家正在破坏方块");

        BlockPos pos = event.getPos();
        if (!hasPermission(serverPlayer, pos)) {
            event.setCanceled(true);
            serverPlayer.sendSystemMessage(Component.literal("§c你没有权限在此领地破坏方块！"));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onItemUse(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        // ServerMessageUtil.sendDebugMessage("玩家正在使用物品");

        // 获取玩家当前位置
        BlockPos pos = serverPlayer.blockPosition();
        if (!hasPermission(serverPlayer, pos)) {
            event.setCanceled(true);
            serverPlayer.sendSystemMessage(Component.literal("§c你没有权限在此领地使用物品！"));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        // ServerMessageUtil.sendDebugMessage("玩家正在右键操作方块");

        BlockPos pos = event.getPos(); // 获取右键点击的方块位置
        if (!hasPermission(serverPlayer, pos)) {
            event.setCanceled(true);
            serverPlayer.sendSystemMessage(Component.literal("§c你没有权限在此领地右键操作方块！"));
        }
    }

    /**
     * 检测玩家是否有在指定位置操作的权限
     */
    private static boolean hasPermission(ServerPlayer player, BlockPos pos) {
        Territory territory = TerritoryManager.getTerritoryAtIgnoreY(pos.getX(), pos.getZ());
        if (territory == null || !(player.serverLevel().dimension().equals(territory.getDimension()))) return true; // 如果不在领地内，允许操作

        // 检查是否是领地所有者或被授权的玩家
        return territory.isOwner(player.getUUID()) || territory.hasPermission(player.getUUID()) || player.hasPermissions(2);
    }
}
