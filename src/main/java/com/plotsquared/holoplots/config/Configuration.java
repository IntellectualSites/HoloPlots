package com.plotsquared.holoplots.config;

import com.plotsquared.core.configuration.Config;

import java.util.List;
import java.util.logging.Logger;

public class Configuration extends Config {

    @Comment("Set to true, if the owners head should float above the hologram")
    public static boolean SPAWN_PLAYER_HEAD = true;

    @Comment({
            "All lines that will be rendered underneath each other as the plot hologram.",
            "May either reference strings from the PlotSquared message file itself (like 'signs.owner_sign_line_1')",
            "or a plain text message, which supports MiniMessage format.",
            "Supported placeholders are <id> and <owner> representing the plot id and it's owner."
    })
    public static List<String> LINES = List.of(
            "signs.owner_sign_line_1",
            "signs.owner_sign_line_2",
            "signs.owner_sign_line_3",
            "signs.owner_sign_line_4"
    );

    @Comment("Determines the offset on the y-axis from the signs location where the hologram should spawn")
    public static int OFFSET = 6;

    public static String SKULL_SERVER_OWNED = "";

    @Ignore
    public static ConfigurableSkullTexture SKULL_SERVER_OWNED_PARSED;

    public static String SKULL_EVERYONE_OWNED = "";

    @Ignore
    public static ConfigurableSkullTexture SKULL_EVERYONE_OWNED_PARSED;

    public static void deserializeSkullTextures(final Logger logger) {
        SKULL_SERVER_OWNED_PARSED = new ConfigurableSkullTexture(SKULL_SERVER_OWNED, logger);
        SKULL_EVERYONE_OWNED_PARSED = new ConfigurableSkullTexture(SKULL_EVERYONE_OWNED, logger);
    }

}
