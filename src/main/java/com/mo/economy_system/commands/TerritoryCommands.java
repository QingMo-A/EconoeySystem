package com.mo.economy_system.commands;

import com.mo.economy_system.territory.InviteManager;
import com.mo.economy_system.territory.Territory;
import com.mo.economy_system.territory.TerritoryManager;
import com.mo.economy_system.utils.MessageKeys;
import com.mo.economy_system.utils.PlayerUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class TerritoryCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("setbackpoint")
                        .requires(source -> source.hasPermission(2)) // 仅允许管理员或玩家执行
                        .executes(TerritoryCommands::setBackPoint)
        );
        dispatcher.register(
                Commands.literal("accept_invite")
                        .executes(context -> handleAccept(context.getSource()))
        );

        dispatcher.register(
                Commands.literal("decline_invite")
                        .executes(context -> handleDecline(context.getSource()))
        );
        dispatcher.register(
                Commands.literal("invite")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer sender = context.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");

                                    Vec3 senderPos = sender.position();
                                    int x = (int) Math.floor(senderPos.x);
                                    int z = (int) Math.floor(senderPos.z);

                                    Territory territory = TerritoryManager.getTerritoryAtIgnoreY(x, z);
                                    if (territory == null || !territory.isOwner(sender.getUUID())) {
                                        sender.sendSystemMessage(Component.translatable(MessageKeys.INVITE_NOT_IN_TERRITORY));
                                        return 0;
                                    }

                                    InviteManager.sendInvite(sender.getUUID(), target.getUUID(), territory.getTerritoryID());
                                    sender.sendSystemMessage(Component.translatable(MessageKeys.INVITE_SENT_TO_PLAYER, target.getName().getString()));
                                    target.sendSystemMessage(Component.translatable(MessageKeys.INVITE_RECEIVED_PLAYER, sender.getName().getString(), territory.getName()));
                                    return 1;
                                }))
        );

    }

    private static int setBackPoint(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable(MessageKeys.COMMAND_PLAYER_ONLY));
            return 0;
        }

        // 获取玩家位置
        Vec3 playerPos = player.position();
        int x = (int) Math.floor(playerPos.x);
        int y = (int) Math.floor(playerPos.y);
        int z = (int) Math.floor(playerPos.z);

        // 检查玩家是否在自己的领地
        Territory territory = TerritoryManager.getTerritoryAtIgnoreY(x, z);
        if (territory == null || !territory.isOwner(player.getUUID())) {
            source.sendFailure(Component.translatable(MessageKeys.TERRITORY_SETBACKPOINT_NO_PERMISSION));
            return 0;
        }

        // 设置回城点
        territory.setBackpoint(new BlockPos(x, y, z));
        TerritoryManager.markDirty(); // 如果有保存机制，标记数据需要保存

        source.sendSuccess(() -> Component.translatable(MessageKeys.TERRITORY_SETBACKPOINT_SUCCESS, x, y, z), true);
        return 1;
    }

    private static int handleAccept(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
                source.sendFailure(Component.translatable(MessageKeys.COMMAND_PLAYER_ONLY));
            return 0;
        }

        InviteManager.Invite invite = InviteManager.getInvite(player.getUUID());
        if (invite == null) {
            source.sendFailure(Component.translatable(MessageKeys.INVITE_NO_PENDING));
            return 0;
        }

        Territory territory = TerritoryManager.getTerritoryByID(invite.getTerritoryID());
        if (territory == null) {
            source.sendFailure(Component.translatable(MessageKeys.INVITE_TARGET_NOT_FOUND));
            InviteManager.removeInvite(player.getUUID());
            return 0;
        }

        territory.addAuthorizedPlayer(player.getUUID(), PlayerUtils.getPlayerNameByUUID(source.getServer(), player.getUUID()));
        TerritoryManager.markDirty();
        InviteManager.removeInvite(player.getUUID());

        source.sendSuccess(() -> Component.translatable(MessageKeys.INVITE_ACCEPTED, territory.getName()), true);
        return 1;
    }

    private static int handleDecline(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable(MessageKeys.COMMAND_PLAYER_ONLY));
            return 0;
        }

        InviteManager.Invite invite = InviteManager.getInvite(player.getUUID());
        if (invite == null) {
            source.sendFailure(Component.translatable(MessageKeys.INVITE_DECLINE_NO_PENDING));
            return 0;
        }

        InviteManager.removeInvite(player.getUUID());
        source.sendSuccess(() -> Component.translatable(MessageKeys.INVITE_DECLINED), true);
        return 1;
    }
}

