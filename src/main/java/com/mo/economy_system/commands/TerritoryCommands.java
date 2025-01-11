package com.mo.economy_system.commands;

import com.mo.economy_system.territory.Territory;
import com.mo.economy_system.territory.TerritoryManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class TerritoryCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("setBackPoint")
                        .requires(source -> source.hasPermission(2)) // 仅允许管理员或玩家执行
                        .executes(TerritoryCommands::setBackPoint)
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
}

