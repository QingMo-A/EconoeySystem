package com.mo.economy_system.network.packets.check_system;

import com.mo.economy_system.network.EconomyNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Supplier;

public class CheckResultRequestPacket {
    private final String playerName;
    private final String playerUUID;
    private final String senderName;
    private final String senderUUID;
    private final String actionType;
    private final String result;

    public CheckResultRequestPacket(String playerName, String playerUUID, String senderName, String senderUUID, String actionType, String result) {
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.senderName = senderName;
        this.senderUUID = senderUUID;
        this.actionType = actionType;
        this.result = result;
    }

    public static void encode(CheckResultRequestPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.playerName);
        buf.writeUtf(msg.playerUUID);
        buf.writeUtf(msg.senderName);
        buf.writeUtf(msg.senderUUID);
        buf.writeUtf(msg.actionType);
        buf.writeUtf(msg.result);
    }

    public static CheckResultRequestPacket decode(FriendlyByteBuf buf) {
        return new CheckResultRequestPacket(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf());
    }

    public static void handle(CheckResultRequestPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            MinecraftServer server = player.server;
            ServerPlayer target = server.getPlayerList().getPlayer(UUID.fromString(msg.senderUUID));
            EconomyNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> target), new CheckResultResponsePacket(msg.playerName, msg.playerUUID, msg.senderName, msg.senderUUID, msg.actionType, msg.result));
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
