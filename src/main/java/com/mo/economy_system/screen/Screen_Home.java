package com.mo.economy_system.screen;

import com.mo.economy_system.network.EconomySystem_NetworkManager;
import com.mo.economy_system.network.packets.economy_system.Packet_BalanceRequest;
import com.mo.economy_system.screen.economy_system.deliver_box.Screen_DeliveryBox;
import com.mo.economy_system.screen.economy_system.market.Screen_Market;
import com.mo.economy_system.screen.economy_system.shop.Screen_Shop;
import com.mo.economy_system.screen.territory_system.Screen_Territory;
import com.mo.economy_system.utils.Util_MessageKeys;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;

/**
 * Screen_Home 类用于创建和管理主界面屏幕，包含多个按钮以导航到不同的子界面，并显示玩家余额和其他相关信息。
 */
public class Screen_Home extends EconomySystem_Screen {

    /**
     * 用于存储玩家余额，默认值为 -1 表示未获取。
     */
    private int balance = -1;

    /**
     * 存储玩家账户列表，键为玩家名称，值为余额。
     */
    private List<Map.Entry<String, Integer>> accounts;

    /**
     * 构造函数，初始化主界面标题。
     */
    public Screen_Home() {
        super(Component.translatable(Util_MessageKeys.HOME_TITLE_KEY));
        EconomySystem_NetworkManager.INSTANCE.sendToServer(new Packet_BalanceRequest());
    }

    /**
     * 初始化屏幕组件，包括添加按钮和请求玩家余额。
     */
    @Override
    protected void init() {
        initPosition();
        // 添加商店按钮
        this.addRenderableWidget(
                Button.builder(Component.translatable(Util_MessageKeys.HOME_SHOP_BUTTON_KEY), button -> {
                            // 请求服务器的商店数据并打开 ShopScreen
                            this.minecraft.setScreen(new Screen_Shop());
                        })
                        .pos(startX, startY)  // 设置按钮位置
                        .size(100, 20)  // 设置按钮大小
                        .build()
        );

        // 添加市场按钮
        this.addRenderableWidget(
                Button.builder(Component.translatable(Util_MessageKeys.HOME_MARKET_BUTTON_KEY), button -> {
                            // 请求服务器的市场数据并打开 MarketScreen
                            this.minecraft.setScreen(new Screen_Market());
                        })
                        .pos(startX, startY + 30)  // 设置按钮位置
                        .size(100, 20)  // 设置按钮大小
                        .build()
        );

        // 添加物资箱按钮
        this.addRenderableWidget(
                Button.builder(Component.translatable(Util_MessageKeys.HOME_DELIVERY_BOX_BUTTON_KEY), button -> {
                            // 请求服务器的市场数据并打开 DeliveryBoxScreen
                            this.minecraft.setScreen(new Screen_DeliveryBox());
                        })
                        .pos(startX, startY + 60)  // 设置按钮位置
                        .size(100, 20)  // 设置按钮大小
                        .build()
        );

        // 添加领地按钮
        this.addRenderableWidget(
                Button.builder(Component.translatable(Util_MessageKeys.HOME_TERRITORY_BUTTON_KEY), button -> {
                            // 请求服务器的领地数据并打开 TerritoryScreen
                            this.minecraft.setScreen(new Screen_Territory());
                        })
                        .pos(startX, startY + 90)  // 设置按钮位置
                        .size(100, 20)  // 设置按钮大小
                        .build()
        );

        // 添加关于按钮
        this.addRenderableWidget(
                Button.builder(Component.translatable(Util_MessageKeys.HOME_ABOUT_BUTTON_KEY), button -> {
                            // 打开 AboutScreen
                            this.minecraft.setScreen(new Screen_About());
                        })
                        .pos(startX, startY + 120)  // 设置按钮位置
                        .size(100, 20)  // 设置按钮大小
                        .build()
        );
    }

    /**
     * 渲染屏幕内容，包括背景、按钮、玩家余额和账户列表。
     *
     * @param guiGraphics GUI 图形对象
     * @param mouseX      鼠标 X 坐标
     * @param mouseY      鼠标 Y 坐标
     * @param partialTicks 部分刻数
     */
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
        int xPosition = startX + textWidth / 2;

        // 绘制文本
        guiGraphics.drawString(this.font, balanceText, xPosition, startY - 20, 0xFFFFFF);

        // 渲染玩家账户列表
        int startY = this.startY - 20;
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
                guiGraphics.drawString(this.font, accountText, this.width - startX - 100, startY, 0xFFFFFF);

                // 增加 y 坐标，确保下一行文本显示在下方
                startY += this.font.lineHeight + 2;  // 增加行高和一些间距
                i++;
            }
            index = getIndexOfPlayer(accounts, this.minecraft.player.getName().getString()) + 1;
        }

        if (index != -1) {
            String leaderboardText = "[" + index + "] 你 拥有 " + balance + " 枚梦鱼币";
            int leaderboardTextWidth = this.font.width(leaderboardText);
            int leaderboardX = 20;  // 居中显示
            // 将富豪榜文本的 y 坐标设置为最后一个账户条目下方
            int leaderboardY = startY + 10;  // 在最后一行账户文本之后加一点间隔

            // 渲染“富豪榜”文本
            guiGraphics.drawString(this.font, leaderboardText, this.width - startX - 100, leaderboardY, 0xFFFFFF);
        }
    }

    /**
     * 更新玩家余额和账户列表的方法，供数据包调用。
     *
     * @param balance 玩家余额
     * @param accounts 玩家账户列表
     */
    public void updateBalance(int balance, List<Map.Entry<String, Integer>> accounts) {
        this.balance = balance;
        this.accounts = accounts;
    }

    /**
     * 获取指定玩家名称在账户列表中的索引。
     *
     * @param accounts 账户列表
     * @param targetName 目标玩家名称
     * @return 索引，如果未找到则返回 -1
     */
    public static int getIndexOfPlayer(List<Map.Entry<String, Integer>> accounts, String targetName) {
        for (int i = 0; i < accounts.size(); i++) {
            Map.Entry<String, Integer> entry = accounts.get(i);
            if (entry.getKey().equals(targetName)) {
                return i;  // 返回索引
            }
        }
        return -1;  // 如果没有找到，返回 -1
    }

    @Override
    protected void initPosition(){
        TOP_MARGIN = this.height - 100;
        thingsPerPage = Math.max(1, TOP_MARGIN / THING_SPACING);

        startX = Math.max((this.width / 2) - 300, 60);
        startY = Math.max((this.height - 300) / 4, 40);
    }
}
