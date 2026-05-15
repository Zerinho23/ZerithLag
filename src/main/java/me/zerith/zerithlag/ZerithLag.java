package me.zerith.zerithlag;

import org.bukkit.plugin.java.JavaPlugin;

public class ZerithLag extends JavaPlugin {

    public static final String AUTHOR = "zerinho23";

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
        getLogger().info("ZerithLag deshabilitado. Total eliminadas: "
                + statsManager.getTotalCleared());
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
    }

    private void printBanner() {
        String version = getPluginMeta().getVersion();
        getLogger().info("  ____             _ _   _     _               ");
        getLogger().info(" |_  /__ _ _ _ _  (_) |_| |   __ _ __ _       ");
        getLogger().info("  / // -_) '_| || || |  _| |__/ _` / _` |     ");
        getLogger().info(" /___\\___|_|  \\_,_||_|\\__|____\\__,_\\__, |");
        getLogger().info("                                     |___/     ");
        getLogger().info(" v" + version + " | by " + AUTHOR);
        getLogger().info(" Plugin antilag cargado correctamente.");
    }

    public static ZerithLag getInstance()      { return instance; }
    public ConfigManager   getConfigManager()  { return configManager; }
    public StatsManager    getStatsManager()   { return statsManager; }
    public TpsMonitor      getTpsMonitor()     { return tpsMonitor; }
}
