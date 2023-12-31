package com.plotsquared.holoplots;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.CaptionMap;
import com.plotsquared.core.configuration.caption.LocaleHolder;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.holoplots.config.Configuration;
import com.plotsquared.holoplots.provider.HologramProvider;
import com.plotsquared.holoplots.provider.HologramProviderResolver;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultHoloPlots implements HoloPlots {

    private static final String META_KEY_HOLOGRAM_UPDATE = "holoplots__pending_update";

    private final HoloPlotsPlugin plugin;
    private final HologramProvider provider;
    private List<String> captions;

    public DefaultHoloPlots(final HoloPlotsPlugin plugin, final HologramProviderResolver resolver) {
        this.plugin = plugin;
        this.provider = resolver.findProvider(this).orElseThrow(() -> {
            Bukkit.getPluginManager().disablePlugin(plugin);
            return new NullPointerException("Missing provider for spawning holograms. " +
                    "Check https://github.com/IntellectualSites/HoloPlots for more information"
            );
        });
        //noinspection deprecation - paper deprecation, compatibility with spigot
        String requiredVersion = this.provider.validateVersion(Objects.requireNonNull(Bukkit.getPluginManager()
                .getPlugin(this.provider.getName())).getDescription().getVersion());
        if (requiredVersion != null) {
            logger().severe(this.provider.getName() + " " + requiredVersion + " is required for HoloPlots to work!");
            logger().severe("Please update " + this.provider.getName() + " by checking out their download links at: " +
                    "https://github.com/IntellectualSites/HoloPlots");
            logger().severe("Disabling HoloPlots...");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }

    }

    @Override
    public @NonNull Logger logger() {
        return this.plugin.getLogger();
    }

    @Override
    public @NonNull HologramProvider provider() {
        return this.provider;
    }

    @Override
    public long hashPlot(@NonNull final Plot plot) {
        return (((long) Objects.requireNonNull(plot.getArea()).hashCode()) << 32) | (plot.hashCode() & 0xffffffffL);
    }

    @Override
    public @NonNull List<@NonNull Component> translateLines(@NonNull final Plot plot, @Nullable final Caption username) throws
            NullPointerException {
        // Translate all translatable lines, or just store the raw config value
        if (this.captions == null) {
            this.captions = Configuration.LINES.stream().map(s -> {
                try {
                    return PlotSquared.get().getCaptionMap(TranslatableCaption.DEFAULT_NAMESPACE)
                            .getMessage(TranslatableCaption.of(s));
                } catch (CaptionMap.NoSuchCaptionException ignored) {
                }
                return s;
            }).toList();
        }

        List<Component> list = new ArrayList<>(Configuration.LINES.size());
        for (final String caption : this.captions) {
            list.add(MiniMessage.miniMessage().deserialize(
                    caption,
                    TagResolver.builder()
                            .tag("id", Tag.inserting(Component.text(plot.getId().toString())))
                            .tag("owner", (argumentQueue, context) -> {
                                if (username == null) {
                                    throw new NullPointerException("Expected owner replacement but received null username caption");
                                }
                                return Tag.inserting(MiniMessage
                                        .miniMessage()
                                        .deserialize(username.getComponent(LocaleHolder.console())));
                            })
                            .build()
            ));
        }
        return list;
    }

    @Override
    public @Nullable ItemStack createOwnerSkull(@NonNull final UUID ownerUuid, @Nullable final String username) {
        if (Objects.equals(ownerUuid, DBFunc.SERVER)) {
            return Configuration.SKULL_SERVER_OWNED_PARSED.itemStack();
        }
        if (Objects.equals(ownerUuid, DBFunc.EVERYONE)) {
            return Configuration.SKULL_EVERYONE_OWNED_PARSED.itemStack();
        }
        if (!Configuration.SPAWN_PLAYER_HEAD) {
            return null;
        }
        final ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta meta = (SkullMeta) skull.getItemMeta();
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ownerUuid);
        // OfflinePlayer could be resolved from playercache and can easily be used for texture assigning
        if (offlinePlayer.hasPlayedBefore() && offlinePlayer.getName() != null) {
            meta.setOwningPlayer(offlinePlayer);
            skull.setItemMeta(meta);
            return skull;
        }

        if (username == null) {
            throw new NullPointerException("Require username for further skull generation but was null");
        }

        if (PaperLib.isPaper()) {
            meta.setPlayerProfile(Bukkit.createProfile(ownerUuid, username));
        } else if (PlotSquared.platform().serverVersion()[1] >= 19) {
            // Starting from 1.19 spigot natively supports setting the skull owner by (Game)Profiles
            //noinspection deprecation - paper deprecation
            meta.setOwnerProfile(Bukkit.createProfile(ownerUuid, username));
        } else {
            // If there is no supported method, gamble by setting the owner via its name and hope for the best
            //noinspection deprecation - "valid" fallback
            meta.setOwner(username);
        }
        skull.setItemMeta(meta);
        return skull;
    }

    @Override
    public void updatePlot(@NonNull final Plot plot) {
        // If the plot does have an owner, we can just remove the potential existing hologram
        if (plot.getOwner() == null || !plot.isBasePlot()) {
            this.provider().removeHologram(plot);
            return;
        }
        final PlotArea area = plot.getArea();
        if (area == null) {
            return;
        }
        final Location signLocation = area.getPlotManager().getSignLoc(plot);
        final World world = Bukkit.getWorld(area.getWorldName());
        if (world == null) {
            return;
        }
        final org.bukkit.Location hologramLocation = new org.bukkit.Location(
                world, signLocation.getX() + .5, signLocation.getY() + Configuration.OFFSET, signLocation.getZ() + .5
        );

        // Cancel potential pending update which is still loading metadata, as we want the newest change to have the highest
        // priority
        Object pending;
        if ((pending = plot.getMeta(META_KEY_HOLOGRAM_UPDATE)) != null && pending instanceof CompletableFuture<?> future) {
            future.cancel(true);
        }

        CompletableFuture<Void> future = PlotSquared.platform().playerManager().getUsernameCaption(plot.getOwner())
                .thenAcceptAsync(caption -> {
                    this.provider().createOrUpdateHologram(
                            hologramLocation, plot, this.translateLines(plot, caption),
                            this.createOwnerSkull(
                                    plot.getOwner(),
                                    // If the caption is an StaticCaption, it's definitely a username
                                    caption instanceof StaticCaption sc ? sc.getComponent(LocaleHolder.console()) : null
                            )
                    );
                    plot.deleteMeta(META_KEY_HOLOGRAM_UPDATE);
                }, TaskManager::runTask)
                .exceptionally(throwable -> {
                    this.logger().log(Level.SEVERE, "Failed to update hologram @ " + plot, throwable);
                    return null;
                });
        plot.setMeta(META_KEY_HOLOGRAM_UPDATE, future);
    }

}
