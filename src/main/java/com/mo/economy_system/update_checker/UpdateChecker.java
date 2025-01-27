package com.mo.economy_system.update_checker;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {
    private static final String GITHUB_API_URL = "https://api.github.com/repos/QingMo-A/EconoeySystem/releases/latest";
    // private static final String CURRENT_VERSION = "1.0.0"; // 当前模组版本

    public static void checkForUpdates(ServerPlayer player) {
        new Thread(() -> {
            try {
                // 获取当前版本号
                String currentVersion = ModList.get().getModContainerById("economy_system")
                        .map(mod -> mod.getModInfo().getVersion().toString())
                        .orElse("unknown");

                // 创建连接
                HttpURLConnection connection = (HttpURLConnection) new URL(GITHUB_API_URL).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Minecraft-Mod"); // 设置 User-Agent 避免 GitHub 拒绝请求

                // 检查响应码
                if (connection.getResponseCode() != 200) {
                    player.sendSystemMessage(Component.literal("无法检查更新，请稍后重试！"));
                    return;
                }

                // 解析 JSON 响应
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                String latestVersion = json.get("tag_name").getAsString(); // 获取最新版本号
                String downloadUrl = json.get("html_url").getAsString(); // 获取版本下载地址

                // 比较版本号
                if (!currentVersion.equals(latestVersion)) {
                    player.sendSystemMessage(Component.literal("发现新版本：" + latestVersion + "！"));
                    player.sendSystemMessage(Component.literal("点击下载更新：" + downloadUrl));
                } else {
                    player.sendSystemMessage(Component.literal("当前已是最新版本！"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                player.sendSystemMessage(Component.literal("检查更新时出错！"));
            }
        }).start();
    }
}
