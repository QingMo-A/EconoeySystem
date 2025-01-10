package com.mo.economy_system.network.packets;

import com.mo.economy_system.market.MarketItem;
import com.mo.economy_system.market.MarketManager;
import com.mo.economy_system.network.EconomyNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class RemoveItemPacket {

    private static final String REMOVE_FAILED_MESSAGE_KEY = "message.market.remove_failed";
    private static final String UNMATCHED_SELLER_MESSAGE_KEY = "message.market.unmatched_seller";
    private static final String ITEM_HAS_BEEN_RETURNED_MESSAGE_KEY = "message.market.item_has_been_returned";

    private final UUID itemId;

    public RemoveItemPacket(UUID itemId) {
        this.itemId = itemId;
    }

    public static void encode(RemoveItemPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.itemId);
    }

    public static RemoveItemPacket decode(FriendlyByteBuf buf) {
        return new RemoveItemPacket(buf.readUUID());
    }

    public static void handle(RemoveItemPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            // 获取市场中的商品
            MarketItem item = MarketManager.getMarketItemById(msg.itemId);
            if (item == null) {
                player.sendSystemMessage(Component.translatable(REMOVE_FAILED_MESSAGE_KEY));
                return;
            }

            // 验证是否是卖家
            if (!item.getSellerID().equals(player.getUUID())) {
                player.sendSystemMessage(Component.translatable(UNMATCHED_SELLER_MESSAGE_KEY));
                return;
            }

            // 从市场中移除商品
            MarketManager.removeMarketItem(item);

            // 将物品返回给卖家
            ItemStack removedItem = item.getItemStack().copy();
            if (!player.getInventory().add(removedItem)) {
                player.drop(removedItem, false);
            }

            // 通知客户端刷新市场界面
            EconomyNetwork.INSTANCE.sendToServer(new MarketRequestPacket());

            player.sendSystemMessage(Component.translatable(ITEM_HAS_BEEN_RETURNED_MESSAGE_KEY));
        });
        context.setPacketHandled(true);
    }
}

