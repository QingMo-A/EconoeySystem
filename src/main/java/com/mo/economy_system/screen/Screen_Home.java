package com.mo.economy_system.screen;

import com.mo.economy_system.network.EconomySystem_NetworkManager;
import com.mo.economy_system.network.packets.economy_system.Packet_BalanceRequest;
import com.mo.economy_system.network.packets.economy_system.Packet_MarketDataRequest;
import com.mo.economy_system.network.packets.economy_system.Packet_ShopDataRequest;
import com.mo.economy_system.network.packets.territory_system.Packet_TerritoryDataRequest;
import com.mo.economy_system.screen.economy_system.Screen_Market;
import com.mo.economy_system.screen.economy_system.Screen_Shop;
import com.mo.economy_system.screen.territory_system.Screen_Territory;
import com.mo.economy_system.utils.Util_MessageKeys;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;

public class Screen_Home extends Screen {

    private int balance = -1; // 用于存储玩家余额，默认值为 -1 表示未获取
    private List<Map.Entry<String, Integer>> accounts;

    public Screen_Home() {
        super(Component.translatable(Util_MessageKeys.HOME_TITLE_KEY));
    }

    @Override
    protected void init() {
        // 添加一个按钮示例
        this.addRenderableWidget(
                Button.builder(Component.translatable(Util_MessageKeys.HOME_SHOP_BUTTON_KEY), button -> {
                            // 请求服务器的商店数据
                            EconomySystem_NetworkManager.INSTANCE.sendToServer(new Packet_ShopDataRequest());
                            // 打开 ShopScreen
                            this.minecraft.setScreen(new Screen_Shop());

                        })
                        .pos(this.width / 2 - 50, this.height / 2 - 30)  // 设置按钮位置
                        .size(100, 20)  // 设置按钮大小
                        .build()
        );

        // 添加按钮以打开 MarketScreen
        this.addRenderableWidget(
                Button.builder(Component.translatable(Util_MessageKeys.HOME_MARKET_BUTTON_KEY), button -> {
                            // 请求服务器数据
                            EconomySystem_NetworkManager.INSTANCE.sendToServer(new Packet_MarketDataRequest());
                            // 打开 MarketScreen（初始化为空列表）
                            this.minecraft.setScreen(new Screen_Market());
                        })
                        .pos(this.width / 2 - 50, this.height / 2)  // 设置按钮位置
                        .size(100, 20)  // 设置按钮大小
                        .build()
        );

        // 添加按钮
        this.addRenderableWidget(
                Button.builder(Component.translatable(Util_MessageKeys.HOME_TERRITORY_BUTTON_KEY), button -> {
                            // 请求服务器数据
                            EconomySystem_NetworkManager.INSTANCE.sendToServer(new Packet_TerritoryDataRequest());
                            this.minecraft.setScreen(new Screen_Territory());
                        })
                        .pos(this.width / 2 - 50, this.height / 2 + 30)  // 设置按钮位置
                        .size(100, 20)  // 设置按钮大小
                        .build()
        );

        // 添加按钮以打开
        this.addRenderableWidget(
                Button.builder(Component.translatable(Util_MessageKeys.HOME_ABOUT_BUTTON_KEY), button -> {
                            this.minecraft.setScreen(new Screen_About());
                        })
                        .pos(this.width / 2 - 50, this.height / 2 + 60)  // 设置按钮位置
                        .size(100, 20)  // 设置按钮大小
                        .build()
        );


        // 请求服务器获取余额
        if (this.minecraft.player != null) {
            // EconomyNetwork.INSTANCE.sendToServer(new ShopRequestPacket());
            EconomySystem_NetworkManager.INSTANCE.sendToServer(new Packet_BalanceRequest());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // 绘制背景
        this.renderBackground(guiGraphics);

        // 渲染父类和按钮
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        String balanceText;

        if (balance == -1) {
            // 获取本地化的 "Fetching balance..." 文本
            balanceText = this.font.plainSubstrByWidth(Component.translatable(Util_MessageKeys.HOME_FETCHING_BALANCE_TEXT_KEY).getString(), Integer.MAX_VALUE);
        } else {
            // 获取本地化的 "Your balance: %s coins" 文本，并替换占位符
            balanceText = Component.translatable(Util_MessageKeys.HOME_BALANCE_TEXT_KEY, balance).getString();
        }

        // 计算文本居中位置
        int textWidth = this.font.width(balanceText);
        int xPosition = (this.width - textWidth) / 2;

        // 绘制文本
        guiGraphics.drawString(this.font, balanceText, xPosition, this.height / 2 - 70, 0xFFFFFF);

        // 绘制自定义文本
        guiGraphics.drawCenteredString(this.font, "Hello, World!", this.width / 2, this.height / 2 - 50, 0xFFFFFF);

        // 渲染玩家账户列表
        int startX = Math.max((this.width / 2) - 450, 30);
        int startY = Math.max((this.height - 400) / 4, 40);
        int index = -1;
        if (accounts != null) {
            int i = 1;
            for (Map.Entry<String, Integer> entry : accounts) {
                if (i > 10) {
                    break;
                }
                String playerName = entry.getKey();
                Integer playerBalance = entry.getValue();

                // 拼接文本 "玩家名称: 余额"
                String accountText = "[" + i + "] " + playerName + " 拥有 " + playerBalance + " 枚梦鱼币";

                // 渲染文本
                guiGraphics.drawString(this.font, accountText, startX, startY, 0xFFFFFF);

                // 增加 y 坐标，确保下一行文本显示在下方
                startY += this.font.lineHeight + 2;  // 增加行高和一些间距
                i++;
            }
            index = getIndexOfPlayer(accounts, this.minecraft.player.getName().getString()) + 1;
        }

        if (index != -1) {
            // 渲染“富豪榜”文本，在账户列表之后
            String leaderboardText = "[" + index + "] 你 拥有 " + balance + " 枚梦鱼币";
            int leaderboardTextWidth = this.font.width(leaderboardText);
            int leaderboardX = 20;  // 居中显示
            // 将富豪榜文本的 y 坐标设置为最后一个账户条目下方
            int leaderboardY = startY + 10;  // 在最后一行账户文本之后加一点间隔

            // 渲染“富豪榜”文本
            guiGraphics.drawString(this.font, leaderboardText, startX, leaderboardY, 0xFFFFFF);
        }
    }

    // 更新余额的方法（供数据包调用）
    public void updateBalance(int balance, List<Map.Entry<String, Integer>> accounts) {
        this.balance = balance;
        this.accounts = accounts;
    }

    @Override
    public boolean isPauseScreen() {
        return false; // 返回 false 表示游戏不会暂停
    }

    // 获取指定 String (玩家名称) 的索引
    public static int getIndexOfPlayer(List<Map.Entry<String, Integer>> accounts, String targetName) {
        for (int i = 0; i < accounts.size(); i++) {
            Map.Entry<String, Integer> entry = accounts.get(i);
            if (entry.getKey().equals(targetName)) {
                return i;  // 返回索引
            }
        }
        return -1;  // 如果没有找到，返回 -1
    }
}
