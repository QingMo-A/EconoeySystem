package com.mo.economy_system.reward;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RewardManager {
    public static final File CONFIG_FILE = new File(FMLPaths.CONFIGDIR.get().toFile(), "economy_rewards.json");
    private static final Gson GSON = new Gson();

    private static final List<RewardEntry> rewards = new ArrayList<>();

    public RewardManager() {
        loadFromConfig();
    }

    public List<RewardEntry> getRewards() {
        return new ArrayList<>(rewards); // 返回副本保护内部列表
    }

    public Optional<RewardEntry> getRewardForEntity(ResourceLocation entityType) {
        return rewards.stream()
                .filter(entry -> entry.type.equals(entityType.toString()))
                .findFirst();
    }

    public void saveToConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(rewards, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromConfig() {
        if (!CONFIG_FILE.exists()) {
            saveDefaultConfig();
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            Type listType = new TypeToken<List<RewardEntry>>() {}.getType();
            List<RewardEntry> loadedRewards = GSON.fromJson(reader, listType);
            rewards.clear();
            if (loadedRewards != null) {
                rewards.addAll(loadedRewards);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDefaultConfig() {
        rewards.add(new RewardEntry("minecraft:zombie", 0.1, 1, 5));
        rewards.add(new RewardEntry("minecraft:skeleton", 0.2, 2, 8));
        rewards.add(new RewardEntry("minecraft:creeper", 0.15, 5, 10));
        saveToConfig();
    }

    public static class RewardEntry {
        public String type; // 实体类型
        public double dropChance; // 掉落几率
        public int dropMin; // 最小掉落值
        public int dropMax; // 最大掉落值

        public RewardEntry(String type, double dropChance, int dropMin, int dropMax) {
            this.type = type;
            this.dropChance = dropChance;
            this.dropMin = dropMin;
            this.dropMax = dropMax;
        }
    }
}
