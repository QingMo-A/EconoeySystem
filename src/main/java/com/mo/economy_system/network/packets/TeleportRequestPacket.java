package com.mo.economy_system.network.packets;

import com.mo.economy_system.item.ModItems;
import com.mo.economy_system.territory.TerritoryManager;
import com.mo.economy_system.utils.MessageKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class TeleportRequestPacket {

    private final UUID territoryID;

    public TeleportRequestPacket(UUID territoryID) {
        this.territoryID = territoryID;
    }

    public static void encode(TeleportRequestPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.territoryID);
    }

    public static TeleportRequestPacket decode(FriendlyByteBuf buf) {
        return new TeleportRequestPacket(buf.readUUID());
    }

    public static void handle(TeleportRequestPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            var territory = TerritoryManager.getTerritoryByID(msg.territoryID);
            if (territory == null) {
                player.sendSystemMessage(Component.translatable(MessageKeys.TELEPORT_TARGET_NOT_FOUND));
                return;
            }

            if (!territory.isOwner(player.getUUID()) && !territory.hasPermission(player.getUUID())) {
                player.sendSystemMessage(Component.translatable(MessageKeys.TELEPORT_NO_PERMISSION));
                return;
            }

            BlockPos backPoint = territory.getBackpoint();
            if (backPoint == null) {
                player.sendSystemMessage(Component.translatable(MessageKeys.TELEPORT_NO_BACKPOINT));
                return;
            }

            ResourceKey<Level> dimension = territory.getDimension();
            ServerLevel targetLevel = player.server.getLevel(dimension);
            if (targetLevel == null) {
                player.sendSystemMessage(Component.translatable(MessageKeys.TELEPORT_DIMENSION_NOT_FOUND));
                return;
            }

            // 检查是否持有回忆药水
            var inventory = player.getInventory();
            ItemStack potionStack = null;
            for (ItemStack itemStack : inventory.items) {
                if (itemStack.getItem() == ModItems.RECALL_POTION.get()) {
                    potionStack = itemStack;
                    break;
                }
            }

            // 强制加载目标区块
            if (!targetLevel.isLoaded(backPoint)) {
                targetLevel.getChunkSource().addRegionTicket(
                        net.minecraft.server.level.TicketType.POST_TELEPORT,
                        new net.minecraft.world.level.ChunkPos(backPoint),
                        1,
                        player.getId()
                );
            }

            // 传送粒子效果
            targetLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    backPoint.getX() + 0.5, backPoint.getY() + 1, backPoint.getZ() + 0.5,
                    50, 1, 1, 1, 0.1
            );

            if (potionStack != null) {
                // 执行传送
                try {
                    player.teleportTo(
                            targetLevel,
                            backPoint.getX() + 0.5,
                            backPoint.getY() + 1,
                            backPoint.getZ() + 0.5,
                            player.getYRot(),
                            player.getXRot()
                    );

                    // 消耗物品
                    potionStack.shrink(1);

                    // 确保目标区块刷新
                    targetLevel.getChunkSource().updateChunkForced(
                            new net.minecraft.world.level.ChunkPos(backPoint),
                            true
                    );

                    player.sendSystemMessage(Component.translatable(MessageKeys.TELEPORT_SUCCESS, territory.getName()));
                } catch (Exception e) {
                    player.sendSystemMessage(Component.translatable(MessageKeys.TELEPORT_FAILED));
                    e.printStackTrace();
                }
            } else {
                player.sendSystemMessage(Component.translatable(MessageKeys.TELEPORT_NO_POTION));
            }
        });
        context.setPacketHandled(true);
    }

}
