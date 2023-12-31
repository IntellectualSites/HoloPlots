package com.plotsquared.holoplots.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.plotsquared.core.PlotSquared;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigurableSkullTexture {

    private static final UUID STATIC_UUID = UUID.fromString("4aa2aaa4-c06b-485c-bc58-186aa1780d9b");
    private static final Pattern URL_PATTERN = Pattern.compile("(https?://textures\\.minecraft\\.net/texture/)?([a-f0-9]{64})");
    private static final String URL_TEMPLATE = "https://textures.minecraft.net/texture/%s";

    private ItemStack itemStack = null;

    @SuppressWarnings("deprecation") // Paper deprecations
    public ConfigurableSkullTexture(final String texture, final Logger logger) {
        if (texture.isEmpty()) {
            return;
        }
        if (!PaperLib.isPaper() && PlotSquared.platform().serverVersion()[1] < 19) {
            logger.log(Level.SEVERE, "Custom textured skulls require either Paper or 1.19 or higher (best is both)");
            return;
        }

        // If the value length is a multiple of 4 and starts with "ey" (Base64 equivalent of "{") we can be pretty sure it's
        // the base64 encoded texture
        if (texture.startsWith("ey") && texture.length() % 4 == 0) {
            try {
                URL url = new URL(getTextureUrl(JsonParser.parseString(new String(Base64.getDecoder().decode(texture)))));
                final PlayerProfile profile = Bukkit.createProfile(STATIC_UUID);
                PlayerTextures textures = profile.getTextures();
                textures.setSkin(url);
                profile.setTextures(textures);
                this.itemStack = new ItemStack(Material.PLAYER_HEAD);
                final SkullMeta meta = (SkullMeta) this.itemStack.getItemMeta();
                meta.setOwnerProfile(profile);
                this.itemStack.setItemMeta(meta);
            } catch (MalformedURLException e) {
                logger.log(Level.SEVERE, "Invalid skull value: " + texture, e);
            } catch (IllegalStateException | NullPointerException e) {
                logger.log(Level.SEVERE, "Invalid structured skull texture string", e);
            }
            return;
        }

        // Otherwise, the only other supported method is the texture url
        // Supported is either the full texture url or the last path segment
        final Matcher matcher = URL_PATTERN.matcher(texture);
        if (!matcher.find()) {
            logger.log(Level.SEVERE, "Invalid skull value: " + texture);
            return;
        }
        String identifier = matcher.group(2);
        final PlayerProfile profile = Bukkit.createProfile(STATIC_UUID);
        PlayerTextures textures = profile.getTextures();
        try {
            textures.setSkin(new URL(String.format(URL_TEMPLATE, identifier)));
            profile.setTextures(textures);
            this.itemStack = new ItemStack(Material.PLAYER_HEAD);
            final SkullMeta meta = (SkullMeta) this.itemStack.getItemMeta();
            meta.setOwnerProfile(profile);
            this.itemStack.setItemMeta(meta);
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, "Invalid skull data for " + texture, e);
        }
    }

    public @Nullable ItemStack itemStack() {
        return itemStack;
    }

    /**
     * Find the actual texture url from base64 encoded texture data
     * Destructs every json property to mitigate potential unhandled NullPointerExceptions
     */
    private static String getTextureUrl(JsonElement object) throws IllegalStateException {
        JsonObject root = object.getAsJsonObject();
        JsonObject textures = Preconditions.checkNotNull(root.get("textures"), "Missing textures property").getAsJsonObject();
        JsonObject skin = Preconditions.checkNotNull(textures.get("SKIN"), "Missing SKIN property").getAsJsonObject();
        JsonElement url = Preconditions.checkNotNull(skin.get("url"), "Missing url property");
        return url.getAsString();
    }

}
