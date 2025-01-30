package com.mo.economy_system.screen.economy_system;

import com.mo.economy_system.network.packets.economy_system.MarketDataRequestPacket;
import com.mo.economy_system.network.packets.economy_system.MarketRequestItemPacket;
import com.mo.economy_system.system.economy_system.market.MarketItem;
import com.mo.economy_system.network.EconomyNetwork;
import com.mo.economy_system.network.packets.economy_system.MarketListItemPacket;
import com.mo.economy_system.utils.MessageKeys;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.UUID;

public class RequestItemScreen extends Screen {

    private final Player player;
    private EditBox priceInput; // 输入框用于设置价格
    private EditBox itemIDInput; // 输入框用于设置物品ID
    private EditBox itemCountInput; // 输入框用于设置物品数量

    private ItemStack itemStackToRequest = ItemStack.EMPTY; // 当前请求的物品
    private Component errorMessage = null; // 错误消息
    private boolean canRequest = false;

    public RequestItemScreen(Player player) {
        super(Component.translatable(MessageKeys.REQUEST_TITLE_KEY));
        this.player = player;
    }

    @Override
    protected void init() {
        super.init();

        // 清除所有组件
        this.clearWidgets();

        // 添加物品ID输入框
        itemIDInput = new EditBox(this.font, this.width / 2 - 75, this.height / 2 - 45, 150, 20, Component.literal("Enter Item ID"));
        this.addRenderableWidget(itemIDInput);
        this.itemIDInput.setHint(Component.translatable(MessageKeys.REQUEST_ITEM_ID_HINT_TEXT_KEY)); // 提示文本

        // 添加物品数量输入框
        itemCountInput = new EditBox(this.font, this.width / 2 - 75, this.height / 2 - 20, 150, 20, Component.literal("Enter Item ID"));
        this.addRenderableWidget(itemCountInput);
        this.itemCountInput.setHint(Component.translatable(MessageKeys.REQUEST_ITEM_COUNT_HINT_TEXT_KEY)); // 提示文本

        // 添加价格输入框
        priceInput = new EditBox(this.font, this.width / 2 - 75, this.height / 2 + 5, 150, 20, Component.literal("Enter Price"));
        this.addRenderableWidget(priceInput);
        this.priceInput.setHint(Component.translatable(MessageKeys.REQUEST_PRICE_HINT_TEXT_KEY)); // 提示文本

        // 添加求购按钮
        this.addRenderableWidget(
                Button.builder(Component.translatable(MessageKeys.REQUEST_REQUEST_BUTTON_KEY), button -> requestItem())
                        .pos(this.width / 2 - 50, this.height / 2 + 35)
                        .size(100, 20)
                        .build()
        );
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.itemIDInput.isFocused() && keyCode == 257) { // 检测回车键（keyCode 257）
            String itemID = this.itemIDInput.getValue();
            String itemCount = this.itemCountInput.getValue();
            String itemPrice = this.priceInput.getValue();
            checkItemID(itemID, itemCount, itemPrice);
            return true; // 防止事件进一步传播
        }

        if (this.itemCountInput.isFocused() && keyCode == 257) { // 检测回车键（keyCode 257）
            String itemID = this.itemIDInput.getValue();
            String itemCount = this.itemCountInput.getValue();
            String itemPrice = this.priceInput.getValue();
            checkItemID(itemID, itemCount, itemPrice);
            return true; // 防止事件进一步传播
        }

        if (this.priceInput.isFocused() && keyCode == 257) { // 检测回车键（keyCode 257）
            String itemID = this.itemIDInput.getValue();
            String itemCount = this.itemCountInput.getValue();
            String itemPrice = this.priceInput.getValue();
            checkItemID(itemID, itemCount, itemPrice);
            return true; // 防止事件进一步传播
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void checkItemID(String itemID, String itemCount, String itemPrice) {
        // 重置状态
        itemStackToRequest = ItemStack.EMPTY;
        errorMessage = null;

        // 验证物品ID
        if (itemID.isEmpty() || getItemStack(itemID).is(Items.AIR)) {
            errorMessage = Component.translatable(MessageKeys.REQUEST_UNKNOWN_ITEM_ID_KEY);
        } else {
            // 验证物品数量
            if (itemCount.isEmpty() || Integer.parseInt(itemCount) <= 0) {
                errorMessage = Component.translatable(MessageKeys.REQUEST_INVALID_ITEM_COUNT_KEY);
            } else {

                itemStackToRequest = getItemStack(itemID);
                int count = Integer.parseInt(itemCount);
                System.out.println(itemStackToRequest.getItem().getMaxStackSize());

                // 验证物品数量是否超过最大堆叠数
                if (count > itemStackToRequest.getItem().getMaxStackSize()) {
                    errorMessage = Component.translatable(MessageKeys.REQUEST_EXCESSIVE_ITEM_COUNT_KEY);
                    itemStackToRequest = ItemStack.EMPTY; // 清空物品
                } else {
                    if (itemPrice.isEmpty() || Integer.parseInt(itemPrice) <= 0) {
                        errorMessage = Component.translatable(MessageKeys.REQUEST_INVALID_PRICE_KEY);
                        itemStackToRequest = ItemStack.EMPTY; // 清空物品
                    } else {
                        canRequest = true;
                    }
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);

        // 渲染物品信息或错误消息
        if (itemStackToRequest != ItemStack.EMPTY) {
            int textWidth = this.font.width(itemStackToRequest.getHoverName().getString());
            int totalWidth = textWidth + 16 + 2; // 物品图标宽度为16，间距为2
            int xPosition = (this.width - totalWidth) / 2;

            // 渲染物品图标
            guiGraphics.renderItem(itemStackToRequest, xPosition, this.height / 2 - 65);

            // 渲染物品名称
            guiGraphics.drawString(this.font, itemStackToRequest.getHoverName().getString(), xPosition + 16 + 2, this.height / 2 - 56, 0xFFFFFF);
        } else if (errorMessage != null) {
            int textWidth = this.font.width(errorMessage);
            int xPosition = (this.width - textWidth) / 2;
            guiGraphics.drawString(this.font, errorMessage, xPosition, this.height / 2 - 65, 0xAAAAAA);
        }

        // 渲染输入框标签
        guiGraphics.drawString(this.font, Component.translatable(MessageKeys.REQUEST_ITEM_ID_TEXT_KEY), this.width / 2 - 78 - 45, this.height / 2 - 40, 0xFFFFFF);
        guiGraphics.drawString(this.font, Component.translatable(MessageKeys.REQUEST_ITEM_COUNT_TEXT_KEY), this.width / 2 - 78 - 45, this.height / 2 - 15, 0xFFFFFF);
        guiGraphics.drawString(this.font, Component.translatable(MessageKeys.REQUEST_PRICE_TEXT_KEY), this.width / 2 - 78 - 45, this.height / 2 + 10, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false; // 界面打开时不暂停游戏
    }

    private void requestItem() {
        if (canRequest) {
            String priceText = priceInput.getValue();
            Optional<Integer> price = parsePrice(priceText);

            if (price.isEmpty() || price.get() <= 0) {
                this.player.sendSystemMessage(Component.translatable(MessageKeys.LIST_INVALID_PRICE_MESSAGE_KEY));
                return;
            }

            // 创建 MarketItem 对象时，自动生成唯一商品 ID
            MarketItem marketItem = new MarketItem(
                    UUID.randomUUID(),
                    itemStackToRequest.getItem().getDescriptionId(), // 物品描述 ID
                    itemStackToRequest.copy(), // 复制物品
                    price.get(),
                    player.getName().getString(),
                    player.getUUID(),
                    System.currentTimeMillis(),
                    false,
                    false
            );

            // 发送上架请求到服务端
            EconomyNetwork.INSTANCE.sendToServer(new MarketRequestItemPacket(marketItem));

            // 请求服务器数据
            EconomyNetwork.INSTANCE.sendToServer(new MarketDataRequestPacket());
            this.minecraft.setScreen(new MarketScreen()); // 关闭界面
        }
    }


    private Optional<Integer> parsePrice(String priceText) {
        try {
            return Optional.of(Integer.parseInt(priceText));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private ItemStack getItemStack(String itemID) {
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(itemID));
        if (item != null) {
            return new ItemStack(item);
        } else {
            return ItemStack.EMPTY; // 如果物品 ID 无效，返回空堆
        }
    }
}
