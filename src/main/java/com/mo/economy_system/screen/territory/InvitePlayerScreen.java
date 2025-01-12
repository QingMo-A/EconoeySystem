package com.mo.economy_system.screen.territory;

import com.mo.economy_system.network.EconomyNetwork;
import com.mo.economy_system.network.packets.InvitePlayerPacket;
import com.mo.economy_system.network.packets.TerritoryRequestPacket;
import com.mo.economy_system.screen.TerritoryScreen;
import com.mo.economy_system.territory.Territory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class InvitePlayerScreen extends Screen {
    private final Territory territory;
    private EditBox playerNameField;

    public InvitePlayerScreen(Territory territory) {
        super(Component.literal("邀请玩家到领地"));
        this.territory = territory;
    }

    @Override
    protected void init() {
        this.clearWidgets();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // 玩家名称输入框
        this.playerNameField = new EditBox(this.font, centerX - 100, centerY - 20, 200, 20, Component.literal("输入玩家名称"));
        this.addRenderableWidget(playerNameField);

        // 发送邀请按钮
        this.addRenderableWidget(
                Button.builder(Component.literal("发送邀请"), button -> {
                            String playerName = playerNameField.getValue();
                            if (!playerName.isEmpty()) {
                                EconomyNetwork.INSTANCE.sendToServer(new InvitePlayerPacket(territory.getTerritoryID(), playerName));
                                this.minecraft.setScreen(null); // 关闭当前界面
                            } else {
                                this.minecraft.player.sendSystemMessage(Component.literal("§c请输入玩家名称！"));
                            }
                        }).pos(centerX - 50, centerY + 20)
                        .size(100, 20)
                        .build()
        );

        // 返回按钮
        this.addRenderableWidget(
                Button.builder(Component.literal("返回"), button -> {
                            // 请求服务器数据
                            EconomyNetwork.INSTANCE.sendToServer(new TerritoryRequestPacket());
                            this.minecraft.setScreen(new TerritoryScreen());
                })
                        .pos(centerX - 50, centerY + 50)
                        .size(100, 20)
                        .build()
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        this.playerNameField.render(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
