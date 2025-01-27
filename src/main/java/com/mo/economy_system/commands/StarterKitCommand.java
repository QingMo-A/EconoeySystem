package com.mo.economy_system.commands;

import com.mo.economy_system.system.economy_system.EconomySavedData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class StarterKitCommand {

    private static final String TAG_KEY = "ReceivedStarterKit";

    // 注册指令
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("starterkit") // 指令名称
                        .requires(commandSource -> commandSource.hasPermission(0)) // 任何玩家可用
                        .executes(StarterKitCommand::execute) // 指令逻辑
        );
    }

    // 指令逻辑
    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (source.getEntity() instanceof ServerPlayer player) {
            CompoundTag persistentData = player.getPersistentData(); // 获取玩家的持久化数据

            // 检查是否已经领取过
            if (persistentData.getBoolean(TAG_KEY)) {
                player.displayClientMessage(Component.literal("§c你已经领取过新手礼包，无法再次领取！"), false);
            } else {
                /*// 发放奖励（1 颗钻石）
                ItemStack diamond = new ItemStack(Items.DIAMOND, 1);
                if (player.getInventory().add(diamond)) {
                    // 成功领取，记录状态
                    persistentData.putBoolean(TAG_KEY, true);
                    player.displayClientMessage(Component.literal("成功领取新手礼包！你获得了 1 颗钻石！"), false);
                } else {
                    // 背包已满
                    player.displayClientMessage(Component.literal("背包已满，无法领取新手礼包！"), false);
                }*/
                EconomySavedData data = EconomySavedData.getInstance(player.serverLevel());
                data.addBalance(player.getUUID(), 10000);

                // 成功领取，记录状态
                persistentData.putBoolean(TAG_KEY, true);
                player.displayClientMessage(Component.literal("§a成功领取新手礼包！你获得了 10000 枚梦鱼币！"), false);
            }
        } else {
            source.sendFailure(Component.literal("§c该指令只能由玩家使用！"));
        }

        return Command.SINGLE_SUCCESS;
    }
}
