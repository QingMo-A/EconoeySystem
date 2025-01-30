package com.mo.economy_system.network.packets.economy_system;

import com.mo.economy_system.system.economy_system.market.MarketItem;
import com.mo.economy_system.system.economy_system.market.MarketManager;
import com.mo.economy_system.utils.MessageKeys;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MarketListItemPacket {

    private final MarketItem marketItem;

    public MarketListItemPacket(MarketItem marketItem) {
        this.marketItem = marketItem;
    }

    public static void encode(MarketListItemPacket msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.marketItem.toNBT());
    }

    public static MarketListItemPacket decode(FriendlyByteBuf buf) {
        return new MarketListItemPacket(MarketItem.fromNBT(buf.readNbt()));
    }

    public static void handle(MarketListItemPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender(); // 获取发送上架请求的玩家
            if (player == null) return;

            // 获取玩家手中的物品
            var heldItem = player.getMainHandItem();

            // 检查是否与上架的物品匹配
            if (!heldItem.isEmpty() && ItemStack.isSameItemSameTags(heldItem, msg.marketItem.getItemStack())) {
                int requiredAmount = msg.marketItem.getItemStack().getCount();

                if (heldItem.getCount() >= requiredAmount) {
                    // 减少背包中的物品
                    heldItem.shrink(requiredAmount);

                    // 将商品加入市场管理器
                    MarketManager.addMarketItem(msg.marketItem);

                    // 发送成功消息给玩家
                    player.sendSystemMessage(Component.translatable(MessageKeys.LIST_SUCCESSFULLY_MESSAGE_KEY));
                } else {
                    // 如果物品数量不足，通知玩家
                    player.sendSystemMessage(Component.translatable(MessageKeys.LIST_INSUFFICIENT_ITEM_MESSAGE_KEY));
                }
            } else {
                // 如果手中的物品与上架物品不匹配，通知玩家
                player.sendSystemMessage(Component.translatable(MessageKeys.LIST_UNMATCHED_ITEM_MESSAGE_KEY));
            }
        });
        context.setPacketHandled(true);
    }

}
