package com.mo.economy_system.territory;

import net.minecraft.server.level.ServerLevel;

import java.util.*;

public class TerritoryManager {

    private static TerritorySavedData savedData;
    private static boolean initialized = false; // 避免重复初始化

    private static final Map<UUID, Territory> territoryByID = new HashMap<>();
    private static final Map<UUID, List<Territory>> territoriesByOwner = new HashMap<>();

    // 初始化领地管理器
    public static void initialize(ServerLevel level) {
        if (initialized) return; // 避免重复初始化
        savedData = TerritorySavedData.getInstance(level);
        for (Territory territory : savedData.getAllTerritories()) {
            addTerritory(territory);
        }
        initialized = true;
    }

    // 添加领地
    public static void addTerritory(Territory territory) {
        if (!territoryByID.containsKey(territory.getTerritoryID())) { // 防止重复添加
            territoryByID.put(territory.getTerritoryID(), territory);
            territoriesByOwner.computeIfAbsent(territory.getOwnerUUID(), k -> new ArrayList<>()).add(territory);
            if (savedData != null) {
                savedData.addTerritory(territory);
            }
        }
    }

    // 移除领地
    public static void removeTerritory(UUID territoryID) {
        Territory territory = territoryByID.remove(territoryID);
        if (territory != null) {
            territoriesByOwner.getOrDefault(territory.getOwnerUUID(), new ArrayList<>()).remove(territory);
            if (savedData != null) {
                savedData.removeTerritory(territoryID);
            }
        }
    }

    // 获取指定位置的领地
    public static Territory getTerritoryAt(int x, int y, int z) {
        for (Territory territory : territoryByID.values()) {
            if (territory.isWithinBounds(x, y, z)) {
                return territory;
            }
        }
        return null;
    }

    // 根据领地 ID 获取领地
    public static Territory getTerritoryByID(UUID territoryID) {
        return territoryByID.get(territoryID);
    }

    // 获取玩家拥有的领地
    public static List<Territory> getTerritoriesByOwner(UUID ownerUUID) {
        return territoriesByOwner.getOrDefault(ownerUUID, new ArrayList<>());
    }

    // 添加授权玩家
    public static void addAuthorizedPlayer(UUID territoryID, UUID playerUUID) {
        Territory territory = getTerritoryByID(territoryID);
        if (territory != null) {
            territory.addAuthorizedPlayer(playerUUID);
            if (savedData != null) {
                savedData.setDirty();
            }
        }
    }

    // 获取玩家有权限的领地（排除自己拥有的领地）
    public static List<Territory> getAuthorizedTerritories(UUID playerUUID) {
        List<Territory> authorizedTerritories = new ArrayList<>();
        for (Territory territory : territoryByID.values()) {
            if (!territory.isOwner(playerUUID) && territory.hasPermission(playerUUID)) { // 排除所有者
                authorizedTerritories.add(territory);
            }
        }
        return authorizedTerritories;
    }

    // 移除授权玩家
    public static void removeAuthorizedPlayer(UUID territoryID, UUID playerUUID) {
        Territory territory = getTerritoryByID(territoryID);
        if (territory != null) {
            territory.removeAuthorizedPlayer(playerUUID);
            if (savedData != null) {
                savedData.setDirty();
            }
        }
    }

    // 检查玩家是否有权限
    public static boolean isPlayerAuthorized(UUID territoryID, UUID playerUUID) {
        Territory territory = getTerritoryByID(territoryID);
        return territory != null && territory.hasPermission(playerUUID);
    }
}