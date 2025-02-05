package com.mo.economy_system.network.packets.economy_system;

import com.mo.economy_system.network.EconomyNetwork;
import com.mo.economy_system.system.economy_system.EconomySavedData;
import com.mo.economy_system.utils.PlayerUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;
import java.util.function.Supplier;

public class BalanceRequestPacket {

    public BalanceRequestPacket() {}

    public static void encode(BalanceRequestPacket msg, FriendlyByteBuf buf) {
        // 无需数据
    }

    public static BalanceRequestPacket decode(FriendlyByteBuf buf) {
        return new BalanceRequestPacket();
    }

    public static void handle(BalanceRequestPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ServerLevel serverLevel = player.serverLevel();
                if (serverLevel != null) {
                    EconomySavedData data = EconomySavedData.getInstance(serverLevel);
                    List<Map.Entry<UUID, Integer>> accounts = data.getAllAccounts();
                    int balance = data.getBalance(player.getUUID());

                    // 创建一个新的列表来存储 <String, Integer> 类型的数据
                    List<Map.Entry<String, Integer>> accountNames = new ArrayList<>();

                    for (Map.Entry<UUID, Integer> entry : accounts) {
                        UUID uuid = entry.getKey();
                        Integer accountBalance = entry.getValue();

                        // 获取玩家名称
                        String playerName = PlayerUtils.getPlayerNameFromUUID(player.server, uuid); // 获取离线玩家名称

                        // 将玩家名称和余额加入到新的列表中
                        accountNames.add(new AbstractMap.SimpleEntry<>(playerName, accountBalance));
                    }

                    // 发送响应包到客户端
                    EconomyNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new BalanceResponsePacket(balance, accountNames));
                }
            }
        });
        context.setPacketHandled(true);
    }

}
