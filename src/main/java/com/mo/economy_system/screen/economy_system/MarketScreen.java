package com.mo.economy_system.screen.economy_system;

import com.mo.economy_system.network.packets.economy_system.*;
import com.mo.economy_system.system.economy_system.market.MarketItem;
import com.mo.economy_system.network.EconomyNetwork;
import com.mo.economy_system.utils.MessageKeys;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class MarketScreen extends Screen {
    private List<MarketItem> items = new ArrayList<>(); // 市场商品列表
    private List<MarketItem> filteredItems = new ArrayList<>(); // 根据搜索过滤后的商品列表
    private List<MarketItem> itemsSnapshot = new ArrayList<>();
    private int currentPage = 0; // 当前页码
    private static final int BOTTOM_MARGIN = 30; // 距离底部的最小空白高度
    private int itemsPerPage; // 动态计算的每页商品数
    private final int GAP = 35; // 动态调整的垂直间距
    // 计数器变量，初始为 0
    private int displayTypeIndex = 0;
    // 定义按钮显示的文本数组
    private final String[] DISPLAY_TYPE_KEYS = {
            MessageKeys.MARKET_SWITCH_DISPLAY_TYPE_0_BUTTON_KEY,
            MessageKeys.MARKET_SWITCH_DISPLAY_TYPE_1_BUTTON_KEY,
            MessageKeys.MARKET_SWITCH_DISPLAY_TYPE_2_BUTTON_KEY,
            MessageKeys.MARKET_SWITCH_DISPLAY_TYPE_3_BUTTON_KEY,
            MessageKeys.MARKET_SWITCH_DISPLAY_TYPE_4_BUTTON_KEY
    };

    private EditBox searchBox; // 搜索框
    private boolean isUserTyping = false; // 标记用户是否正在输入

    private UUID playerUUID;
    private String playerName;

    private List<RunnableWithGraphics> renderCache = new ArrayList<>();

    // 构造方法
    public MarketScreen() {
        super(Component.translatable(MessageKeys.MARKET_TITLE_KEY));
    }

    // 初始化
    @Override
    protected void init() {
        super.init();

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
        this.searchBox.setResponder(text -> isUserTyping = true); // 标记用户正在输入
        this.searchBox.setFocused(false); // 默认不聚焦
        this.searchBox.setMaxLength(50); // 限制输入长度
        this.searchBox.setHint(Component.translatable(MessageKeys.MARKET_HINT_TEXT_KEY)); // 提示文本

        // 添加翻页按钮
        addPageButtons();

        // 添加切换显示类型的按钮
        addSwitchDisplayTypeButton();

        // 添加上架按钮
        addListItemButton();

        // 添加求购按钮
        addRequestItemButton();

        // 初始化渲染缓存（在所有按钮添加后调用）
        initializeRenderCache();
    }

    // 渲染(一帧一更新)
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

    // 初始化渲染缓存
    private void initializeRenderCache() {
        renderCache.clear(); // 清空旧的缓存

        if (filteredItems.isEmpty()) {
            // 如果没有商品，添加无商品提示的渲染任务
            renderCache.add((guiGraphics) -> {
                int textWidth = this.font.width(Component.translatable(MessageKeys.MARKET_NO_ITEMS_TEXT_KEY));
                int xPosition = (this.width - textWidth) / 2;
                guiGraphics.drawString(this.font, Component.translatable(MessageKeys.MARKET_NO_ITEMS_TEXT_KEY), xPosition, this.height / 2 - 10, 0xFFFFFF, false);
            });
            return;
        }

        // 动态计算每页可显示的商品数和间距
        int availableHeight = this.height - 100; // 减去顶部和底部的空白区域
        itemsPerPage = Math.max(1, availableHeight / GAP); // 至少显示 1 件商品

        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredItems.size());

        int startX = Math.max((this.width / 2) - 300, 60);
        int startY = Math.max((this.height - 400) / 4, 40);

        int y = startY;

        for (int i = startIndex; i < endIndex; i++) {
            MarketItem item = filteredItems.get(i);
            ItemStack itemStack = item.getItemStack();

            final int currentY = y; // 使用最终变量供 Lambda 表达式使用

            // 添加图标渲染任务
            renderCache.add((guiGraphics) -> guiGraphics.renderItem(itemStack, startX, currentY));

            // 添加物品名称渲染任务
            renderCache.add((guiGraphics) -> guiGraphics.drawString(this.font,
                    Component.translatable(MessageKeys.MARKET_ITEM_NAME_AND_COUNT_KEY,
                            itemStack.getHoverName().getString(),
                            itemStack.getCount()), startX + 20, currentY + 5, 0xFFFFFF, false));

            // 添加价格渲染任务
            renderCache.add((guiGraphics) -> guiGraphics.drawString(this.font,
                    Component.translatable(MessageKeys.MARKET_ITEM_PRICE_KEY, item.getPrice()), startX, currentY + 18, 0xAAAAAA, false));

            // 添加购买或下架按钮（确保在初始化时添加按钮）
            addPurchaseOrRemoveButton(item, this.width - startX, currentY, playerUUID);

            y += GAP; // 调整下一件商品的位置
        }
    }

    private void detectMouseHoverAndRenderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int startX = Math.max((this.width / 2) - 300, 60);
        int startY = Math.max((this.height - 400) / 4, 40);

        int y = startY;

        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            MarketItem item = filteredItems.get(i);

            if (isMouseOver(mouseX, mouseY, startX, y, 16, 16)) {
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(Component.translatable(MessageKeys.MARKET_SELLER_NAME_KEY, item.getSellerName()));
                tooltip.add(Component.translatable(MessageKeys.MARKET_SELLER_UUID_KEY, item.getSellerID()));
                tooltip.add(Component.translatable(MessageKeys.MARKET_TRADE_ID_KEY, item.getTradeID()));
                tooltip.add(Component.translatable(MessageKeys.MARKET_ITEM_ID_KEY, item.getItemID()));

                guiGraphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
            }

            y += GAP;
        }
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
        return buttonMessage.equals(Component.translatable(MessageKeys.MARKET_BUY_BUTTON_KEY)) ||
                buttonMessage.equals(Component.translatable(MessageKeys.MARKET_REMOVE_BUTTON_KEY)) ||
                buttonMessage.equals(Component.translatable(MessageKeys.REQUEST_DELIVER_BUTTON_KEY)) ||
                buttonMessage.equals(Component.translatable(MessageKeys.REQUEST_DELIVERED_STATUS_KEY)) ||
                buttonMessage.equals(Component.translatable(MessageKeys.REQUEST_CLAIM_BUTTON_KEY));
    }

    // 添加购买按钮
    private void addItemButtons() {
        // 动态计算每页可显示的商品数
        this.itemsPerPage = Math.max((this.height - 50 - BOTTOM_MARGIN) / GAP, 1);

        // 计算当前页的起始和结束索引
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredItems.size());

        int startX = Math.max((this.width / 2) - 300, 60);
        int startY = Math.max((this.height - 400) / 4, 40);

        int y = startY;
        UUID playerUUID = this.minecraft.player.getUUID();

        for (int i = startIndex; i < endIndex; i++) {
            MarketItem item = filteredItems.get(i);

            // 添加购买或下架按钮
            this.addPurchaseOrRemoveButton(item, this.width - startX, y, playerUUID);

            y += GAP;
        }
    }

    // 添加翻页按钮
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

    // 添加求购按钮
    private void addRequestItemButton() {
        int buttonWidth = 70;
        int buttonHeight = 20;

        this.addRenderableWidget(
                Button.builder(Component.translatable(MessageKeys.MARKET_REQUEST_BUTTON_KEY), button -> {
                            // 打开上架界面
                            this.minecraft.setScreen(new RequestItemScreen(minecraft.player));
                        })
                        .pos(this.width - Math.max((this.width / 2) - 300, 60) - buttonWidth, 20)
                        .size(buttonWidth, buttonHeight)
                        .build()
        );
    }

    // 添加上架按钮
    private void addListItemButton() {
        int buttonWidth = 70;
        int buttonHeight = 20;

        this.addRenderableWidget(
                Button.builder(Component.translatable(MessageKeys.MARKET_LIST_BUTTON_KEY), button -> {
                            // 打开上架界面
                            this.minecraft.setScreen(new ListItemScreen(minecraft.player));
                        })
                        .pos(this.width - Math.max((this.width / 2) - 300, 60) - (2 * buttonWidth + 10), 20)
                        .size(buttonWidth, buttonHeight)
                        .build()
        );
    }

    // 添加切换显示类型按钮
    private void addSwitchDisplayTypeButton() {
        int buttonWidth = 100;
        int buttonHeight = 20;

        // 创建按钮
        Button switchDisplayButton = Button.builder(
                        Component.translatable(DISPLAY_TYPE_KEYS[displayTypeIndex]), // 初始文本
                        button -> {
                            // 执行切换逻辑
                            displayTypeIndex = (displayTypeIndex + 1) % DISPLAY_TYPE_KEYS.length; // 循环切换文本索引
                            button.setMessage(Component.translatable(DISPLAY_TYPE_KEYS[displayTypeIndex])); // 更新按钮显示文本

                            // 执行与当前显示类型相关的操作
                            handleDisplayTypeAction(displayTypeIndex);
                        }
                )
                .pos(this.width - Math.max((this.width / 2) - 300, 60) - (2 * 70 + 20) - buttonWidth, 20)
                .size(buttonWidth, buttonHeight)
                .build();

        // 添加到界面
        this.addRenderableWidget(switchDisplayButton);
    }

    // 处理与显示类型相关的操作
    private void handleDisplayTypeAction(int displayTypeIndex) {
        switch (displayTypeIndex) {
            case 0: // 无显示类型
                new Thread(() -> {
                    List<MarketItem> result;
                    result = new ArrayList<>(itemsSnapshot); // 如果没有搜索内容，显示全部商品

                    this.minecraft.execute(() -> {
                        this.filteredItems = result;
                        this.currentPage = 0;
                        refreshItemButtons();
                        initializeRenderCache(); // 重新初始化渲染缓存
                    });
                }).start();

                break;
            case 1: // 仅显示自己的订单
                new Thread(() -> {
                    List<MarketItem> result;
                    result = itemsSnapshot.stream()
                            .filter(item -> item.getSellerName().toLowerCase().contains(playerName.toLowerCase()))
                            .collect(Collectors.toList());

                    this.minecraft.execute(() -> {
                        this.filteredItems = result;
                        this.currentPage = 0;
                        refreshItemButtons();
                        initializeRenderCache(); // 重新初始化渲染缓存
                    });
                }).start();

                break;
            case 2: // 仅显示非自己的订单
                new Thread(() -> {
                    List<MarketItem> result;
                    result = itemsSnapshot.stream()
                            .filter(item -> !(item.getSellerName().toLowerCase().contains(playerName.toLowerCase())))
                            .collect(Collectors.toList());

                    this.minecraft.execute(() -> {
                        this.filteredItems = result;
                        this.currentPage = 0;
                        refreshItemButtons();
                        initializeRenderCache(); // 重新初始化渲染缓存
                    });
                }).start();

                break;
            case 3: // 仅显示出货单
                new Thread(() -> {
                    List<MarketItem> result;
                    result = itemsSnapshot.stream()
                            .filter(item -> item.isMarketItem())
                            .collect(Collectors.toList());

                    this.minecraft.execute(() -> {
                        this.filteredItems = result;
                        this.currentPage = 0;
                        refreshItemButtons();
                        initializeRenderCache(); // 重新初始化渲染缓存
                    });
                }).start();

                break;
            case 4: // 仅显示订购单
                new Thread(() -> {
                    List<MarketItem> result;
                    result = itemsSnapshot.stream()
                            .filter(item -> !(item.isMarketItem()))
                            .collect(Collectors.toList());

                    this.minecraft.execute(() -> {
                        this.filteredItems = result;
                        this.currentPage = 0;
                        refreshItemButtons();
                        initializeRenderCache(); // 重新初始化渲染缓存
                    });
                }).start();

                break;
        }
    }

    // 添加购买或下架按钮方法
    private void addPurchaseOrRemoveButton(MarketItem item, int buttonX, int buttonY, UUID playerUUID) {
        // 如果是出货单
        if (item.isMarketItem()) {
            // 如果玩家ID等于商品的卖家ID (卖家)
            if (item.getSellerID().equals(playerUUID)) {
                Button removeButton = Button.builder(Component.translatable(MessageKeys.MARKET_REMOVE_BUTTON_KEY), btn -> {
                            // 发送下架请求到服务器
                            EconomyNetwork.INSTANCE.sendToServer(new MarketRemoveMarketItemPacket(item.getTradeID()));
                            refreshItemButtons(); // 局部更新商品按钮
                            // 立即请求更新商品列表
                            requestMarketUpdate();
                        })
                        .pos(buttonX - 60, buttonY)
                        .size(60, 20)
                        .build();
                this.addRenderableWidget(removeButton);
                // 如果玩家ID不等于商品的卖家ID (买家)
            } else if (!(item.getSellerID().equals(playerUUID))) {
                // 如果是OP
                if (this.minecraft.player.hasPermissions(2)) {
                    Button removeButton = Button.builder(Component.translatable(MessageKeys.MARKET_REMOVE_BUTTON_KEY), btn -> {
                                // 发送下架请求到服务器
                                EconomyNetwork.INSTANCE.sendToServer(new MarketRemoveMarketItemPacket(item.getTradeID()));
                                refreshItemButtons(); // 局部更新商品按钮
                                // 立即请求更新商品列表
                                requestMarketUpdate();
                            })
                            .pos(buttonX - 60, buttonY)
                            .size(60, 20)
                            .build();
                    this.addRenderableWidget(removeButton);

                    Button buyButton = Button.builder(Component.translatable(MessageKeys.MARKET_BUY_BUTTON_KEY), btn -> {
                                // 发送下架请求到服务器
                                EconomyNetwork.INSTANCE.sendToServer(new MarketPurchaseItemPacket(item.getTradeID()));
                                refreshItemButtons(); // 局部更新商品按钮
                                // 立即请求更新商品列表
                                requestMarketUpdate();
                            })
                            .pos(buttonX - 130, buttonY)
                            .size(60, 20)
                            .build();
                    this.addRenderableWidget(buyButton);
                    // 如果不是OP
                } else {
                    Button buyButton = Button.builder(Component.translatable(MessageKeys.MARKET_BUY_BUTTON_KEY), btn -> {
                                // 发送购买请求到服务器
                                EconomyNetwork.INSTANCE.sendToServer(new MarketPurchaseItemPacket(item.getTradeID()));
                                refreshItemButtons(); // 局部更新商品按钮
                                // 立即请求更新商品列表
                                requestMarketUpdate();
                            })
                            .pos(buttonX - 60, buttonY)
                            .size(60, 20)
                            .build();
                    this.addRenderableWidget(buyButton);
                }
            }
            // 如果是订购单
        } else {
            // 如果玩家ID等于商品的卖家ID (卖家)
            if (item.getSellerID().equals(playerUUID)) {
                if (item.isDeliveredItem()) {
                    Button claimButton = Button.builder(Component.translatable(MessageKeys.REQUEST_CLAIM_BUTTON_KEY), btn -> {
                                // 发送下架请求到服务器
                                EconomyNetwork.INSTANCE.sendToServer(new MarketClaimRequestItemPacket(item.getTradeID()));
                                refreshItemButtons(); // 局部更新商品按钮
                                // 立即请求更新商品列表
                                requestMarketUpdate();
                            })
                            .pos(buttonX - 60, buttonY)
                            .size(60, 20)
                            .build();
                    this.addRenderableWidget(claimButton);
                } else {
                    Button removeButton = Button.builder(Component.translatable(MessageKeys.MARKET_REMOVE_BUTTON_KEY), btn -> {
                                // 发送下架请求到服务器
                                EconomyNetwork.INSTANCE.sendToServer(new MarketRemoveRequestItemPacket(item.getTradeID()));
                                refreshItemButtons(); // 局部更新商品按钮
                                // 立即请求更新商品列表
                                requestMarketUpdate();
                            })
                            .pos(buttonX - 60, buttonY)
                            .size(60, 20)
                            .build();
                    this.addRenderableWidget(removeButton);
                }
                // 如果玩家ID不等于商品的卖家ID (买家)
            } else if (!(item.getSellerID().equals(playerUUID))) {
                // 如果是OP
                if (this.minecraft.player.hasPermissions(2)) {
                    // 如果已经交付
                    if (item.isDeliveredItem()) {
                        Button claimButton = Button.builder(Component.translatable(MessageKeys.REQUEST_CLAIM_BUTTON_KEY), btn -> {
                                    // 发送下架请求到服务器
                                    EconomyNetwork.INSTANCE.sendToServer(new MarketClaimRequestItemPacket(item.getTradeID()));
                                    refreshItemButtons(); // 局部更新商品按钮
                                    // 立即请求更新商品列表
                                    requestMarketUpdate();
                                })
                                .pos(buttonX - 60, buttonY)
                                .size(60, 20)
                                .build();
                        this.addRenderableWidget(claimButton);

                        Button deliverButton = Button.builder(Component.translatable(MessageKeys.REQUEST_DELIVERED_STATUS_KEY), btn -> {
                                    // 发送下架请求到服务器
                                    EconomyNetwork.INSTANCE.sendToServer(new MarketDeliverItemPacket(item.getTradeID()));
                                    refreshItemButtons(); // 局部更新商品按钮
                                    // 立即请求更新商品列表
                                    requestMarketUpdate();
                                })
                                .pos(buttonX - 130, buttonY)
                                .size(60, 20)
                                .build();
                        this.addRenderableWidget(deliverButton);
                        deliverButton.active = false;
                        // 如果没有交付
                    } else {
                        Button removeButton = Button.builder(Component.translatable(MessageKeys.MARKET_REMOVE_BUTTON_KEY), btn -> {
                                    // 发送下架请求到服务器
                                    EconomyNetwork.INSTANCE.sendToServer(new MarketRemoveMarketItemPacket(item.getTradeID()));
                                    refreshItemButtons(); // 局部更新商品按钮
                                    // 立即请求更新商品列表
                                    requestMarketUpdate();
                                })
                                .pos(buttonX - 60, buttonY)
                                .size(60, 20)
                                .build();
                        this.addRenderableWidget(removeButton);

                        Button deliverButton = Button.builder(Component.translatable(MessageKeys.REQUEST_DELIVER_BUTTON_KEY), btn -> {
                                    // 发送下架请求到服务器
                                    EconomyNetwork.INSTANCE.sendToServer(new MarketDeliverItemPacket(item.getTradeID()));
                                    refreshItemButtons(); // 局部更新商品按钮
                                    // 立即请求更新商品列表
                                    requestMarketUpdate();
                                })
                                .pos(buttonX - 130, buttonY)
                                .size(60, 20)
                                .build();
                        this.addRenderableWidget(deliverButton);
                    }
                    // 如果不是OP
                } else {
                    // 如果已经交付
                    if (item.isDeliveredItem()) {
                        Button deliverButton = Button.builder(Component.translatable(MessageKeys.REQUEST_DELIVERED_STATUS_KEY), btn -> {
                                    // 发送购买请求到服务器
                                    EconomyNetwork.INSTANCE.sendToServer(new MarketPurchaseItemPacket(item.getTradeID()));
                                    refreshItemButtons(); // 局部更新商品按钮
                                    // 立即请求更新商品列表
                                    requestMarketUpdate();
                                })
                                .pos(buttonX - 60, buttonY)
                                .size(60, 20)
                                .build();
                        this.addRenderableWidget(deliverButton);
                        deliverButton.active = false;
                        // 如果没有交付
                    } else {
                        Button deliverButton = Button.builder(Component.translatable(MessageKeys.REQUEST_DELIVER_BUTTON_KEY), btn -> {
                                    // 发送购买请求到服务器
                                    EconomyNetwork.INSTANCE.sendToServer(new MarketDeliverItemPacket(item.getTradeID()));
                                    refreshItemButtons(); // 局部更新商品按钮
                                    // 立即请求更新商品列表
                                    requestMarketUpdate();
                                })
                                .pos(buttonX - 60, buttonY)
                                .size(60, 20)
                                .build();
                        this.addRenderableWidget(deliverButton);
                    }
                }
            }
        }
    }

    private void requestMarketUpdate() {
        EconomyNetwork.INSTANCE.sendToServer(new MarketDataRequestPacket());
    }


    // 刷新购买按钮
    private void refreshItemButtons() {
        clearItemButtons(); // 清除旧的商品按钮
        addItemButtons();   // 添加新的商品按钮
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchBox.isFocused() && keyCode == 257) { // 检测回车键（keyCode 257）
            String searchText = this.searchBox.getValue();
            applySearch(searchText);
            return true; // 防止事件进一步传播
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void applySearch(String searchText) {
        // List<MarketItem> itemsSnapshot = new ArrayList<>(this.items); // 深拷贝当前的 items 列表
        new Thread(() -> {
            List<MarketItem> result;
            if (searchText.isEmpty()) {
                result = new ArrayList<>(itemsSnapshot); // 如果没有搜索内容，显示全部商品
            } else {
                result = itemsSnapshot.stream()
                        .filter(item -> item.getItemID().toLowerCase().contains(searchText.toLowerCase()) ||
                                item.getSellerName().toLowerCase().contains(searchText.toLowerCase()) ||
                                item.getItemStack().getHoverName().getString().toLowerCase().contains(searchText.toLowerCase()))
                        .collect(Collectors.toList());
            }

            this.minecraft.execute(() -> {
                this.filteredItems = result;
                this.currentPage = 0;
                refreshItemButtons();
                initializeRenderCache(); // 重新初始化渲染缓存
            });
        }).start();
    }

    private boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void updateMarketItems(List<MarketItem> items) {
        this.items = items;
        this.filteredItems = new ArrayList<>(items); // 初始化过滤后的列表
        this.itemsSnapshot = new ArrayList<>(items); // 初始化过滤后的列表
        this.init(); // 每次更新市场物品后重新初始化界面
    }

    @Override
    public boolean isPauseScreen() {
        return false; // 界面打开时不暂停游戏
    }

    // 动态计算总页数
    private int getTotalPages() {
        return (int) Math.ceil((double) this.filteredItems.size() / itemsPerPage);
    }

    @FunctionalInterface
    private interface RunnableWithGraphics {
        void run(GuiGraphics guiGraphics);
    }
}