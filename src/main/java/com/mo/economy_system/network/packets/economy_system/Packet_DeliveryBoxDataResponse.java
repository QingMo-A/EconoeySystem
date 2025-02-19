package com.mo.economy_system.network.packets.economy_system;

import com.mo.economy_system.core.economy_system.delivery_box.DeliveryItem;
import com.mo.economy_system.screen.economy_system.deliver_box.Screen_DeliveryBox;
import com.mo.economy_system.screen.economy_system.market.Screen_Market;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Packet_DeliveryBoxDataResponse {
    private final List<DeliveryItem> deliveryItems;

    // 构造函数
    public Packet_DeliveryBoxDataResponse(List<DeliveryItem> deliveryItems) {
        this.deliveryItems = deliveryItems;
    }

    // 编码数据包
    public static void encode(Packet_DeliveryBoxDataResponse msg, FriendlyByteBuf buf) {
        // 先写物品数量
        buf.writeInt(msg.deliveryItems.size());
        for (DeliveryItem item : msg.deliveryItems) {
            buf.writeNbt(item.toNBT());
        }
    }

    // 解码数据包
    public static Packet_DeliveryBoxDataResponse decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<DeliveryItem> items = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            items.add(DeliveryItem.fromNBT(buf.readNbt()));
        }
        return new Packet_DeliveryBoxDataResponse(items);
    }

    // 处理响应包
    public static void handle(Packet_DeliveryBoxDataResponse msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 获取当前屏幕实例并更新市场商品
            if (Minecraft.getInstance().screen instanceof Screen_DeliveryBox screenDeliveryBox) {
                screenDeliveryBox.updateDeliveryBoxItems(msg.deliveryItems);
            }
        });
        context.setPacketHandled(true);  // 标记数据包已处理
    }
}
