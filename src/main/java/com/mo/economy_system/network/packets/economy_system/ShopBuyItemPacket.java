package com.mo.economy_system.network.packets.economy_system;

import com.mo.economy_system.system.economy_system.EconomySavedData;
import com.mo.economy_system.utils.MessageKeys;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
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
            ServerPlayer player = context.getSender();
            if (player == null) return;

            EconomySavedData economyData = EconomySavedData.getInstance(player.serverLevel());
            int totalPrice = msg.price * msg.quantity;

            // 1. 检查余额是否足够
            if (economyData.getBalance(player.getUUID()) < totalPrice) {
                player.sendSystemMessage(Component.translatable(MessageKeys.SHOP_BUY_FAILED_MESSAGE_KEY));
                return;
            }

            // 2. 检查物品是否有效
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(msg.itemID));
            if (item == null) {
                player.sendSystemMessage(Component.translatable(MessageKeys.SHOP_INVALID_ITEM_MESSAGE_KEY));
                return;
            }

            int maxStackSize = item.getMaxStackSize();
            int remainingQuantity = msg.quantity;

            // 3. 计算实际需要的槽位（考虑现有堆叠和空槽）
            int requiredSlots = calculateRequiredSlots(player.getInventory(), item, remainingQuantity);

            // 4. 检查是否有足够的槽位
            if (requiredSlots > 0) {
                player.sendSystemMessage(Component.translatable(MessageKeys.SHOP_BUY_FAILED_INVENTORY_FULL_MESSAGE_KEY));
                return;
            }

            // 5. 执行购买逻辑（扣除余额并添加物品）
            try {
                economyData.minBalance(player.getUUID(), totalPrice);
                addItemsToInventory(player.getInventory(), item, msg.quantity);
                player.sendSystemMessage(Component.translatable(
                        MessageKeys.SHOP_BUY_SUCCESSFULLY_MESSAGE_KEY,
                        totalPrice,
                        msg.quantity,
                        item.getDescription().getString()
                ));
            } catch (Exception e) {
                economyData.addBalance(player.getUUID(), totalPrice); // 回滚余额
                player.sendSystemMessage(Component.translatable(MessageKeys.SHOP_BUY_ERROR_MESSAGE_KEY));
            }
        });
        context.setPacketHandled(true);
    }

    // 辅助方法：计算需要的槽位
    private static int calculateRequiredSlots(Inventory inventory, Item item, int quantity) {
        int maxStackSize = item.getMaxStackSize();
        int remaining = quantity;

        // 1. 尝试合并到现有堆叠（仅限可堆叠物品）
        if (maxStackSize > 1) {
            for (ItemStack stack : inventory.items) {
                if (stack.getItem() == item && stack.getCount() < stack.getMaxStackSize()) {
                    int availableSpace = stack.getMaxStackSize() - stack.getCount();
                    int transfer = Math.min(availableSpace, remaining);
                    remaining -= transfer;
                    if (remaining == 0) return 0; // 无需新槽位
                }
            }
        }

        // 2. 计算剩余需要的新槽位
        int requiredSlots = 0;
        if (remaining > 0) {
            if (maxStackSize == 1) {
                requiredSlots = remaining; // 不可堆叠物品
            } else {
                requiredSlots = (remaining + maxStackSize - 1) / maxStackSize; // 向上取整
            }

            // 检查实际空槽位是否足够
            int freeSlots = 0;
            for (ItemStack stack : inventory.items) {
                if (stack.isEmpty()) freeSlots++;
            }
            if (freeSlots < requiredSlots) {
                return requiredSlots - freeSlots; // 返回不足的槽位数
            }
        }

        return 0; // 槽位足够
    }

    // 辅助方法：将物品添加到背包
    private static void addItemsToInventory(Inventory inventory, Item item, int quantity) {
        int maxStackSize = item.getMaxStackSize();
        int remaining = quantity;

        // 1. 优先填充现有堆叠（仅限可堆叠物品）
        if (maxStackSize > 1) {
            for (ItemStack stack : inventory.items) {
                if (stack.getItem() == item && stack.getCount() < stack.getMaxStackSize()) {
                    int availableSpace = stack.getMaxStackSize() - stack.getCount();
                    int transfer = Math.min(availableSpace, remaining);
                    stack.grow(transfer);
                    remaining -= transfer;
                    if (remaining == 0) return;
                }
            }
        }

        // 2. 填充新槽位
        while (remaining > 0) {
            int stackSize = Math.min(remaining, maxStackSize);
            ItemStack newStack = new ItemStack(item, stackSize);
            inventory.add(newStack); // 自动处理掉落逻辑
            remaining -= stackSize;
        }
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
