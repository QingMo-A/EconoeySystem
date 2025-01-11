package com.mo.economy_system.commands;

import com.mo.economy_system.item.items.ClaimWandItem;
import com.mo.economy_system.territory.Territory;
import com.mo.economy_system.territory.TerritoryManager;
import com.mo.economy_system.system.EconomySavedData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class ClaimCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("confirm_claim")
                .then(Commands.argument("name", StringArgumentType.string())
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            UUID playerUUID = player.getUUID();

                            // 检查玩家是否有两个选定点
                            if (ClaimWandItem.getFirstPosition(playerUUID) == null || ClaimWandItem.getSecondPosition(playerUUID) == null) {
                                player.sendSystemMessage(Component.literal("§c请先用圈地杖选定两个点！"));
                                return 0;
                            }

                            // 获取选定的点
                            BlockPos firstPos = ClaimWandItem.getFirstPosition(playerUUID);
                            BlockPos secondPos = ClaimWandItem.getSecondPosition(playerUUID);

                            // 计算价格
                            int volume = calculateVolume(firstPos, secondPos);
                            int price = volume * 20;

                            // 检查余额
                            EconomySavedData data = EconomySavedData.getInstance(player.serverLevel());
                            if (data.getBalance(playerUUID) < price) {
                                player.sendSystemMessage(Component.literal("§c余额不足，圈地所需价格为 " + price + " 金币。"));
                                return 0;
                            }

                            // 扣除金额并创建领地
                            String name = StringArgumentType.getString(context, "name");
                            Territory territory = new Territory(name, playerUUID, player.getName().getString(), firstPos.getX(), firstPos.getY(), firstPos.getZ(), secondPos.getX(), secondPos.getY(), secondPos.getZ());
                            territory.setBackpoint(firstPos);
                            TerritoryManager.addTerritory(territory);
                            data.minBalance(playerUUID, price);

                            player.sendSystemMessage(Component.literal("§a领地创建成功！名称: " + name + "，价格: " + price + " 金币。"));
                            ClaimWandItem.clearPositions(playerUUID); // 清除点位记录
                            return 1;
                        })));
    }

    private static int calculateVolume(BlockPos pos1, BlockPos pos2) {
        int xSize = Math.abs(pos2.getX() - pos1.getX()) + 1;
        int ySize = Math.abs(pos2.getY() - pos1.getY()) + 1;
        int zSize = Math.abs(pos2.getZ() - pos1.getZ()) + 1;
        return xSize * ySize * zSize; // 计算体积
    }
}
