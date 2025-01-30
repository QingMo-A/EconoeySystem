package com.mo.economy_system.system.territory_system;

import com.mo.economy_system.utils.ServerMessageUtil;
import net.minecraft.server.level.ServerLevel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TerritoryManager {

    private static TerritorySavedData savedData;
    private static boolean initialized = false;

    private static final Map<UUID, Territory> territoryByID = Collections.synchronizedMap(new HashMap<>());
    private static final Map<UUID, List<Territory>> territoriesByOwner = new ConcurrentHashMap<>();

    // 初始化一个有限边界的四叉树
    private static QuadTree quadTree = new QuadTree(0, new Bounds(-30000, -30000, 60000, 60000));

    // 自动保存间隔（60秒）
    private static final long AUTO_SAVE_INTERVAL = 60000L;
    private static long lastSaveTime = System.currentTimeMillis();

    // 初始化领地管理器
    public static void initialize(ServerLevel level) {
        if (initialized) return;

        quadTree.clear(); // 清空四叉树
        savedData = TerritorySavedData.getInstance(level);

        // ServerMessageUtil.log("Initializing TerritoryManager...");

        for (Territory territory : savedData.getAllTerritories()) {
            addTerritory(territory);
        }

        initialized = true;
        // ServerMessageUtil.log("TerritoryManager initialized with " + territoryByID.size() + " territories.");
    }

    // 添加领地
    public static void addTerritory(Territory territory) {
        if (!territoryByID.containsKey(territory.getTerritoryID())) {
            territoryByID.put(territory.getTerritoryID(), territory);
            territoriesByOwner.computeIfAbsent(territory.getOwnerUUID(), k -> new ArrayList<>()).add(territory);

            // 动态扩展四叉树边界
            if (!quadTree.getBounds().contains(territory.getBounds())) {
                expandQuadTreeBounds(territory.getBounds());
            }

            quadTree.insert(territory); // 插入四叉树
            if (savedData != null) {
                savedData.addTerritory(territory);
            }

            autoSave(); // 自动保存
            // ServerMessageUtil.log("Territory added: " + territory.getName());
        }
    }

    // 移除领地
    public static void removeTerritory(UUID territoryID) {
        Territory territory = territoryByID.remove(territoryID);
        if (territory != null) {
            territoriesByOwner.getOrDefault(territory.getOwnerUUID(), new ArrayList<>()).remove(territory);

            quadTree.remove(territory); // 从四叉树中移除
            if (savedData != null) {
                savedData.removeTerritory(territoryID);
            }

            autoSave(); // 自动保存
            // ServerMessageUtil.log("Territory removed: " + territory.getName());
        }
    }

    // 查询指定位置的领地（X 和 Z 轴）
    public static Territory getTerritoryAtIgnoreY(int x, int z) {
        List<Territory> candidates = quadTree.query(x, z);
        return candidates.stream()
                .filter(territory -> territory.isWithinBoundsIgnoreY(x, z))
                .findFirst()
                .orElse(null);
    }

    // 获取玩家拥有的所有领地
    public static List<Territory> getTerritoriesByOwner(UUID ownerUUID) {
        return territoriesByOwner.getOrDefault(ownerUUID, new ArrayList<>());
    }

    // 获取玩家有权限的领地（排除自己拥有的领地）
    public static List<Territory> getAuthorizedTerritories(UUID playerUUID) {
        List<Territory> authorizedTerritories = new ArrayList<>();
        synchronized (territoryByID) {
            for (Territory territory : territoryByID.values()) {
                if (!territory.isOwner(playerUUID) && territory.hasPermission(playerUUID)) {
                    authorizedTerritories.add(territory);
                }
            }
        }
        return authorizedTerritories;
    }

    // 根据 ID 获取领地
    public static Territory getTerritoryByID(UUID territoryID) {
        return territoryByID.get(territoryID);
    }

    // 获取所有领地
    public static List<Territory> getAllTerritories() {
        return new ArrayList<>(territoryByID.values());
    }

    // 检查玩家是否有权限
    public static boolean isPlayerAuthorized(UUID territoryID, UUID playerUUID) {
        Territory territory = getTerritoryByID(territoryID);
        return territory != null && territory.hasPermission(playerUUID);
    }

    // 扩展四叉树边界
    private static void expandQuadTreeBounds(Bounds newBounds) {
        Bounds currentBounds = quadTree.getBounds();
        int newMinX = Math.min(currentBounds.x, newBounds.x);
        int newMinZ = Math.min(currentBounds.z, newBounds.z);
        int newMaxX = Math.max(currentBounds.x + currentBounds.width, newBounds.x + newBounds.width);
        int newMaxZ = Math.max(currentBounds.z + currentBounds.height, newBounds.z + newBounds.height);

        Bounds expandedBounds = new Bounds(newMinX, newMinZ, newMaxX - newMinX, newMaxZ - newMinZ);

        QuadTree newQuadTree = new QuadTree(0, expandedBounds);
        synchronized (territoryByID) {
            for (Territory territory : territoryByID.values()) {
                newQuadTree.insert(territory);
            }
        }
        quadTree.clear();
        quadTree.copyFrom(newQuadTree);
        // ServerMessageUtil.log("QuadTree bounds expanded: " + expandedBounds);
    }

    // 自动保存
    private static void autoSave() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSaveTime >= AUTO_SAVE_INTERVAL) {
            markDirty();
            lastSaveTime = currentTime;
            // ServerMessageUtil.log("Territory data saved.");
        }
    }

    public static void markDirty() {
        if (savedData != null) {
            savedData.setDirty();
        }
    }

    // 清空领地管理器
    public static void reset() {
        savedData = null;
        initialized = false;
        territoryByID.clear();
        territoriesByOwner.clear();
        quadTree.clear();
        ServerMessageUtil.log("TerritoryManager has been reset.");
    }
}
