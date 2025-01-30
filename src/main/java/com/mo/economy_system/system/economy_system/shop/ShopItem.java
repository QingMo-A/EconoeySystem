package com.mo.economy_system.system.economy_system.shop;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ShopItem {
    private final String itemId;       // 物品 ID
    private final String description; // 商品描述
    private final int basePrice;       // 初始价格
    private int currentPrice;          // 当前价格
    private double fluctuationFactor; // 涨幅系数（用于动态调整价格）

    public ShopItem(String itemId, int basePrice, String description) {
        this.itemId = itemId;
        this.basePrice = basePrice;
        this.currentPrice = basePrice; // 初始化时当前价格等于基础价格
        this.description = description;
        this.fluctuationFactor = 1.0;  // 默认涨幅系数为 1.0（无变化）
    }

    public String getItemId() {
        return itemId;
    }

    public int getBasePrice() {
        return basePrice;
    }

    public int getCurrentPrice() {
        return currentPrice;
    }

    public String getDescription() {
        return description;
    }

    public double getFluctuationFactor() {
        return fluctuationFactor;
    }

    public void setCurrentPrice(int currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setFluctuationFactor(double fluctuationFactor) {
        this.fluctuationFactor = fluctuationFactor;
    }

    public ItemStack getItemStack() {
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(itemId));
        if (item != null) {
            return new ItemStack(item);
        } else {
            return ItemStack.EMPTY; // 如果物品 ID 无效，返回空堆
        }
    }

    // 保存到 NBT
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("itemId", itemId);
        tag.putInt("basePrice", basePrice);
        tag.putInt("currentPrice", currentPrice);
        tag.putString("description", description);
        tag.putDouble("fluctuationFactor", fluctuationFactor);
        return tag;
    }

    // 从 NBT 加载
    public static ShopItem fromNBT(CompoundTag tag) {
        ShopItem shopItem = new ShopItem(
                tag.getString("itemId"),
                tag.getInt("basePrice"),
                tag.getString("description")
        );
        shopItem.setCurrentPrice(tag.getInt("currentPrice"));
        shopItem.setFluctuationFactor(tag.getDouble("fluctuationFactor"));
        return shopItem;
    }
}
