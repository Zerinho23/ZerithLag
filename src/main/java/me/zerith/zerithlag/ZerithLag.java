package me.zerith.zerithlag;

import org.bukkit.plugin.java.JavaPlugin;

public class ZerithLag extends JavaPlugin {

    public static final String AUTHOR = "zerinho23";

    // ── ANSI color codes for the server console ───────────────────────────────
    // Paper's console (jline) supports 24-bit true-color ANSI escape sequences.
    private static final String ANSI_RESET   = "\u001B[0m";
    private static final String ANSI_GOLD    = "\u001B[38;2;255;170;0m";    // §6
    private static final String ANSI_YELLOW  = "\u001B[38;2;255;255;85m";   // §e
    private static final String ANSI_WHITE   = "\u001B[38;2;255;255;255m";  // §f
    private static final String ANSI_GRAY    = "\u001B[38;2;170;170;170m";  // §7
    private static final String ANSI_DGRAY   = "\u001B[38;2;85;85;85m";     // §8
    private static final String ANSI_GREEN   = "\u001B[38;2;85;255;85m";    // §a
    private static final String ANSI_CYAN    = "\u001B[38;2;85;255;255m";   // §b

    private static ZerithLag instance;
    private ConfigManager configManager;
    private StatsManager statsManager;
    private TpsMonitor tpsMonitor;
    private ClearTask clearTask;

    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this);
        configManager.loadConfig();

        statsManager = new StatsManager();
        tpsMonitor   = new TpsMonitor(this);
        tpsMonitor.start();

        ZerithCommand cmd = new ZerithCommand(this);
        getCommand("zerith").setExecutor(cmd);
        getCommand("zerith").setTabCompleter(cmd);

        startClearTask();
        printBanner();
    }

    @Override
    public void onDisable() {
        if (clearTask  != null) clearTask.cancel();
        if (tpsMonitor != null) tpsMonitor.cancel();
        getLogger().info(
                ANSI_GOLD + "ZerithLag" + ANSI_GRAY + " deshabilitado. " +
                ANSI_GRAY + "Total eliminadas: " +
                ANSI_YELLOW + statsManager.getTotalCleared() + ANSI_RESET);
    }

    public void startClearTask() {
        if (clearTask != null) {
            clearTask.cancel();
            clearTask = null;
        }
        if (configManager.isAutoClearEnabled()) {
            clearTask = new ClearTask(this);
            clearTask.runTaskTimer(this, configManager.getClearInterval() * 20L, 20L);
        }
    }

    public void reload() {
        configManager.loadConfig();
        startClearTask();
        getLogger().info(ANSI_GREEN + "Configuración recargada correctamente." + ANSI_RESET);
    }

    private void printBanner() {
        String v = getPluginMeta().getVersion();
        String G = ANSI_GOLD;
        String Y = ANSI_YELLOW;
        String W = ANSI_WHITE;
        String C = ANSI_CYAN;
        String A = ANSI_GREEN;
        String D = ANSI_DGRAY;
        String S = ANSI_GRAY;
        String R = ANSI_RESET;

        getLogger().info(G + "        ______           _ __  __  __                    " + R);
        getLogger().info(G + "       /_  / /__ _______(_) /_/ / / /  ___ ___ _        " + R);
        getLogger().info(G + "        / / / -_) __/ _/ / __/ /_/ /__/ _ `/ _ `/       " + R);
        getLogger().info(G + "       /___/\\__/_/ /_/ /_/\\__/____/____/\\_,_/\\_, /  " + R);
        getLogger().info(G + "                                              /___/      " + R);
        getLogger().info("");
        getLogger().info(
                D + "  ┌─────────────────────────────────────────┐" + R);
        getLogger().info(
                D + "  │  " + Y + "Plugin" + D + "  " + W + "ZerithLag " + Y + "v" + v +
                D + "                        │" + R);
        getLogger().info(
                D + "  │  " + S + "Autor " + D + "  " + C + AUTHOR +
                D + "                              │" + R);
        getLogger().info(
                D + "  │  " + S + "Estado" + D + "  " + A + "✔ Cargado correctamente" +
                D + "          │" + R);
        getLogger().info(
                D + "  │  " + S + "MC    " + D + "  " + W + "1.20 → 1.21+" +
                D + "                       │" + R);
        getLogger().info(
                D + "  └─────────────────────────────────────────┘" + R);
        getLogger().info("");
    }

    public static ZerithLag getInstance()      { return instance; }
    public ConfigManager   getConfigManager()  { return configManager; }
    public StatsManager    getStatsManager()   { return statsManager; }
    public TpsMonitor      getTpsMonitor()     { return tpsMonitor; }
}
