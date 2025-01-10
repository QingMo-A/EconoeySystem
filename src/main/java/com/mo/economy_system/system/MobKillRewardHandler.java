package com.mo.economy_system.system;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.Random;

public class MobKillRewardHandler {
    private static final Random RANDOM = new Random();

    public void rewardPlayer(ServerPlayer player, int reward) {
        // 获取 EconomySavedData 实例
        EconomySavedData savedData = EconomySavedData.getInstance(player.serverLevel());

        // 给玩家存入奖励
        savedData.deposit(player.getUUID(), reward);

        // 通知玩家奖励信息
        player.sendSystemMessage(Component.literal("You earned " + reward + " coins for defeating a mob!"));
    }
}
