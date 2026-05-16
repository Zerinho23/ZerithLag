package me.zerith.zerithlag;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Pre-loads chunks around each world's spawn point asynchronously
 * so players experience no chunk-generation lag when they first enter.
 *
 * Flow:
 *  1. Waits 2 seconds after plugin enable (server finishes booting).
 *  2. For each configured world, collects chunks within the radius
 *     that are not already loaded.
 *  3. Loads them in small batches (5 per tick) to avoid lag spikes.
 *  4. Logs progress and completion per world to the console.
 */
public class ChunkPreloader {

    private static final int BATCH_SIZE   = 5;   // chunks loaded per tick
    private static final long START_DELAY = 40L;  // ticks after enable (2 s)

    private final ZerithLag plugin;

    public ChunkPreloader(ZerithLag plugin) {
        this.plugin = plugin;
    }

    /** Schedule the pre-load to start after the server finishes booting. */
    public void start() {
        ConfigManager cfg = plugin.getConfigManager();
        if (!cfg.isChunkPreloaderEnabled()) return;

        Bukkit.getScheduler().runTaskLater(plugin, this::preloadAll, START_DELAY);
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private void preloadAll() {
        ConfigManager cfg    = plugin.getConfigManager();
        int           radius = cfg.getChunkPreloaderRadius();
        List<String>  worlds = cfg.getChunkPreloaderWorlds();

        for (World world : Bukkit.getWorlds()) {
            boolean all = worlds.contains("all");
            if (!all && !worlds.contains(world.getName())) continue;
            preloadWorld(world, radius);
        }
    }

    private void preloadWorld(World world, int radius) {
        Location spawn   = world.getSpawnLocation();
        int      centerX = spawn.getBlockX() >> 4;
        int      centerZ = spawn.getBlockZ() >> 4;

        List<int[]> toLoad = new ArrayList<>();
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                if (!world.isChunkLoaded(x, z)) {
                    toLoad.add(new int[]{x, z});
                }
            }
        }

        int total = toLoad.size();
        if (total == 0) {
            plugin.getLogger().info(
                    "[ChunkPreloader] " + world.getName()
                    + ": todos los chunks del spawn ya están cargados.");
            return;
        }

        int diameter = radius * 2 + 1;
        plugin.getLogger().info(
                "[ChunkPreloader] " + world.getName()
                + " — pre-cargando " + total + " chunks "
                + "(" + diameter + "×" + diameter + " alrededor del spawn)...");

        loadBatch(world, toLoad, 0, total);
    }

    private void loadBatch(World world, List<int[]> chunks, int index, int total) {
        if (index >= chunks.size()) {
            plugin.getLogger().info(
                    "[ChunkPreloader] ✔ " + world.getName()
                    + ": " + total + " chunks pre-cargados correctamente.");
            return;
        }

        int end = Math.min(index + BATCH_SIZE, chunks.size());
        for (int i = index; i < end; i++) {
            int[] c = chunks.get(i);
            world.getChunkAtAsync(c[0], c[1]);
        }

        Bukkit.getScheduler().runTaskLater(plugin,
                () -> loadBatch(world, chunks, end, total), 1L);
    }
}
