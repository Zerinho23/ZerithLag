package me.zerith.zerithlag;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles all configuration access and text color parsing.
 *
 * Supported color syntax in ANY config value:
 *   &X        — legacy Minecraft color codes  (&a, &c, &l, etc.)
 *   &#RRGGBB  — hex RGB colors                (&#FF6600, &#00AAFF, etc.)
 *
 * Both formats can be mixed freely: "&6ZerithLag &#FF4400v{version}"
 */
public class ConfigManager {

    // Matches &#RRGGBB hex color tags
    private static final Pattern HEX_PATTERN =
            Pattern.compile("&#([A-Fa-f0-9]{6})");

    // Matches time components in strings like "1h30m20s"
    private static final Pattern TIME_PATTERN =
            Pattern.compile("(?:(\\d+)h)?(?:(\\d+)m)?(?:(\\d+)s)?");

    // Legacy-section serializer with hex-color support (§x§R§R§G§G§B§B format)
    private static final LegacyComponentSerializer SECTION_HEX =
            LegacyComponentSerializer.legacySection().toBuilder()
                    .hexColors()
                    .useUnusualXRepeatedCharacterHexFormat()
                    .build();

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
        String raw = plugin.getConfig().getString("auto-clear.interval", "5m");
        int seconds = parseTime(raw, 300);
        if (seconds < 10) {
            plugin.getLogger().warning("auto-clear.interval es muy bajo (" + raw
                    + "). Usando mínimo de 10 segundos.");
        }
        return Math.max(10, seconds);
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

    public String getChatMinuteMessage() {
        return plugin.getConfig().getString("chat-alerts.minute-message",
                "&8[&6ZerithLag&8] &r&7La limpieza de entidades ocurrirá en &61 minuto&7.");
    }

    public String getChatSecondsMessage() {
        return plugin.getConfig().getString("chat-alerts.seconds-message",
                "&8[&6ZerithLag&8] &r&7La limpieza de entidades ocurrirá en &e{time} segundos&7.");
    }

    public List<Integer> getChatSecondAlerts() {
        List<Integer> list = plugin.getConfig().getIntegerList("chat-alerts.second-alerts");
        return list.isEmpty() ? List.of(30) : list;
    }

    public String getChatCountdownMessage() {
        return plugin.getConfig().getString("chat-alerts.countdown-message",
                "&8[&6ZerithLag&8] &r&cLimpiando en &l{time}&c...");
    }

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

    // ── Chunk preloader ───────────────────────────────────────────────────────

    public boolean isChunkPreloaderEnabled() {
        return plugin.getConfig().getBoolean("chunk-preloader.enabled", true);
    }

    public int getChunkPreloaderRadius() {
        return Math.max(1, plugin.getConfig().getInt("chunk-preloader.radius", 10));
    }

    public List<String> getChunkPreloaderWorlds() {
        List<String> list = plugin.getConfig().getStringList("chunk-preloader.worlds");
        return list.isEmpty() ? List.of("all") : list;
    }

    // ── TPS-triggered clear ───────────────────────────────────────────────────

    public boolean isTpsClearEnabled() {
        return plugin.getConfig().getBoolean("tps-clear.enabled", true);
    }

    public double getTpsClearThreshold() {
        return plugin.getConfig().getDouble("tps-clear.threshold", 15.0);
    }

    public int getTpsClearCooldown() {
        String raw = plugin.getConfig().getString("tps-clear.cooldown", "1m");
        return Math.max(10, parseTime(raw, 60));
    }

    public String getTpsClearMessage() {
        return plugin.getConfig().getString("tps-clear.message",
                "&cTPS bajo &7(&c{tps}&7) &c— limpiando &e{amount} &centidades de emergencia.");
    }

    // ── Mob stacker ───────────────────────────────────────────────────────────

    public boolean isMobStackerEnabled() {
        return plugin.getConfig().getBoolean("mob-stacker.enabled", true);
    }

    public double getMobStackerRadius() {
        return plugin.getConfig().getDouble("mob-stacker.radius", 5.0);
    }

    public boolean isMobStackerAutoEnabled() {
        return plugin.getConfig().getBoolean("mob-stacker.auto", false);
    }

    public List<String> getMobStackerWorlds() {
        List<String> list = plugin.getConfig().getStringList("mob-stacker.worlds");
        return list.isEmpty() ? List.of("all") : list;
    }

    public String getMobStackerNameFormat() {
        return plugin.getConfig().getString("mob-stacker.name-format",
                "&7[&e\u00d7{count}&7] &f{type}");
    }

    public List<EntityType> getMobStackerTypes() {
        List<String>     names = plugin.getConfig().getStringList("mob-stacker.types");
        List<EntityType> types = new ArrayList<>();
        for (String name : names) {
            try {
                types.add(EntityType.valueOf(name.toUpperCase()));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Tipo desconocido en mob-stacker.types: " + name);
            }
        }
        return types;
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

    public String getMsgStackCommand() {
        return plugin.getConfig().getString("messages.stack-command",
                "&aApilados &e{merged} &amobs en &e{stacks} &agrupos.");
    }

    public String getMsgInfo() {
        return plugin.getConfig().getString("messages.info",
                "&6ZerithLag &fv{version} &7por &e{author} &8| &7Total eliminadas: &e{total}");
    }

    // ── Time parsing ──────────────────────────────────────────────────────────

    /**
     * Parses a human-readable time string into total seconds.
     *
     * Supported formats (case-insensitive, combinable):
     *   "5m"       →  300
     *   "10m"      →  600
     *   "30s"      →   30
     *   "1h"       → 3600
     *   "1h30m"    → 5400
     *   "1h30m20s" → 5420
     *   "300"      →  300  (plain integer = seconds, backwards-compatible)
     *
     * Returns {@code defaultValue} if the string is null, blank, or produces 0.
     */
    public static int parseTime(String raw, int defaultValue) {
        if (raw == null || raw.isBlank()) return defaultValue;
        raw = raw.trim().toLowerCase();

        // Plain integer → treat as seconds (backwards-compatible)
        if (raw.matches("\\d+")) {
            int v = Integer.parseInt(raw);
            return v > 0 ? v : defaultValue;
        }

        // Named time string: [Xh][Xm][Xs]
        Matcher m = TIME_PATTERN.matcher(raw);
        if (m.find()) {
            int h   = m.group(1) != null ? Integer.parseInt(m.group(1)) : 0;
            int min = m.group(2) != null ? Integer.parseInt(m.group(2)) : 0;
            int sec = m.group(3) != null ? Integer.parseInt(m.group(3)) : 0;
            int total = h * 3600 + min * 60 + sec;
            if (total > 0) return total;
        }

        return defaultValue;
    }

    // ── Color parsing ─────────────────────────────────────────────────────────

    /**
     * Converts a string with &-codes and/or &#RRGGBB hex codes into an Adventure Component.
     *
     * Examples:
     *   "&aHola &fmundo"           → green "Hola" + white "mundo"
     *   "&#FF6600Texto naranja"    → true orange text
     *   "&l&#00FFFFNegrita cyan"   → bold cyan text
     */
    public Component component(String text) {
        return SECTION_HEX.deserialize(toSectionFormat(text));
    }

    /**
     * Same as {@link #component(String)} but returns a plain legacy-section String.
     * Useful for senders that only accept raw strings (e.g. console via Logger).
     */
    public String colorString(String text) {
        return LegacyComponentSerializer.legacySection()
                .serialize(component(text));
    }

    /**
     * Converts mixed &-code / &#RRGGBB input into the §-section format that
     * LegacyComponentSerializer.legacySection() understands, including the
     * §x§R§R§G§G§B§B hex notation.
     */
    private static String toSectionFormat(String text) {
        // Step 1 — &#RRGGBB  →  §x§R§R§G§G§B§B
        Matcher hex = HEX_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder(text.length() + 32);
        while (hex.find()) {
            StringBuilder rep = new StringBuilder("\u00A7x");
            for (char c : hex.group(1).toUpperCase().toCharArray()) {
                rep.append('\u00A7').append(c);
            }
            hex.appendReplacement(sb, Matcher.quoteReplacement(rep.toString()));
        }
        hex.appendTail(sb);

        // Step 2 — &X  →  §X  (only valid color/format chars; skips & inside URLs etc.)
        String raw = sb.toString();
        StringBuilder out = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '&' && i + 1 < raw.length()) {
                char next = raw.charAt(i + 1);
                if ("0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(next) >= 0) {
                    out.append('\u00A7').append(next);
                    i++;
                    continue;
                }
            }
            out.append(c);
        }
        return out.toString();
    }
}
