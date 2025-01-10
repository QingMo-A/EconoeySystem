package com.mo.economy_system.item.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class RecallPotion extends Item {
    public RecallPotion(Properties properties) {
        super(properties.food(new FoodProperties.Builder()
                .alwaysEat() // 无论是否饱食都能喝
                .nutrition(0) // 不提供营养值
                .saturationMod(0.0F) // 无饱和度
                .build()));
    }

    // 设置使用时的动画为饮用
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK; // 使用时显示“饮用”的动画
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            player.startUsingItem(hand); // 启动饮用动画
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            ServerLevel serverLevel = (ServerLevel) level; // 强制转换为 ServerLevel

            // 获取玩家的出生点
            BlockPos respawnPosition = player.getRespawnPosition();

            // 检查出生点是否设置以及是否在同一维度
            if (respawnPosition == null || player.getRespawnDimension() != level.dimension()) {
                respawnPosition = level.getSharedSpawnPos(); // 如果未设置出生点，使用世界默认出生点
            }

            // 传送前粒子效果
            serverLevel.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY(), player.getZ(), 50, 1, 1, 1, 0.1);

            // 传送玩家
            player.teleportTo(respawnPosition.getX() + 0.5, respawnPosition.getY(), respawnPosition.getZ() + 0.5);

            // 传送后粒子效果
            serverLevel.sendParticles(ParticleTypes.PORTAL, respawnPosition.getX(), respawnPosition.getY(), respawnPosition.getZ(), 50, 1, 1, 1, 0.1);

            // 播放音效
            level.playSound(null, respawnPosition, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

            // 消耗物品
            stack.shrink(1);
        }

        return super.finishUsingItem(stack, level, entity);
    }
}
