package com.mo.economy_system.screen;

import com.mo.economy_system.market.MarketItem;
import com.mo.economy_system.network.EconomyNetwork;
import com.mo.economy_system.network.packets.ListItemPacket;
import com.mo.economy_system.network.packets.MarketRequestPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

public class ListItemScreen extends Screen {

    private static final String TITLE_KEY = "screen.list.title";
    private static final String NO_ITEM_IN_HAND_TEXT_KEY = "text.list.no_item_in_hand";
    private static final String PRICE_TEXT_KEY = "text.list.price";
    private static final String LIST_BUTTON_KEY = "button.list.list";
    private static final String NO_ITEM_IN_HAND_MESSAGE_KEY = "message.list.no_item_in_hand";
    private static final String INVALID_PRICE_MESSAGE_KEY = "message.list.invalid_price";
    private static final String HINT_TEXT_KEY = "text.list.hint";

    private final Player player;
    private EditBox priceInput; // 输入框用于设置价格

    public ListItemScreen(Player player) {
        super(Component.translatable(TITLE_KEY));
        this.player = player;
    }

    @Override
    protected void init() {
        super.init();

        // 清除所有组件
        this.clearWidgets();

        // 添加价格输入框
        priceInput = new EditBox(this.font, this.width / 2 - 75, this.height / 2 - 20, 150, 20, Component.literal("Enter Price"));
        this.addRenderableWidget(priceInput);
        this.priceInput.setHint(Component.translatable(HINT_TEXT_KEY)); // 提示文本

        // 添加上架按钮
        this.addRenderableWidget(
                Button.builder(Component.translatable(LIST_BUTTON_KEY), button -> listItem())
                        .pos(this.width / 2 - 50, this.height / 2 + 20)
                        .size(100, 20)
                        .build()
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);

        // 获取玩家手中的物品
        ItemStack heldItem = player.getMainHandItem();

        // 渲染手中物品信息
        if (!heldItem.isEmpty()) {
            int textWidth = this.font.width(heldItem.getHoverName().getString());

            // 计算整体宽度（物品图标宽度为16，间距为2）
            int totalWidth = textWidth + 16 + 2;

            // 计算整体的居中位置
            int xPosition = (this.width - totalWidth) / 2;

            // 渲染物品图标
            guiGraphics.renderItem(heldItem, xPosition, this.height / 2 - 40);

            // 渲染物品名称（图标右边，间距2）
            guiGraphics.drawString(this.font, heldItem.getHoverName().getString(), xPosition + 16 + 2, this.height / 2 - 36, 0xFFFFFF);

        } else {
            // 动态计算文字居中的位置
            int textWidth = this.font.width(Component.translatable(NO_ITEM_IN_HAND_TEXT_KEY));
            int xPosition = (this.width - textWidth) / 2;

            guiGraphics.drawString(this.font, Component.translatable(NO_ITEM_IN_HAND_TEXT_KEY), xPosition, this.height / 2 - 40, 0xAAAAAA);
        }

        int textWidth = this.font.width(Component.translatable(PRICE_TEXT_KEY));
        // 计算整体的居中位置
        int xPosition = (this.width - textWidth) / 2;
        // 渲染价格输入框
        guiGraphics.drawString(this.font, Component.translatable(PRICE_TEXT_KEY), this.width / 2 - 78 - textWidth, this.height / 2 - 15, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false; // 界面打开时不暂停游戏
    }

    private void listItem() {
        ItemStack heldItem = player.getMainHandItem(); // 获取玩家手中的物品

        if (heldItem.isEmpty()) {
            this.player.sendSystemMessage(Component.translatable(NO_ITEM_IN_HAND_MESSAGE_KEY));
            return;
        }

        String priceText = priceInput.getValue();
        Optional<Integer> price = parsePrice(priceText);

        if (price.isEmpty() || price.get() <= 0) {
            this.player.sendSystemMessage(Component.translatable(INVALID_PRICE_MESSAGE_KEY));
            return;
        }

        // 创建 MarketItem 对象时，自动生成唯一商品 ID
        MarketItem marketItem = new MarketItem(
                UUID.randomUUID(),
                heldItem.getItem().getDescriptionId(), // 物品描述 ID
                heldItem.copy(), // 复制物品
                price.get(),
                player.getName().getString(),
                player.getUUID(),
                System.currentTimeMillis()
        );

        // 发送上架请求到服务端
        EconomyNetwork.INSTANCE.sendToServer(new ListItemPacket(marketItem));

        // 请求服务器数据
        EconomyNetwork.INSTANCE.sendToServer(new MarketRequestPacket());
        this.minecraft.setScreen(new MarketScreen()); // 关闭界面
    }


    private Optional<Integer> parsePrice(String priceText) {
        try {
            return Optional.of(Integer.parseInt(priceText));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
