package com.mo.economy_system.network.packets.economy_system.sales_order;

import com.mo.economy_system.network.packets.economy_system.MarketDataRequestPacket;
import com.mo.economy_system.system.economy_system.market.MarketItem;
import com.mo.economy_system.system.economy_system.market.MarketManager;
import com.mo.economy_system.network.EconomyNetwork;
import com.mo.economy_system.system.economy_system.EconomySavedData;
import com.mo.economy_system.utils.MessageKeys;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class MarketPurchaseItemPacket {

    private final UUID itemId; // 商品的唯一 ID

    public MarketPurchaseItemPacket(UUID itemId) {
        this.itemId = itemId;
    }

    public static void encode(MarketPurchaseItemPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.itemId);
    }

    public static MarketPurchaseItemPacket decode(FriendlyByteBuf buf) {
        return new MarketPurchaseItemPacket(buf.readUUID());
    }

    public static void handle(MarketPurchaseItemPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer buyer = context.getSender();
            if (buyer == null) return;

            ServerLevel serverLevel = buyer.serverLevel();
            EconomySavedData savedData = EconomySavedData.getInstance(serverLevel);

            // 查找市场中的商品
            MarketItem item = MarketManager.getMarketItemById(msg.itemId);
            if (item == null) {
                buyer.sendSystemMessage(Component.translatable(MessageKeys.MARKET_ITEM_DOES_NOT_EXIST_MESSAGE_KEY));
                return;
            }

            // 验证买家是否有足够货币
            int price = item.getBasePrice();
            if (!savedData.hasEnoughBalance(buyer.getUUID(), price)) {
                buyer.sendSystemMessage(Component.translatable(MessageKeys.MARKET_PURCHASE_FAILED_MESSAGE_KEY));
                return;
            }

            // 扣除买家货币并将物品发放给买家
            savedData.minBalance(buyer.getUUID(), price);
            ItemStack purchasedItem = item.getItemStack().copy();
            if (!buyer.getInventory().add(purchasedItem)) {
                buyer.drop(purchasedItem, false); // 如果背包满了，直接丢在地上
            }

            // 直接通过 SellerUUID 增加余额
            UUID sellerID = item.getSellerID();
            savedData.addBalance(sellerID, price);

            // 通知买家成功购买
            buyer.sendSystemMessage(Component.translatable(MessageKeys.MARKET_PURCHASE_SUCCESSFULLY_MESSAGE_KEY, price, item.getItemStack().getHoverName().getString(), item.getItemStack().getCount()));

            // 通知卖家（如果在线）
            ServerPlayer seller = serverLevel.getServer().getPlayerList().getPlayer(sellerID);
            if (seller != null) {
                // 卖家在线，直接发送消息
                seller.sendSystemMessage(Component.translatable(MessageKeys.MARKET_COLLECT_MONEY_MESSAGE_KEY, item.getItemStack().getHoverName().getString(), buyer.getName().getString(), price));
            } else {
                // 卖家不在线，将通知存储到离线消息中
                String text = Component.translatable(MessageKeys.MARKET_COLLECT_MONEY_MESSAGE_KEY, item.getItemStack().getHoverName().getString(), buyer.getName().getString(), price).getString();
                savedData.storeOfflineMessage(sellerID, text);
            }

            // 从市场中移除商品
            MarketManager.removeMarketItem(item);

            // 通知客户端刷新市场界面
            EconomyNetwork.INSTANCE.sendToServer(new MarketDataRequestPacket());

            // 打印日志
            System.out.println("Item sold: " + item.getItemStack().getHoverName().getString() +
                    ", Price: " + price + " coins, Buyer: " + buyer.getName().getString() + ", Seller: " + sellerID);
        });
        context.setPacketHandled(true);
    }
}