package com.mo.economy_system.network.packets.economy_system;

import com.mo.economy_system.EconomySystem;
import com.mo.economy_system.system.economy_system.shop.ShopItem;
import com.mo.economy_system.network.EconomyNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Supplier;

public class ShopRequestPacket {

    public ShopRequestPacket() {}

    public static void encode(ShopRequestPacket msg, FriendlyByteBuf buf) {}

    public static ShopRequestPacket decode(FriendlyByteBuf buf) {
        return new ShopRequestPacket();
    }

    public static void handle(ShopRequestPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                // 从 ShopManager 获取商店商品
                List<ShopItem> shopItems = EconomySystem.SHOP_MANAGER.getItems();

                // 将商品列表发送到客户端
                EconomyNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ShopResponsePacket(shopItems));
            }
        });
        context.setPacketHandled(true);
    }
}
