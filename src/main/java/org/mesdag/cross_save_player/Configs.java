package org.mesdag.cross_save_player;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Configs {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue MAINTAIN_POSITION = BUILDER.define("maintainPosition", false);

    static final ModConfigSpec SPEC = BUILDER.build();
}
