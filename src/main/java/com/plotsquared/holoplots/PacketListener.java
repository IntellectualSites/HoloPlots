package com.plotsquared.holoplots;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import org.bukkit.entity.Player;

public class PacketListener {

    public PacketListener() {
        final ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        try {
            PacketAdapter.AdapterParameteters mapChunkBulkParam = new PacketAdapter.AdapterParameteters().serverSide().optionAsync()
                    .types((PacketType) PacketType.Play.Server.class.getDeclaredField("MAP_CHUNK_BULK").get(null)).listenerPriority(ListenerPriority.HIGHEST).plugin(
                    HoloPlotsPlugin.THIS);
            manager.addPacketListener(new PacketAdapter(mapChunkBulkParam) {
                @Override
                public void onPacketSending(final PacketEvent event) {
                    final PacketContainer packet = event.getPacket();
                    final int[] x = packet.getIntegerArrays().read(0);
                    final int[] z = packet.getIntegerArrays().read(1);
                    final Player player = event.getPlayer();
                    for (int i = 0; i < x.length; i++) {
                        final ChunkWrapper chunk = new ChunkWrapper(x[i], z[i], player.getWorld().getName());
                        TaskManager.getPlatformImplementation().taskLater(() -> HoloPlotsPlugin.HOLO.updatePlayer(player, chunk), TaskTime
                            .ticks(20));
                    }
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }

        PacketAdapter.AdapterParameteters mapChunkParam = new PacketAdapter.AdapterParameteters().optionAsync()
                .types(PacketType.Play.Server.MAP_CHUNK).listenerPriority(ListenerPriority.NORMAL).plugin(
                HoloPlotsPlugin.THIS);
        manager.addPacketListener(new PacketAdapter(mapChunkParam) {
            @Override
            public void onPacketSending(final PacketEvent event) {
                final PacketContainer packet = event.getPacket();
                final int x = packet.getIntegers().read(0);
                final int z = packet.getIntegers().read(1);
                final Player player = event.getPlayer();
                final ChunkWrapper chunk = new ChunkWrapper(x, z, player.getWorld().getName());
                TaskManager.getPlatformImplementation().taskLater(() -> HoloPlotsPlugin.HOLO.updatePlayer(player, chunk), TaskTime
                    .ticks(20));
            }
        });
    }
}
