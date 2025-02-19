package com.mo.economy_system.screen.economy_system.deliver_box;

import com.mo.economy_system.core.economy_system.delivery_box.DeliveryItem;
import com.mo.economy_system.network.EconomySystem_NetworkManager;
import com.mo.economy_system.network.packets.economy_system.Packet_DeliveryBoxClaimItem;
import com.mo.economy_system.network.packets.economy_system.Packet_DeliveryBoxDataRequest;
import com.mo.economy_system.network.packets.economy_system.sales_order.Packet_RemoveSalesOrder;
import com.mo.economy_system.screen.EconomySystem_Screen;
import com.mo.economy_system.screen.Screen_Home;
import com.mo.economy_system.utils.Util_MessageKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class Screen_DeliveryBox extends EconomySystem_Screen {
    private List<DeliveryItem> items = new ArrayList<>(); // 物品列表
    private List<DeliveryItem> filteredItems = new ArrayList<>(); // 根据搜索过滤后的物品列表
    private List<DeliveryItem> itemsSnapshot = new ArrayList<>();

    private EditBox searchBox; // 搜索框

    private UUID playerUUID;
    private String playerName;

    public Screen_DeliveryBox() {
        super(Component.literal("test"));
        EconomySystem_NetworkManager.INSTANCE.sendToServer(new Packet_DeliveryBoxDataRequest());
    }

    @Override
    protected void init() {
        super.init();
        initPosition();

        if (this.minecraft != null && this.minecraft.player != null) {
            this.playerUUID = this.minecraft.player.getUUID();
            this.playerName = this.minecraft.player.getName().getString();
        }

        // 清除现有按钮
        this.clearWidgets();

        // 添加搜索框
        this.searchBox = new EditBox(this.font, Math.max((this.width / 2) - 300, 60), 20, 200, 20, Component.translatable("search.market"));
        this.addRenderableWidget(searchBox);

        // 设置搜索框的键盘监听器
        this.searchBox.setFocused(false); // 默认不聚焦
        this.searchBox.setMaxLength(50); // 限制输入长度
        this.searchBox.setHint(Component.translatable(Util_MessageKeys.MARKET_HINT_TEXT_KEY)); // 提示文本

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
        if (!filteredItems.isEmpty()) {
            detectMouseHoverAndRenderTooltip(guiGraphics, mouseX, mouseY);
        }

        // 渲染当前页数
        guiGraphics.drawCenteredString(this.font, (currentPage + 1) + " / " + getTotalPages(),
                this.width / 2, this.height - 33, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void initializeRenderCache() {
        renderCache.clear(); // 清空旧的缓存

        if (filteredItems.isEmpty()) {
            // 如果没有商品，添加无商品提示的渲染任务
            renderCache.add((guiGraphics) -> {
                int textWidth = this.font.width(Component.literal("你的收货箱是空的~"));
                int xPosition = (this.width - textWidth) / 2;
                guiGraphics.drawString(this.font, Component.literal("你的收货箱是空的~"), xPosition, this.height / 2 - 10, 0xFFFFFF, false);
            });
            return;
        }

        int y = startY;

        for (int i = startIndex; i < endIndex; i++) {
            DeliveryItem item = filteredItems.get(i);
            ItemStack itemStack = item.getItemStack();

            final int currentY = y; // 使用最终变量供 Lambda 表达式使用

            // 添加图标渲染任务
            renderCache.add((guiGraphics) -> guiGraphics.renderItem(itemStack, startX, currentY));

            // 添加物品名称渲染任务
            renderCache.add((guiGraphics) -> guiGraphics.drawString(this.font,
                    Component.translatable(Util_MessageKeys.MARKET_ITEM_NAME_AND_COUNT_KEY,
                            itemStack.getHoverName().getString(),
                            itemStack.getCount()), startX + 20, currentY + 5, 0xFFFFFF, false));

            // 添加价格渲染任务
            renderCache.add((guiGraphics) -> guiGraphics.drawString(this.font,
                    Component.literal("来源: " + item.getSource()), startX, currentY + 18, 0xAAAAAA, false));

            addItemButtons();

            y += THING_SPACING; // 调整下一件商品的位置
        }

        super.initializeRenderCache();
    }

    @Override
    protected void detectMouseHoverAndRenderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        initPosition();
        int y = startY;

        for (int i = startIndex; i < endIndex; i++) {
            DeliveryItem item = filteredItems.get(i);

            if (isMouseOver(mouseX, mouseY, startX, y, 16, 16)) {
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(Component.literal("数据ID: " + item.getDataID()));
                tooltip.add(Component.translatable(Util_MessageKeys.MARKET_ITEM_ID_KEY, item.getItemID()));

                guiGraphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
            }

            y += THING_SPACING;
        }
    }

    // 添加购买按钮
    private void addItemButtons() {
        initPosition();

        int y = startY;
        for (int i = startIndex; i < endIndex; i++) {
            DeliveryItem item = filteredItems.get(i);

            // 添加 购买 按钮
            this.addRenderableWidget(
                    Button.builder(Component.literal("领取"), button -> {
                                EconomySystem_NetworkManager.INSTANCE.sendToServer(new Packet_DeliveryBoxClaimItem(item.getDataID()));
                            })
                            .pos( this.width - startX - 60, y)
                            .size(60, 20)
                            .build()
            );

            y += THING_SPACING;
        }
    }

    // 添加翻页按钮
    private void addPageButtons() {
        initPosition();
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

    public void updateDeliveryBoxItems(List<DeliveryItem> deliveryItems) {
        this.items = deliveryItems;
        this.filteredItems = new ArrayList<>(items); // 初始化过滤后的列表
        this.itemsSnapshot = new ArrayList<>(items); // 初始化过滤后的列表
        this.init(); // 每次更新物品后重新初始化界面
    }

    // 动态计算总页数
    private int getTotalPages() {
        return (int) Math.ceil((double) this.filteredItems.size() / thingsPerPage);
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
            List<DeliveryItem> result = itemsSnapshot;

            // 2. 应用搜索条件
            if (searchBox != null && !searchBox.getValue().isEmpty()) {
                result = result.stream()
                        .filter(item -> itemMatchesSearch(item, searchBox.getValue()))
                        .collect(Collectors.toList());
            }

            // 3. 更新UI
            List<DeliveryItem> finalResult = result;
            this.minecraft.execute(() -> {
                this.filteredItems = finalResult;
                this.currentPage = 0;
                refreshItemButtons();
                initializeRenderCache(); // 重新初始化渲染缓存
            });
        }).start();
    }

    private boolean itemMatchesSearch(DeliveryItem item, String searchText) {
        return item.getItemID().toLowerCase().contains(searchText.toLowerCase()) ||
                item.getSource().toLowerCase().contains(searchText.toLowerCase()) ||
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
        Component buttonMessage = button.getMessage();
        return buttonMessage.equals(Component.literal("领取"));
    }

    @Override
    protected void initPosition(){
        TOP_MARGIN = this.height - 100;
        thingsPerPage = Math.max(1, TOP_MARGIN / THING_SPACING);

        startIndex = currentPage * thingsPerPage;
        endIndex = Math.min(startIndex + thingsPerPage, filteredItems.size());

        startX = Math.max((this.width / 2) - 300, 60);
        startY = Math.max((this.height - 400) / 4, 40);
    }
}
