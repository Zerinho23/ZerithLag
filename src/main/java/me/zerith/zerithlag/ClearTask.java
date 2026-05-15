package me.zerith.zerithlag;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Compatibility: Minecraft 1.20 – 1.21+
 *  - Java 17 bytecode (runs on Java 17 and Java 21 JVMs)
 *  - Uses Adventure API throughout (no deprecated ChatColor or broadcastMessage)
 *  - ChestBoat exists since 1.19 → safe to reference from 1.20+
 *  - Bogged / Breeze / Armadillo (1.21) are caught by instanceof Monster
 */
public class ClearTask extends BukkitRunnable {

    private final ZerithLag plugin;
    private int timeLeft;

    public ClearTask(ZerithLag plugin) {
        this.plugin = plugin;
        this.timeLeft = plugin.getConfigManager().getClearInterval();
    }

    @Override
    public void run() {
        timeLeft--;
        ConfigManager cfg = plugin.getConfigManager();

        // ── Action bar / chat countdown (existing system) ────────────────────
        if (cfg.shouldBroadcast() && cfg.getCountdown().contains(timeLeft)) {
            String raw  = cfg.getCountdownMessage().replace("{time}", String.valueOf(timeLeft));
            Component bar  = cfg.component(raw);
            Component chat = cfg.getPrefix().append(bar);

            if (cfg.useActionBar()) {
                Bukkit.getOnlinePlayers().forEach(p -> p.sendActionBar(bar));
            } else {
                Bukkit.broadcast(chat);
            }
        }

        // ── Chat alerts (always in chat, independent of action bar) ──────────
        if (cfg.isChatAlertsEnabled()) {
            sendChatAlert(cfg, timeLeft);
        }

        // ── Execute clear ────────────────────────────────────────────────────
        if (timeLeft <= 0) {
            // Optional: stack mobs first, then clear remaining
            if (cfg.isMobStackerAutoEnabled()) {
                plugin.getMobStacker().stackAll();
            }

            int cleared = clearEntities();
            plugin.getStatsManager().recordClear(cleared);

            Component msg = cfg.getPrefix().append(cfg.component(
                    cfg.getClearMessage().replace("{amount}", String.valueOf(cleared))));
            if (cfg.shouldBroadcast()) {
                Bukkit.broadcast(msg);
            }

            timeLeft = cfg.getClearInterval();
        }
    }

    /**
     * Sends the appropriate chat alert based on how many seconds remain.
     *
     * Priority:
     *  1. timeLeft == 60         → minute-message  (always "1 minuto")
     *  2. timeLeft in second-alerts (e.g. 30) → seconds-message with {time}
     *  3. 1 ≤ timeLeft ≤ countdown-from       → countdown-message with {time}
     */
    private void sendChatAlert(ConfigManager cfg, int time) {
        Component msg = null;

        if (time == 60) {
            msg = cfg.component(cfg.getChatMinuteMessage());

        } else if (cfg.getChatSecondAlerts().contains(time)) {
            msg = cfg.component(
                    cfg.getChatSecondsMessage().replace("{time}", String.valueOf(time)));

        } else if (time >= 1 && time <= cfg.getChatCountdownFrom()) {
            msg = cfg.component(
                    cfg.getChatCountdownMessage().replace("{time}", String.valueOf(time)));
        }

        if (msg != null) {
            Bukkit.broadcast(msg);
        }
    }

    public int clearEntities() {
        ConfigManager    cfg       = plugin.getConfigManager();
        List<String>     worlds    = cfg.getWorldsToClear();
        List<EntityType> custom    = cfg.getEntityTypesToRemove();
        boolean          keepNamed = cfg.keepNamedEntities();
        boolean          keepTamed = cfg.keepTamedAnimals();

        int count = 0;
        for (World world : Bukkit.getWorlds()) {
            boolean allWorlds = worlds.contains("all");
            if (!allWorlds && !worlds.contains(world.getName())) continue;

            List<Entity> toRemove = new ArrayList<>();
            for (Entity entity : world.getEntities()) {
                if (shouldRemove(entity, custom, keepNamed, keepTamed)) {
                    toRemove.add(entity);
                }
            }
            for (Entity entity : toRemove) {
                entity.remove();
                count++;
            }
        }
        return count;
    }

    private boolean shouldRemove(Entity entity, List<EntityType> custom,
                                  boolean keepNamed, boolean keepTamed) {
        if (entity instanceof Player) return false;

        if (keepNamed && entity.customName() != null) return false;

        if (keepTamed && entity instanceof Tameable t && t.isTamed()) return false;

        if (!custom.isEmpty()) {
            return custom.contains(entity.getType());
        }

        if (entity instanceof Item)           return true;
        if (entity instanceof ExperienceOrb)  return true;
        if (entity instanceof AbstractArrow)  return true;
        if (entity instanceof Fireball)       return true;
        if (entity instanceof TNTPrimed)      return true;
        if (entity instanceof FallingBlock)   return true;
        if (entity instanceof Monster)        return true;
        if (entity instanceof Boat && entity.getPassengers().isEmpty())     return true;
        if (entity instanceof Minecart && entity.getPassengers().isEmpty()) return true;
        if (entity instanceof Projectile
                && !(entity instanceof AbstractArrow)
                && !(entity instanceof Fireball)) return true;

        return false;
    }
}
