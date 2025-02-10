package com.mo.economy_system.enchant;

import com.mo.economy_system.EconomySystem;
import com.mo.economy_system.enchant.enchants.BountyHunterEnchantment;
import com.mo.economy_system.enchant.enchants.CarefullyEnchantment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = EconomySystem.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EconomySystem_Enchants {
    // 创建 DeferredRegister
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, EconomySystem.MODID);

    // 注册附魔
    public static final RegistryObject<Enchantment> CAREFULLY = ENCHANTMENTS.register("carefully",
            () -> new CarefullyEnchantment(
                    Enchantment.Rarity.UNCOMMON,     // 稀有度
                    EnchantmentCategory.WEAPON,      // 分类，比如武器、盔甲、弓等
                    // 作用到哪些部位
                    EquipmentSlot.MAINHAND
            )
    );

    public static final RegistryObject<Enchantment> BOUNTY_HUNTER = ENCHANTMENTS.register("bounty_hunter",
            () -> new BountyHunterEnchantment(
                    Enchantment.Rarity.UNCOMMON,     // 稀有度
                    EnchantmentCategory.WEAPON,      // 分类，比如武器、盔甲、弓等
                    // 作用到哪些部位
                    EquipmentSlot.MAINHAND
            )
    );

    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus); // 注册附魔
    }
}
