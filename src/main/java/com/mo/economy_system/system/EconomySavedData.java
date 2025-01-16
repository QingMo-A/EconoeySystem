package com.mo.economy_system.system;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class EconomySavedData extends SavedData {
    private static final String DATA_NAME = "economy_data";
    private final Map<UUID, Integer> accounts = new HashMap<>();
    private final Map<UUID, List<String>> offlineMessages = new HashMap<>(); // 用于存储离线消息

    // 获取玩家余额
    public int getBalance(UUID playerUUID) {
        return accounts.getOrDefault(playerUUID, 0);
    }

    // 设置玩家余额
    public void setBalance(UUID playerUUID, int amount) {
        accounts.put(playerUUID, amount);
        this.setDirty(); // 标记数据已更改，确保保存到文件
    }

    // 存款
    public void addBalance(UUID playerUUID, int amount) {
        setBalance(playerUUID, getBalance(playerUUID) + amount);
    }

    // 取款
    public boolean minBalance(UUID playerUUID, int amount) {
        int balance = getBalance(playerUUID);
        if (balance >= amount) {
            setBalance(playerUUID, balance - amount);
            return true;
        }
        return false; // 余额不足
    }

    // 检查是否有足够余额
    public boolean hasEnoughBalance(UUID playerUUID, int amount) {
        return getBalance(playerUUID) >= amount;
    }

    // 增加余额
    public void increaseBalance(UUID playerUUID, int amount) {
        addBalance(playerUUID, amount);
    }

    // 减少余额
    public void decreaseBalance(UUID playerUUID, int amount) {
        if (!minBalance(playerUUID, amount)) {
            throw new IllegalStateException("Insufficient balance for player: " + playerUUID);
        }
    }

    // 存储离线消息
    public void storeOfflineMessage(UUID playerUUID, String message) {
        offlineMessages.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(message);
        this.setDirty(); // 标记数据已更改
    }

    // 获取并清除离线消息
    public List<String> getOfflineMessages(UUID playerUUID) {
        List<String> messages = offlineMessages.remove(playerUUID);
        this.setDirty(); // 标记数据已更改
        return messages != null ? messages : new ArrayList<>();
    }

    // 保存数据到 NBT
    @Override
    public CompoundTag save(CompoundTag tag) {
        // 保存账户数据
        CompoundTag accountsTag = new CompoundTag();
        accounts.forEach((uuid, balance) -> accountsTag.putInt(uuid.toString(), balance));
        tag.put("accounts", accountsTag);

        // 保存离线消息
        CompoundTag offlineMessagesTag = new CompoundTag();
        offlineMessages.forEach((uuid, messages) -> {
            CompoundTag playerTag = new CompoundTag();
            for (int i = 0; i < messages.size(); i++) {
                playerTag.putString("message" + i, messages.get(i));
            }
            offlineMessagesTag.put(uuid.toString(), playerTag);
        });
        tag.put("offlineMessages", offlineMessagesTag);

        return tag;
    }

    // 从 NBT 加载数据
    public static EconomySavedData load(CompoundTag tag) {
        EconomySavedData data = new EconomySavedData();

        // 加载账户数据
        if (tag.contains("accounts")) {
            CompoundTag accountsTag = tag.getCompound("accounts");
            for (String key : accountsTag.getAllKeys()) {
                UUID uuid = UUID.fromString(key);
                int balance = accountsTag.getInt(key);
                data.accounts.put(uuid, balance);
            }
        }

        // 加载离线消息
        if (tag.contains("offlineMessages")) {
            CompoundTag offlineMessagesTag = tag.getCompound("offlineMessages");
            for (String key : offlineMessagesTag.getAllKeys()) {
                UUID uuid = UUID.fromString(key);
                CompoundTag playerTag = offlineMessagesTag.getCompound(key);
                List<String> messages = new ArrayList<>();
                for (String messageKey : playerTag.getAllKeys()) {
                    messages.add(playerTag.getString(messageKey));
                }
                data.offlineMessages.put(uuid, messages);
            }
        }

        return data;
    }

    public static EconomySavedData getInstance(ServerLevel level) {
        // 获取主世界（Overworld）的保存数据
        ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("Overworld is not loaded!");
        }
        return overworld.getDataStorage().computeIfAbsent(EconomySavedData::load, EconomySavedData::new, DATA_NAME);
    }

}
