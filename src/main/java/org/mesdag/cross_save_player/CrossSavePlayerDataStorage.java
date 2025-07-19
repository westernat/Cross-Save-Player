package org.mesdag.cross_save_player;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.FileNameDateFormatter;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@EventBusSubscriber(modid = CrossSavePlayer.MODID)
public final class CrossSavePlayerDataStorage {
    private static final File playerDir = FMLPaths.GAMEDIR.get().resolve("cross_save_player_data").toFile();
    private static final DateTimeFormatter FORMATTER = FileNameDateFormatter.create();

    @SubscribeEvent
    public static void player$SaveToFile(PlayerEvent.SaveToFile event) {
        if (event.getEntity() instanceof ServerPlayer player && player.server.isSingleplayerOwner(player.getGameProfile())) {
            try {
                CompoundTag tag = player.saveWithoutId(new CompoundTag());
                playerDir.mkdirs();
                Path path = playerDir.toPath();
                Path path1 = Files.createTempFile(path, event.getPlayerUUID() + "-", ".dat");
                NbtIo.writeCompressed(tag, path1);
                Path path2 = path.resolve(event.getPlayerUUID() + ".dat");
                Path path3 = path.resolve(event.getPlayerUUID() + ".dat_old");
                Util.safeReplaceFile(path2, path1, path3);
            } catch (Exception exception) {
                CrossSavePlayer.LOGGER.warn("Failed to save player data for {}", player.getName().getString());
            }
        }
    }

    private static void backup(Player player) {
        Path path = playerDir.toPath();
        Path path1 = path.resolve(player.getStringUUID() + ".dat");
        Path path2 = path.resolve(player.getStringUUID() + "_corrupted_" + LocalDateTime.now().format(FORMATTER) + ".dat");
        if (Files.isRegularFile(path1)) {
            try {
                Files.copy(path1, path2, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            } catch (Exception exception) {
                CrossSavePlayer.LOGGER.warn("Failed to copy the player.dat file for {}", player.getName().getString(), exception);
            }
        }
    }

    private static Optional<CompoundTag> load(Player player, String suffix) {
        File file1 = new File(playerDir, player.getStringUUID() + suffix);
        if (file1.exists() && file1.isFile()) {
            try {
                return Optional.of(NbtIo.readCompressed(file1.toPath(), NbtAccounter.unlimitedHeap()));
            } catch (Exception exception) {
                CrossSavePlayer.LOGGER.warn("Failed to load player data for {}", player.getName().getString());
            }
        }

        return Optional.empty();
    }

    public static Optional<CompoundTag> load(Player player) {
        Optional<CompoundTag> optional = load(player, ".dat");
        if (optional.isEmpty()) {
            backup(player);
        }

        return optional.or(() -> load(player, ".dat_old")).map(tag -> {
            player.load(tag);
            return tag;
        });
    }
}
