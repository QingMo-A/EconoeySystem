package com.mo.economy_system.network.packets;

import com.mo.economy_system.territory.InviteManager;
import com.mo.economy_system.territory.Territory;
import com.mo.economy_system.territory.TerritoryManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class InvitePlayerPacket {
    private final UUID territoryID;
    private final String playerName;

    public InvitePlayerPacket(UUID territoryID, String playerName) {
        this.territoryID = territoryID;
        this.playerName = playerName;
    }

    public static void encode(InvitePlayerPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.territoryID);
        buf.writeUtf(msg.playerName);
    }

    public static InvitePlayerPacket decode(FriendlyByteBuf buf) {
        return new InvitePlayerPacket(buf.readUUID(), buf.readUtf(256));
    }

    public static void handle(InvitePlayerPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer inviter = context.getSender();
            if (inviter == null) return;

            // 获取领地信息
            Territory territory = TerritoryManager.getTerritoryByID(msg.territoryID);
            if (territory == null || !territory.isOwner(inviter.getUUID())) {
                inviter.sendSystemMessage(Component.literal("§c你没有权限邀请玩家加入此领地！"));
                return;
            }

            // 获取目标玩家
            ServerPlayer target = inviter.server.getPlayerList().getPlayerByName(msg.playerName);
            if (target == null) {
                inviter.sendSystemMessage(Component.literal("§c玩家 " + msg.playerName + " 不在线！"));
                return;
            }

            // 添加邀请到管理器
            InviteManager.sendInvite(inviter.getUUID(), target.getUUID(), msg.territoryID);

            // 发送邀请消息
            inviter.sendSystemMessage(Component.literal("§a已向玩家 " + msg.playerName + " 发送邀请！"));
            target.sendSystemMessage(Component.literal("§e玩家 " + inviter.getName().getString() + " 邀请你加入领地: " + territory.getName()));
            target.sendSystemMessage(Component.literal("§a输入 /accept_invite 接受 或 /decline_invite 拒绝"));
        });
        context.setPacketHandled(true);
    }
}
