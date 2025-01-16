package com.mo.economy_system.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    private static final ZoneId EAST_8_ZONE = ZoneId.of("Asia/Shanghai");

    // 初始游戏时间，仅设置一次
    private static boolean initialized = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void syncRealTime(CallbackInfo ci) {
        ServerLevel serverLevel = (ServerLevel) (Object) this;
        if (!initialized) {
            long realWorldTime = getRealWorldTimeInGameTicks();
            serverLevel.setDayTime(realWorldTime); // 初始化游戏时间为现实时间
            initialized = true;
        }

        // 每 tick 校正时间
        long realWorldTime = getRealWorldTimeInGameTicks();
        serverLevel.setDayTime(realWorldTime);
    }

    /**
     * 获取东八区当前时间对应的游戏时间（以 tick 为单位）
     */
    private static long getRealWorldTimeInGameTicks() {
        ZonedDateTime now = ZonedDateTime.now(EAST_8_ZONE);
        int hour = now.getHour();
        int minute = now.getMinute();
        int second = now.getSecond();

        // 游戏内时间映射: 每小时 = 1000 tick
        return hour * 1000 + (minute * 1000 / 60) + (second * 1000 / 3600);
    }
}
