package org.mesdag.cross_save_player.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.mesdag.cross_save_player.Configs;
import org.mesdag.cross_save_player.CrossSavePlayerDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(method = "load", at = @At("RETURN"), cancellable = true)
    private void redirect(ServerPlayer player, CallbackInfoReturnable<Optional<CompoundTag>> cir) {
        if (player.server.isSingleplayerOwner(player.getGameProfile())) {
            Optional<CompoundTag> load = CrossSavePlayerDataStorage.load(player);
            if (!Configs.MAINTAIN_POSITION.get()) {
                load.ifPresent(tag -> tag.putString("Dimension", Level.OVERWORLD.location().toString()));
            }
            cir.setReturnValue(load);
        }
    }

    @WrapOperation(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;teleport(DDDFF)V"))
    private void redirect(ServerGamePacketListenerImpl instance, double x, double y, double z, float yaw, float pitch, Operation<Void> original, @Local(argsOnly = true) ServerPlayer player) {
        if (!Configs.MAINTAIN_POSITION.get() && player.server.isSingleplayerOwner(player.getGameProfile())) {
            ServerLevel overworld = player.server.overworld();
            Vec3 position = player.adjustSpawnLocation(overworld, overworld.getSharedSpawnPos()).getBottomCenter();
            original.call(instance, position.x, position.y, position.z, yaw, pitch);
        } else {
            original.call(instance, x, y, z, yaw, pitch);
        }
    }
}
