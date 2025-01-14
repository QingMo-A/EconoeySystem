package com.mo.economy_system.screen;

import com.mo.economy_system.network.EconomyNetwork;
import com.mo.economy_system.network.packets.BalanceRequestPacket;
import com.mo.economy_system.network.packets.MarketRequestPacket;
import com.mo.economy_system.network.packets.ShopRequestPacket;
import com.mo.economy_system.network.packets.TerritoryRequestPacket;
import com.mo.economy_system.utils.MessageKeys;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class HomeScreen extends Screen {

    private int balance = -1; // 用于存储玩家余额，默认值为 -1 表示未获取

    public HomeScreen() {
        super(Component.translatable(MessageKeys.HOME_TITLE_KEY));
    }

    @Override
    protected void init() {
        // 添加一个按钮示例
        this.addRenderableWidget(
                Button.builder(Component.translatable(MessageKeys.HOME_SHOP_BUTTON_KEY), button -> {
                            // 请求服务器的商店数据
                            EconomyNetwork.INSTANCE.sendToServer(new ShopRequestPacket());
                            // 打开 ShopScreen
                            this.minecraft.setScreen(new ShopScreen());

                        })
                        .pos(this.width / 2 - 50, this.height / 2 - 30)  // 设置按钮位置
                        .size(100, 20)  // 设置按钮大小
                        .build()
        );

        // 添加按钮以打开 MarketScreen
        this.addRenderableWidget(
                Button.builder(Component.translatable(MessageKeys.HOME_MARKET_BUTTON_KEY), button -> {
                            // 请求服务器数据
                            EconomyNetwork.INSTANCE.sendToServer(new MarketRequestPacket());
                            // 打开 MarketScreen（初始化为空列表）
                            this.minecraft.setScreen(new MarketScreen());
                        })
                        .pos(this.width / 2 - 50, this.height / 2)  // 设置按钮位置
                        .size(100, 20)  // 设置按钮大小
                        .build()
        );

        // 添加按钮
        this.addRenderableWidget(
                Button.builder(Component.translatable(MessageKeys.HOME_TERRITORY_BUTTON_KEY), button -> {
                            // 请求服务器数据
                            EconomyNetwork.INSTANCE.sendToServer(new TerritoryRequestPacket());
                            this.minecraft.setScreen(new TerritoryScreen());
                        })
                        .pos(this.width / 2 - 50, this.height / 2 + 30)  // 设置按钮位置
                        .size(100, 20)  // 设置按钮大小
                        .build()
        );

        // 添加按钮以打开
        this.addRenderableWidget(
                Button.builder(Component.translatable(MessageKeys.HOME_ABOUT_BUTTON_KEY), button -> {
                            this.minecraft.setScreen(new AboutScreen());
                        })
                        .pos(this.width / 2 - 50, this.height / 2 + 60)  // 设置按钮位置
                        .size(100, 20)  // 设置按钮大小
                        .build()
        );


        // 请求服务器获取余额
        if (this.minecraft.player != null) {
            // EconomyNetwork.INSTANCE.sendToServer(new ShopRequestPacket());
            EconomyNetwork.INSTANCE.sendToServer(new BalanceRequestPacket());
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
            balanceText = this.font.plainSubstrByWidth(Component.translatable(MessageKeys.HOME_FETCHING_BALANCE_TEXT_KEY).getString(), Integer.MAX_VALUE);
        } else {
            // 获取本地化的 "Your balance: %s coins" 文本，并替换占位符
            balanceText = Component.translatable(MessageKeys.HOME_BALANCE_TEXT_KEY, balance).getString();
        }

        // 计算文本居中位置
        int textWidth = this.font.width(balanceText);
        int xPosition = (this.width - textWidth) / 2;

        // 绘制文本
        guiGraphics.drawString(this.font, balanceText, xPosition, this.height / 2 - 70, 0xFFFFFF);

        // 绘制自定义文本
        guiGraphics.drawCenteredString(this.font, "Hello, World!", this.width / 2, this.height / 2 - 50, 0xFFFFFF);
    }

    // 更新余额的方法（供数据包调用）
    public void updateBalance(int balance) {
        this.balance = balance;
    }

    @Override
    public boolean isPauseScreen() {
        return false; // 返回 false 表示游戏不会暂停
    }

}
