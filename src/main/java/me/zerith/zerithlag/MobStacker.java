package me.zerith.zerithlag;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.*;

import java.util.*;

/**
 * Groups nearby identical mobs into a single entity with a stacked name,
 * reducing active entity count without permanently deleting creatures.
 *
 * Stack name format (configurable):  &7[&e×{count}&7] &f{type}
 *   e.g.  [×4] Zombie
 *
 * Rules:
 *  - Only stacks Monsters by default (configurable list).
 *  - Respects keep-named and keep-tamed settings from ConfigManager.
 *  - Entities that already carry a stack name are merged correctly
 *    (their count is added to the new stack total).
 *  - Groups are formed per-world, per-EntityType, within the radius.
 */
public class MobStacker {

    private static final PlainTextComponentSerializer PLAIN =
            PlainTextComponentSerializer.plainText();

    private final ZerithLag plugin;

    public MobStacker(ZerithLag plugin) {
        this.plugin = plugin;
    }

    /**
     * Stacks mobs in all eligible worlds.
     * @return total number of entities merged away (removed)
     */
    public int stackAll() {
        ConfigManager cfg = plugin.getConfigManager();
        if (!cfg.isMobStackerEnabled()) return 0;

        int merged = 0;
        for (World world : Bukkit.getWorlds()) {
            merged += stackWorld(world, cfg);
        }
        return merged;
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private int stackWorld(World world, ConfigManager cfg) {
        List<String> worlds   = cfg.getWorldsToClear();
        boolean      allWorlds = worlds.contains("all");
        if (!allWorlds && !worlds.contains(world.getName())) return 0;

        double radiusSq = cfg.getMobStackerRadius() * cfg.getMobStackerRadius();

        Map<EntityType, List<Mob>> byType = new LinkedHashMap<>();
        for (Entity entity : world.getEntities()) {
            if (!(entity instanceof Mob mob)) continue;
            if (!isStackable(mob, cfg)) continue;
            byType.computeIfAbsent(mob.getType(), k -> new ArrayList<>()).add(mob);
        }

        int merged = 0;
        for (List<Mob> group : byType.values()) {
            merged += stackGroup(group, radiusSq, cfg);
        }
        return merged;
    }

    private int stackGroup(List<Mob> entities, double radiusSq, ConfigManager cfg) {
        Set<UUID> absorbed = new HashSet<>();
        int merged = 0;

        for (int i = 0; i < entities.size(); i++) {
            Mob base = entities.get(i);
            if (absorbed.contains(base.getUniqueId())) continue;

            int stackCount = getStackCount(base);

            for (int j = i + 1; j < entities.size(); j++) {
                Mob other = entities.get(j);
                if (absorbed.contains(other.getUniqueId())) continue;
                if (!base.getWorld().equals(other.getWorld())) continue;
                if (base.getLocation().distanceSquared(other.getLocation()) > radiusSq) continue;

                stackCount += getStackCount(other);
                other.remove();
                absorbed.add(other.getUniqueId());
                merged++;
            }

            if (stackCount > 1) {
                applyStackName(base, stackCount, cfg);
            }
        }
        return merged;
    }

    /**
     * Reads the numeric count out of a stack name like "[×4] Zombie".
     * Returns 1 if the entity carries no stack name.
     */
    private int getStackCount(Mob mob) {
        if (mob.customName() == null) return 1;
        String plain = PLAIN.serialize(mob.customName());
        int xIdx  = plain.indexOf('×');
        int endIdx = plain.indexOf(']');
        if (xIdx >= 0 && endIdx > xIdx) {
            try {
                return Integer.parseInt(plain.substring(xIdx + 1, endIdx).trim());
            } catch (NumberFormatException ignored) {}
        }
        return 1;
    }

    private void applyStackName(Mob mob, int count, ConfigManager cfg) {
        String typeName = formatTypeName(mob.getType());
        String nameStr  = cfg.getMobStackerNameFormat()
                .replace("{count}", String.valueOf(count))
                .replace("{type}",  typeName);
        mob.customName(cfg.component(nameStr));
        mob.setCustomNameVisible(true);
    }

    private static String formatTypeName(EntityType type) {
        String raw = type.name().replace('_', ' ').toLowerCase();
        if (raw.isEmpty()) return raw;
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }

    private boolean isStackable(Mob mob, ConfigManager cfg) {
        if (mob instanceof Player) return false;

        if (cfg.keepTamedAnimals() && mob instanceof Tameable t && t.isTamed()) return false;

        if (cfg.keepNamedEntities() && mob.customName() != null) {
            String plain    = PLAIN.serialize(mob.customName());
            boolean isStack = plain.contains("×");
            if (!isStack) return false;
        }

        List<EntityType> types = cfg.getMobStackerTypes();
        if (types.isEmpty()) {
            return mob instanceof Monster;
        }
        return types.contains(mob.getType());
    }
}
