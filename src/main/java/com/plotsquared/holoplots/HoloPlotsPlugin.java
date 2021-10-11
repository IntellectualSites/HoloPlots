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

    @Override
    public void onEnable() {
        HoloPlotsPlugin.THIS = this;

        Configuration.load(new File(getDataFolder(), "settings.yml"), Configuration.class);
        Configuration.save(new File(getDataFolder(), "settings.yml"), Configuration.class);

        new PacketListener();
        HOLO = new PSHoloUtil();
        // Enable metrics
        new Metrics(this, BSTATS_ID);
        new PlotAPI().registerListener(HOLO);
    }

    public ItemStack getPlayerSkull(UUID owner) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

}
