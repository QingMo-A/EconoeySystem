package com.mo.economy_system.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements WorldGenLevel {

    private static final ZoneId EAST_8_ZONE = ZoneId.of("Asia/Shanghai");

    // 初始游戏时间，仅设置一次
    private static boolean initialized = false;

    protected ServerLevelMixin(WritableLevelData p_270739_, ResourceKey<Level> p_270683_, RegistryAccess p_270200_, Holder<DimensionType> p_270240_, Supplier<ProfilerFiller> p_270692_, boolean p_270904_, boolean p_270470_, long p_270248_, int p_270466_) {
        super(p_270739_, p_270683_, p_270200_, p_270240_, p_270692_, p_270904_, p_270470_, p_270248_, p_270466_);
    }

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

        // 调整：现实时间下午2点，映射为游戏内的白天时间段
        long gameTime;

        if (hour >= 6 && hour < 18) {
            // 白天：游戏时间映射到白天时段 0-12000（白天）
            gameTime = (hour - 6) * 1000 + (minute * 1000 / 60) + (second * 1000 / 3600);
            gameTime = gameTime % 12000; // 保证在 0 到 12000 之间
        } else {
            // 夜晚：游戏时间映射到夜晚时段 12000-24000（夜晚）
            gameTime = ((hour - 18) + 24) * 1000 + (minute * 1000 / 60) + (second * 1000 / 3600);
            gameTime = (12000 + gameTime % 12000) % 24000; // 保证在 12000 到 24000 之间
        }

        return gameTime;
    }
}
