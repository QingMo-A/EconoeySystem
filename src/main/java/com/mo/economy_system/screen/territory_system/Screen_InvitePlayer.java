package com.mo.economy_system.screen.territory_system;

import com.mo.economy_system.network.EconomySystem_NetworkManager;
import com.mo.economy_system.network.packets.territory_system.Packet_InvitePlayer;
import com.mo.economy_system.core.territory_system.Territory;
import com.mo.economy_system.utils.Util_MessageKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class Screen_InvitePlayer extends Screen {
    private final Territory territory;
    private EditBox playerNameField;

    public Screen_InvitePlayer(Territory territory) {
        super(Component.translatable(Util_MessageKeys.INVITE_TITLE_KEY));
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
                Button.builder(Component.translatable(Util_MessageKeys.INVITE_INVITE_BUTTON_KEY), button -> {
                            String playerName = playerNameField.getValue();
                            if (!playerName.isEmpty()) {
                                EconomySystem_NetworkManager.INSTANCE.sendToServer(new Packet_InvitePlayer(territory.getTerritoryID(), playerName));
                                this.minecraft.setScreen(null); // 关闭当前界面
                            } else {
                                this.minecraft.player.sendSystemMessage(Component.translatable(Util_MessageKeys.INVITE_NO_NAME_KEY));
                            }
                        }).pos(centerX - 50, centerY + 20)
                        .size(100, 20)
                        .build()
        );

        // 返回按钮
        this.addRenderableWidget(
                Button.builder(Component.translatable(Util_MessageKeys.INVITE_BACK_BUTTON), button -> {
                            this.minecraft.setScreen(new Screen_Territory());
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

    @Override
    public boolean keyPressed(int p_96552_, int p_96553_, int p_96554_) {
        if (p_96552_ == 256 && this.shouldCloseOnEsc()) {
            Minecraft.getInstance().setScreen(new Screen_Territory());
            return true;
        }
        return  false;
    }
}
