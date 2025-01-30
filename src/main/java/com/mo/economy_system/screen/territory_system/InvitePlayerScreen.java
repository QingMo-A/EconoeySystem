package com.mo.economy_system.screen.territory_system;

import com.mo.economy_system.network.EconomyNetwork;
import com.mo.economy_system.network.packets.territory_system.TerrirotyInvitePlayerPacket;
import com.mo.economy_system.network.packets.territory_system.TerritoryRequestPacket;
import com.mo.economy_system.system.territory_system.Territory;
import com.mo.economy_system.utils.MessageKeys;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class InvitePlayerScreen extends Screen {
    private final Territory territory;
    private EditBox playerNameField;

    public InvitePlayerScreen(Territory territory) {
        super(Component.translatable(MessageKeys.INVITE_TITLE_KEY));
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
                Button.builder(Component.translatable(MessageKeys.INVITE_INVITE_BUTTON_KEY), button -> {
                            String playerName = playerNameField.getValue();
                            if (!playerName.isEmpty()) {
                                EconomyNetwork.INSTANCE.sendToServer(new TerrirotyInvitePlayerPacket(territory.getTerritoryID(), playerName));
                                this.minecraft.setScreen(null); // 关闭当前界面
                            } else {
                                this.minecraft.player.sendSystemMessage(Component.translatable(MessageKeys.INVITE_NO_NAME_KEY));
                            }
                        }).pos(centerX - 50, centerY + 20)
                        .size(100, 20)
                        .build()
        );

        // 返回按钮
        this.addRenderableWidget(
                Button.builder(Component.translatable(MessageKeys.INVITE_BACK_BUTTON), button -> {
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
