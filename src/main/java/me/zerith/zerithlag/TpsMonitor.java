package me.zerith.zerithlag;

import org.bukkit.scheduler.BukkitRunnable;

public class TpsMonitor extends BukkitRunnable {

    private static final int SAMPLE_SIZE = 20;
    private final long[] tickTimes = new long[SAMPLE_SIZE];
    private int index = 0;
    private long lastTick = System.currentTimeMillis();

    private final ZerithLag plugin;

    public TpsMonitor(ZerithLag plugin) {
        this.plugin = plugin;
    }

    public void start() {
        runTaskTimer(plugin, 1L, 1L);
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        tickTimes[index % SAMPLE_SIZE] = now - lastTick;
        lastTick = now;
        index++;
    }

    public double getTps() {
        long sum = 0;
        int count = Math.min(index, SAMPLE_SIZE);
        if (count == 0) return 20.0;
        for (int i = 0; i < count; i++) {
            sum += tickTimes[i];
        }
        double avgMs = (double) sum / count;
        double tps = 1000.0 / avgMs;
        return Math.min(20.0, Math.round(tps * 100.0) / 100.0);
    }

    public String getTpsColored() {
        double tps = getTps();
        String color;
        if (tps >= 18.0) color = "&a";
        else if (tps >= 14.0) color = "&e";
        else color = "&c";
        return color + String.format("%.2f", tps);
    }
}
