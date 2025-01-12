package com.mo.economy_system.screen.territory;

import com.mo.economy_system.network.EconomyNetwork;
import com.mo.economy_system.network.packets.RemoveTerritoryPacket;
import com.mo.economy_system.territory.Territory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class TerritoryManagementScreen extends Screen {
    private final Territory territory;

    public TerritoryManagementScreen(Territory territory) {
        super(Component.literal("管理领地: " + territory.getName()));
        this.territory = territory;
    }

    @Override
    protected void init() {
        this.clearWidgets();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // 显示领地 ID
        this.addRenderableWidget(
                Button.builder(Component.literal("复制领地ID"), button -> {
                            GLFW.glfwSetClipboardString(Minecraft.getInstance().getWindow().getWindow(),
                                    territory.getTerritoryID().toString());
                            this.minecraft.player.sendSystemMessage(Component.literal("领地ID已复制到剪贴板！"));
                        }).pos(centerX - 60, centerY - 60)
                        .size(120, 20)
                        .build()
        );

        // 邀请玩家按钮
        this.addRenderableWidget(
                Button.builder(Component.literal("邀请玩家"), button -> {
                            Minecraft.getInstance().setScreen(new InvitePlayerScreen(territory));
                        }).pos(centerX - 60, centerY - 30)
                        .size(120, 20)
                        .build()
        );

        // 删除领地按钮
        this.addRenderableWidget(
                Button.builder(Component.literal("删除领地"), button -> {
                            EconomyNetwork.INSTANCE.sendToServer(new RemoveTerritoryPacket(territory.getTerritoryID()));
                            this.minecraft.setScreen(null); // 关闭界面
                        }).pos(centerX - 60, centerY + 10)
                        .size(120, 20)
                        .build()
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false; // 打开界面时不暂停游戏
    }
}
