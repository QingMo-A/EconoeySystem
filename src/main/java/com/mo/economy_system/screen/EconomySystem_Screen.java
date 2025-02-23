package com.mo.economy_system.screen;

import com.google.common.collect.Lists;
import com.mo.economy_system.screen.components.ItemIconAnimation;
import com.mo.economy_system.screen.components.TextAnimation;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EconomySystem_Screen extends Screen {
    protected int currentPage = 0; // 当前页码
    protected static int TOP_MARGIN; // 距离底部的最小空白高度
    protected static final int BOTTOM_MARGIN = 60; // 距离底部的最小空白高度
    protected int thingsPerPage; // 动态计算的每页东西
    protected final int THING_SPACING = 35; // 动态调整的垂直间距
    protected List<RunnableWithGraphics> renderCache = new ArrayList<>();
    protected int PAGE_BUTTON_WIDTH = 40;
    protected int PAGE_BUTTON_HEIGHT = 20;
    protected int startIndex;
    protected int endIndex;
    protected int startX;
    protected int startY;

    protected int flag = 0;

    protected EconomySystem_Screen(Component title) {
        super(title);
    }

    // 初始化渲染缓存
    protected void initializeRenderCache() {}

    // 初始化坐标
    protected void initPosition() {}

    // 初始化部分
    protected void initPart() {}

    // 根据鼠标位置渲染描述
    protected void detectMouseHoverAndRenderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {}

    protected void addPageAnimatedButtons() {

    }

    protected void addPageButtons() {

    }

    @Override
    public boolean isPauseScreen() {
        return false; // 界面打开时不暂停游戏
    }

    // 检测鼠标位置
    protected boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @FunctionalInterface
    protected interface RunnableWithGraphics {
        void run(GuiGraphics guiGraphics);
    }

    // 渲染动画文本
    protected void renderAnimatedText(GuiGraphics guiGraphics, Component text, TextAnimation animation) {
        // 计算当前属性
        int x = animation.getCurrentX();
        int y = animation.getCurrentY();
        float alpha = animation.getCurrentAlpha();

        // 设置透明度（ARGB格式：0xAARRGGBB）
        int color = 0xFFFFFF | ((int) (alpha * 255) << 24);

        // 文字绘制
        guiGraphics.drawString(
                minecraft.font,
                text,
                x,
                y,
                color,
                true // 启用阴影
        );
    }

    // 渲染自定义颜色的动画文本
    protected void renderAnimatedText(GuiGraphics guiGraphics, Component text, TextAnimation animation, int customHexColor) {
        // 计算当前属性
        int x = animation.getCurrentX();
        int y = animation.getCurrentY();
        float alpha = animation.getCurrentAlpha();

        // 解析并应用自定义颜色
        int rgb = customHexColor & 0x00FFFFFF; // 提取RGB部分（忽略原始透明度）
        int alphaChannel = (int) (alpha * 255) << 24; // 将动画透明度转为ARGB的Alpha通道
        int finalColor = alphaChannel | rgb; // 合并颜色和透明度

        // 文字绘制
        guiGraphics.drawString(
                minecraft.font,
                text,
                x,
                y,
                finalColor,
                true // 启用阴影
        );
    }

    // 渲染动画图标
    protected void renderAnimatedItem(GuiGraphics guiGraphics, ItemStack itemStack, ItemIconAnimation animation) {
        if (animation == null) return;

        // 更新动画状态
        boolean isCompleted = animation.update();

        // 应用动画参数
        int x = animation.getCurrentX();
        int y = animation.getCurrentY();
        float alpha = animation.getCurrentAlpha();
        float scale = animation.getCurrentScale();

        // 设置透明度（需支持透明渲染）
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(scale, scale, 1f);
        guiGraphics.setColor(1f, 1f, 1f, alpha);
        guiGraphics.renderItem(itemStack, 0, 0);
        guiGraphics.setColor(1f, 1f, 1f, 1f); // 重置颜色
        guiGraphics.pose().popPose();
    }
}
