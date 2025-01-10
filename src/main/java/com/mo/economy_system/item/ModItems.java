package com.mo.economy_system.item;

import com.mo.economy_system.EconomySystem;
import com.mo.economy_system.item.items.GuitarItem;
import com.mo.economy_system.item.items.RecallPotion;
import com.mo.economy_system.item.items.WormholePotion;
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
public class ModItems {

    // 创建物品的 DeferredRegister
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, EconomySystem.MODID);

    // 注册吉他物品
    public static final RegistryObject<Item> GUITAR = ITEMS.register("guitar",
            () -> new GuitarItem(new Item.Properties()
                    .stacksTo(1) // 堆叠限制为 1
                    .fireResistant() // 可选，防火
            ));

    // 注册虫洞药水
    public static final RegistryObject<Item> WORMHOLE_POTION = ITEMS.register("wormhole_potion",
            () -> new WormholePotion(new Item.Properties()
                    .stacksTo(1) // 堆叠数量为1
                    .fireResistant())); // 可选，防火

    // 注册回忆药水
    public static final RegistryObject<Item> RECALL_POTION = ITEMS.register("recall_potion",
            () -> new RecallPotion(new Item.Properties()
                    .stacksTo(1) // 堆叠数量为1
                    .fireResistant())); // 可选，防火

    // 注册物品到默认创造模式标签（如工具或装饰标签）
    @SubscribeEvent
    public static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) { // 将吉他添加到工具标签中
            event.accept(GUITAR.get());
        } else if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(WORMHOLE_POTION.get());
            event.accept(RECALL_POTION.get());
        }
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus); // 注册物品
    }
}
