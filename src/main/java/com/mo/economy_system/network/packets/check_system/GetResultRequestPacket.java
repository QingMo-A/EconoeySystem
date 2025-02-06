package com.mo.economy_system.network.packets.check_system;

import com.mo.economy_system.network.EconomyNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

public class GetResultRequestPacket {
    private final String playerName;
    private final String playerUUID;
    private final String senderName;
    private final String senderUUID;
    private final String actionType;
    private final String fileName;
    private final String base64;

    public GetResultRequestPacket(String playerName, String playerUUID, String senderName, String senderUUID, String actionType, String fileName, String base64) {
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.senderName = senderName;
        this.senderUUID = senderUUID;
        this.actionType = actionType;
        this.fileName = fileName;
        this.base64 = base64;
    }

    public static void encode(GetResultRequestPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.playerName);
        buf.writeUtf(msg.playerUUID);
        buf.writeUtf(msg.senderName);
        buf.writeUtf(msg.senderUUID);
        buf.writeUtf(msg.actionType);
        buf.writeUtf(msg.fileName);
        buf.writeUtf(msg.base64);
    }

    public static GetResultRequestPacket decode(FriendlyByteBuf buf) {
        return new GetResultRequestPacket(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf());
    }

    public static void handle(GetResultRequestPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            MinecraftServer server = player.server;
            ServerPlayer target = server.getPlayerList().getPlayer(UUID.fromString(msg.senderUUID));
            EconomyNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> target), new GetResultResponsePacket(msg.playerName, msg.playerUUID, msg.senderName, msg.senderUUID, msg.actionType, msg.fileName, msg.base64));
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
