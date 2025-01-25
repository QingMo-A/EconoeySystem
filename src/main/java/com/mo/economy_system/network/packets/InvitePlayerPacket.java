package com.mo.economy_system.network.packets;

import com.mo.economy_system.territory.InviteManager;
import com.mo.economy_system.territory.PlayerInfo;
import com.mo.economy_system.territory.Territory;
import com.mo.economy_system.territory.TerritoryManager;
import com.mo.economy_system.utils.MessageKeys;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
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
                inviter.sendSystemMessage(Component.translatable(MessageKeys.INVITE_NO_PERMISSION));
                return;
            }

            // 获取目标玩家
            ServerPlayer target = inviter.server.getPlayerList().getPlayerByName(msg.playerName);
            if (target == null) {
                inviter.sendSystemMessage(Component.translatable(MessageKeys.INVITE_PLAYER_OFFLINE, msg.playerName));
                return;
            }

            if (!(target.getUUID().equals(territory.getOwnerUUID()))){
                System.out.println(target.getUUID() + "\n" + territory.getOwnerUUID());
                for (PlayerInfo playerInfo : territory.getAuthorizedPlayers()) {
                    if (playerInfo.getUuid().equals(target.getUUID())) {
                        inviter.sendSystemMessage(Component.translatable(MessageKeys.INVITE_ALREADY_MEMBER, msg.playerName));
                        return;
                    }
                }
                // 添加邀请到管理器
                InviteManager.sendInvite(inviter.getUUID(), target.getUUID(), msg.territoryID);

                // 发送邀请消息
                inviter.sendSystemMessage(Component.translatable(MessageKeys.INVITE_SENT, msg.playerName));

                Component acceptButton = Component.translatable(MessageKeys.INVITE_ACCEPT_BUTTON)
                        .withStyle(style -> style
                                .withColor(0x55FF55)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/accept_invite"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("§eClick to accept!"))));

                Component declinedButton = Component.translatable(MessageKeys.INVITE_DECLINE_BUTTON)
                        .withStyle(style -> style
                                .withColor(0x55FF55)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/decline_invite"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("§eClick to decline!"))));

                target.sendSystemMessage(Component.translatable(MessageKeys.INVITE_RECEIVED, inviter.getName().getString(), territory.getName()).append(acceptButton).append(declinedButton));
                target.sendSystemMessage(Component.translatable(MessageKeys.INVITE_INSTRUCTIONS));
            } else {
                inviter.sendSystemMessage(Component.translatable(MessageKeys.INVITE_SELF_ERROR));
            }

        });
        context.setPacketHandled(true);
    }
}
