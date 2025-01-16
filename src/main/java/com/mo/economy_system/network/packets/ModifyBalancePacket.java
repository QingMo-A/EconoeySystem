package com.mo.economy_system.network.packets;

import com.mo.economy_system.system.economy_system.EconomySavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ModifyBalancePacket {
    private final int amount; // 正数表示增加，负数表示减少
    private final boolean set; // 是否直接设置为指定值

    public ModifyBalancePacket(int amount, boolean set) {
        this.amount = amount;
        this.set = set;
    }

    public static void encode(ModifyBalancePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.amount);
        buf.writeBoolean(msg.set);
    }

    public static ModifyBalancePacket decode(FriendlyByteBuf buf) {
        return new ModifyBalancePacket(buf.readInt(), buf.readBoolean());
    }

    public static void handle(ModifyBalancePacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender(); // 获取发送请求的玩家
            if (player != null) {
                ServerLevel serverLevel = player.serverLevel(); // 使用 player.serverLevel() 获取 ServerLevel
                if (serverLevel != null) {
                    EconomySavedData data = EconomySavedData.getInstance(serverLevel);
                    if (msg.set) {
                        data.setBalance(player.getUUID(), msg.amount);
                    } else {
                        if (msg.amount > 0) {
                            data.addBalance(player.getUUID(), msg.amount);
                        } else {
                            data.minBalance(player.getUUID(), -msg.amount);
                        }
                    }
                    player.sendSystemMessage(Component.literal("Balance updated successfully!"));
                }
            }
        });
        context.setPacketHandled(true);
    }
}
