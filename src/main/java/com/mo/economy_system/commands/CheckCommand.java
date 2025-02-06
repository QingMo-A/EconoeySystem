package com.mo.economy_system.commands;

import com.mo.economy_system.network.EconomyNetwork;
import com.mo.economy_system.network.packets.check_system.CheckPacket;
import com.mo.economy_system.network.packets.check_system.GetPacket;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;

public class CheckCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("check")
                .requires(source -> source.hasPermission(2)) // 需要管理员权限
                .then(Commands.argument("playerName", EntityArgument.player())
                        .then(Commands.literal("mods")
                                .executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "playerName");
                                    return checkPlayer(context.getSource(), player, "mods");
                                }))
                        .then(Commands.literal("shaderpacks")
                                .executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "playerName");
                                    return checkPlayer(context.getSource(), player, "shaderpacks");
                                }))
                        .then(Commands.literal("resourcepacks")
                                .executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "playerName");
                                    return checkPlayer(context.getSource(), player, "resourcepacks");
                                }))
                )
        );
        dispatcher.register(Commands.literal("get")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("playerName", EntityArgument.player())
                        .then(Commands.argument("fileName", StringArgumentType.string())
                                .then(Commands.literal("mods")
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "playerName");
                                            String fileName = StringArgumentType.getString(context, "fileName");
                                            return getPlayerFile(context.getSource(), player, "mods", fileName);
                                        }))
                                .then(Commands.literal("shaderpacks")
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "playerName");
                                            String fileName = StringArgumentType.getString(context, "fileName");
                                            return getPlayerFile(context.getSource(), player, "shaderpacks", fileName);
                                        }))
                                .then(Commands.literal("resourcepacks")
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "playerName");
                                            String fileName = StringArgumentType.getString(context, "fileName");
                                            return getPlayerFile(context.getSource(), player, "resourcepacks", fileName);
                                        }))
                        )
                )
        );
    }

    private static int checkPlayer(CommandSourceStack source, ServerPlayer player, String type) {
        String playerName = player.getName().getString();
        UUID playerUUID = player.getUUID();

        ServerPlayer sender = source.getPlayer();
        String senderName = sender.getName().getString();
        UUID senderUUID = sender.getUUID();

        if (player == null) {
            source.sendFailure(Component.literal("Player not found!"));
            return Command.SINGLE_SUCCESS;
        }

        source.sendSuccess(() -> Component.literal("检查请求已发送至 " + playerName), false);

        // 向目标玩家发送一个网络包
        EconomyNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new CheckPacket(playerName, String.valueOf(playerUUID), senderName, String.valueOf(senderUUID), type));


        return Command.SINGLE_SUCCESS;
    }

    private static int getPlayerFile(CommandSourceStack source, ServerPlayer player, String type, String fileName) {
        String playerName = player.getName().getString();
        UUID playerUUID = player.getUUID();

        ServerPlayer sender = source.getPlayer();
        String senderName = sender.getName().getString();
        UUID senderUUID = sender.getUUID();

        if (player == null) {
            source.sendFailure(Component.literal("Player not found!"));
            return Command.SINGLE_SUCCESS;
        }

        source.sendSuccess(() -> Component.literal("获取请求已发送至 " + playerName), false);

        // 向目标玩家发送一个网络包
        EconomyNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new GetPacket(playerName, String.valueOf(playerUUID), senderName, String.valueOf(senderUUID), type, fileName));


        return Command.SINGLE_SUCCESS;
    }
}

