package com.mo.economy_system.system.economy_system.shop;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
        items.add(new ShopItem("economy_system:recall_potion", 5, "回忆药水"));
        items.add(new ShopItem("economy_system:wormhole_potion", 10, "虫洞药水"));

        items.add(new ShopItem("minecraft:dirt", 5, "泥土"));
        items.add(new ShopItem("minecraft:grass_block", 5, "草方块"));
        items.add(new ShopItem("minecraft:sand", 5, "沙子"));
        items.add(new ShopItem("minecraft:stone", 5, "石头"));

        // 原木
        items.add(new ShopItem("minecraft:oak_log", 5, "橡木原木"));
        items.add(new ShopItem("minecraft:spruce_log", 5, "云杉原木"));
        items.add(new ShopItem("minecraft:birch_log", 5, "白桦原木"));
        items.add(new ShopItem("minecraft:jungle_log", 5, "丛林原木"));
        items.add(new ShopItem("minecraft:acacia_log", 5, "金合欢原木"));
        items.add(new ShopItem("minecraft:dark_oak_log", 5, "深色橡木原木"));
        items.add(new ShopItem("minecraft:mangrove_log", 5, "红树林原木"));
        items.add(new ShopItem("minecraft:cherry_log", 5, "樱花原木")); // 1.20 新增
        items.add(new ShopItem("minecraft:crimson_stem", 5, "绯红菌柄")); // 下界
        items.add(new ShopItem("minecraft:warped_stem", 5, "诡异菌柄")); // 下界

        // 树苗
        items.add(new ShopItem("minecraft:oak_sapling", 5, "橡树树苗"));
        items.add(new ShopItem("minecraft:spruce_sapling", 5, "云杉树苗"));
        items.add(new ShopItem("minecraft:birch_sapling", 5, "白桦树苗"));
        items.add(new ShopItem("minecraft:jungle_sapling", 5, "丛林树苗"));
        items.add(new ShopItem("minecraft:acacia_sapling", 5, "金合欢树苗"));
        items.add(new ShopItem("minecraft:dark_oak_sapling", 5, "深色橡树树苗"));
        items.add(new ShopItem("minecraft:mangrove_propagule", 5, "红树林胎生苗")); // 1.19 新增
        items.add(new ShopItem("minecraft:cherry_sapling", 5, "樱花树苗")); // 1.20 新增

        // 树叶
        items.add(new ShopItem("minecraft:oak_leaves", 5, "橡树树叶"));
        items.add(new ShopItem("minecraft:spruce_leaves", 5, "云杉树叶"));
        items.add(new ShopItem("minecraft:birch_leaves", 5, "白桦树叶"));
        items.add(new ShopItem("minecraft:jungle_leaves", 5, "丛林树叶"));
        items.add(new ShopItem("minecraft:acacia_leaves", 5, "金合欢树叶"));
        items.add(new ShopItem("minecraft:dark_oak_leaves", 5, "深色橡树树叶"));
        items.add(new ShopItem("minecraft:mangrove_leaves", 5, "红树林树叶")); // 1.19 新增
        items.add(new ShopItem("minecraft:cherry_leaves", 5, "樱花树叶")); // 1.20 新增
        items.add(new ShopItem("minecraft:azalea_leaves", 5, "杜鹃树叶")); // 1.17 新增
        items.add(new ShopItem("minecraft:flowering_azalea_leaves", 5, "开花杜鹃树叶")); // 1.17 新增

        items.add(new ShopItem("minecraft:quartz", 5, "下界石英"));
        // 萤石
        items.add(new ShopItem("minecraft:glowstone", 5, "萤石"));

        // 红石
        items.add(new ShopItem("minecraft:redstone", 5, "红石"));

        // 海晶灯
        items.add(new ShopItem("minecraft:sea_lantern", 5, "海晶灯"));

        // 石砖
        items.add(new ShopItem("minecraft:stone_bricks", 5, "石砖"));
        items.add(new ShopItem("minecraft:mossy_stone_bricks", 5, "苔石砖")); // 苔石砖
        items.add(new ShopItem("minecraft:cracked_stone_bricks", 5, "裂纹石砖")); // 裂纹石砖
        items.add(new ShopItem("minecraft:chiseled_stone_bricks", 5, "雕纹石砖")); // 雕纹石砖

        // 烧好的石头（平滑石头）
        items.add(new ShopItem("minecraft:smooth_stone", 5, "平滑石头"));
        // 混凝土
        items.add(new ShopItem("minecraft:white_concrete", 5, "白色混凝土"));
        items.add(new ShopItem("minecraft:orange_concrete", 5, "橙色混凝土"));
        items.add(new ShopItem("minecraft:magenta_concrete", 5, "品红色混凝土"));
        items.add(new ShopItem("minecraft:light_blue_concrete", 5, "淡蓝色混凝土"));
        items.add(new ShopItem("minecraft:yellow_concrete", 5, "黄色混凝土"));
        items.add(new ShopItem("minecraft:lime_concrete", 5, "黄绿色混凝土"));
        items.add(new ShopItem("minecraft:pink_concrete", 5, "粉红色混凝土"));
        items.add(new ShopItem("minecraft:gray_concrete", 5, "灰色混凝土"));
        items.add(new ShopItem("minecraft:light_gray_concrete", 5, "淡灰色混凝土"));
        items.add(new ShopItem("minecraft:cyan_concrete", 5, "青色混凝土"));
        items.add(new ShopItem("minecraft:purple_concrete", 5, "紫色混凝土"));
        items.add(new ShopItem("minecraft:blue_concrete", 5, "蓝色混凝土"));
        items.add(new ShopItem("minecraft:brown_concrete", 5, "棕色混凝土"));
        items.add(new ShopItem("minecraft:green_concrete", 5, "绿色混凝土"));
        items.add(new ShopItem("minecraft:red_concrete", 5, "红色混凝土"));
        items.add(new ShopItem("minecraft:black_concrete", 5, "黑色混凝土"));
        // 羊毛
        items.add(new ShopItem("minecraft:white_wool", 5, "白色羊毛"));
        items.add(new ShopItem("minecraft:orange_wool", 5, "橙色羊毛"));
        items.add(new ShopItem("minecraft:magenta_wool", 5, "品红色羊毛"));
        items.add(new ShopItem("minecraft:light_blue_wool", 5, "淡蓝色羊毛"));
        items.add(new ShopItem("minecraft:yellow_wool", 5, "黄色羊毛"));
        items.add(new ShopItem("minecraft:lime_wool", 5, "黄绿色羊毛"));
        items.add(new ShopItem("minecraft:pink_wool", 5, "粉红色羊毛"));
        items.add(new ShopItem("minecraft:gray_wool", 5, "灰色羊毛"));
        items.add(new ShopItem("minecraft:light_gray_wool", 5, "淡灰色羊毛"));
        items.add(new ShopItem("minecraft:cyan_wool", 5, "青色羊毛"));
        items.add(new ShopItem("minecraft:purple_wool", 5, "紫色羊毛"));
        items.add(new ShopItem("minecraft:blue_wool", 5, "蓝色羊毛"));
        items.add(new ShopItem("minecraft:brown_wool", 5, "棕色羊毛"));
        items.add(new ShopItem("minecraft:green_wool", 5, "绿色羊毛"));
        items.add(new ShopItem("minecraft:red_wool", 5, "红色羊毛"));
        items.add(new ShopItem("minecraft:black_wool", 5, "黑色羊毛"));
        saveToConfig();
    }

    public void adjustPrices() {
        for (ShopItem item : items) {
            double basePrice = item.getBasePrice(); // 获取物品基础价格
            double currentPrice = item.getCurrentPrice(); // 获取物品当前价格

            // 计算当前价格与基础价格的差距百分比
            double priceDifferencePercent = (currentPrice - basePrice) / basePrice;

            // 生成浮动系数的范围
            double randomFactor;

            if (priceDifferencePercent > 0.30) {
                // 如果当前价格大于基础价格的 30%，减少涨价的概率
                randomFactor = 0.1 + (RANDOM.nextDouble() * (1.5 - 0.1)); // 较小的涨幅系数
            } else if (priceDifferencePercent < -0.30) {
                // 如果当前价格小于基础价格的 30%，减少降价的概率
                randomFactor = 0.5 + (RANDOM.nextDouble() * (3.0 - 0.5)); // 较大的涨幅系数
            } else {
                // 否则维持普通的浮动范围
                randomFactor = 0.5 + (RANDOM.nextDouble() * (2.0 - 0.5)); // 生成浮动系数
            }

            // 使用 BigDecimal 来确保浮动系数保留两位小数
            BigDecimal fluctuationFactor = new BigDecimal(randomFactor).setScale(2, RoundingMode.HALF_UP);
            item.setFluctuationFactor(fluctuationFactor.doubleValue()); // 更新涨幅系数

            // 计算新的价格，确保价格至少为 1
            int newPrice = (int) Math.max(1, currentPrice * randomFactor); // 确保价格至少为 1
            item.setCurrentPrice(newPrice);
        }

        // 保存调整后的价格
        saveToConfig();
    }
}
