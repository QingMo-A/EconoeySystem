package com.mo.economy_system.screen.economy_system.shop;

import com.mo.economy_system.core.economy_system.market.SalesOrder;
import com.mo.economy_system.core.economy_system.shop.ShopItem;
import com.mo.economy_system.network.EconomySystem_NetworkManager;
import com.mo.economy_system.network.packets.economy_system.Packet_ShopBuyItem;
import com.mo.economy_system.network.packets.economy_system.sales_order.Packet_CreateSalesOrder;
import com.mo.economy_system.screen.EconomySystem_Screen;
import com.mo.economy_system.screen.economy_system.market.Screen_Market;
import com.mo.economy_system.utils.Util_MessageKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

public class Screen_BuyItem extends EconomySystem_Screen {

    private Player player;
    private ShopItem shopItem;
    private ItemStack itemStack;
    private EditBox countInput; // 输入框用于设置价格

    protected Screen_BuyItem(ShopItem shopItem) {
        super(Component.translatable(Util_MessageKeys.SHOP_BUY_TITLE_KEY));
        this.shopItem = shopItem;
        this.itemStack = shopItem.getItemStack();
    }

    @Override
    protected void init() {
        super.init();

        if (this.minecraft != null && this.minecraft.player != null) {
            this.player = this.minecraft.player;
        }

        // 清除所有组件
        this.clearWidgets();

        // 添加数量输入框
        countInput = new EditBox(this.font, this.width / 2 - 75, this.height / 2 - 20, 150, 20, Component.literal("Enter Count"));
        this.addRenderableWidget(countInput);
        this.countInput.setHint(Component.translatable(Util_MessageKeys.SHOP_BUY_HINT_TEXT_KEY)); // 提示文本

        // 添加购买按钮
        this.addRenderableWidget(
                Button.builder(Component.translatable(Util_MessageKeys.SHOP_BUY_BUY_BUTTON_KEY), button -> buyItem())
                        .pos(this.width / 2 - 50, this.height / 2 + 10)
                        .size(100, 20)
                        .build()
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);

        // 渲染手中物品信息
        if (!itemStack.isEmpty()) {
            int textWidth = this.font.width(itemStack.getHoverName().getString());

            // 计算整体宽度（物品图标宽度为16，间距为2）
            int totalWidth = textWidth + 16 + 2;

            // 计算整体的居中位置
            int xPosition = (this.width - totalWidth) / 2;

            // 渲染物品图标
            guiGraphics.renderItem(itemStack, xPosition, this.height / 2 - 40);

            // 渲染物品名称（图标右边，间距2）
            guiGraphics.drawString(this.font, itemStack.getHoverName().getString(), xPosition + 16 + 2, this.height / 2 - 36, 0xFFFFFF);

        } else {
            // 动态计算文字居中的位置
            int textWidth = this.font.width(Component.translatable(Util_MessageKeys.SHOP_BUY_NO_ITEM_TEXT_KEY));
            int xPosition = (this.width - textWidth) / 2;

            guiGraphics.drawString(this.font, Component.translatable(Util_MessageKeys.SHOP_BUY_NO_ITEM_TEXT_KEY), xPosition, this.height / 2 - 40, 0xAAAAAA);
        }

        int textWidth = this.font.width(Component.translatable(Util_MessageKeys.SHOP_BUY_COUNT_TEXT_KEY));
        // 计算整体的居中位置
        int xPosition = (this.width - textWidth) / 2;
        // 渲染价格输入框
        guiGraphics.drawString(this.font, Component.translatable(Util_MessageKeys.SHOP_BUY_COUNT_TEXT_KEY), this.width / 2 - 78 - textWidth, this.height / 2 - 15, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    private void buyItem() {
        if (itemStack.isEmpty()) {
            this.player.sendSystemMessage(Component.translatable(Util_MessageKeys.SHOP_BUY_NO_ITEM_MESSAGE_KEY));
            return;
        }

        String countText = countInput.getValue();
        Optional<Integer> count = parsePrice(countText);

        if (count.isEmpty() || count.get() <= 0) {
            this.player.sendSystemMessage(Component.translatable(Util_MessageKeys.SHOP_BUY_INVALID_COUNT_MESSAGE_KEY));
            return;
        }

        EconomySystem_NetworkManager.INSTANCE.sendToServer(new Packet_ShopBuyItem(shopItem.getItemId(), shopItem.getNbt(), shopItem.getCurrentPrice(), count.get()));

        this.minecraft.setScreen(new Screen_Shop());
    }


    private Optional<Integer> parsePrice(String priceText) {
        try {
            return Optional.of(Integer.parseInt(priceText));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 && this.shouldCloseOnEsc()) {
            Minecraft.getInstance().setScreen(new Screen_Shop());
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
