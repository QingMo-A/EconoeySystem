package com.mo.economy_system.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;

public class AboutScreen extends Screen {

    private static final String MOD_NAME = "Economy System";
    private static final String AUTHOR_NAME = "Author: QingMo";
    private static final String GITHUB_URL = "https://github.com/QingMo-A/EconoeySystem/tree/master"; // 替换为你的 GitHub 链接

    // 背景图像的资源路径（可选，如果需要自定义背景）
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("economy_system", "textures/gui/about_screen_background.png");

    public AboutScreen() {
        super(Component.literal("About Economy System"));
    }

    @Override
    protected void init() {
        super.init();

        // 添加一个返回按钮
        this.addRenderableWidget(
                Button.builder(Component.literal("Back"), button -> {
                            this.minecraft.setScreen(new HomeScreen()); // 返回主菜单
                        })
                        .pos(this.width / 2 - 50, this.height - 40)
                        .size(100, 20)
                        .build()
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // 渲染背景
        this.renderBackground(guiGraphics);

        // 如果有自定义背景图像，可以启用以下代码（确保资源文件路径正确）
        /*
        guiGraphics.blit(BACKGROUND_TEXTURE, 0, 0, 0, 0, this.width, this.height, 256, 256);
        */

        // 渲染标题
        guiGraphics.drawCenteredString(this.font, this.title.getString(), this.width / 2, 20, 0xFFFFFF);

        // 渲染模组名称
        guiGraphics.drawCenteredString(this.font, MOD_NAME, this.width / 2, 50, 0xAAAAFF);

        // 渲染作者名称
        guiGraphics.drawCenteredString(this.font, AUTHOR_NAME, this.width / 2, 70, 0xFFFFFF);

        // 渲染 GitHub 链接
        Component githubLink = Component.literal(GITHUB_URL)
                .withStyle(style -> style
                        .withColor(0x55FF55) // 绿色
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, GITHUB_URL))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to open GitHub")))
                );
        guiGraphics.drawCenteredString(this.font, githubLink.getString(), this.width / 2, 90, 0x00FF00);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 检测 GitHub 链接的点击事件
        if (mouseX >= this.width / 2 - 100 && mouseX <= this.width / 2 + 100 && mouseY >= 85 && mouseY <= 105) {
            Minecraft.getInstance().keyboardHandler.setClipboard(GITHUB_URL);
            Minecraft.getInstance().getChatListener().handleSystemMessage(
                    Component.literal("The github link has been copied to the clipboard").withStyle(style -> style.withColor(0x00FF00)),
                    false
            );
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false; // 打开关于页面时游戏不暂停
    }
}
