package com.mo.economy_system.territory;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Territory {

    private final UUID territoryID; // 领地 ID
    private final UUID ownerUUID;   // 领主 UUID
    private final String ownerName; // 领主名字
    private final String name;      // 领地名称
    private final int x1, y1, z1, x2, y2, z2; // 领地范围
    private final Set<UUID> authorizedPlayers; // 有权限的玩家 UUID 列表
    private BlockPos backpoint; // 回城点

    public Territory(String name, UUID ownerUUID, String ownerName, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.territoryID = UUID.randomUUID();
        this.name = name;
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        this.authorizedPlayers = new HashSet<>();
        this.backpoint = null; // 默认无回城点
    }

    public UUID getTerritoryID() {
        return territoryID;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getName() {
        return name;
    }

    // 添加 getPos1() 和 getPos2() 方法
    public BlockPos getPos1() {
        return new BlockPos(x1, y1, z1);
    }

    public BlockPos getPos2() {
        return new BlockPos(x2, y2, z2);
    }

    public boolean isWithinBounds(int x, int y, int z) {
        return x >= Math.min(x1, x2) && x <= Math.max(x1, x2) &&
                y >= Math.min(y1, y2) && y <= Math.max(y1, y2) &&
                z >= Math.min(z1, z2) && z <= Math.max(z1, z2);
    }

    public Set<UUID> getAuthorizedPlayers() {
        return authorizedPlayers;
    }

    public void addAuthorizedPlayer(UUID playerUUID) {
        authorizedPlayers.add(playerUUID);
    }

    public void removeAuthorizedPlayer(UUID playerUUID) {
        authorizedPlayers.remove(playerUUID);
    }

    /**
     * 判断玩家是否为领地所有者
     *
     * @param playerUUID 玩家 UUID
     * @return 如果玩家是所有者返回 true，否则返回 false
     */
    public boolean isOwner(UUID playerUUID) {
        return ownerUUID.equals(playerUUID);
    }

    /**
     * 判断玩家是否有权限（不包括所有者）
     *
     * @param playerUUID 玩家 UUID
     * @return 如果玩家有权限（但不是所有者）返回 true，否则返回 false
     */
    public boolean hasPermission(UUID playerUUID) {
        return !isOwner(playerUUID) && authorizedPlayers.contains(playerUUID);
    }

    public BlockPos getBackpoint() {
        return backpoint;
    }

    public void setBackpoint(BlockPos backpoint) {
        this.backpoint = backpoint;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("TerritoryID", territoryID);
        tag.putUUID("OwnerUUID", ownerUUID);
        tag.putString("OwnerName", ownerName);
        tag.putString("Name", name);
        tag.putInt("X1", x1);
        tag.putInt("Y1", y1);
        tag.putInt("Z1", z1);
        tag.putInt("X2", x2);
        tag.putInt("Y2", y2);
        tag.putInt("Z2", z2);

        // 保存有权限的玩家
        ListTag authorizedPlayersTag = new ListTag();
        for (UUID playerUUID : authorizedPlayers) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("PlayerUUID", playerUUID);
            authorizedPlayersTag.add(playerTag);
        }
        tag.put("AuthorizedPlayers", authorizedPlayersTag);

        // 保存回城点
        if (backpoint != null) {
            CompoundTag backpointTag = new CompoundTag();
            backpointTag.putInt("BackX", backpoint.getX());
            backpointTag.putInt("BackY", backpoint.getY());
            backpointTag.putInt("BackZ", backpoint.getZ());
            tag.put("Backpoint", backpointTag);
        }

        return tag;
    }

    public static Territory fromNBT(CompoundTag tag) {
        UUID territoryID = tag.getUUID("TerritoryID");
        UUID ownerUUID = tag.getUUID("OwnerUUID");
        String ownerName = tag.getString("OwnerName");
        String name = tag.getString("Name");
        int x1 = tag.getInt("X1");
        int y1 = tag.getInt("Y1");
        int z1 = tag.getInt("Z1");
        int x2 = tag.getInt("X2");
        int y2 = tag.getInt("Y2");
        int z2 = tag.getInt("Z2");

        Territory territory = new Territory(name, ownerUUID, ownerName, x1, y1, z1, x2, y2, z2);

        // 加载有权限的玩家
        ListTag authorizedPlayersTag = tag.getList("AuthorizedPlayers", Tag.TAG_COMPOUND);
        for (Tag playerTag : authorizedPlayersTag) {
            UUID playerUUID = ((CompoundTag) playerTag).getUUID("PlayerUUID");
            territory.addAuthorizedPlayer(playerUUID);
        }

        // 加载回城点
        if (tag.contains("Backpoint", Tag.TAG_COMPOUND)) {
            CompoundTag backpointTag = tag.getCompound("Backpoint");
            BlockPos backpoint = new BlockPos(backpointTag.getInt("BackX"),
                    backpointTag.getInt("BackY"),
                    backpointTag.getInt("BackZ"));
            territory.setBackpoint(backpoint);
        }

        return territory;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // 同一引用，必定相等
        if (obj == null || getClass() != obj.getClass()) return false; // 类不同，不相等
        Territory that = (Territory) obj; // 类型转换
        return Objects.equals(territoryID, that.territoryID); // 使用领地 ID 判断是否相等
    }

    @Override
    public int hashCode() {
        return Objects.hash(territoryID); // 根据领地 ID 生成哈希值
    }
}
