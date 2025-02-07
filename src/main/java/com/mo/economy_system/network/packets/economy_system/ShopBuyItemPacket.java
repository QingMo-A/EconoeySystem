package com.mo.economy_system.network.packets.economy_system;

import com.mo.economy_system.system.economy_system.EconomySavedData;
import com.mo.economy_system.utils.MessageKeys;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
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
    private final String itemNbt;
    private final int price;
    private final int quantity;

    public ShopBuyItemPacket(String itemID, String itemNbt, int price, int quantity) {
        this.itemID = itemID;
        this.itemNbt = itemNbt;
        this.price = price;
        this.quantity = quantity;
    }

    public static void encode(ShopBuyItemPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.itemID);
        buf.writeUtf(msg.itemNbt);
        buf.writeInt(msg.price); // 将价格编码
        buf.writeInt(msg.quantity); // 将购买数量编码
    }

    public static ShopBuyItemPacket decode(FriendlyByteBuf buf) {
        String itemID = buf.readUtf(); // 解码物品名称
        String itemNbt = buf.readUtf();
        int price = buf.readInt(); // 解码价格
        int quantity = buf.readInt(); // 解码购买数量
        return new ShopBuyItemPacket(itemID, itemNbt, price, quantity);
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
            ItemStack itemStack = getItemStack(msg.itemID, msg.itemNbt);
            /*// 获取物品的 NBT 数据
            CompoundTag nbt = itemStack.getTag();

            if (nbt != null) {
                // 遍历 NBT 中的所有键
                for (String key : nbt.getAllKeys()) {
                    // 获取键对应的值（支持不同类型的值）
                    if (nbt.contains(key)) {
                        System.out.println("键名: " + key);

                        // 打印不同类型的值
                        if (nbt.contains(key, 8)) { // 8 表示字符串类型
                            System.out.println("值 (String): " + nbt.getString(key));
                        } else if (nbt.contains(key, 3)) { // 3 表示整数类型
                            System.out.println("值 (Integer): " + nbt.getInt(key));
                        } else if (nbt.contains(key, 10)) { // 10 表示 CompoundTag 类型
                            System.out.println("值 (Compound): " + nbt.getCompound(key));
                        } else if (nbt.contains(key, 9)) { // 9 表示 ListTag 类型
                            System.out.println("值 (List): " + nbt.getList(key, 10));  // 假设 List 是由 CompoundTag 组成
                        }
                    }
                }
            } else {
                System.out.println("该物品没有 NBT 数据");
            }*/
            Item item = itemStack.getItem();

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
                if (itemStack.getTag() != null) {
                    addItemsToInventory(player.getInventory(), item, msg.quantity, itemStack.getTag());
                } else {
                    addItemsToInventory(player.getInventory(), item, msg.quantity);
                }
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

    // 辅助方法：将物品添加到背包
    private static void addItemsToInventory(Inventory inventory, Item item, int quantity, CompoundTag tag) {
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
            newStack.setTag(tag);
            inventory.add(newStack); // 自动处理掉落逻辑
            remaining -= stackSize;
        }
    }

    public static ItemStack getItemStack(String itemId, String nbt) {
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(itemId));
        if (item == null) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(item);

        // 如果有自定义 NBT，则解析并写入
        if (nbt != null && !nbt.isEmpty()) {
            System.out.println("nbt not null !!!");
            stack = applyEnchantmentNBT(stack, nbt);
        }
        return stack;
    }

    public static ItemStack applyEnchantmentNBT(ItemStack itemStack, String nbtString) {
        // 解析NBT字符串
        CompoundTag userNbt;
        try {
            userNbt = TagParser.parseTag(nbtString);
            System.out.println("OK1");
        } catch (CommandSyntaxException e) {
            System.err.println("NBT格式错误: " + e.getMessage());
            return null;
        }

        // 应用NBT
        if (userNbt != null) {
            itemStack.setTag(userNbt);
            System.out.println("OK2");
        }

        return itemStack;
    }
}
