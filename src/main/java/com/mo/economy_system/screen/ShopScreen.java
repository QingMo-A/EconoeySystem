package com.mo.economy_system.screen;

import com.mo.economy_system.shop.ShopItem;
import com.mo.economy_system.network.EconomyNetwork;
import com.mo.economy_system.network.packets.ShopRequestPacket;
import com.mo.economy_system.network.packets.BuyItemPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShopScreen extends Screen {

    private static final String TITLE_KEY = "screen.shop.title";
    private static final String ITEM_PRICE_KEY = "screen.shop.item.price";
    private static final String ITEM_ID_KEY = "screen.shop.item.id";
    private static final String ITEM_BASIC_PRICE_KEY = "screen.shop.item.basic_price";
    private static final String ITEM_CURRENT_PRICE_KEY = "screen.shop.item.current_price";
    private static final String ITEM_CHANGE_PRICE_KEY = "screen.shop.item.change_price";
    private static final String ITEM_FLUCTUATION_FACTOR_KEY = "screen.shop.item.fluctuation_factor";
    private static final String LOADING_SHOP_DATA_TEXT_KEY = "text.shop.loading_shop_data";
    private static final String BUY_BUTTON_KEY = "button.shop.buy";
    private static final String HINT_TEXT_KEY = "text.shop.hint";

    private List<ShopItem> items = new ArrayList<>(); // 商品列表
    private int currentPage = 0; // 当前页码
    private final int GAP = 35; // 每个商品的垂直间距
    private static final int BOTTOM_MARGIN = 30; // 距离底部的最小空白高度
    private int itemsPerPage; // 动态计算的每页商品数

    private EditBox searchBox; // 搜索框

    public ShopScreen() {
        super(Component.translatable(TITLE_KEY));
    }

    public void updateShopItems(List<ShopItem> items) {
        this.items = items;
        this.init(); // 每次更新商店物品后重新初始化界面
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);

        // 动态计算每页可显示的商品数
        itemsPerPage = Math.max((this.height - 50 - BOTTOM_MARGIN) / GAP, 1); // 确保至少显示 1 个商品

        // 计算当前页的起始和结束索引
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, items.size());

        // 渲染当前页的商品
        if (items != null && !items.isEmpty()) {
            int startX = Math.max((this.width / 2) - 300, 60);
            int startY = Math.max((this.height - 400) / 4, 40);

            int y = startY;
            for (int i = startIndex; i < endIndex; i++) {
                ShopItem item = items.get(i);
                ItemStack itemStack = item.getItemStack();

                // 渲染物品图标
                guiGraphics.renderItem(itemStack, startX, y);

                int priceDifference = item.getCurrentPrice() - item.getBasePrice();
                String priceChangeText;

                if (priceDifference > 0) {
                    priceChangeText = "+" + priceDifference; // 正数显示 "+ xxx"
                } else {
                    priceChangeText = String.valueOf(priceDifference); // 负数直接显示 "- xxx"
                }

                // 检测鼠标是否悬停在物品图标上
                if (mouseX >= startX && mouseX <= startX + 16 && mouseY >= y && mouseY <= y + 16) {
                    // 显示 Tooltip
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(Component.translatable(ITEM_CHANGE_PRICE_KEY, priceChangeText));
                    tooltip.add(Component.translatable(ITEM_BASIC_PRICE_KEY, item.getBasePrice()));
                    tooltip.add(Component.translatable(ITEM_CURRENT_PRICE_KEY, item.getCurrentPrice()));
                    tooltip.add(Component.translatable(ITEM_FLUCTUATION_FACTOR_KEY, item.getFluctuationFactor()));
                    tooltip.add(Component.translatable(ITEM_ID_KEY, item.getItemId()));
                    guiGraphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
                }

                // 渲染物品价格
                guiGraphics.drawString(this.font, Component.translatable(ITEM_PRICE_KEY, item.getCurrentPrice()), startX + 20, y + 5, 0xFFFFFF);

                // 渲染物品描述
                guiGraphics.drawString(this.font, item.getDescription(), startX, y + 18, 0xAAAAAA);

                y += GAP;
            }
        } else {
            // 动态计算文字居中的位置
            int textWidth = this.font.width(Component.translatable(LOADING_SHOP_DATA_TEXT_KEY));
            int xPosition = (this.width - textWidth) / 2;

            guiGraphics.drawString(this.font, Component.translatable(LOADING_SHOP_DATA_TEXT_KEY), xPosition, this.height / 2 - 10, 0xFFFFFF);
        }

        // 显示当前页数
        guiGraphics.drawCenteredString(this.font, (currentPage + 1) + " / " + getTotalPages(), this.width / 2, this.height - 33, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void init() {
        super.init();

        // 清除现有按钮
        this.clearWidgets();

        // 请求商店数据
        EconomyNetwork.INSTANCE.sendToServer(new ShopRequestPacket());

        // 动态添加商品购买按钮
        addItemButtons();

        // 添加翻页按钮
        addPageButtons();
    }

    @Override
    public boolean isPauseScreen() {
        return false; // 界面打开时不暂停游戏
    }

    // 动态计算总页数
    private int getTotalPages() {
        return (int) Math.ceil((double) items.size() / itemsPerPage);
    }

    // 添加分页按钮
    private void addPageButtons() {
        int startX = Math.max((this.width / 2 - 150), 60);
        int buttonWidth = 40;
        int buttonHeight = 20;
        int buttonY = this.height - 40;

        // 上一页按钮
        this.addRenderableWidget(
                Button.builder(Component.literal("<"), button -> {
                            if (currentPage > 0) {
                                currentPage--;
                                this.init(); // 刷新页面
                            }
                        })
                        .pos(startX, buttonY)
                        .size(buttonWidth, buttonHeight)
                        .build()
        );

        // 下一页按钮
        this.addRenderableWidget(
                Button.builder(Component.literal(">"), button -> {
                            if (currentPage < getTotalPages() - 1) {
                                currentPage++;
                                this.init(); // 刷新页面
                            }
                        })
                        .pos(this.width - startX - buttonWidth, buttonY)
                        .size(buttonWidth, buttonHeight)
                        .build()
        );
    }

    // 动态为每个商品添加按钮
    private void addItemButtons() {
        // 动态计算每页可显示的商品数
        itemsPerPage = Math.max((this.height - 50 - BOTTOM_MARGIN) / GAP, 1); // 确保至少显示 1 个商品

        // 计算当前页的起始和结束索引
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, items.size());

        // int startX = Math.max((this.width / 2) + 300, 60); // 居中
        int startX = this.width - Math.max((this.width / 2) - 300, 60);
        int startY = Math.max((this.height - 400) / 4, 40);

        int y = startY;
        for (int i = startIndex; i < endIndex; i++) {
            ShopItem item = items.get(i);

            // 添加 Buy x1 按钮
            this.addRenderableWidget(
                    Button.builder(Component.translatable(BUY_BUTTON_KEY, 1), button -> {
                                EconomyNetwork.INSTANCE.sendToServer(new BuyItemPacket(item.getItemId(), item.getCurrentPrice(), 1));
                            })
                            .pos(startX - 210, y)
                            .size(60, 20)
                            .build()
            );

            // 添加 Buy x32 按钮
            this.addRenderableWidget(
                    Button.builder(Component.translatable(BUY_BUTTON_KEY, 32), button -> {
                                EconomyNetwork.INSTANCE.sendToServer(new BuyItemPacket(item.getItemId(), item.getCurrentPrice(), 32));
                            })
                            .pos(startX - 140, y)
                            .size(60, 20)
                            .build()
            );

            // 添加 Buy x64 按钮
            this.addRenderableWidget(
                    Button.builder(Component.translatable(BUY_BUTTON_KEY, 64), button -> {
                                EconomyNetwork.INSTANCE.sendToServer(new BuyItemPacket(item.getItemId(), item.getCurrentPrice(), 64));
                            })
                            .pos(startX - 70, y)
                            .size(60, 20)
                            .build()
            );

            y += GAP;
        }
    }
}