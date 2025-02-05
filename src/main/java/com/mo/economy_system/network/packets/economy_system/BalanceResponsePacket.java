package com.mo.economy_system.network.packets.economy_system;

import com.mo.economy_system.screen.HomeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

public class BalanceResponsePacket {
    private final int balance;
    private final List<Map.Entry<String, Integer>> accounts; // 新增字段

    public BalanceResponsePacket(int balance, List<Map.Entry<String, Integer>> accounts) {
        this.balance = balance;
        this.accounts = accounts;
    }

    // BalanceResponsePacket.java
    public static void encode(BalanceResponsePacket msg, FriendlyByteBuf buf) {
        // 正确顺序：先写 balance，再写账户数据
        buf.writeInt(msg.balance);
        buf.writeInt(msg.accounts.size());
        for (Map.Entry<String, Integer> entry : msg.accounts) {
            buf.writeUtf(entry.getKey());
            buf.writeInt(entry.getValue());
        }
    }

    public static BalanceResponsePacket decode(FriendlyByteBuf buf) {
        // 正确顺序：先读 balance，再读账户数据
        int balance = buf.readInt(); // 先读取 balance
        int size = buf.readInt();    // 再读取账户数量

        List<Map.Entry<String, Integer>> accounts = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String name = buf.readUtf();
            int amount = buf.readInt();
            accounts.add(new AbstractMap.SimpleEntry<>(name, amount));
        }

        return new BalanceResponsePacket(balance, accounts);
    }


    public static void handle(BalanceResponsePacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            Screen screen = minecraft.screen;
            if (screen instanceof HomeScreen homeScreen) {
                homeScreen.updateBalance(msg.balance, msg.accounts); // 更新界面余额
            } else {
                minecraft.player.sendSystemMessage(Component.literal("Your Balance: " + msg.balance + " coins"));
            }
        });
        context.setPacketHandled(true);
    }

}
