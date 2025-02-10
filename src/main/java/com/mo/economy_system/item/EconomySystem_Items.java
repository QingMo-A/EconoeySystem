package com.mo.economy_system.item;

import com.mo.economy_system.EconomySystem;
import com.mo.economy_system.item.items.Item_ClaimWand;
import com.mo.economy_system.item.items.Item_Guitar;
import com.mo.economy_system.item.items.Potion_Recall;
import com.mo.economy_system.item.items.Potion_Wormhole;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = EconomySystem.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EconomySystem_Items {

    // 创建物品的 DeferredRegister
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, EconomySystem.MODID);

    // 注册吉他物品
    public static final RegistryObject<Item> GUITAR = ITEMS.register("guitar",
            () -> new Item_Guitar(new Item.Properties()
                    .stacksTo(1) // 堆叠限制为 1
                    .fireResistant() // 可选，防火
            ));

    // 注册虫洞药水
    public static final RegistryObject<Item> WORMHOLE_POTION = ITEMS.register("wormhole_potion",
            () -> new Potion_Wormhole(new Item.Properties()
                    .stacksTo(1) // 堆叠数量为1
                    .fireResistant())); // 可选，防火

    // 注册回忆药水
    public static final RegistryObject<Item> RECALL_POTION = ITEMS.register("recall_potion",
            () -> new Potion_Recall(new Item.Properties()
                    .stacksTo(1) // 堆叠数量为1
                    .fireResistant())); // 可选，防火

    // 注册圈地杖
    public static final RegistryObject<Item> CLAIM_WAND = ITEMS.register("claim_wand",
            () -> new Item_ClaimWand(new Item.Properties()
                    .stacksTo(1) // 限制每堆只能有一个
            ));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus); // 注册物品
    }
}
