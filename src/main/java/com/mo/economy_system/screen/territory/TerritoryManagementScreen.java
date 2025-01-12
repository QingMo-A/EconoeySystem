package com.mo.economy_system.screen.territory;

import com.mo.economy_system.network.EconomyNetwork;
import com.mo.economy_system.network.packets.RemoveTerritoryPacket;
import com.mo.economy_system.network.packets.RemovePlayerFromTerritoryPacket;
import com.mo.economy_system.network.packets.TerritoryRequestPacket;
import com.mo.economy_system.screen.ListItemScreen;
import com.mo.economy_system.screen.TerritoryScreen;
import com.mo.economy_system.territory.PlayerInfo;
import com.mo.economy_system.territory.Territory;
import com.mo.economy_system.utils.MessageKeys;
import com.mo.economy_system.utils.PlayerUtils;
import com.mo.economy_system.utils.SkullUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TerritoryManagementScreen extends Screen {
    private final Territory territory;

    private int currentPage = 0; // 当前页码
    private static final int BOTTOM_MARGIN = 30; // 距离底部的最小空白高度
    private int playersPerPage; // 动态计算的每页商品数
    private final int GAP = 35; // 动态调整的垂直间距

    private UUID playerUUID;
    private List<PlayerInfo> authorizedPlayers;

    private List<RunnableWithGraphics> renderCache = new ArrayList<>();

    public TerritoryManagementScreen(Territory territory) {
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
                Button.builder(Component.literal("复制领地ID"), button -> {
                            GLFW.glfwSetClipboardString(Minecraft.getInstance().getWindow().getWindow(),
                                    territory.getTerritoryID().toString());
                            this.minecraft.player.sendSystemMessage(Component.literal("领地ID已复制到剪贴板！"));
                        }).pos(this.width - (this.width / 4) - 60, startY)
                        .size(120, 20)
                        .build()
        );

        // 邀请玩家按钮
        this.addRenderableWidget(
                Button.builder(Component.literal("邀请玩家"), button -> {
                            Minecraft.getInstance().setScreen(new InvitePlayerScreen(territory));
                        }).pos(this.width - (this.width / 4) - 60, startY + 55)
                        .size(120, 20)
                        .build()
        );

        // 删除领地按钮
        this.addRenderableWidget(
                Button.builder(Component.literal("删除领地"), button -> {
                            EconomyNetwork.INSTANCE.sendToServer(new RemoveTerritoryPacket(territory.getTerritoryID()));
                            this.minecraft.setScreen(null); // 关闭界面
                        }).pos(this.width - (this.width / 4) - 60, startY + 110)
                        .size(120, 20)
                        .build()
        );

        // 显示有权限玩家列表
        authorizedPlayers = territory.getAuthorizedPlayers().stream().toList();

        if (authorizedPlayers.isEmpty()) {
            renderCache.add((guiGraphics) -> {
                int textWidth = this.font.width(Component.translatable(MessageKeys.TERRITORY_TERRITORY_NO_AUTHORIZED_PLAYER_KEY));
                int xPosition = (this.width / 4) - (textWidth / 2);
                guiGraphics.drawString(this.font, Component.translatable(MessageKeys.TERRITORY_TERRITORY_NO_AUTHORIZED_PLAYER_KEY), xPosition, startY, 0xFFFFFF, false);
            });
            return;
        }

        int startIndex = currentPage * playersPerPage;
        int endIndex = Math.min(startIndex + playersPerPage, authorizedPlayers.size());

        for (int i = startIndex; i < endIndex; i++) {
            PlayerInfo playerInfo = authorizedPlayers.get(i);

            final int currentY = y; // 使用最终变量供 Lambda 表达式使用

            // 添加图标渲染任务
            // renderCache.add((guiGraphics) -> guiGraphics.renderItem(SkullUtils.createPlayerHead(playerUUID, PlayerUtils.getPlayerNameByUUID(playerUUID.)), startX, currentY));

            // 添加物品名称渲染任务
            renderCache.add((guiGraphics) -> guiGraphics.drawString(this.font, playerInfo.getName(), startX + 20, currentY + 5, 0xFFFFFF, false));

            addKickButton(playerInfo.getUuid(), (this.width / 2) - startX, currentY);

            y += GAP;
        }
    }

    private void addKickButton(UUID playerUUID, int buttonX, int buttonY) {
        this.addRenderableWidget(
                Button.builder(Component.literal("踢出"), button -> {
                            EconomyNetwork.INSTANCE.sendToServer(new RemovePlayerFromTerritoryPacket(territory.getTerritoryID(), playerUUID));
                            this.minecraft.player.sendSystemMessage(Component.literal("已移除玩家: " + playerUUID));
                            EconomyNetwork.INSTANCE.sendToServer(new TerritoryRequestPacket());
                            this.minecraft.setScreen(new TerritoryScreen());
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
}
