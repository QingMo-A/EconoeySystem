package com.mo.economy_system.network.packets;

import com.mo.economy_system.system.EconomySavedData;
import com.mo.economy_system.utils.MessageKeys;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class TransferPacket {

    private final UUID targetUUID;
    private final int amount;

    public TransferPacket(UUID targetUUID, int amount) {
        this.targetUUID = targetUUID;
        this.amount = amount;
    }

    public static void encode(TransferPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.targetUUID);
        buf.writeInt(msg.amount);
    }

    public static TransferPacket decode(FriendlyByteBuf buf) {
        return new TransferPacket(buf.readUUID(), buf.readInt());
    }

    public static void handle(TransferPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender(); // 获取发送请求的玩家
            if (sender != null) {
                ServerLevel serverLevel = sender.serverLevel(); // 使用 sender.serverLevel() 获取 ServerLevel
                if (serverLevel != null) {
                    EconomySavedData data = EconomySavedData.getInstance(serverLevel);
                    Player target = serverLevel.getPlayerByUUID(msg.targetUUID); // 根据 UUID 获取目标玩家

                    if (target != null && data.minBalance(sender.getUUID(), msg.amount) && target.getUUID() != sender.getUUID()) {
                        data.addBalance(target.getUUID(), msg.amount);
                        sender.sendSystemMessage(Component.translatable(MessageKeys.TRANSFER_SUCCESSFULLY_MESSAGE_KEY, msg.amount, target.getName().getString()));
                        target.sendSystemMessage(Component.translatable(MessageKeys.RECEIVE_SUCCESSFULLY_MESSAGE_KEY, sender.getName().getString(), msg.amount));
                    } else {
                        sender.sendSystemMessage(Component.translatable(MessageKeys.TRANSFER_FAILED_MESSAGE_KEY));
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}
