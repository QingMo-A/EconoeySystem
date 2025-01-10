package com.mo.economy_system.market;

import net.minecraft.nbt.CompoundTag;
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

    public MarketItem(UUID tradeID, String itemID, ItemStack itemStack, int price, String sellerName, UUID sellerID, long listingTime) {
        this.tradeID = tradeID; // 生成唯一商品 ID
        this.itemID = itemID;
        this.itemStack = itemStack;
        this.price = price;
        this.sellerName = sellerName;
        this.sellerID = sellerID;
        this.listingTime = listingTime;
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

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("tradeID", tradeID);
        tag.putString("itemID", itemID);
        tag.put("itemStack", itemStack.save(new CompoundTag()));
        tag.putInt("price", price);
        tag.putString("sellerName", sellerName);
        tag.putUUID("sellerID", sellerID);
        tag.putLong("listingTime", listingTime);
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
        return new MarketItem(tradeID, itemID, itemStack, price, sellerName, sellerID, listingTime);
    }
}
