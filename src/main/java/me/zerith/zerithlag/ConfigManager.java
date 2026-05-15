package me.zerith.zerithlag;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacyAmpersand();

    private final ZerithLag plugin;

    public ConfigManager(ZerithLag plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
    }

    // ── General ──────────────────────────────────────────────────────────────

    public Component getPrefix() {
        return component(plugin.getConfig().getString("prefix", "&8[&6ZerithLag&8] &r"));
    }

    // ── Auto-clear ───────────────────────────────────────────────────────────

    public boolean isAutoClearEnabled() {
        return plugin.getConfig().getBoolean("auto-clear.enabled", true);
    }

    public int getClearInterval() {
        return Math.max(10, plugin.getConfig().getInt("auto-clear.interval", 300));
    }

    public List<Integer> getCountdown() {
        return plugin.getConfig().getIntegerList("auto-clear.countdown");
    }

    public boolean shouldBroadcast() {
        return plugin.getConfig().getBoolean("auto-clear.broadcast", true);
    }

    public boolean useActionBar() {
        return plugin.getConfig().getBoolean("auto-clear.use-action-bar", true);
    }

    public String getCountdownMessage() {
        return plugin.getConfig().getString("auto-clear.countdown-message",
                "&eLimpiando entidades en &c{time}s&e...");
    }

    public String getClearMessage() {
        return plugin.getConfig().getString("auto-clear.clear-message",
                "&aEliminadas &e{amount} &aentidades del mundo.");
    }

    // ── Worlds ───────────────────────────────────────────────────────────────

    public List<String> getWorldsToClear() {
        List<String> list = plugin.getConfig().getStringList("auto-clear.worlds");
        return list.isEmpty() ? List.of("all") : list;
    }

    // ── Chat alerts ──────────────────────────────────────────────────────────

    public boolean isChatAlertsEnabled() {
        return plugin.getConfig().getBoolean("chat-alerts.enabled", true);
    }

    /** Message sent when exactly 60 seconds remain. */
    public String getChatMinuteMessage() {
        return plugin.getConfig().getString("chat-alerts.minute-message",
                "&8[&6ZerithLag&8] &r&7La limpieza de entidades ocurrirá en &61 minuto&7.");
    }

    /** Message sent at the times listed in second-alerts (e.g. 30 s). {time} = seconds. */
    public String getChatSecondsMessage() {
        return plugin.getConfig().getString("chat-alerts.seconds-message",
                "&8[&6ZerithLag&8] &r&7La limpieza de entidades ocurrirá en &e{time} segundos&7.");
    }

    /** Seconds (other than 60) that trigger the seconds-message. */
    public List<Integer> getChatSecondAlerts() {
        List<Integer> list = plugin.getConfig().getIntegerList("chat-alerts.second-alerts");
        return list.isEmpty() ? List.of(30) : list;
    }

    /** Countdown message used for the final N seconds. {time} = seconds. */
    public String getChatCountdownMessage() {
        return plugin.getConfig().getString("chat-alerts.countdown-message",
                "&8[&6ZerithLag&8] &r&cLimpiando en &l{time}&c...");
    }

    /** How many seconds before clearing to start the chat countdown (e.g. 5). */
    public int getChatCountdownFrom() {
        return plugin.getConfig().getInt("chat-alerts.countdown-from", 5);
    }

    // ── Entity types ─────────────────────────────────────────────────────────

    public List<EntityType> getEntityTypesToRemove() {
        List<String> names = plugin.getConfig().getStringList("entity-types.remove");
        List<EntityType> types = new ArrayList<>();
        for (String name : names) {
            try {
                types.add(EntityType.valueOf(name.toUpperCase()));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Tipo de entidad desconocido en config: " + name);
            }
        }
        return types;
    }

    public boolean keepNamedEntities() {
        return plugin.getConfig().getBoolean("entity-types.keep-named", true);
    }

    public boolean keepTamedAnimals() {
        return plugin.getConfig().getBoolean("entity-types.keep-tamed", true);
    }

    // ── Messages ─────────────────────────────────────────────────────────────

    public String getMsgReload() {
        return plugin.getConfig().getString("messages.reload",
                "&aConfiguración recargada correctamente.");
    }

    public String getMsgClearCommand() {
        return plugin.getConfig().getString("messages.clear-command",
                "&aEliminadas &e{amount} &aentidades manualmente.");
    }

    public String getMsgNoPermission() {
        return plugin.getConfig().getString("messages.no-permission",
                "&cNo tienes permiso para usar este comando.");
    }

    public String getMsgInfo() {
        return plugin.getConfig().getString("messages.info",
                "&6ZerithLag &fv{version} &7por &e{author} &8| &7Total eliminadas: &e{total}");
    }

    // ── Util ─────────────────────────────────────────────────────────────────

    public Component component(String text) {
        return LEGACY.deserialize(text);
    }

    public String colorString(String text) {
        return LegacyComponentSerializer.legacySection().serialize(LEGACY.deserialize(text));
    }
}
