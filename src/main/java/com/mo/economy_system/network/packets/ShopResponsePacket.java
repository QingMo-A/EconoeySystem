package com.mo.economy_system.network.packets;

import com.mo.economy_system.screen.ShopScreen;
import com.mo.economy_system.shop.ShopItem;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ShopResponsePacket {
    private final List<ShopItem> shopItems;

    public ShopResponsePacket(List<ShopItem> shopItems) {
        this.shopItems = shopItems;
    }

    public static void encode(ShopResponsePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.shopItems.size());
        for (ShopItem item : msg.shopItems) {
            buf.writeNbt(item.toNBT());
        }
    }

    public static ShopResponsePacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<ShopItem> shopItems = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            CompoundTag nbt = buf.readNbt();
            if (nbt != null) {
                shopItems.add(ShopItem.fromNBT(nbt));
            }
        }
        return new ShopResponsePacket(shopItems);
    }

    public static void handle(ShopResponsePacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof ShopScreen shopScreen) {
                shopScreen.updateShopItems(msg.shopItems);
            }
        });
        context.setPacketHandled(true);
    }
}
