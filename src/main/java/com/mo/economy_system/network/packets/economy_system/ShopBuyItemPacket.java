package com.mo.economy_system.network.packets.economy_system;

import com.mo.economy_system.system.economy_system.EconomySavedData;
import com.mo.economy_system.utils.MessageKeys;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ShopBuyItemPacket {

    private final String itemID;
    private final int price;
    private final int quantity;

    public ShopBuyItemPacket(String itemID, int price, int quantity) {
        this.itemID = itemID;
        this.price = price;
        this.quantity = quantity;
    }

    public static void encode(ShopBuyItemPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.itemID);
        buf.writeInt(msg.price); // 将价格编码
        buf.writeInt(msg.quantity); // 将购买数量编码
    }

    public static ShopBuyItemPacket decode(FriendlyByteBuf buf) {
        String itemID = buf.readUtf(); // 解码物品名称
        int price = buf.readInt(); // 解码价格
        int quantity = buf.readInt(); // 解码购买数量
        return new ShopBuyItemPacket(itemID, price, quantity);
    }

    public static void handle(ShopBuyItemPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender(); // 获取发送购买请求的玩家
            if (player != null) {
                // 获取玩家的经济数据
                EconomySavedData economyData = EconomySavedData.getInstance(player.serverLevel());

                // 获取购买总价
                int totalPrice = msg.price * msg.quantity;

                // 检查玩家是否有足够的余额
                if (economyData.getBalance(player.getUUID()) >= totalPrice) {
                    // 扣除玩家余额
                    economyData.minBalance(player.getUUID(), totalPrice);

                    // 创建物品并添加到玩家背包
                    ItemStack purchasedItem = getItemStack(msg.itemID);
                    purchasedItem.setCount(msg.quantity); // 设置购买数量

                    boolean added = player.getInventory().add(purchasedItem); // 尝试将物品添加到玩家背包

                    if (!added) {
                        // 如果背包满了，将物品丢到玩家附近
                        player.drop(purchasedItem, false);
                    }

                    // 通知玩家购买成功
                    player.sendSystemMessage(Component.translatable(MessageKeys.SHOP_BUY_SUCCESSFULLY_MESSAGE_KEY, (msg.price * msg.quantity), msg.quantity, getItemStack(msg.itemID).getHoverName().getString()));
                } else {
                    // 通知玩家余额不足
                    player.sendSystemMessage(Component.translatable(MessageKeys.SHOP_BUY_FAILED_MESSAGE_KEY));
                }
            }
        });
        context.setPacketHandled(true);
    }

    public static ItemStack getItemStack(String itemID) {
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(itemID));
        if (item != null) {
            return new ItemStack(item);
        } else {
            return ItemStack.EMPTY; // 如果物品 ID 无效，返回空堆
        }
    }
}
