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

        if (cfg.getCountdown().contains(timeLeft)) {
            String raw = cfg.getCountdownMessage().replace("{time}", String.valueOf(timeLeft));
            Component bar  = cfg.component(raw);
            Component chat = cfg.getPrefix().append(bar);

            if (cfg.shouldBroadcast()) {
                if (cfg.useActionBar()) {
                    Bukkit.getOnlinePlayers().forEach(p -> p.sendActionBar(bar));
                } else {
                    Bukkit.broadcast(chat);
                }
            }
        }

        if (timeLeft <= 0) {
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

        // entity.customName() is the Adventure API method (non-deprecated in 1.20+)
        if (keepNamed && entity.customName() != null) return false;

        if (keepTamed && entity instanceof Tameable t && t.isTamed()) return false;

        // ── Custom list mode ─────────────────────────────────────────────────
        if (!custom.isEmpty()) {
            return custom.contains(entity.getType());
        }

        // ── Default smart removal ─────────────────────────────────────────────
        if (entity instanceof Item)           return true;
        if (entity instanceof ExperienceOrb)  return true;
        // All arrow types (Arrow, SpectralArrow, TippedArrow)
        if (entity instanceof AbstractArrow)  return true;
        // All fireball types + WindCharge (1.21) all extend Fireball
        if (entity instanceof Fireball)       return true;
        if (entity instanceof TNTPrimed)      return true;
        if (entity instanceof FallingBlock)   return true;
        // All hostile mobs including Bogged, Breeze (1.21) which extend Monster
        if (entity instanceof Monster)        return true;
        // Boats (Boat + ChestBoat since 1.19) with no passengers
        if (entity instanceof Boat && entity.getPassengers().isEmpty())     return true;
        if (entity instanceof Minecart && entity.getPassengers().isEmpty()) return true;
        // Thrown projectiles (snowballs, eggs, potions, ender pearls, tridents in flight)
        if (entity instanceof Projectile
                && !(entity instanceof AbstractArrow)
                && !(entity instanceof Fireball)) return true;

        return false;
    }
}
