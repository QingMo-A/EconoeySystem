package com.mo.economy_system.network.packets;

import com.mo.economy_system.network.EconomyNetwork;
import com.mo.economy_system.system.economy_system.EconomySavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class BalanceRequestPacket {

    public BalanceRequestPacket() {}

    public static void encode(BalanceRequestPacket msg, FriendlyByteBuf buf) {
        // 无需数据
    }

    public static BalanceRequestPacket decode(FriendlyByteBuf buf) {
        return new BalanceRequestPacket();
    }

    public static void handle(BalanceRequestPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ServerLevel serverLevel = player.serverLevel();
                if (serverLevel != null) {
                    EconomySavedData data = EconomySavedData.getInstance(serverLevel);
                    int balance = data.getBalance(player.getUUID());

                    // 发送响应包到客户端
                    EconomyNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new BalanceResponsePacket(balance));
                }
            }
        });
        context.setPacketHandled(true);
    }

}
