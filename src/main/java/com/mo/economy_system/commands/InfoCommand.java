package com.mo.economy_system.commands;

import com.mo.economy_system.system.economy_system.EconomySavedData;
import com.mo.economy_system.system.territory_system.PlayerInfo;
import com.mo.economy_system.system.territory_system.Territory;
import com.mo.economy_system.system.territory_system.TerritoryManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.stream.Collectors;

public class InfoCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("info")
                        // balance 子指令
                        .then(Commands.literal("player")
                                // 添加 player 参数
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> showPlayerInfo(context.getSource().getPlayerOrException(),
                                                EntityArgument.getPlayer(context, "player"))))
                                .executes(context -> showPlayerInfo(context.getSource().getPlayerOrException(), context.getSource().getPlayerOrException())))
                        // territory 子指令
                        .then(Commands.literal("territory")
                                .executes(context -> showTerritory(context.getSource().getPlayerOrException())))
                        // territory 子指令
                        .then(Commands.literal("item")
                                .executes(context -> showItemInfo(context.getSource().getPlayerOrException(), context.getSource().getPlayerOrException())))
        );
    }

    // 显示余额
    private static int showPlayerInfo(ServerPlayer requester, ServerPlayer target) {
        ServerLevel serverLevel = target.serverLevel(); // 获取服务器世界实例
        EconomySavedData data = EconomySavedData.getInstance(serverLevel);
        int balance = data.getBalance(target.getUUID());

        requester.sendSystemMessage(Component.literal(target.getName().getString() + "拥有 " + balance + " 枚梦鱼币"));

        return 1;
    }

    // 显示物品信息
    private static int showItemInfo(ServerPlayer requester, ServerPlayer target) {
        ItemStack itemStack = requester.getItemInHand(InteractionHand.MAIN_HAND);

        // 获取物品的 NBT 数据
        CompoundTag nbt = itemStack.getTag();

        if (nbt != null) {
            // 遍历 NBT 中的所有键
            for (String key : nbt.getAllKeys()) {
                // 获取键对应的值（支持不同类型的值）
                if (nbt.contains(key)) {
                    System.out.println("键名: " + key);

                    // 打印不同类型的值
                    if (nbt.contains(key, 8)) { // 8 表示字符串类型
                        System.out.println("值 (String): " + nbt.getString(key));
                    } else if (nbt.contains(key, 3)) { // 3 表示整数类型
                        System.out.println("值 (Integer): " + nbt.getInt(key));
                    } else if (nbt.contains(key, 10)) { // 10 表示 CompoundTag 类型
                        System.out.println("值 (Compound): " + nbt.getCompound(key));
                    } else if (nbt.contains(key, 9)) { // 9 表示 ListTag 类型
                        System.out.println("值 (List): " + nbt.getList(key, 10));  // 假设 List 是由 CompoundTag 组成
                    }
                }
            }
        } else {
            System.out.println("该物品没有 NBT 数据");
        }

        return 1;
    }

    // territory 子指令的逻辑
    private static int showTerritory(ServerPlayer sender) {
        Vec3 senderPos = sender.position();
        int x = (int) Math.floor(senderPos.x);
        int z = (int) Math.floor(senderPos.z);

        Territory territory = TerritoryManager.getTerritoryAtIgnoreY(x, z);
        if (territory == null) {
            sender.sendSystemMessage(Component.literal("你未处于领地中"));
            return 0;
        } else {
            // 检查是否有授权玩家
            String hoverTextContent;
            if (territory.getAuthorizedPlayers().isEmpty()) {
                hoverTextContent = "§7无"; // 如果没有玩家，显示“无”
            } else {
                // 拼接有权限玩家的名字，以换行分隔
                hoverTextContent = territory.getAuthorizedPlayers().stream()
                        .map(PlayerInfo::getName)
                        .collect(Collectors.joining("\n"));
            }

            // 悬浮提示文字
            Component hoverText = Component.literal(hoverTextContent);

            // 添加 HoverEvent
            Component messageWithHover = Component.literal("[领地成员]").withStyle(style ->
                    style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText))
            );

            sender.sendSystemMessage(Component.literal(
                    "-----------------------" + "\n" +
                    "领地名称: " + territory.getName() + "\n" +
                            "领地UUID: " + territory.getTerritoryID() + "\n" +
                            "领地所有者: " + territory.getOwnerName() + "\n" +
                            "领地所有者UUID: " + territory.getOwnerUUID() + "\n"
                ).append(messageWithHover)
            );
        }
        return 1;
    }
}
