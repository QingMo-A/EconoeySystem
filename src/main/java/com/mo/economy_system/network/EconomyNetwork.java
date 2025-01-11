package com.mo.economy_system.network;

import com.mo.economy_system.EconomySystem;
import com.mo.economy_system.network.packets.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class EconomyNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(EconomySystem.MODID, "network"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int packetId = 0;

        // 注册数据包
        INSTANCE.registerMessage(packetId++, BalanceRequestPacket.class, BalanceRequestPacket::encode, BalanceRequestPacket::decode, BalanceRequestPacket::handle);
        INSTANCE.registerMessage(packetId++, BalanceResponsePacket.class, BalanceResponsePacket::encode, BalanceResponsePacket::decode, BalanceResponsePacket::handle);
        INSTANCE.registerMessage(packetId++, ModifyBalancePacket.class, ModifyBalancePacket::encode, ModifyBalancePacket::decode, ModifyBalancePacket::handle);
        INSTANCE.registerMessage(packetId++, TransferPacket.class, TransferPacket::encode, TransferPacket::decode, TransferPacket::handle);
        INSTANCE.registerMessage(packetId++, ShopRequestPacket.class, ShopRequestPacket::encode, ShopRequestPacket::decode, ShopRequestPacket::handle);
        INSTANCE.registerMessage(packetId++, ShopResponsePacket.class, ShopResponsePacket::encode, ShopResponsePacket::decode, ShopResponsePacket::handle);
        INSTANCE.registerMessage(packetId++, BuyItemPacket.class, BuyItemPacket::encode, BuyItemPacket::decode, BuyItemPacket::handle);
        INSTANCE.registerMessage(packetId++, ListItemPacket.class, ListItemPacket::encode, ListItemPacket::decode, ListItemPacket::handle);
        INSTANCE.registerMessage(packetId++, MarketRequestPacket.class, MarketRequestPacket::encode, MarketRequestPacket::decode, MarketRequestPacket::handle);
        INSTANCE.registerMessage(packetId++, MarketResponsePacket.class, MarketResponsePacket::encode, MarketResponsePacket::decode, MarketResponsePacket::handle);
        INSTANCE.registerMessage(packetId++, PurchaseItemPacket.class, PurchaseItemPacket::encode, PurchaseItemPacket::decode, PurchaseItemPacket::handle);
        INSTANCE.registerMessage(packetId++, RemoveItemPacket.class, RemoveItemPacket::encode, RemoveItemPacket::decode, RemoveItemPacket::handle);
        INSTANCE.registerMessage(packetId++, TerritoryRequestPacket.class, TerritoryRequestPacket::encode, TerritoryRequestPacket::decode, TerritoryRequestPacket::handle);
        INSTANCE.registerMessage(packetId++, TerritoryResponsePacket.class, TerritoryResponsePacket::encode, TerritoryResponsePacket::decode, TerritoryResponsePacket::handle);
    }
}
