package com.mo.economy_system.shop;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShopManager {
    public static final File CONFIG_FILE = new File(FMLPaths.CONFIGDIR.get().toFile(), "economy_shop.json");
    private static final Gson GSON = new Gson();
    private static final Random RANDOM = new Random();

    private final List<ShopItem> items = new ArrayList<>();

    public ShopManager() {
        loadFromConfig();
    }

    public List<ShopItem> getItems() {
        return new ArrayList<>(items); // 返回副本以保护内部列表
    }

    public void saveToConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(items, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromConfig() {
        if (!CONFIG_FILE.exists()) {
            saveDefaultConfig();
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            Type listType = new TypeToken<List<ShopItem>>() {}.getType();
            List<ShopItem> loadedItems = GSON.fromJson(reader, listType);
            items.clear();
            if (loadedItems != null) {
                items.addAll(loadedItems);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDefaultConfig() {
        items.add(new ShopItem("minecraft:dirt", 10, "泥土"));
        items.add(new ShopItem("minecraft:grass_block", 15, "草方块"));
        items.add(new ShopItem("minecraft:sand", 10, "沙子"));
        items.add(new ShopItem("minecraft:stone", 20, "石头"));
        items.add(new ShopItem("minecraft:oak_wood", 10, "橡木原木"));
        items.add(new ShopItem("minecraft:oak_sapling", 15, "橡木树苗"));
        items.add(new ShopItem("minecraft:spruce_wood", 15, "云杉木原木"));
        items.add(new ShopItem("minecraft:spruce_sapling", 20, "云杉木树苗"));
        items.add(new ShopItem("minecraft:birch_wood", 10, "白桦木原木"));
        items.add(new ShopItem("minecraft:birch_sapling", 15, "白桦木树苗"));
        items.add(new ShopItem("minecraft:jungle_wood", 20, "从林木原木"));
        items.add(new ShopItem("minecraft:jungle_sapling", 25, "从林木树苗"));
        items.add(new ShopItem("minecraft:acacia_wood", 15, "金合欢木原木"));
        items.add(new ShopItem("minecraft:acacia_sapling", 20, "金合欢木树苗"));
        items.add(new ShopItem("minecraft:dark_oak_wood", 20, "深色橡木原木"));
        items.add(new ShopItem("minecraft:dark_oak_sapling", 25, "深色橡木树苗"));
        items.add(new ShopItem("minecraft:mangrove_wood", 15, "红木原木"));
        items.add(new ShopItem("minecraft:mangrove_propagule", 20, "红树胎生苗"));
        items.add(new ShopItem("minecraft:crimson_hyphae", 25, "绯红木原木"));
        items.add(new ShopItem("minecraft:warped_hyphae", 25, "诡异木原木"));
        items.add(new ShopItem("minecraft:cherry_wood", 15, "樱花木原木"));
        items.add(new ShopItem("minecraft:cherry_sapling", 20, "樱花树苗"));
        saveToConfig();
    }

    // 定期调整价格的方法
    public void adjustPrices() {
        for (ShopItem item : items) {
            double randomFactor = 0.5 + (RANDOM.nextDouble() * (1.5 - 0.5)); // 随机生成 0.5 到 1.5 之间的浮动系数
            item.setFluctuationFactor(randomFactor); // 更新涨幅系数
            int newPrice = (int) Math.max(1, item.getBasePrice() * randomFactor); // 确保价格至少为 1
            item.setCurrentPrice(newPrice);
        }
        saveToConfig(); // 保存调整后的价格
    }
}
