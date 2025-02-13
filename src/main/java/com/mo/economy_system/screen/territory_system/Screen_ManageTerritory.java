package com.mo.economy_system.screen.territory_system;

import com.mo.economy_system.network.EconomySystem_NetworkManager;
import com.mo.economy_system.network.packets.territory_system.Packet_RemoveTerritory;
import com.mo.economy_system.network.packets.territory_system.Packet_RemovePlayer;
import com.mo.economy_system.network.packets.territory_system.Packet_TerritoryDataRequest;
import com.mo.economy_system.core.territory_system.PlayerInfo;
import com.mo.economy_system.core.territory_system.Territory;
import com.mo.economy_system.utils.Util_MessageKeys;
import com.mo.economy_system.utils.Util_Skull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Screen_ManageTerritory extends Screen {
    private final Territory territory;

    private int currentPage = 0; // 当前页码
    private static final int BOTTOM_MARGIN = 30; // 距离底部的最小空白高度
    private int playersPerPage; // 动态计算的每页商品数
    private final int GAP = 35; // 动态调整的垂直间距

    private UUID playerUUID;
    private List<PlayerInfo> authorizedPlayers;

    private List<RunnableWithGraphics> renderCache = new ArrayList<>();

    public Screen_ManageTerritory(Territory territory) {
        super(Component.literal("管理领地: " + territory.getName()));
        this.territory = territory;
    }

    @Override
    protected void init() {
        this.clearWidgets();

        if (this.minecraft != null && this.minecraft.player != null) {
            this.playerUUID = this.minecraft.player.getUUID();
        }

        // 初始化渲染缓存（在所有按钮添加后调用）
        initializeRenderCache();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);

        // 执行渲染缓存中的任务
        for (RunnableWithGraphics task : renderCache) {
            task.run(guiGraphics);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    private void initializeRenderCache() {
        renderCache.clear(); // 清空旧的缓存

        // 动态计算每页可显示的商品数和间距
        int availableHeight = this.height - 100; // 减去顶部和底部的空白区域
        playersPerPage = Math.max(1, availableHeight / GAP); // 至少显示 1 件商品

        int startX = Math.max((this.width / 2) - 450, 60);
        int startY = Math.max((this.height - 400) / 4, 40);

        int y = startY;

        // 显示领地 ID
        this.addRenderableWidget(
                Button.builder(Component.translatable(Util_MessageKeys.TERRITORY_MANAGEMENT_COPY_ID), button -> {
                            GLFW.glfwSetClipboardString(Minecraft.getInstance().getWindow().getWindow(),
                                    territory.getTerritoryID().toString());
                            this.minecraft.player.sendSystemMessage(Component.translatable(Util_MessageKeys.TERRITORY_MANAGEMENT_COPY_SUCCESS));
                        }).pos(this.width - (this.width / 4) - 60, startY)
                        .size(120, 20)
                        .build()
        );

        // 邀请玩家按钮
        this.addRenderableWidget(
                Button.builder(Component.translatable(Util_MessageKeys.TERRITORY_MANAGEMENT_INVITE_PLAYER), button -> {
                            Minecraft.getInstance().setScreen(new Screen_InvitePlayer(territory));
                        }).pos(this.width - (this.width / 4) - 60, startY + 55)
                        .size(120, 20)
                        .build()
        );

        // 删除领地按钮
        this.addRenderableWidget(
                Button.builder(Component.translatable(Util_MessageKeys.TERRITORY_MANAGEMENT_DELETE_TERRITORY), button -> {
                            EconomySystem_NetworkManager.INSTANCE.sendToServer(new Packet_RemoveTerritory(territory.getTerritoryID()));
                            this.minecraft.setScreen(null); // 关闭界面
                        }).pos(this.width - (this.width / 4) - 60, startY + 110)
                        .size(120, 20)
                        .build()
        );

        // 显示有权限玩家列表
        authorizedPlayers = territory.getAuthorizedPlayers().stream().toList();

        if (authorizedPlayers.isEmpty()) {
            renderCache.add((guiGraphics) -> {
                int textWidth = this.font.width(Component.translatable(Util_MessageKeys.TERRITORY_TERRITORY_NO_AUTHORIZED_PLAYER_KEY));
                int xPosition = (this.width / 4) - (textWidth / 2);
                guiGraphics.drawString(this.font, Component.translatable(Util_MessageKeys.TERRITORY_TERRITORY_NO_AUTHORIZED_PLAYER_KEY), xPosition, startY, 0xFFFFFF, false);
            });
            return;
        }

        int startIndex = currentPage * playersPerPage;
        int endIndex = Math.min(startIndex + playersPerPage, authorizedPlayers.size());

        for (int i = startIndex; i < endIndex; i++) {
            PlayerInfo playerInfo = authorizedPlayers.get(i);

            final int currentY = y; // 使用最终变量供 Lambda 表达式使用

            // 添加图标渲染任务
            renderCache.add((guiGraphics) -> guiGraphics.renderItem(Util_Skull.createPlayerHead(playerUUID, playerInfo.getName()), startX, currentY));

            // 添加物品名称渲染任务
            renderCache.add((guiGraphics) -> guiGraphics.drawString(this.font, playerInfo.getName(), startX + 20, currentY + 5, 0xFFFFFF, false));
            renderCache.add((guiGraphics) -> guiGraphics.drawString(this.font, String.valueOf(playerInfo.getUuid()), startX, currentY + 18, 0xAAAAAA, false));

            addKickButton(playerInfo.getUuid(), (this.width / 2) - startX, currentY);

            y += GAP;
        }
    }

    private void addKickButton(UUID playerUUID, int buttonX, int buttonY) {
        this.addRenderableWidget(
                Button.builder(Component.translatable(Util_MessageKeys.TERRITORY_MANAGEMENT_KICK_PLAYER), button -> {
                            EconomySystem_NetworkManager.INSTANCE.sendToServer(new Packet_RemovePlayer(territory.getTerritoryID(), playerUUID));
                            EconomySystem_NetworkManager.INSTANCE.sendToServer(new Packet_TerritoryDataRequest());
                            this.minecraft.setScreen(new Screen_Territory());
                        })
                        .pos(buttonX, buttonY)
                        .size(60, 20)
                        .build()
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false; // 打开界面时不暂停游戏
    }

    @FunctionalInterface
    private interface RunnableWithGraphics {
        void run(GuiGraphics guiGraphics);
    }

    @Override
    public boolean keyPressed(int p_96552_, int p_96553_, int p_96554_) {
        if (p_96552_ == 256 && this.shouldCloseOnEsc()) {
            Minecraft.getInstance().setScreen(new Screen_ManageTerritory(territory));
            return true;
        }
        return  false;
    }
}
