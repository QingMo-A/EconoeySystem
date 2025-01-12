package com.mo.economy_system.network.packets;

import com.mo.economy_system.territory.TerritoryManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class RemoveTerritoryPacket {
    private final UUID territoryID;

    public RemoveTerritoryPacket(UUID territoryID) {
        this.territoryID = territoryID;
    }

    public static void encode(RemoveTerritoryPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.territoryID);
    }

    public static RemoveTerritoryPacket decode(FriendlyByteBuf buf) {
        return new RemoveTerritoryPacket(buf.readUUID());
    }

    public static void handle(RemoveTerritoryPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            // 获取目标领地
            var territory = TerritoryManager.getTerritoryByID(msg.territoryID);
            if (territory == null) {
                player.sendSystemMessage(Component.literal("§c未找到要删除的领地！"));
                return;
            }

            // 检查权限（只有领地所有者才能删除）
            if (!territory.isOwner(player.getUUID())) {
                player.sendSystemMessage(Component.literal("§c你不是此领地的所有者，无法删除！"));
                return;
            }

            // 从管理器中移除领地
            TerritoryManager.removeTerritory(msg.territoryID);
            player.sendSystemMessage(Component.literal("§a成功删除领地: " + territory.getName()));
        });
        context.setPacketHandled(true);
    }
}
