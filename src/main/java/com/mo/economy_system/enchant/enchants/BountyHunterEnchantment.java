package com.mo.economy_system.enchant.enchants;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class BountyHunterEnchantment extends Enchantment {
    public BountyHunterEnchantment(Rarity rarityIn, EnchantmentCategory categoryIn, EquipmentSlot... slots) {
        super(rarityIn, categoryIn, slots);
    }

    /**
     * 附魔最低等级消耗。
     */
    @Override
    public int getMinCost(int level) {
        return 1 + (level - 1) * 10;
    }

    /**
     * 附魔最高等级消耗。
     */
    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 15;
    }

    @Override
    public int getMinLevel() {
        return 1;
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public boolean isTreasureOnly() {
        return false; // 是否只能通过宝箱获取
    }

    @Override
    public boolean isTradeable() {
        return false; // 是否可通过村民交易获得
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        // 如果想禁止和火焰附加兼容，可以：
        if (other instanceof CarefullyEnchantment) return false;
        return super.checkCompatibility(other);
    }

    /**
     * 是否可以给指定物品附魔。
     */
    @Override
    public boolean canEnchant(ItemStack stack) {
        // 如果只允许给剑类和斧头类附魔，可以写：
        // return stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem;
        return super.canEnchant(stack);
    }
}
