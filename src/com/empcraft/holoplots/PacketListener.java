package com.empcraft.holoplots;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

public class PacketListener {

    public PacketListener() {
        final ProtocolManager protocolmanager = ProtocolLibrary.getProtocolManager();
        protocolmanager.addPacketListener(new PacketAdapter(Main.THIS, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGHEST, Packets.Server.MAP_CHUNK_BULK) {
            @Override
            public void onPacketSending(final PacketEvent event) {
                final PacketContainer packet = event.getPacket();
                final int[] x = packet.getIntegerArrays().read(0);
                final int[] z = packet.getIntegerArrays().read(1);
                final Player player = event.getPlayer();
                for (int i = 0; i < x.length; i++) {
                    final ChunkWrapper chunk = new ChunkWrapper(x[i], z[i], player.getWorld().getName());
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.THIS, new Runnable() {
                        @Override
                        public void run() {
                            Main.HOLO.updatePlayer(player, chunk);                        
                        }
                    }, 20);
                }
            }
        });

        protocolmanager.addPacketListener(new PacketAdapter(Main.THIS, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGHEST, Packets.Server.MAP_CHUNK) {
            @Override
            public void onPacketSending(final PacketEvent event) {
                final PacketContainer packet = event.getPacket();
                final int x = packet.getIntegers().read(0);
                final int z = packet.getIntegers().read(1);
                final Player player = event.getPlayer();
                final ChunkWrapper chunk = new ChunkWrapper(x, z, player.getWorld().getName());
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.THIS, new Runnable() {
                    @Override
                    public void run() {
                        Main.HOLO.updatePlayer(player, chunk);                        
                    }
                }, 20);
            }
        });
    }
}
