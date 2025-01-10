package com.mo.economy_system.network.packets;

import com.mo.economy_system.market.MarketItem;
import com.mo.economy_system.market.MarketManager;
import com.mo.economy_system.network.EconomyNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Supplier;

public class MarketRequestPacket {
    public MarketRequestPacket() {}

    public static void encode(MarketRequestPacket msg, FriendlyByteBuf buf) {}

    public static MarketRequestPacket decode(FriendlyByteBuf buf) {
        return new MarketRequestPacket();
    }

    public static void handle(MarketRequestPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                // 获取市场数据
                List<MarketItem> marketItems = MarketManager.getMarketItems();
                // 发送响应数据包
                EconomyNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new MarketResponsePacket(marketItems));
            }
        });
        context.setPacketHandled(true);
    }
}


