package com.mo.economy_system.system.economy_system.market;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class MarketItem {
    private final UUID tradeID; // 唯一商品 ID
    private final String itemID;
    private final ItemStack itemStack;
    private final int price;
    private final String sellerName;
    private final UUID sellerID;
    private final long listingTime;
    private final boolean isMarketItem;
    private boolean isDeliveredItem;

    public MarketItem(UUID tradeID, String itemID, ItemStack itemStack, int price, String sellerName, UUID sellerID, long listingTime, boolean isMarketItem, boolean isDeliveredItem) {
        this.tradeID = tradeID; // 生成唯一商品 ID
        this.itemID = itemID;
        this.itemStack = itemStack;
        this.price = price;
        this.sellerName = sellerName;
        this.sellerID = sellerID;
        this.listingTime = listingTime;
        this.isMarketItem = isMarketItem;
        this.isDeliveredItem = isDeliveredItem;
    }

    public MarketItem(UUID tradeID, String itemID, int price, String sellerName, UUID sellerID, long listingTime, boolean isMarketItem) {
        this.tradeID = tradeID; // 生成唯一商品 ID
        this.itemID = itemID;
        this.itemStack = getItemStack(itemID);
        this.price = price;
        this.sellerName = sellerName;
        this.sellerID = sellerID;
        this.listingTime = listingTime;
        this.isMarketItem = isMarketItem;
        this.isDeliveredItem = false;
    }

    public UUID getTradeID() {
        return tradeID;
    }

    public String getItemID() {
        return itemID;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getPrice() {
        return price;
    }

    public String getSellerName() {
        return sellerName;
    }

    public UUID getSellerID() {
        return sellerID;
    }

    public long getListingTime() {
        return listingTime;
    }

    public boolean isMarketItem() {
        return isMarketItem;
    }

    public boolean isDeliveredItem() {
        return isDeliveredItem;
    }

    public void setDeliveredItem(boolean deliveredItem) {
        isDeliveredItem = deliveredItem;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("tradeID", tradeID);
        tag.putString("itemID", itemID);
        tag.put("itemStack", itemStack.save(new CompoundTag()));
        tag.putInt("price", price);
        tag.putString("sellerName", sellerName);
        tag.putUUID("sellerID", sellerID);
        tag.putLong("listingTime", listingTime);
        tag.putBoolean("isMarketItem", isMarketItem);
        tag.putBoolean("isDeliveredItem", isDeliveredItem);
        return tag;
    }

    public static MarketItem fromNBT(CompoundTag tag) {
        UUID tradeID = tag.getUUID("tradeID");
        String itemID = tag.getString("itemID");
        ItemStack itemStack = ItemStack.of(tag.getCompound("itemStack"));
        int price = tag.getInt("price");
        String sellerName = tag.getString("sellerName");
        UUID sellerID = tag.getUUID("sellerID");
        long listingTime = tag.getLong("listingTime");
        boolean isMarketItem = tag.getBoolean("isMarketItem");
        boolean isDeliveredItem = tag.getBoolean("isDeliveredItem");
        return new MarketItem(tradeID, itemID, itemStack, price, sellerName, sellerID, listingTime, isMarketItem, isDeliveredItem);
    }

    private ItemStack getItemStack(String itemID) {
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(itemID));
        if (item != null) {
            return new ItemStack(item);
        } else {
            return ItemStack.EMPTY; // 如果物品 ID 无效，返回空堆
        }
    }
}
