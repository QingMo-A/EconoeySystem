package com.mo.economy_system.screen.territory_system;

import com.mo.economy_system.network.EconomySystem_NetworkManager;
import com.mo.economy_system.network.packets.territory_system.Packet_TeleportToTerritory;
import com.mo.economy_system.network.packets.territory_system.Packet_TerritoryDataRequest;
import com.mo.economy_system.screen.Screen_Home;
import com.mo.economy_system.system.territory_system.Territory;
import com.mo.economy_system.utils.Util_MessageKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.*;

public class Screen_Territory extends Screen {

    private List<Territory> allTerritories = new ArrayList<>(); // 拥有的领地
    private List<Territory> ownedTerritories = new ArrayList<>(); // 拥有的领地
    private List<Territory> authorizedTerritories = new ArrayList<>(); // 有权限的领地

    private int currentPage = 0; // 当前页码
    private int itemsPerPage; // 每页显示的领地数量

    private static final int GAP = 35; // 每个领地之间的间距

    private List<RunnableWithGraphics> renderCache = new ArrayList<>();

    public Screen_Territory() {
        super(Component.translatable(Util_MessageKeys.TERRITORY_TITLE_KEY));
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        // 添加分页按钮
        addPageButtons();

        // 初始化渲染缓存（在所有按钮添加后调用）
        initializeRenderCache();

        if (this.minecraft.player != null) {
            EconomySystem_NetworkManager.INSTANCE.sendToServer(new Packet_TerritoryDataRequest());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);

        // 执行渲染缓存中的任务
        for (RunnableWithGraphics task : renderCache) {
            task.run(guiGraphics);
        }

        // 如果有商品，进行鼠标悬停检测并显示 Tooltip
        if (!allTerritories.isEmpty()) {
            detectMouseHoverAndRenderTooltip(guiGraphics, mouseX, mouseY);
        }

        // 渲染当前页数
        guiGraphics.drawCenteredString(this.font, (currentPage + 1) + " / " + getTotalPages(),
                this.width / 2, this.height - 33, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    private void initializeRenderCache() {
        renderCache.clear(); // 清空旧的缓存
        allTerritories.clear();

        if (ownedTerritories.isEmpty() && authorizedTerritories.isEmpty()) {
            // 没有领地时，显示提示信息
            renderCache.add((guiGraphics) -> {
                int textWidth = this.font.width(Component.translatable(Util_MessageKeys.TERRITORY_NO_TERRITORIES_TEXT_KEY));
                int xPosition = (this.width - textWidth) / 2;
                guiGraphics.drawString(this.font, Component.translatable(Util_MessageKeys.TERRITORY_NO_TERRITORIES_TEXT_KEY), xPosition, this.height / 2 - 10, 0xFFFFFF, false);
            });
            return;
        }

        // 动态计算每页可显示的商品数和间距
        int availableHeight = this.height - 100; // 减去顶部和底部的空白区域
        itemsPerPage = Math.max(1, availableHeight / GAP); // 至少显示 1 件商品

        // 合并所有领地
        allTerritories = new ArrayList<>();
        allTerritories.addAll(ownedTerritories); // 首先添加拥有的领地
        allTerritories.addAll(authorizedTerritories); // 再添加有权限但不重复的领地

        // 当前页的起始和结束索引
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allTerritories.size());

        int startX = Math.max((this.width / 2) - 300, 60);
        int startY = Math.max((this.height - 400) / 4, 40);

        int y = startY;

        // 渲染领地列表
        for (int i = startIndex; i < endIndex; i++) {
            Territory territory = allTerritories.get(i);

            final int currentY = y; // 使用最终变量供 Lambda 表达式使用

            // 添加图标渲染任务
            renderCache.add((guiGraphics) -> {
                if (ownedTerritories.contains(territory)) {
                    if (territory.getDimension().equals(Level.OVERWORLD)) {
                        guiGraphics.renderItem(Items.GRASS_BLOCK.getDefaultInstance(), startX, currentY);
                    } else if (territory.getDimension().equals(Level.NETHER)) {
                        guiGraphics.renderItem(Items.NETHERRACK.getDefaultInstance(), startX, currentY);
                    } else if (territory.getDimension().equals(Level.END)) {
                        guiGraphics.renderItem(Items.END_STONE.getDefaultInstance(), startX, currentY);
                    } else {
                        guiGraphics.renderItem(Items.BEDROCK.getDefaultInstance(), startX, currentY);
                    }

                } else {
                    guiGraphics.renderItem(Items.OAK_DOOR.getDefaultInstance(), startX, currentY);
                }
            });


            // 添加图标渲染任务
            renderCache.add((guiGraphics) -> {
                guiGraphics.drawString(this.font, Component.translatable(Util_MessageKeys.TERRITORY_TERRITORY_NAME_TEXT_KEY, territory.getName()), startX+ 20, currentY + 5, 0xFFFFFF);
            });
            renderCache.add((guiGraphics) -> {
                guiGraphics.drawString(this.font, Component.translatable(Util_MessageKeys.TERRITORY_TERRITORY_AREA_TEXT_KEY,
                                "范围: " +
                                territory.getPos1().getX() + " " + territory.getPos1().getY() + " " + territory.getPos1().getZ()
                                + " -> " +
                                territory.getPos2().getX() + " " + territory.getPos2().getY() + " " + territory.getPos2().getZ()) ,
                        startX, currentY + 18, 0xAAAAAA);
            });

            // 如果是拥有的领地，显示“传送”和"管理"按钮
            if (ownedTerritories.contains(territory)) {
                this.addRenderableWidget(
                        Button.builder(Component.translatable(Util_MessageKeys.TERRITORY_TELEPORT_BUTTON_KEY), button -> {
                                    EconomySystem_NetworkManager.INSTANCE.sendToServer(new Packet_TeleportToTerritory(territory.getTerritoryID()));
                                }).pos(this.width - startX - 60 - 80, currentY)
                                .size(60, 20)
                                .build()
                );
                this.addRenderableWidget(
                        Button.builder(Component.translatable(Util_MessageKeys.TERRITORY_MANAGE_BUTTON_KEY), button -> {
                                    Minecraft.getInstance().setScreen(new Screen_ManageTerritory(territory));
                                }).pos(this.width - startX - 60, currentY)
                                .size(60, 20)
                                .build()
                );
            } else {
                // 如果是有权限的领地，显示“传送”按钮
                this.addRenderableWidget(
                        Button.builder(Component.translatable(Util_MessageKeys.TERRITORY_TELEPORT_BUTTON_KEY), button -> {
                                    EconomySystem_NetworkManager.INSTANCE.sendToServer(new Packet_TeleportToTerritory(territory.getTerritoryID()));
                                }).pos(this.width - startX - 60, currentY)
                                .size(60, 20)
                                .build()
                );
            }

            y += GAP;
        }
    }

    private void detectMouseHoverAndRenderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int startX = Math.max((this.width / 2) - 300, 60);
        int startY = Math.max((this.height - 400) / 4, 40);

        int y = startY;

        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allTerritories.size());

        for (int i = startIndex; i < endIndex; i++) {
            Territory territory = allTerritories.get(i);

            if (isMouseOver(mouseX, mouseY, startX, y, 16, 16)) {
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(Component.translatable(Util_MessageKeys.TERRITORY_TERRITORY_NAME_KEY, territory.getName()));
                tooltip.add(Component.translatable(Util_MessageKeys.TERRITORY_TERRITORY_UUID_KEY, territory.getTerritoryID()));
                tooltip.add(Component.translatable(Util_MessageKeys.TERRITORY_TERRITORY_OWNER_NAME_KEY, territory.getOwnerName()));
                tooltip.add(Component.translatable(Util_MessageKeys.TERRITORY_TERRITORY_OWNER_UUID_KEY, territory.getOwnerUUID()));
                if (territory.getBackpoint() != null) {
                    tooltip.add(Component.translatable(Util_MessageKeys.TERRITORY_TERRITORY_BACK_POINT_KEY, territory.getBackpoint().getX(), territory.getBackpoint().getY(), territory.getBackpoint().getZ()));
                } else {
                    tooltip.add(Component.translatable(Util_MessageKeys.TERRITORY_TERRITORY_BACK_POINT_KEY, "null", "null", "null"));
                }

                guiGraphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
            }

            y += GAP;
        }
    }

    private void addPageButtons() {
        int startX = Math.max((this.width / 2 - 150), 60);
        int buttonWidth = 40;
        int buttonHeight = 20;
        int buttonY = this.height - 40;

        // 上一页按钮
        this.addRenderableWidget(
                Button.builder(Component.literal("<"), button -> {
                            if (currentPage > 0) {
                                currentPage--;
                                this.init(); // 刷新页面
                            }
                        }).pos(startX, buttonY)
                        .size(buttonWidth, buttonHeight)
                        .build()
        );

        // 下一页按钮
        this.addRenderableWidget(
                Button.builder(Component.literal(">"), button -> {
                            if ((currentPage + 1) * itemsPerPage < ownedTerritories.size() + authorizedTerritories.size()) {
                                currentPage++;
                                this.init(); // 刷新页面
                            }
                        }).pos(this.width - startX - buttonWidth, buttonY)
                        .size(buttonWidth, buttonHeight)
                        .build()
        );
    }

    private boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void updateTerritoryData(List<Territory> owned, List<Territory> authorized) {
        this.ownedTerritories.clear(); // 清空旧的拥有领地
        this.authorizedTerritories.clear(); // 清空旧的有权限领地
        this.ownedTerritories.addAll(owned); // 更新拥有的领地
        this.authorizedTerritories.addAll(authorized); // 更新有权限的领地
        this.init(); // 刷新界面
    }

    @Override
    public boolean isPauseScreen() {
        return false; // 界面打开时不暂停游戏
    }

    // 动态计算总页数
    private int getTotalPages() {
        return (int) Math.ceil((double) this.allTerritories.size() / itemsPerPage);
    }

    @FunctionalInterface
    private interface RunnableWithGraphics {
        void run(GuiGraphics guiGraphics);
    }

    @Override
    public boolean keyPressed(int p_96552_, int p_96553_, int p_96554_) {
        if (p_96552_ == 256 && this.shouldCloseOnEsc()) {
            Minecraft.getInstance().setScreen(new Screen_Home());
            return true;
        }
        return  false;
    }
}
