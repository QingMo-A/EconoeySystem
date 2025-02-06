package com.mo.economy_system.network;

import com.mo.economy_system.EconomySystem;
import com.mo.economy_system.network.packets.check_system.*;
import com.mo.economy_system.network.packets.economy_system.*;
import com.mo.economy_system.network.packets.economy_system.demand_order.MarketClaimRequestItemPacket;
import com.mo.economy_system.network.packets.economy_system.demand_order.MarketDeliverItemPacket;
import com.mo.economy_system.network.packets.economy_system.demand_order.MarketRemoveRequestItemPacket;
import com.mo.economy_system.network.packets.economy_system.demand_order.CreateDemandOrderPacket;
import com.mo.economy_system.network.packets.economy_system.sales_order.CreateSalesOrderPacket;
import com.mo.economy_system.network.packets.economy_system.sales_order.MarketPurchaseItemPacket;
import com.mo.economy_system.network.packets.economy_system.sales_order.MarketRemoveMarketItemPacket;
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
        INSTANCE.registerMessage(packetId++, CreateSalesOrderPacket.class, CreateSalesOrderPacket::encode, CreateSalesOrderPacket::decode, CreateSalesOrderPacket::handle);
        INSTANCE.registerMessage(packetId++, CreateDemandOrderPacket.class, CreateDemandOrderPacket::encode, CreateDemandOrderPacket::decode, CreateDemandOrderPacket::handle);
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
        INSTANCE.registerMessage(packetId++, CheckPacket.class, CheckPacket::encode, CheckPacket::decode, CheckPacket::handle);
        INSTANCE.registerMessage(packetId++, CheckResultRequestPacket.class, CheckResultRequestPacket::encode, CheckResultRequestPacket::decode, CheckResultRequestPacket::handle);
        INSTANCE.registerMessage(packetId++, CheckResultResponsePacket.class, CheckResultResponsePacket::encode, CheckResultResponsePacket::decode, CheckResultResponsePacket::handle);
        INSTANCE.registerMessage(packetId++, GetPacket.class, GetPacket::encode, GetPacket::decode, GetPacket::handle);
        INSTANCE.registerMessage(packetId++, GetResultRequestPacket.class, GetResultRequestPacket::encode, GetResultRequestPacket::decode, GetResultRequestPacket::handle);
        INSTANCE.registerMessage(packetId++, GetResultResponsePacket.class, GetResultResponsePacket::encode, GetResultResponsePacket::decode, GetResultResponsePacket::handle);
        INSTANCE.registerMessage(packetId++, ChunkPacket.class, ChunkPacket::encode, ChunkPacket::decode, ChunkPacket::handle);
        INSTANCE.registerMessage(packetId++, ChunkResponsePacket.class, ChunkResponsePacket::encode, ChunkResponsePacket::decode, ChunkResponsePacket::handle);
    }
}
