package com.ludvig.combatplus;

import com.ludvig.combatplus.CombatPlusPlusPlugin;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public final class LegacyCombatManager {

    private static final UUID SLOW_UUID = UUID.fromString("8c3d5e0e-6d1c-4c1a-9d4e-3f1b2a7c9e01");

    private final CombatPlusPlusPlugin plugin;
    private final Set<UUID> blocking = ConcurrentHashMap.newKeySet();

    private Object task;

    public LegacyCombatManager(CombatPlusPlusPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        task = SchedulerCompat.repeatForPlayers(plugin, 1L, 1L, this::tickPlayer);
    }

    public void stop() {
        SchedulerCompat.cancel(task);
        task = null;
        stopAllBlocks();
    }

    public boolean isLegacy() {
        return plugin.getCombatManager().getMode() == CombatMode.LEGACY_1_8_9;
    }

    public boolean isBlocking(Player player) {
        return isLegacy()
                && blocking.contains(player.getUniqueId())
                && isSword(player.getInventory().getItemInMainHand());
    }

    public boolean startBlocking(Player player) {
        if (!isLegacy() || !isSword(player.getInventory().getItemInMainHand())) {
            return false;
        }
        if (!blocking.add(player.getUniqueId())) {
            return false;
        }
        applySlow(player);
        return true;
    }

    public void stopBlocking(Player player) {
        if (blocking.remove(player.getUniqueId())) {
            removeSlow(player);
        }
    }

    public double blockedDamage(double damage) {
        double multiplier = clamp(plugin.getConfig().getDouble(
                "settings.combat.legacy.blocking-damage-multiplier", 0.5), 0.0, 1.0);
        double flat = Math.max(0.0, plugin.getConfig().getDouble(
                "settings.combat.legacy.blocking-damage-flat", 0.5));
        return damage * multiplier + flat;
    }

    public void applyKnockback(Player attacker, LivingEntity victim, Vector before, boolean sprinting) {
        AttributeInstance resistanceAttribute = victim.getAttribute(Attributes.get("knockback_resistance"));
        double resistance = resistanceAttribute == null ? 0.0 : resistanceAttribute.getValue();
        if (resistance >= 1.0 || (resistance > 0.0 && Math.random() < resistance)) {
            return;
        }

        double horizontal = plugin.getConfig().getDouble("settings.combat.legacy.knockback-horizontal", 0.4);
        double sprintHorizontal = plugin.getConfig().getDouble("settings.combat.legacy.knockback-sprint-horizontal", 0.9);
        double vertical = plugin.getConfig().getDouble("settings.combat.legacy.knockback-vertical", 0.4);
        double force = sprinting ? sprintHorizontal : horizontal;

        double yaw = Math.toRadians(attacker.getLocation().getYaw());
        double x = before.getX() / 2.0 - (-Math.sin(yaw)) * force;
        double z = before.getZ() / 2.0 - Math.cos(yaw) * force;
        double y = before.getY() / 2.0 + vertical;
        if (y > vertical) {
            y = vertical;
        }

        Vector velocity = new Vector(x, y, z);
        SchedulerCompat.runForEntity(plugin, victim, () -> {
            if (victim.isValid() && !victim.isDead()) {
                victim.setVelocity(velocity);
            }
        });
    }

    private void tickPlayer(Player player) {
        if (!isLegacy()) {
            if (!blocking.isEmpty()) {
                stopAllBlocks();
            }
            return;
        }

        if (!blocking.contains(player.getUniqueId())) {
            return;
        }
        if (!player.isOnline() || player.isDead()
                || !isSword(player.getInventory().getItemInMainHand())) {
            stopBlocking(player);
        }
    }

    private void stopAllBlocks() {
        for (UUID id : new ArrayList<>(blocking)) {
            Player player = plugin.getServer().getPlayer(id);
            if (player != null) {
                SchedulerCompat.runForPlayer(plugin, player, () -> removeSlow(player));
            }
        }
        blocking.clear();
    }

    private void applySlow(Player player) {
        AttributeInstance attribute = player.getAttribute(Attributes.get("movement_speed"));
        if (attribute == null) {
            return;
        }
        removeModifier(attribute, SLOW_UUID);
        double multiplier = clamp(plugin.getConfig().getDouble(
                "settings.combat.legacy.blocking-speed-multiplier", 0.2), 0.05, 1.0);
        attribute.addModifier(new AttributeModifier(
                SLOW_UUID, "legacy_sword_block", multiplier - 1.0, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
    }

    private void removeSlow(Player player) {
        AttributeInstance attribute = player.getAttribute(Attributes.get("movement_speed"));
        if (attribute != null) {
            removeModifier(attribute, SLOW_UUID);
        }
    }

    private void removeModifier(AttributeInstance attribute, UUID uuid) {
        for (AttributeModifier modifier : attribute.getModifiers()) {
            if (modifier.getUniqueId().equals(uuid)) {
                attribute.removeModifier(modifier);
            }
        }
    }

    private boolean isSword(ItemStack item) {
        return item != null && item.getType().name().endsWith("_SWORD");
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
