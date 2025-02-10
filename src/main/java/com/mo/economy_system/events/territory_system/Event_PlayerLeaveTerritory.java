package com.mo.economy_system.events.territory_system;

import com.mo.economy_system.system.territory_system.Territory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

public class Event_PlayerLeaveTerritory extends Event {
    private final ServerPlayer player;
    private final Territory territory;

    public Event_PlayerLeaveTerritory(ServerPlayer player, Territory territory) {
        this.player = player;
        this.territory = territory;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public Territory getTerritory() {
        return territory;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}

