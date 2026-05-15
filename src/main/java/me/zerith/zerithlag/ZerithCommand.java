package me.zerith.zerithlag;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

public class ZerithCommand implements CommandExecutor, TabCompleter {

    private final ZerithLag plugin;
    private static final List<String> SUBCOMMANDS = List.of(
            "reload", "clear", "stack", "info", "tps", "entities"
    );

    public ZerithCommand(ZerithLag plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager cfg    = plugin.getConfigManager();
        Component     prefix = cfg.getPrefix();

        if (args.length == 0) {
            sendHelp(sender, cfg);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "reload" -> {
                if (!sender.hasPermission("zerithlag.reload")) {
                    sender.sendMessage(prefix.append(cfg.component(cfg.getMsgNoPermission())));
                    return true;
                }
                plugin.reload();
                sender.sendMessage(prefix.append(cfg.component(cfg.getMsgReload())));
            }

            case "clear" -> {
                if (!sender.hasPermission("zerithlag.clear")) {
                    sender.sendMessage(prefix.append(cfg.component(cfg.getMsgNoPermission())));
                    return true;
                }
                int cleared = new ClearTask(plugin).clearEntities();
                plugin.getStatsManager().recordClear(cleared);
                sender.sendMessage(prefix.append(cfg.component(
                        cfg.getMsgClearCommand().replace("{amount}", String.valueOf(cleared)))));
            }

            case "stack" -> {
                if (!sender.hasPermission("zerithlag.stack")) {
                    sender.sendMessage(prefix.append(cfg.component(cfg.getMsgNoPermission())));
                    return true;
                }
                if (!cfg.isMobStackerEnabled()) {
                    sender.sendMessage(prefix.append(cfg.component(
                            "&cEl mob-stacker está desactivado en la config.")));
                    return true;
                }
                int merged = plugin.getMobStacker().stackAll();
                // Count remaining stacked groups (entities with × in name) is expensive;
                // just report merged count directly.
                String msg = cfg.getMsgStackCommand()
                        .replace("{merged}", String.valueOf(merged))
                        .replace("{stacks}", String.valueOf(merged)); // approximate
                sender.sendMessage(prefix.append(cfg.component(msg)));
            }

            case "info" -> {
                StatsManager stats   = plugin.getStatsManager();
                String       version = plugin.getPluginMeta().getVersion();
                String infoLine = cfg.getMsgInfo()
                        .replace("{version}", version)
                        .replace("{author}",  ZerithLag.AUTHOR)
                        .replace("{total}",   String.valueOf(stats.getTotalCleared()));
                sender.sendMessage(prefix.append(cfg.component(infoLine)));
                sender.sendMessage(cfg.component("  &7Última limpieza: &e"
                        + stats.getLastClearTimeFormatted()
                        + " &7(&e" + stats.getLastClearAmount() + " &7entidades)"));
                double tps = plugin.getTpsMonitor().getTps();
                sender.sendMessage(cfg.component("  &7TPS actual: "
                        + plugin.getTpsMonitor().getTpsColored()
                        + (tps < plugin.getConfigManager().getTpsClearThreshold()
                           ? " &c⚠ Por debajo del umbral de emergencia" : "")));
            }

            case "tps" -> {
                if (!sender.hasPermission("zerithlag.tps")) {
                    sender.sendMessage(prefix.append(cfg.component(cfg.getMsgNoPermission())));
                    return true;
                }
                String tps = plugin.getTpsMonitor().getTpsColored();
                sender.sendMessage(prefix.append(cfg.component(
                        "TPS del servidor: " + tps + " &7/ &a20.00")));
            }

            case "entities" -> {
                if (!sender.hasPermission("zerithlag.entities")) {
                    sender.sendMessage(prefix.append(cfg.component(cfg.getMsgNoPermission())));
                    return true;
                }
                sendEntityReport(sender, cfg, args);
            }

            default -> sendHelp(sender, cfg);
        }

        return true;
    }

    private void sendEntityReport(CommandSender sender, ConfigManager cfg, String[] args) {
        Component prefix = cfg.getPrefix();
        World target = null;

        if (args.length >= 2) {
            target = Bukkit.getWorld(args[1]);
            if (target == null) {
                sender.sendMessage(prefix.append(cfg.component(
                        "&cMundo '&e" + args[1] + "&c' no encontrado.")));
                return;
            }
        }

        List<World> worlds = target != null ? List.of(target) : Bukkit.getWorlds();

        sender.sendMessage(cfg.component("&8&m------------------------------"));
        sender.sendMessage(cfg.component("&6Entidades por mundo:"));

        int grandTotal = 0;
        for (World w : worlds) {
            Map<String, Integer> counts = new LinkedHashMap<>();
            int total = 0;
            for (Entity entity : w.getEntities()) {
                counts.merge(entity.getType().name(), 1, Integer::sum);
                total++;
            }
            grandTotal += total;

            sender.sendMessage(cfg.component("  &e" + w.getName()
                    + " &8(&7" + total + " total&8)"));
            counts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .forEach(e -> sender.sendMessage(cfg.component(
                            "    &7" + e.getKey() + ": &f" + e.getValue())));
        }

        sender.sendMessage(cfg.component("&7Total global: &e" + grandTotal));
        sender.sendMessage(cfg.component("&8&m------------------------------"));
    }

    private void sendHelp(CommandSender sender, ConfigManager cfg) {
        String version = plugin.getPluginMeta().getVersion();
        sender.sendMessage(cfg.component("&8&m------------------------------"));
        sender.sendMessage(cfg.component("&6ZerithLag &7v" + version
                + " &8| &7by &e" + ZerithLag.AUTHOR));
        sender.sendMessage(cfg.component("  &e/zerith reload    &8» &7Recargar config"));
        sender.sendMessage(cfg.component("  &e/zerith clear     &8» &7Limpiar entidades ahora"));
        sender.sendMessage(cfg.component("  &e/zerith stack     &8» &7Apilar mobs cercanos"));
        sender.sendMessage(cfg.component("  &e/zerith entities  &8» &7Ver entidades [mundo]"));
        sender.sendMessage(cfg.component("  &e/zerith tps       &8» &7Ver TPS del servidor"));
        sender.sendMessage(cfg.component("  &e/zerith info      &8» &7Info y estadísticas"));
        sender.sendMessage(cfg.component("&8&m------------------------------"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        if (args.length == 1) {
            List<String> result = new ArrayList<>();
            for (String sub : SUBCOMMANDS) {
                if (sub.startsWith(args[0].toLowerCase())) result.add(sub);
            }
            return result;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("entities")) {
            List<String> worldNames = new ArrayList<>();
            for (World w : Bukkit.getWorlds()) {
                if (w.getName().startsWith(args[1])) worldNames.add(w.getName());
            }
            return worldNames;
        }
        return new ArrayList<>();
    }
}
