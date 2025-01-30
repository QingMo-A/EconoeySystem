package com.mo.economy_system.network.packets.economy_system;

import com.mo.economy_system.screen.HomeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BalanceResponsePacket {
    private final int balance;

    public BalanceResponsePacket(int balance) {
        this.balance = balance;
    }

    public static void encode(BalanceResponsePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.balance);
    }

    public static BalanceResponsePacket decode(FriendlyByteBuf buf) {
        return new BalanceResponsePacket(buf.readInt());
    }

    public static void handle(BalanceResponsePacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            Screen screen = minecraft.screen;
            if (screen instanceof HomeScreen homeScreen) {
                homeScreen.updateBalance(msg.balance); // 更新界面余额
            } else {
                minecraft.player.sendSystemMessage(Component.literal("Your Balance: " + msg.balance + " coins"));
            }
        });
        context.setPacketHandled(true);
    }

}
