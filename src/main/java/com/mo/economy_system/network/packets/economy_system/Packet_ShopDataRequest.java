package com.mo.economy_system.network.packets.economy_system;

import com.mo.economy_system.EconomySystem;
import com.mo.economy_system.core.economy_system.shop.ShopItem;
import com.mo.economy_system.network.EconomySystem_NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Supplier;

public class Packet_ShopDataRequest {

    public Packet_ShopDataRequest() {}

    public static void encode(Packet_ShopDataRequest msg, FriendlyByteBuf buf) {}

    public static Packet_ShopDataRequest decode(FriendlyByteBuf buf) {
        return new Packet_ShopDataRequest();
    }

    public static void handle(Packet_ShopDataRequest msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                // 从 ShopManager 获取商店商品
                List<ShopItem> shopItems = EconomySystem.SHOP_MANAGER.getItems();

                // 将商品列表发送到客户端
                EconomySystem_NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new Packet_ShopDataResponse(shopItems));
            }
        });
        context.setPacketHandled(true);
    }
}
