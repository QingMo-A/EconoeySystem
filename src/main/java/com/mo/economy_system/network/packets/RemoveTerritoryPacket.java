package com.mo.economy_system.network.packets;

import com.mo.economy_system.territory.TerritoryManager;
import com.mo.economy_system.utils.MessageKeys;
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
                player.sendSystemMessage(Component.translatable(MessageKeys.TERRITORY_NOT_FOUND));
                return;
            }

            // 检查权限（只有领地所有者才能删除）
            if (!territory.isOwner(player.getUUID())) {
                player.sendSystemMessage(Component.translatable(MessageKeys.TERRITORY_NO_OWNER_PERMISSION));
                return;
            }

            // 从管理器中移除领地
            TerritoryManager.removeTerritory(msg.territoryID);
            player.sendSystemMessage(Component.translatable(MessageKeys.TERRITORY_REMOVE_SUCCESS));
        });
        context.setPacketHandled(true);
    }
}
