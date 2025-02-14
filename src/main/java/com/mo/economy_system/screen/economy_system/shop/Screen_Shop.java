package com.mo.economy_system.screen.economy_system.shop;

import com.mo.economy_system.core.economy_system.market.DemandOrder;
import com.mo.economy_system.core.economy_system.market.MarketItem;
import com.mo.economy_system.core.economy_system.market.SalesOrder;
import com.mo.economy_system.screen.EconomySystem_Screen;
import com.mo.economy_system.screen.Screen_Home;
import com.mo.economy_system.core.economy_system.shop.ShopItem;
import com.mo.economy_system.network.EconomySystem_NetworkManager;
import com.mo.economy_system.network.packets.economy_system.Packet_ShopDataRequest;
import com.mo.economy_system.network.packets.economy_system.Packet_ShopBuyItem;
import com.mo.economy_system.utils.Util_MessageKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Screen_Shop extends EconomySystem_Screen {

    private List<ShopItem> items = new ArrayList<>(); // 商品列表
    private List<ShopItem> itemsSnapshot = new ArrayList<>();

    private EditBox searchBox; // 搜索框

    public Screen_Shop() {
        super(Component.translatable(Util_MessageKeys.SHOP_TITLE_KEY));
        EconomySystem_NetworkManager.INSTANCE.sendToServer(new Packet_ShopDataRequest());
    }

    public void updateShopItems(List<ShopItem> items) {
        this.items = items;
        this.itemsSnapshot = new ArrayList<>(items);
        this.init(); // 每次更新商店物品后重新初始化界面
    }

    @Override
    protected void init() {
        super.init();

        initPosition();

        // 清除现有按钮
        this.clearWidgets();

        // 添加搜索框
        this.searchBox = new EditBox(this.font, Math.max((this.width / 2) - 300, 60), 20, 200, 20, Component.translatable("search.market"));
        this.addRenderableWidget(searchBox);

        // 设置搜索框的键盘监听器
        this.searchBox.setFocused(false); // 默认不聚焦
        this.searchBox.setMaxLength(50); // 限制输入长度
        this.searchBox.setHint(Component.translatable(Util_MessageKeys.SHOP_HINT_TEXT_KEY)); // 提示文本

        // 动态添加商品购买按钮
        addItemButtons();

        // 添加翻页按钮
        addPageButtons();

        // 初始化渲染缓存（在所有按钮添加后调用）
        initializeRenderCache();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);

        // 执行渲染缓存中的任务
        for (RunnableWithGraphics task : renderCache) {
            task.run(guiGraphics);
        }

        // 如果有商品，进行鼠标悬停检测并显示 Tooltip
        if (!items.isEmpty()) {
            detectMouseHoverAndRenderTooltip(guiGraphics, mouseX, mouseY);
        }

        // 显示当前页数
        guiGraphics.drawCenteredString(this.font, (currentPage + 1) + " / " + getTotalPages(),
                this.width / 2, this.height - 33, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void initializeRenderCache() {
        renderCache.clear(); // 清空旧的缓存

        if (items.isEmpty()) {
            // 如果没有商品，添加无商品提示的渲染任务
            renderCache.add((guiGraphics) -> {
                // 动态计算文字居中的位置
                int textWidth = this.font.width(Component.translatable(Util_MessageKeys.SHOP_LOADING_SHOP_DATA_TEXT_KEY));
                int xPosition = (this.width - textWidth) / 2;
                guiGraphics.drawString(this.font, Component.translatable(Util_MessageKeys.SHOP_LOADING_SHOP_DATA_TEXT_KEY), xPosition, this.height / 2 - 10, 0xFFFFFF);
            });
            return;
        }

        int y = startY;

        for (int i = startIndex; i < endIndex; i++) {
            ShopItem item = items.get(i);
            ItemStack itemStack = item.getItemStack();

            final int currentY = y; // 使用最终变量供 Lambda 表达式使用

            // 渲染物品图标
            renderCache.add((guiGraphics) -> guiGraphics.renderItem(itemStack, startX, currentY));

            int priceDifference = item.getCurrentPrice() - item.getLastPrice();
            String priceChangeText;

            if (priceDifference > 0) {
                priceChangeText = "+" + priceDifference; // 正数显示 "+ xxx"
            } else {
                priceChangeText = String.valueOf(priceDifference); // 负数直接显示 "- xxx"
            }

            // 渲染物品价格
            renderCache.add((guiGraphics -> guiGraphics.drawString(this.font, Component.translatable(Util_MessageKeys.SHOP_ITEM_PRICE_KEY, item.getCurrentPrice()), startX + 20, currentY + 5, 0xFFFFFF)));

            // 渲染物品描述
            renderCache.add((guiGraphics -> guiGraphics.drawString(this.font, item.getDescription(), startX, currentY + 18, 0xAAAAAA)));

            y += THING_SPACING;
        }
    }

    @Override
    protected void detectMouseHoverAndRenderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        initPosition();

        int y = startY;

        for (int i = startIndex; i < endIndex; i++) {
            ShopItem item = items.get(i);

            int priceDifference = item.getCurrentPrice() - item.getLastPrice();
            String priceChangeText;

            if (priceDifference > 0) {
                priceChangeText = "+" + priceDifference; // 正数显示 "+ xxx"
            } else {
                priceChangeText = String.valueOf(priceDifference); // 负数直接显示 "- xxx"
            }

            if (isMouseOver(mouseX, mouseY, startX, y, 16, 16)) {
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(Component.translatable(Util_MessageKeys.SHOP_ITEM_CHANGE_PRICE_KEY, priceChangeText));
                tooltip.add(Component.translatable(Util_MessageKeys.SHOP_ITEM_BASIC_PRICE_KEY, item.getBasePrice()));
                tooltip.add(Component.translatable(Util_MessageKeys.SHOP_ITEM_CURRENT_PRICE_KEY, item.getCurrentPrice()));
                tooltip.add(Component.translatable(Util_MessageKeys.SHOP_ITEM_FLUCTUATION_FACTOR_KEY, item.getFluctuationFactor()));
                tooltip.add(Component.translatable(Util_MessageKeys.SHOP_ITEM_ID_KEY, item.getItemId()));

                guiGraphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
            }

            y += THING_SPACING;
        }
    }

    // 动态为每个商品添加按钮
    private void addItemButtons() {
        initPosition();

        int y = startY;
        for (int i = startIndex; i < endIndex; i++) {
            ShopItem item = items.get(i);

            // 添加 购买 按钮
            this.addRenderableWidget(
                    Button.builder(Component.translatable(Util_MessageKeys.SHOP_BUY_BUTTON_KEY), button -> {
                                this.minecraft.setScreen(new Screen_BuyItem(item));
                            })
                            .pos( this.width - startX - 60, y)
                            .size(60, 20)
                            .build()
            );

            y += THING_SPACING;
        }
    }

    // 添加分页按钮
    private void addPageButtons() {
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
                        .size(PAGE_BUTTON_WIDTH, PAGE_BUTTON_HEIGHT)
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
                        .pos(this.width - startX - PAGE_BUTTON_WIDTH, buttonY)
                        .size(PAGE_BUTTON_WIDTH, PAGE_BUTTON_HEIGHT)
                        .build()
        );
    }

    // 动态计算总页数
    private int getTotalPages() {
        return (int) Math.ceil((double) items.size() / thingsPerPage);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchBox.isFocused() && keyCode == 257) { // 检测回车键（keyCode 257）
            applySearch();
            return true; // 防止事件进一步传播
        } else if (keyCode == 256 && this.shouldCloseOnEsc()) {
            Minecraft.getInstance().setScreen(new Screen_Home());
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void applySearch() {
        applyFilters(); // 调用联合过滤逻辑
    }

    private void applyFilters() {
        new Thread(() -> {
            List<ShopItem> result = itemsSnapshot;

            // 2. 应用搜索条件
            if (searchBox != null && !searchBox.getValue().isEmpty()) {
                result = result.stream()
                        .filter(item -> itemMatchesSearch(item, searchBox.getValue()))
                        .collect(Collectors.toList());
            }

            // 3. 更新UI
            List<ShopItem> finalResult = result;
            this.minecraft.execute(() -> {
                this.items = finalResult;
                this.currentPage = 0;
                refreshItemButtons();
                initializeRenderCache(); // 重新初始化渲染缓存
            });
        }).start();
    }

    private boolean itemMatchesSearch(ShopItem item, String searchText) {
        return item.getItemId().toLowerCase().contains(searchText.toLowerCase()) ||
                item.getDescription().toLowerCase().contains(searchText.toLowerCase()) ||
                item.getItemStack().getHoverName().getString().toLowerCase().contains(searchText.toLowerCase());
    }

    // 刷新购买按钮
    private void refreshItemButtons() {
        clearItemButtons(); // 清除旧的商品按钮
        addItemButtons();   // 添加新的商品按钮
    }

    // 移除购买按钮
    private void clearItemButtons() {
        // 遍历所有已渲染的控件并移除与商品相关的按钮
        this.renderables.removeIf(widget -> widget instanceof Button && isItemButton((Button) widget));
        this.children().removeIf(widget -> widget instanceof Button && isItemButton((Button) widget));
    }

    // 判断是否为购买按钮
    private boolean isItemButton(Button button) {
        // 判断按钮是否为 "Buy" 或 "Remove" 按钮
        Component buttonMessage = button.getMessage();
        return buttonMessage.equals(Component.translatable(Util_MessageKeys.SHOP_BUY_BUTTON_KEY));
    }

    @Override
    protected void initPosition(){
        TOP_MARGIN = this.height - 100;
        thingsPerPage = Math.max(1, TOP_MARGIN / THING_SPACING);

        startIndex = currentPage * thingsPerPage;
        endIndex = Math.min(startIndex + thingsPerPage, items.size());

        startX = Math.max((this.width / 2) - 300, 60);
        startY = Math.max((this.height - 400) / 4, 40);
    }
}
