package com.mo.economy_system.network.packets.economy_system;

import com.mo.economy_system.network.EconomyNetwork;
import com.mo.economy_system.system.economy_system.EconomySavedData;
import com.mo.economy_system.system.economy_system.market.MarketItem;
import com.mo.economy_system.system.economy_system.market.MarketManager;
import com.mo.economy_system.utils.MessageKeys;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class MarketDeliverItemPacket {

    private final UUID itemId; // 商品的唯一 ID

    public MarketDeliverItemPacket(UUID itemId) {
        this.itemId = itemId;
    }

    public static void encode(MarketDeliverItemPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.itemId);
    }

    public static MarketDeliverItemPacket decode(FriendlyByteBuf buf) {
        return new MarketDeliverItemPacket(buf.readUUID());
    }

    public static void handle(MarketDeliverItemPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
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

            // 验证买家是否有足够资源
            // 检测并移除物品
            int price = item.getPrice();
            if (consumeItem(buyer, item.getItemStack(), item.getItemStack().getCount())) {
                // 扣除供货者资源并将货币发放给供货者
                savedData.addBalance(buyer.getUUID(), price);

                item.setDeliveredItem(true);
                // 通知买家成功购买
                buyer.sendSystemMessage(Component.translatable(MessageKeys.DELIVERY_SUCCESS_KEY, item.getItemStack().getHoverName().getString(), item.getItemStack().getCount()));

                UUID sellerID = item.getSellerID();
                // 通知卖家（如果在线）
                ServerPlayer seller = serverLevel.getServer().getPlayerList().getPlayer(sellerID);
                if (seller != null) {
                    // 卖家在线，直接发送消息
                    seller.sendSystemMessage(Component.translatable(MessageKeys.ORDER_DELIVERED_BY_PLAYER_KEY, item.getItemStack().getHoverName().getString(), item.getItemStack().getCount(), buyer.getName().getString()));
                } else {
                    // 卖家不在线，将通知存储到离线消息中
                    String text = Component.translatable(MessageKeys.ORDER_DELIVERED_BY_PLAYER_KEY, item.getItemStack().getHoverName().getString(), item.getItemStack().getCount(), buyer.getName().getString()).getString();
                    savedData.storeOfflineMessage(sellerID, text);
                }
            } else {
                buyer.sendSystemMessage(Component.translatable(MessageKeys.DELIVERY_NOT_ENOUGH_ITEMS_KEY));
            }

            // 通知客户端刷新市场界面
            EconomyNetwork.INSTANCE.sendToServer(new MarketDataRequestPacket());
        });
        context.setPacketHandled(true);
    }

    /**
     * 检测玩家是否有指定数量的指定物品，并移除这些物品。
     *
     * @param player       玩家
     * @param targetStack  目标物品堆栈（用于匹配物品类型）
     * @param requiredCount 需要的数量
     * @return 如果玩家有足够数量的物品并成功移除，返回 true；否则返回 false
     */
    public static boolean consumeItem(ServerPlayer player, ItemStack targetStack, int requiredCount) {
        // 检查玩家是否有足够数量的物品
        int totalCount = getItemCount(player, targetStack);
        if (totalCount < requiredCount) {
            return false; // 物品数量不足
        }

        // 移除指定数量的物品
        removeItem(player, targetStack, requiredCount);
        return true;
    }

    /**
     * 获取玩家身上指定物品的数量。
     *
     * @param player      玩家
     * @param targetStack 目标物品堆栈
     * @return 物品数量
     */
    private static int getItemCount(ServerPlayer player, ItemStack targetStack) {
        int count = 0;
        NonNullList<ItemStack> inventory = player.getInventory().items; // 获取主物品栏
        for (ItemStack stack : inventory) {
            if (ItemStack.isSameItemSameTags(stack, targetStack)) { // 检查物品类型和 NBT 是否匹配
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * 移除玩家身上指定数量的指定物品。
     *
     * @param player      玩家
     * @param targetStack 目标物品堆栈
     * @param count       数量
     */
    private static void removeItem(ServerPlayer player, ItemStack targetStack, int count) {
        NonNullList<ItemStack> inventory = player.getInventory().items; // 获取主物品栏
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);
            if (ItemStack.isSameItemSameTags(stack, targetStack)) { // 检查物品类型和 NBT 是否匹配
                int removeAmount = Math.min(stack.getCount(), count);
                stack.shrink(removeAmount); // 减少物品数量
                count -= removeAmount;
                if (count <= 0) {
                    break; // 已经移除足够数量的物品
                }
            }
        }
    }
}