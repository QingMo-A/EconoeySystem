package com.mo.economy_system.commands;

import com.mo.economy_system.red_packet.RedPacket;
import com.mo.economy_system.red_packet.RedPacketManager;
import com.mo.economy_system.system.economy_system.EconomySavedData;
import com.mo.economy_system.utils.MessageKeys;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;

import java.util.Random;

public class RedPacketCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("redpacket")
                .then(Commands.literal("create")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .then(Commands.argument("duration", IntegerArgumentType.integer(1))
                                        .then(Commands.literal("lucky")
                                                .executes(context -> createRedPacket(context.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(context, "amount"), IntegerArgumentType.getInteger(context, "duration"), true)))
                                        .then(Commands.literal("even")
                                                .executes(context -> createRedPacket(context.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(context, "amount"), IntegerArgumentType.getInteger(context, "duration"), false))))))
                .then(Commands.literal("claim")
                        .executes(context -> claimRedPacket(context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> claimRedPacket(context.getSource().getPlayerOrException(), EntityArgument.getPlayer(context, "player"))))));
    }

    private static int createRedPacket(ServerPlayer sender, int amount, int duration, boolean isLucky) {
        EconomySavedData data = EconomySavedData.getInstance(sender.serverLevel());

        if (data.getBalance(sender.getUUID()) < amount) {
            sender.sendSystemMessage(Component.translatable(MessageKeys.RED_PACKET_INSUFFICIENT_BALANCE));
            return 0;
        }

        if (!RedPacketManager.addRedPacket(sender.getUUID(), new RedPacket(sender.getUUID(), sender.getName().getString(), amount, isLucky, duration))) {
            sender.sendSystemMessage(Component.translatable(MessageKeys.RED_PACKET_ALREADY_ACTIVE));
            return 0;
        }

        data.minBalance(sender.getUUID(), amount);

        Component claimButton = Component.translatable(MessageKeys.RED_PACKET_CLAIM_BUTTON)
                .withStyle(style -> style
                        .withColor(0x55FF55)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/redpacket claim " + sender.getName().getString()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("§eClick to claim!"))));

        sender.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable(MessageKeys.RED_PACKET_BROADCAST, sender.getName().getString()).append(claimButton), false);

        sender.sendSystemMessage(Component.translatable(MessageKeys.RED_PACKET_CREATED_SUCCESSFULLY));
        return 1;
    }

    private static int claimRedPacket(ServerPlayer player) {
        return claimRedPacket(player, null);
    }

    private static int claimRedPacket(ServerPlayer player, ServerPlayer sender) {
        RedPacket redPacket;
        if (sender == null) {
            redPacket = RedPacketManager.getRecentRedPacket();
        } else {
            redPacket = RedPacketManager.getRedPacketBySender(sender.getUUID());
        }

        if (redPacket == null || !redPacket.isClaimable()) {
            player.sendSystemMessage(Component.translatable(MessageKeys.RED_PACKET_NO_AVAILABLE));
            return 0;
        }

        if (redPacket.hasClaimed(player.getUUID())) {
            player.sendSystemMessage(Component.translatable(MessageKeys.RED_PACKET_ALREADY_CLAIMED));
            return 0;
        }

        int amount = redPacket.isLucky
                ? Math.max(1, new Random().nextInt(Math.max(1, redPacket.totalAmount - redPacket.claimedAmount)))
                : Math.max(1, (redPacket.totalAmount - redPacket.claimedAmount) / Math.max(1, redPacket.claimedPlayers.size()));

        redPacket.claimedAmount += amount;
        redPacket.addClaimedPlayer(player.getUUID());

        EconomySavedData data = EconomySavedData.getInstance(player.serverLevel());
        data.addBalance(player.getUUID(), amount);

        player.sendSystemMessage(Component.translatable(MessageKeys.RED_PACKET_CLAIM_SUCCESS, amount, redPacket.senderName));
        return 1;
    }
}
