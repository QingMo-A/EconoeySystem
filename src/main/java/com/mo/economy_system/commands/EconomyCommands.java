package com.mo.economy_system.commands;

import com.mo.economy_system.system.EconomySavedData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

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
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(context, "amount");
                                    ServerLevel serverLevel = player.serverLevel(); // 获取服务器世界实例
                                    EconomySavedData data = EconomySavedData.getInstance(serverLevel);
                                    data.deposit(player.getUUID(), amount);
                                    context.getSource().sendSuccess(() -> Component.literal("Added " + amount + " coins to your balance."), false);
                                    return 1;
                                })))
                // 减少余额
                .then(Commands.literal("min")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(context, "amount");
                                    ServerLevel serverLevel = player.serverLevel(); // 获取服务器世界实例
                                    EconomySavedData data = EconomySavedData.getInstance(serverLevel);
                                    if (data.withdraw(player.getUUID(), amount)) {
                                        context.getSource().sendSuccess(() -> Component.literal("Removed " + amount + " coins from your balance."), false);
                                    } else {
                                        context.getSource().sendFailure(Component.literal("Insufficient balance!"));
                                    }
                                    return 1;
                                })))
                // 设置余额
                .then(Commands.literal("set")
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
                                            ServerPlayer sender = context.getSource().getPlayerOrException();
                                            ServerPlayer receiver = EntityArgument.getPlayer(context, "target");
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            ServerLevel serverLevel = sender.serverLevel(); // 获取服务器世界实例
                                            EconomySavedData data = EconomySavedData.getInstance(serverLevel);

                                            if (data.withdraw(sender.getUUID(), amount)) {
                                                data.deposit(receiver.getUUID(), amount);
                                                context.getSource().sendSuccess(() -> Component.literal("Transferred " + amount + " coins to " + receiver.getName().getString()), false);
                                                receiver.sendSystemMessage(Component.literal(sender.getName().getString() + " sent you " + amount + " coins."));
                                            } else {
                                                context.getSource().sendFailure(Component.literal("Insufficient balance!"));
                                            }
                                            return 1;
                                        }))))
        );
    }
}
