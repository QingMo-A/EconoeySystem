package com.mo.economy_system.network.packets.territory_system;

import com.mo.economy_system.network.EconomyNetwork;
import com.mo.economy_system.system.territory_system.TerritoryManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class TerritoryRequestPacket {
    public TerritoryRequestPacket() {}

    public static void encode(TerritoryRequestPacket msg, FriendlyByteBuf buf) {}

    public static TerritoryRequestPacket decode(FriendlyByteBuf buf) {
        return new TerritoryRequestPacket();
    }

    public static void handle(TerritoryRequestPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 获取当前玩家
            var player = context.getSender();
            if (player != null) {
                var ownedTerritories = TerritoryManager.getTerritoriesByOwner(player.getUUID());
                var authorizedTerritories = TerritoryManager.getAuthorizedTerritories(player.getUUID());

                // 返回领地数据
                EconomyNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new TerritoryResponsePacket(ownedTerritories, authorizedTerritories));
            }
        });
        context.setPacketHandled(true);
    }
}
