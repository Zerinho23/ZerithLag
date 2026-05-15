package me.zerith.zerithlag;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Periodically checks TPS and fires an emergency entity clear
 * if the server drops below the configured threshold.
 *
 * Runs every 10 seconds (200 ticks). Has a configurable cooldown
 * to prevent repeated triggers during sustained lag.
 */
public class TpsClearTask extends BukkitRunnable {

    private static final int CHECK_INTERVAL_S = 10;

    private final ZerithLag plugin;
    private int cooldownSeconds = 0;

    public TpsClearTask(ZerithLag plugin) {
        this.plugin = plugin;
    }

    public void start() {
        runTaskTimer(plugin, 200L, 200L);
    }

    @Override
    public void run() {
        ConfigManager cfg = plugin.getConfigManager();
        if (!cfg.isTpsClearEnabled()) return;

        if (cooldownSeconds > 0) {
            cooldownSeconds -= CHECK_INTERVAL_S;
            return;
        }

        double tps       = plugin.getTpsMonitor().getTps();
        double threshold = cfg.getTpsClearThreshold();

        if (tps < threshold) {
            int cleared = new ClearTask(plugin).clearEntities();
            plugin.getStatsManager().recordClear(cleared);

            String raw = cfg.getTpsClearMessage()
                    .replace("{tps}",    String.format("%.2f", tps))
                    .replace("{amount}", String.valueOf(cleared));

            Component full = cfg.getPrefix().append(cfg.component(raw));
            Bukkit.broadcast(full);

            plugin.getLogger().warning(
                    "TPS bajo (" + String.format("%.2f", tps) + " < " + threshold
                    + ") — limpieza de emergencia: " + cleared + " entidades eliminadas.");

            cooldownSeconds = cfg.getTpsClearCooldown();
        }
    }
}
