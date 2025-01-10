package com.mo.economy_system.market;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

public class MarketSavedData extends SavedData {
    private static final String DATA_NAME = "market_data";

    private final List<MarketItem> marketItems = new ArrayList<>();

    // 获取市场商品列表
    public List<MarketItem> getMarketItems() {
        return marketItems;
    }

    // 添加商品
    public void addMarketItem(MarketItem item) {
        marketItems.add(item);
        setDirty(); // 标记为脏数据，表示需要保存
    }

    // 移除商品
    public void removeMarketItem(MarketItem item) {
        marketItems.remove(item);
        setDirty(); // 标记为脏数据，表示需要保存
    }

    // 清空市场
    public void clearMarketItems() {
        marketItems.clear();
        setDirty(); // 标记为脏数据，表示需要保存
    }

    // 序列化到 NBT
    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag listTag = new ListTag();
        for (MarketItem item : marketItems) {
            listTag.add(item.toNBT());
        }
        tag.put("marketItems", listTag);
        return tag;
    }

    // 从 NBT 反序列化
    public static MarketSavedData load(CompoundTag tag) {
        MarketSavedData data = new MarketSavedData();
        if (tag.contains("marketItems")) {
            ListTag listTag = tag.getList("marketItems", CompoundTag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                MarketItem item = MarketItem.fromNBT(listTag.getCompound(i));
                data.addMarketItem(item);
            }
        }
        return data;
    }

    // 获取实例
    public static MarketSavedData getInstance(net.minecraft.server.level.ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(MarketSavedData::load, MarketSavedData::new, DATA_NAME);
    }
}
