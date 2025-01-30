package com.mo.economy_system.network.packets.economy_system;

import com.mo.economy_system.system.economy_system.market.MarketItem;
import com.mo.economy_system.screen.economy_system.MarketScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MarketDataResponsePacket {
    private final List<MarketItem> items;

    public MarketDataResponsePacket(List<MarketItem> items) {
        this.items = items;
    }

    public static void encode(MarketDataResponsePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.items.size());
        for (MarketItem item : msg.items) {
            buf.writeNbt(item.toNBT());
        }
    }

    public static MarketDataResponsePacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<MarketItem> items = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            items.add(MarketItem.fromNBT(buf.readNbt()));
        }
        return new MarketDataResponsePacket(items);
    }

    public static void handle(MarketDataResponsePacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 获取当前屏幕实例并更新市场商品
            if (Minecraft.getInstance().screen instanceof MarketScreen marketScreen) {
                marketScreen.updateMarketItems(msg.items);
            }
        });
        context.setPacketHandled(true);
    }
}

