package com.mo.economy_system.network.packets.economy_system;

import com.mo.economy_system.system.economy_system.EconomySavedData;
import com.mo.economy_system.utils.Util_MessageKeys;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class Packet_Transfer {

    private final UUID targetUUID;
    private final int amount;

    public Packet_Transfer(UUID targetUUID, int amount) {
        this.targetUUID = targetUUID;
        this.amount = amount;
    }

    public static void encode(Packet_Transfer msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.targetUUID);
        buf.writeInt(msg.amount);
    }

    public static Packet_Transfer decode(FriendlyByteBuf buf) {
        return new Packet_Transfer(buf.readUUID(), buf.readInt());
    }

    public static void handle(Packet_Transfer msg, Supplier<NetworkEvent.Context> contextSupplier) {
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
                        sender.sendSystemMessage(Component.translatable(Util_MessageKeys.TRANSFER_SUCCESSFULLY_MESSAGE_KEY, msg.amount, target.getName().getString()));
                        target.sendSystemMessage(Component.translatable(Util_MessageKeys.RECEIVE_SUCCESSFULLY_MESSAGE_KEY, sender.getName().getString(), msg.amount));
                    } else {
                        sender.sendSystemMessage(Component.translatable(Util_MessageKeys.TRANSFER_FAILED_MESSAGE_KEY));
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}
