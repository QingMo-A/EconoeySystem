package com.mo.economy_system.commands;

import com.mo.economy_system.territory.InviteManager;
import com.mo.economy_system.territory.Territory;
import com.mo.economy_system.territory.TerritoryManager;
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
                Commands.literal("accept")
                        .executes(context -> handleAccept(context.getSource()))
        );

        dispatcher.register(
                Commands.literal("decline")
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

                                    Territory territory = TerritoryManager.getTerritoryAtIgnoringY(x, z);
                                    if (territory == null || !territory.isOwner(sender.getUUID())) {
                                        sender.sendSystemMessage(Component.literal("你不在自己的领地范围内，无法发送邀请！"));
                                        return 0;
                                    }

                                    InviteManager.sendInvite(sender.getUUID(), target.getUUID(), territory.getTerritoryID());
                                    sender.sendSystemMessage(Component.literal("邀请已发送给 " + target.getName().getString()));
                                    target.sendSystemMessage(Component.literal(sender.getName().getString() + " 邀请你加入领地: " + territory.getName()));
                                    return 1;
                                }))
        );

    }

    private static int setBackPoint(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("此指令仅能由玩家执行！"));
            return 0;
        }

        // 获取玩家位置
        Vec3 playerPos = player.position();
        int x = (int) Math.floor(playerPos.x);
        int y = (int) Math.floor(playerPos.y);
        int z = (int) Math.floor(playerPos.z);

        // 检查玩家是否在自己的领地
        Territory territory = TerritoryManager.getTerritoryAtIgnoringY(x, z);
        if (territory == null || !territory.isOwner(player.getUUID())) {
            source.sendFailure(Component.literal("你不在自己的领地范围内，无法设置回城点！"));
            return 0;
        }

        // 设置回城点
        territory.setBackpoint(new BlockPos(x, y, z));
        TerritoryManager.markDirty(); // 如果有保存机制，标记数据需要保存

        source.sendSuccess(() -> Component.literal("成功设置回城点为: " + x + ", " + y + ", " + z), true);
        return 1;
    }

    private static int handleAccept(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("此指令仅能由玩家执行！"));
            return 0;
        }

        InviteManager.Invite invite = InviteManager.getInvite(player.getUUID());
        if (invite == null) {
            source.sendFailure(Component.literal("没有待接受的领地邀请！"));
            return 0;
        }

        Territory territory = TerritoryManager.getTerritoryByID(invite.getTerritoryID());
        if (territory == null) {
            source.sendFailure(Component.literal("目标领地不存在！"));
            InviteManager.removeInvite(player.getUUID());
            return 0;
        }

        territory.addAuthorizedPlayer(player.getUUID());
        TerritoryManager.markDirty();
        InviteManager.removeInvite(player.getUUID());

        source.sendSuccess(() -> Component.literal("成功接受邀请，现在你有权进入领地: " + territory.getName()), true);
        return 1;
    }

    private static int handleDecline(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("此指令仅能由玩家执行！"));
            return 0;
        }

        InviteManager.Invite invite = InviteManager.getInvite(player.getUUID());
        if (invite == null) {
            source.sendFailure(Component.literal("没有待拒绝的领地邀请！"));
            return 0;
        }

        InviteManager.removeInvite(player.getUUID());
        source.sendSuccess(() -> Component.literal("已拒绝邀请。"), true);
        return 1;
    }
}

