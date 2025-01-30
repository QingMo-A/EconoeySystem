package com.mo.economy_system.utils;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class PlayerUtils {

    /**
     * 根据玩家 UUID 获取玩家名称
     *
     * @param server    Minecraft 服务端对象
     * @param playerUUID 玩家 UUID
     * @return 玩家名称（如果玩家在线）；否则返回 null
     */
    public static String getPlayerNameByUUID(MinecraftServer server, UUID playerUUID) {
        // 获取玩家列表
        PlayerList playerList = server.getPlayerList();

        // 查找玩家
        ServerPlayer player = playerList.getPlayer(playerUUID);
        if (player != null) {
            return player.getName().getString(); // 返回玩家名称
        }

        return null; // 玩家不在线
    }

    public static boolean isOP(Player player) {
        return player.hasPermissions(2) || player.hasPermissions(3) || player.hasPermissions(4);
    }

    public static boolean isOP(ServerPlayer player) {
        return player.hasPermissions(2) || player.hasPermissions(3) || player.hasPermissions(4);
    }
}
