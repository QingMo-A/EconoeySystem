package com.mo.economy_system.network.packets.check_system;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class GetResultResponsePacket {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final String playerName;
    private final String playerUUID;
    private final String senderName;
    private final String senderUUID;
    private final String actionType;
    private final String fileName;
    private final String base64;

    public GetResultResponsePacket(String playerName, String playerUUID, String senderName, String senderUUID, String actionType, String fileName, String base64) {
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.senderName = senderName;
        this.senderUUID = senderUUID;
        this.actionType = actionType;
        this.fileName = fileName;
        this.base64 = base64;
    }

    public static void encode(GetResultResponsePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.playerName);
        buf.writeUtf(msg.playerUUID);
        buf.writeUtf(msg.senderName);
        buf.writeUtf(msg.senderUUID);
        buf.writeUtf(msg.actionType);
        buf.writeUtf(msg.fileName);
        buf.writeUtf(msg.base64);
    }

    public static GetResultResponsePacket decode(FriendlyByteBuf buf) {
        return new GetResultResponsePacket(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf());
    }

    public static void handle(GetResultResponsePacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            executor.execute(() -> {
                Minecraft mc = Minecraft.getInstance();
                LocalPlayer localPlayer = mc.player;

                // 如果 base64 为 "NotFound", 提示文件不存在, 直接返回
                if ("NotFound".equals(msg.base64)) {
                    if (localPlayer != null) {
                        localPlayer.sendSystemMessage(Component.literal("远程玩家 " + msg.playerName + " 提示：文件不存在!"));
                    }
                    return;
                }

                // 否则 base64 不为 "NotFound"，说明文件存在
                if (localPlayer != null) {
                    localPlayer.sendSystemMessage(Component.literal("收到来自玩家 " + msg.playerName + " 的文件！"));
                }

                // 在本地将 base64 解码为二进制文件
                try {
                    byte[] fileBytes = Base64.getDecoder().decode(msg.base64);

                    // 你可以自行决定存储的文件名和扩展名。
                    // 比如说在同目录下，把 actionType 作为文件夹或前缀
                    File gameDir = mc.gameDirectory;
                    File outputFile = new File(gameDir, msg.fileName);

                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        fos.write(fileBytes);
                    }

                    if (localPlayer != null) {
                        localPlayer.sendSystemMessage(Component.literal("已保存文件为 " + outputFile.getAbsolutePath()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
        });
        contextSupplier.get().setPacketHandled(true);
    }

    private static String computeSHA256(Path path) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] fileBytes = Files.readAllBytes(path);
        byte[] hashBytes = digest.digest(fileBytes);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
