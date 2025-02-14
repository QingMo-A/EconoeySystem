package com.mo.economy_system.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class EconomySystem_Screen extends Screen {

    protected int currentPage = 0; // 当前页码
    protected static int TOP_MARGIN; // 距离底部的最小空白高度
    protected static final int BOTTOM_MARGIN = 40; // 距离底部的最小空白高度
    protected int thingsPerPage; // 动态计算的每页东西
    protected final int THING_SPACING = 35; // 动态调整的垂直间距
    protected List<RunnableWithGraphics> renderCache = new ArrayList<>();
    protected int PAGE_BUTTON_WIDTH = 40;
    protected int PAGE_BUTTON_HEIGHT = 20;
    protected int startIndex;
    protected int endIndex;
    protected int startX;
    protected int startY;


    protected EconomySystem_Screen(Component title) {
        super(title);
    }

    protected void initializeRenderCache() {

    }

    protected void initPosition() {}

    protected void detectMouseHoverAndRenderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {}

    @Override
    public boolean isPauseScreen() {
        return false; // 界面打开时不暂停游戏
    }

    protected boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @FunctionalInterface
    protected interface RunnableWithGraphics {
        void run(GuiGraphics guiGraphics);
    }
}
