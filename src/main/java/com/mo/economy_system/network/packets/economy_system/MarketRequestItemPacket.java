package com.mo.economy_system.network.packets.economy_system;

import com.mo.economy_system.system.economy_system.EconomySavedData;
import com.mo.economy_system.system.economy_system.market.MarketItem;
import com.mo.economy_system.system.economy_system.market.MarketManager;
import com.mo.economy_system.utils.MessageKeys;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MarketRequestItemPacket {

    private static final String LIST_SUCCESSFULLY_MESSAGE_KEY = "message.list.list_successfully";

    private final MarketItem marketItem;

    public MarketRequestItemPacket(MarketItem marketItem) {
        this.marketItem = marketItem;
    }

    public static void encode(MarketRequestItemPacket msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.marketItem.toNBT());
    }

    public static MarketRequestItemPacket decode(FriendlyByteBuf buf) {
        return new MarketRequestItemPacket(MarketItem.fromNBT(buf.readNbt()));
    }

    public static void handle(MarketRequestItemPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender(); // 获取发送上架请求的玩家
            if (player == null) return;

            ServerLevel serverLevel = player.serverLevel();
            EconomySavedData savedData = EconomySavedData.getInstance(serverLevel);

            // 验证买家是否有足够货币
            int price = msg.marketItem.getPrice();
            if (!savedData.hasEnoughBalance(player.getUUID(), price)) {
                player.sendSystemMessage(Component.translatable(MessageKeys.MARKET_PURCHASE_FAILED_MESSAGE_KEY));
                return;
            }

            savedData.minBalance(player.getUUID(), price);
            // 将商品加入市场管理器
            MarketManager.addMarketItem(msg.marketItem);

            // 发送成功消息给玩家
            player.sendSystemMessage(Component.translatable(LIST_SUCCESSFULLY_MESSAGE_KEY));
        });
        context.setPacketHandled(true);
    }

}
