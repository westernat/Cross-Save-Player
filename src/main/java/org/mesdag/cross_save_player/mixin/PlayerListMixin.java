package org.mesdag.cross_save_player.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
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
        Optional<CompoundTag> load = CrossSavePlayerDataStorage.load(player);
        if (Configs.MAINTAIN_POSITION.get()) {
            cir.setReturnValue(load);
        }
    }
}
