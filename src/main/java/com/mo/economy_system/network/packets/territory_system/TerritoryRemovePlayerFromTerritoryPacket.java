package com.mo.economy_system.network.packets.territory_system;

import com.mo.economy_system.system.territory_system.TerritoryManager;
import com.mo.economy_system.utils.MessageKeys;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class TerritoryRemovePlayerFromTerritoryPacket {
    private final UUID territoryID;
    private final UUID playerUUID;

    public TerritoryRemovePlayerFromTerritoryPacket(UUID territoryID, UUID playerUUID) {
        this.territoryID = territoryID;
        this.playerUUID = playerUUID;
    }

    public static void encode(TerritoryRemovePlayerFromTerritoryPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.territoryID);
        buf.writeUUID(msg.playerUUID);
    }

    public static TerritoryRemovePlayerFromTerritoryPacket decode(FriendlyByteBuf buf) {
        return new TerritoryRemovePlayerFromTerritoryPacket(buf.readUUID(), buf.readUUID());
    }

    public static void handle(TerritoryRemovePlayerFromTerritoryPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) return;

            var territory = TerritoryManager.getTerritoryByID(msg.territoryID);
            if (territory == null) {
                sender.sendSystemMessage(Component.translatable(MessageKeys.TERRITORY_NOT_EXIST));
                return;
            }

            if (!territory.isOwner(sender.getUUID())) {
                sender.sendSystemMessage(Component.translatable(MessageKeys.TERRITORY_NO_PERMISSION));
                return;
            }

            // 获取目标玩家
            ServerPlayer target = sender.server.getPlayerList().getPlayer(msg.playerUUID);

            if (target != null) {
                target.sendSystemMessage(Component.translatable(MessageKeys.TERRITORY_PLAYER_KICKED, territory.getName()));
            }

            territory.removeAuthorizedPlayer(msg.playerUUID);
            TerritoryManager.markDirty();

            sender.sendSystemMessage(Component.translatable(MessageKeys.TERRITORY_PLAYER_REMOVED));
        });
        context.setPacketHandled(true);
    }
}
