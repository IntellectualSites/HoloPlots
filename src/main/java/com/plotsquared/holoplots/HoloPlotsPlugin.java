package com.plotsquared.holoplots;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.LocaleHolder;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.PlayerManager;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.holoplots.config.Configuration;
import com.plotsquared.holoplots.listener.ChunkListener;
import com.plotsquared.holoplots.listener.PlotSquaredListener;
import com.plotsquared.holoplots.provider.IHologramProvider;
import com.plotsquared.holoplots.provider.impl.DecentHologramsProvider;
import com.plotsquared.holoplots.provider.impl.HolographicDisplaysProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class HoloPlotsPlugin extends JavaPlugin {

    public static final List<Caption> HOLOGRAM_CAPTIONS = List.of(
            TranslatableCaption.of("signs.owner_sign_line_1"),
            TranslatableCaption.of("signs.owner_sign_line_2"),
            TranslatableCaption.of("signs.owner_sign_line_3"),
            TranslatableCaption.of("signs.owner_sign_line_4")
    );
    private static final int BSTATS_ID = 6402;

    private static final Map<String, Function<HoloPlotsPlugin, IHologramProvider>> SUPPORTED_PROVIDERS = Map.of(
            HolographicDisplaysProvider.PLUGIN_NAME, HolographicDisplaysProvider::new,
            DecentHologramsProvider.PLUGIN_NAME, DecentHologramsProvider::new
    );

    private PSHoloUtil holoUtil;

    private IHologramProvider provider;


    @Override
    public void onEnable() {
        Configuration.load(new File(getDataFolder(), "settings.yml"), Configuration.class);
        Configuration.save(new File(getDataFolder(), "settings.yml"), Configuration.class);

        for (final Map.Entry<String, Function<HoloPlotsPlugin, IHologramProvider>> entry : SUPPORTED_PROVIDERS.entrySet()) {
            if (Bukkit.getPluginManager().getPlugin(entry.getKey()) != null) {
                this.provider = entry.getValue().apply(this);
                break;
            }
        }

        if (this.provider == null) {
            getLogger().severe("Missing provider for holograms. Install at least one: " + SUPPORTED_PROVIDERS.keySet());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("Using " + this.provider.getName() + " as the hologram provider");
        this.holoUtil = new PSHoloUtil(this);

        new PlotAPI().registerListener(new PlotSquaredListener(this));
        getServer().getPluginManager().registerEvents(new ChunkListener(this), this);

        // Enable metrics
        new Metrics(this, BSTATS_ID).addCustomChart(new SimplePie(
                "hologram_provider",
                () -> this.provider.getName()
        ));
    }

    /**
     * Create a new {@link ItemStack} for the head of a specific player.
     *
     * @param owner The {@link UUID} of the player for whose head should be created.
     * @return The {@link ItemStack} for the player head.
     */
    public ItemStack getPlayerSkull(UUID owner) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * Packs the hash code of the PlotArea's name and the hash code of the PlotId (X & Z coordinates) into a long.
     *
     * @param plot The plot to calculate the hash code into a long
     * @return The packed long based on the plot area's hash code and the plot id's hash code
     */
    public long hashPlot(Plot plot) {
        return (((long) Objects.requireNonNull(plot.getArea()).hashCode()) << 32) | (plot.hashCode() & 0xffffffffL);
    }

    /**
     * Attempts to translate a List of {@link Caption}s in parallel and returns them in their original order as a combined
     * {@link CompletableFuture}
     *
     * @param plot     The plot context to translate for
     * @param captions A list of captions to translate
     * @return A {@link CompletableFuture} containing a list of translated {@link Component}s in original order
     */
    public CompletableFuture<List<Component>> translateAll(Plot plot, List<Caption> captions) {
        List<CompletableFuture<Component>> futures = captions.stream().map(c -> translate(plot, c)).toList();
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(unused -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull).toList()
                );
    }

    /**
     * Translate the passed caption for the specified plot.
     * Resolves the owner's name - Sadly in every case (even if no owner is displayed), due to the version of MiniMessage
     *
     * @param plot    The plot context to translate for
     * @param caption The caption to translate
     * @return A fully parsed {@link Component}
     */
    public CompletableFuture<Component> translate(Plot plot, Caption caption) {
        return CompletableFuture.supplyAsync(() -> {
            String id = plot.getId().toString();
            Component name = BukkitUtil.MINI_MESSAGE.parse(PlayerManager
                    .resolveName(plot.getOwnerAbs())
                    .getComponent(LocaleHolder.console()));
            return BukkitUtil.MINI_MESSAGE.parse(
                    caption.getComponent(LocaleHolder.console()),
                    Template.of("id", id),
                    Template.of("owner", name)
            );
        }, TaskManager.getPlatformImplementation()::taskAsync);
    }

    public IHologramProvider provider() {
        return provider;
    }

    public PSHoloUtil holoUtil() {
        return holoUtil;
    }

}
