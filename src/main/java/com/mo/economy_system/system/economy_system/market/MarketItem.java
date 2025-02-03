package com.mo.economy_system.system.economy_system.market;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import java.util.UUID;

public abstract class MarketItem {
    protected final UUID tradeID;
    protected final String itemID;
    protected final ItemStack itemStack;
    protected final int basePrice;
    protected final String sellerName;
    protected final UUID sellerID;
    protected final long listingTime;

    public MarketItem(UUID tradeID, String itemID, ItemStack itemStack, int basePrice, String sellerName, UUID sellerID, long listingTime) {
        this.tradeID = tradeID;
        this.itemID = itemID;
        this.itemStack = itemStack;
        this.basePrice = basePrice;
        this.sellerName = sellerName;
        this.sellerID = sellerID;
        this.listingTime = listingTime;
    }

    // 公共方法（Getters）...

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", this.getClass().getName()); // 关键：保存子类类型
        tag.putUUID("tradeID", tradeID);
        tag.putString("itemID", itemID);
        CompoundTag itemTag = new CompoundTag();
        itemStack.save(itemTag);
        tag.put("itemStack", itemTag);
        tag.putInt("basePrice", basePrice);
        tag.putString("sellerName", sellerName);
        tag.putUUID("sellerID", sellerID);
        tag.putLong("listingTime", listingTime);
        return tag;
    }

    public static MarketItem fromNBT(CompoundTag tag) {
        String type = tag.getString("type");
        try {
            return switch (type) { // 根据类型分发到子类
                case "com.mo.economy_system.system.economy_system.market.SalesOrder" -> SalesOrder.fromNBT(tag);
                case "com.mo.economy_system.system.economy_system.market.DemandOrder" -> DemandOrder.fromNBT(tag);
                default -> throw new IllegalArgumentException("未知的 MarketItem 类型: " + type);
            };
        } catch (Exception e) {
            throw new RuntimeException("反序列化 MarketItem 失败", e);
        }
    }

    // Getters...
    public UUID getTradeID() { return tradeID; }
    public String getItemID() { return itemID; }
    public ItemStack getItemStack() { return itemStack; }
    public int getBasePrice() { return basePrice; }
    public String getSellerName() { return sellerName; }
    public UUID getSellerID() { return sellerID; }
    public long getListingTime() { return listingTime; }
}