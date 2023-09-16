package com.plotsquared.holoplots;

import com.plotsquared.core.PlotAPI;
import com.plotsquared.holoplots.config.Configuration;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;

public class HoloPlotsPlugin extends JavaPlugin {

    private static final int BSTATS_ID = 6402;
    public static HoloPlotsPlugin THIS;
    public static IHoloUtil HOLO = null;

    @SuppressWarnings("deprecation") // Paper deprecation
    @Override
    public void onEnable() {
        if (!Bukkit.getPluginManager().getPlugin("PlotSquared").getDescription().getVersion().startsWith("7")) {
            getLogger().severe("PlotSquared 7.x is required for HoloPlots to work!");
            getLogger().severe("Please update PlotSquared: https://www.spigotmc.org/resources/77506/");
            getLogger().severe("Disabling HoloPlots...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (!Bukkit.getPluginManager().getPlugin("HolographicDisplays").getDescription().getVersion().startsWith("3")) {
            getLogger().severe("HolographicDisplays 3.x is required for HoloPlots to work!");
            getLogger().severe("Please update HolographicDisplays: https://dev.bukkit.org/projects/holographic-displays/files");
            getLogger().severe("Disabling HoloPlots...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        HoloPlotsPlugin.THIS = this;

        Configuration.load(new File(getDataFolder(), "settings.yml"), Configuration.class);
        Configuration.save(new File(getDataFolder(), "settings.yml"), Configuration.class);

        new PacketListener();
        HOLO = new PSHoloUtil();
        // Enable metrics
        new Metrics(this, BSTATS_ID);
        new PlotAPI().registerListener(HOLO);
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

}
