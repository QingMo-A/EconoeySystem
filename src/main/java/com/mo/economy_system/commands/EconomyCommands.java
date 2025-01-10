package com.mo.economy_system.commands;

import com.mo.economy_system.network.EconomyNetwork;
import com.mo.economy_system.network.packets.TransferPacket;
import com.mo.economy_system.system.EconomySavedData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class EconomyCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("coin")
                // 查询余额
                .then(Commands.literal("balance")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ServerLevel serverLevel = player.serverLevel(); // 获取服务器世界实例
                            EconomySavedData data = EconomySavedData.getInstance(serverLevel);
                            int balance = data.getBalance(player.getUUID());
                            context.getSource().sendSuccess(() -> Component.literal("Your balance: " + balance + " coins"), false);
                            return 1;
                        }))
                // 增加余额
                .then(Commands.literal("add")
                        .requires(source -> source.hasPermission(2)) // 设置需要权限等级 2
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(context, "amount");
                                    ServerLevel serverLevel = player.serverLevel(); // 获取服务器世界实例
                                    EconomySavedData data = EconomySavedData.getInstance(serverLevel);
                                    data.addBalance(player.getUUID(), amount);
                                    context.getSource().sendSuccess(() -> Component.literal("Added " + amount + " coins to your balance."), false);
                                    return 1;
                                })))
                // 减少余额
                .then(Commands.literal("min")
                        .requires(source -> source.hasPermission(2)) // 设置需要权限等级 2
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(context, "amount");
                                    ServerLevel serverLevel = player.serverLevel(); // 获取服务器世界实例
                                    EconomySavedData data = EconomySavedData.getInstance(serverLevel);
                                    if (data.minBalance(player.getUUID(), amount)) {
                                        context.getSource().sendSuccess(() -> Component.literal("Removed " + amount + " coins from your balance."), false);
                                    } else {
                                        context.getSource().sendFailure(Component.literal("Insufficient balance!"));
                                    }
                                    return 1;
                                })))
                // 设置余额
                .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2)) // 设置需要权限等级 2
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(context, "amount");
                                    ServerLevel serverLevel = player.serverLevel(); // 获取服务器世界实例
                                    EconomySavedData data = EconomySavedData.getInstance(serverLevel);
                                    data.setBalance(player.getUUID(), amount);
                                    context.getSource().sendSuccess(() -> Component.literal("Set your balance to " + amount + " coins."), false);
                                    return 1;
                                })))
                // 转账功能
                .then(Commands.literal("transfer")
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            ServerPlayer receiver = EntityArgument.getPlayer(context, "target");
                                            int amount = IntegerArgumentType.getInteger(context, "amount");

                                            EconomyNetwork.INSTANCE.sendToServer(new TransferPacket(receiver.getUUID(), amount));
                                            /*if (data.minBalance(sender.getUUID(), amount)) {
                                                data.addBalance(receiver.getUUID(), amount);
                                                context.getSource().sendSuccess(() -> Component.literal("Transferred " + amount + " coins to " + receiver.getName().getString()), false);
                                                receiver.sendSystemMessage(Component.literal(sender.getName().getString() + " sent you " + amount + " coins."));
                                            } else {
                                                context.getSource().sendFailure(Component.literal("Insufficient balance!"));
                                            }*/
                                            return 1;
                                        }))))
        );
    }
}
