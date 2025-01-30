package com.mo.economy_system.network.packets.economy_system;

import com.mo.economy_system.system.economy_system.market.MarketItem;
import com.mo.economy_system.system.economy_system.market.MarketManager;
import com.mo.economy_system.network.EconomyNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Supplier;

public class MarketDataRequestPacket {
    public MarketDataRequestPacket() {}

    public static void encode(MarketDataRequestPacket msg, FriendlyByteBuf buf) {}

    public static MarketDataRequestPacket decode(FriendlyByteBuf buf) {
        return new MarketDataRequestPacket();
    }

    public static void handle(MarketDataRequestPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                // 获取市场数据
                List<MarketItem> marketItems = MarketManager.getMarketItems();
                // 发送响应数据包
                EconomyNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new MarketDataResponsePacket(marketItems));
            }
        });
        context.setPacketHandled(true);
    }
}


