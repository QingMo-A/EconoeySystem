package com.mo.economy_system.network;

import com.mo.economy_system.EconomySystem;
import com.mo.economy_system.network.packets.economy_system.*;
import com.mo.economy_system.network.packets.territory_system.*;
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
        INSTANCE.registerMessage(packetId++, TransferPacket.class, TransferPacket::encode, TransferPacket::decode, TransferPacket::handle);
        INSTANCE.registerMessage(packetId++, ShopRequestPacket.class, ShopRequestPacket::encode, ShopRequestPacket::decode, ShopRequestPacket::handle);
        INSTANCE.registerMessage(packetId++, ShopResponsePacket.class, ShopResponsePacket::encode, ShopResponsePacket::decode, ShopResponsePacket::handle);
        INSTANCE.registerMessage(packetId++, ShopBuyItemPacket.class, ShopBuyItemPacket::encode, ShopBuyItemPacket::decode, ShopBuyItemPacket::handle);
        INSTANCE.registerMessage(packetId++, MarketListItemPacket.class, MarketListItemPacket::encode, MarketListItemPacket::decode, MarketListItemPacket::handle);
        INSTANCE.registerMessage(packetId++, MarketRequestItemPacket.class, MarketRequestItemPacket::encode, MarketRequestItemPacket::decode, MarketRequestItemPacket::handle);
        INSTANCE.registerMessage(packetId++, MarketDataRequestPacket.class, MarketDataRequestPacket::encode, MarketDataRequestPacket::decode, MarketDataRequestPacket::handle);
        INSTANCE.registerMessage(packetId++, MarketDataResponsePacket.class, MarketDataResponsePacket::encode, MarketDataResponsePacket::decode, MarketDataResponsePacket::handle);
        INSTANCE.registerMessage(packetId++, MarketPurchaseItemPacket.class, MarketPurchaseItemPacket::encode, MarketPurchaseItemPacket::decode, MarketPurchaseItemPacket::handle);
        INSTANCE.registerMessage(packetId++, MarketClaimRequestItemPacket.class, MarketClaimRequestItemPacket::encode, MarketClaimRequestItemPacket::decode, MarketClaimRequestItemPacket::handle);
        INSTANCE.registerMessage(packetId++, MarketDeliverItemPacket.class, MarketDeliverItemPacket::encode, MarketDeliverItemPacket::decode, MarketDeliverItemPacket::handle);
        INSTANCE.registerMessage(packetId++, MarketRemoveMarketItemPacket.class, MarketRemoveMarketItemPacket::encode, MarketRemoveMarketItemPacket::decode, MarketRemoveMarketItemPacket::handle);
        INSTANCE.registerMessage(packetId++, MarketRemoveRequestItemPacket.class, MarketRemoveRequestItemPacket::encode, MarketRemoveRequestItemPacket::decode, MarketRemoveRequestItemPacket::handle);
        INSTANCE.registerMessage(packetId++, TerritoryRequestPacket.class, TerritoryRequestPacket::encode, TerritoryRequestPacket::decode, TerritoryRequestPacket::handle);
        INSTANCE.registerMessage(packetId++, TerritoryResponsePacket.class, TerritoryResponsePacket::encode, TerritoryResponsePacket::decode, TerritoryResponsePacket::handle);
        INSTANCE.registerMessage(packetId++, TerritoryTeleportRequestPacket.class, TerritoryTeleportRequestPacket::encode, TerritoryTeleportRequestPacket::decode, TerritoryTeleportRequestPacket::handle);
        INSTANCE.registerMessage(packetId++, TerrirotyInvitePlayerPacket.class, TerrirotyInvitePlayerPacket::encode, TerrirotyInvitePlayerPacket::decode, TerrirotyInvitePlayerPacket::handle);
        INSTANCE.registerMessage(packetId++, TerritoryRemovePacket.class, TerritoryRemovePacket::encode, TerritoryRemovePacket::decode, TerritoryRemovePacket::handle);
        INSTANCE.registerMessage(packetId++, TerritoryRemovePlayerFromTerritoryPacket.class, TerritoryRemovePlayerFromTerritoryPacket::encode, TerritoryRemovePlayerFromTerritoryPacket::decode, TerritoryRemovePlayerFromTerritoryPacket::handle);
    }
}
