package com.mo.economy_system.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
public class BedBlockMixin {

    // 注入 BedBlock 的 use 方法，绕过白天和怪物限制
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void allowSleeping(BlockState blockState, Level level, BlockPos blockPos, Player player,
                               InteractionHand interactionHand, BlockHitResult blockHitResult,
                               CallbackInfoReturnable<InteractionResult> cir) {
        if (!level.isClientSide) {
            // 让玩家进入睡眠状态
            player.setSleepingPos(blockPos); // 设定玩家睡觉位置
            player.startSleeping(blockPos);

            // 重置玩家幻翼生成倒计时
            CompoundTag persistentData = player.getPersistentData();
            persistentData.putLong("TimeSinceRest", 0); // 重置休息时间
            // 阻止原有逻辑（如跳过夜晚的处理）
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
