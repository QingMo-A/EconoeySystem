package com.mo.economy_system.network.packets;

import com.mo.economy_system.territory.TerritoryManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class RemovePlayerFromTerritoryPacket {
    private final UUID territoryID;
    private final UUID playerUUID;

    public RemovePlayerFromTerritoryPacket(UUID territoryID, UUID playerUUID) {
        this.territoryID = territoryID;
        this.playerUUID = playerUUID;
    }

    public static void encode(RemovePlayerFromTerritoryPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.territoryID);
        buf.writeUUID(msg.playerUUID);
    }

    public static RemovePlayerFromTerritoryPacket decode(FriendlyByteBuf buf) {
        return new RemovePlayerFromTerritoryPacket(buf.readUUID(), buf.readUUID());
    }

    public static void handle(RemovePlayerFromTerritoryPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) return;

            var territory = TerritoryManager.getTerritoryByID(msg.territoryID);
            if (territory == null) {
                sender.sendSystemMessage(Component.literal("领地不存在！"));
                return;
            }

            if (!territory.isOwner(sender.getUUID())) {
                sender.sendSystemMessage(Component.literal("你无权管理此领地！"));
                return;
            }

            // 获取目标玩家
            ServerPlayer target = sender.server.getPlayerList().getPlayer(msg.playerUUID);

            if (target != null) {
                target.sendSystemMessage(Component.literal("你被从领地 " + territory.getName() + " 中踢出"));
            }

            territory.removeAuthorizedPlayer(msg.playerUUID);
            TerritoryManager.markDirty();

            sender.sendSystemMessage(Component.literal("成功将玩家移出领地！"));
        });
        context.setPacketHandled(true);
    }
}
