package com.mo.economy_system.network.packets.economy_system.sales_order;

import com.mo.economy_system.network.packets.economy_system.MarketDataRequestPacket;
import com.mo.economy_system.system.economy_system.market.MarketItem;
import com.mo.economy_system.system.economy_system.market.MarketManager;
import com.mo.economy_system.network.EconomyNetwork;
import com.mo.economy_system.utils.MessageKeys;
import com.mo.economy_system.utils.PlayerUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class MarketRemoveMarketItemPacket {

    private final UUID itemId;

    public MarketRemoveMarketItemPacket(UUID itemId) {
        this.itemId = itemId;
    }

    public static void encode(MarketRemoveMarketItemPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.itemId);
    }

    public static MarketRemoveMarketItemPacket decode(FriendlyByteBuf buf) {
        return new MarketRemoveMarketItemPacket(buf.readUUID());
    }

    public static void handle(MarketRemoveMarketItemPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            // 获取市场中的商品
            MarketItem item = MarketManager.getMarketItemById(msg.itemId);
            if (item == null) {
                player.sendSystemMessage(Component.translatable(MessageKeys.MARKET_REMOVE_FAILED_MESSAGE_KEY));
                return;
            }

            System.out.println(!item.getSellerID().equals(player.getUUID()));
            System.out.println(player.hasPermissions(2));
            // 验证是否是卖家
            if (!item.getSellerID().equals(player.getUUID())) {
                if (!PlayerUtils.isOP(player)) {
                    player.sendSystemMessage(Component.translatable(MessageKeys.MARKET_UNMATCHED_SELLER_MESSAGE_KEY));
                    return;
                }
            }

            // 从市场中移除商品
            MarketManager.removeMarketItem(item);

            // 将物品返回给卖家
            ItemStack removedItem = item.getItemStack().copy();
            if (!player.getInventory().add(removedItem)) {
                player.drop(removedItem, false);
            }

            // 通知客户端刷新市场界面
            EconomyNetwork.INSTANCE.sendToServer(new MarketDataRequestPacket());

            player.sendSystemMessage(Component.translatable(MessageKeys.MARKET_ITEM_HAS_BEEN_RETURNED_MESSAGE_KEY));
        });
        context.setPacketHandled(true);
    }
}

