package com.ludvig.combatplus;

import com.ludvig.combatplus.CombatPlusPlusPlugin;
import com.ludvig.combatplus.Text;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public final class CombatManager {

    private static final String COMBAT_TAG_KEY = "pvpplugin_combat_until";
    private static final String COMBO_KEY = "combatplus_combo";
    private static final String COMBO_UNTIL_KEY = "combatplus_combo_until";
    private static final String MODE_KEY = "combatplus_mode";

    private final CombatPlusPlusPlugin plugin;
    private final Map<UUID, Long> taggedUntil = new ConcurrentHashMap<>();
    private final Map<UUID, Combo> combos = new ConcurrentHashMap<>();

    private CombatMode mode;
    private Object attackStrengthTask;
    private Object cleanupTask;
    private boolean attackStrengthWarned;
    private boolean attackStrengthConfirmed;

    public CombatManager(CombatPlusPlusPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        this.mode = CombatMode.parse(plugin.getConfig().getString("settings.combat.mode", "1.21.11"));
    }

    public void start() {
        SchedulerCompat.cancel(attackStrengthTask);
        attackStrengthTask = SchedulerCompat.repeatForPlayers(plugin, 0L, 1L, this::enforceNoCooldown);
        cleanupTask = SchedulerCompat.repeatForPlayers(plugin, 20L, 20L, this::cleanupPlayer);
        plugin.getServer().getOnlinePlayers().forEach(player ->
                SchedulerCompat.runForPlayer(plugin, player, () -> refreshModeMetadata(player)));
        plugin.getLogger().info("Legacy cooldown hook: " + (AttackStrength.available() ? "ready" : "unavailable"));
    }

    public void stop() {
        SchedulerCompat.cancel(attackStrengthTask);
        attackStrengthTask = null;
        SchedulerCompat.cancel(cleanupTask);
        cleanupTask = null;
    }

    private void enforceNoCooldown(Player player) {
        if (mode.attackCooldown()) {
            return;
        }
        if (AttackStrength.fill(player)) {
            if (!attackStrengthConfirmed) {
                attackStrengthConfirmed = true;
                plugin.getLogger().info("Legacy mode active: attack cooldown disabled.");
            }
        } else if (!attackStrengthWarned) {
            attackStrengthWarned = true;
            plugin.getLogger().warning("Could not reset attack cooldown; 1.8.9 mode will feel modern.");
        }
    }

    public CombatMode getMode() {
        return mode;
    }

    public void setMode(CombatMode mode) {
        this.mode = mode;
        plugin.getConfig().set("settings.combat.mode", mode.id());
        plugin.saveConfig();

        plugin.getServer().getOnlinePlayers().forEach(player ->
                SchedulerCompat.runForPlayer(plugin, player, () -> refreshModeMetadata(player)));
    }

    public void refreshModeMetadata(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        player.setMetadata(MODE_KEY, new FixedMetadataValue(plugin, mode.id()));
    }

    public void handlePlayerHit(Player attacker, Player victim) {
        long tagMillis = Math.max(1, plugin.getConfig().getInt("settings.combat.tag-seconds", 15)) * 1000L;
        long until = System.currentTimeMillis() + tagMillis;
        taggedUntil.put(attacker.getUniqueId(), until);
        taggedUntil.put(victim.getUniqueId(), until);
        attacker.setMetadata(COMBAT_TAG_KEY, new FixedMetadataValue(plugin, until));
        victim.setMetadata(COMBAT_TAG_KEY, new FixedMetadataValue(plugin, until));

        Combo combo = combos.computeIfAbsent(attacker.getUniqueId(), ignored -> new Combo());
        long comboWindow = Math.max(1, plugin.getConfig().getInt("settings.combat.combo-seconds", 3)) * 1000L;
        long now = System.currentTimeMillis();
        if (now - combo.lastHit > comboWindow || !victim.getUniqueId().equals(combo.lastVictim)) {
            combo.count = 0;
        }
        combo.count++;
        combo.lastHit = now;
        combo.lastVictim = victim.getUniqueId();

        long comboUntil = now + comboWindow;
        attacker.setMetadata(COMBO_KEY, new FixedMetadataValue(plugin, combo.count));
        attacker.setMetadata(COMBO_UNTIL_KEY, new FixedMetadataValue(plugin, comboUntil));
    }

    public int getCombo(Player player) {
        Combo combo = combos.get(player.getUniqueId());
        if (combo == null) {
            return 0;
        }

        long window = Math.max(1, plugin.getConfig().getInt("settings.combat.combo-seconds", 3)) * 1000L;
        if (System.currentTimeMillis() - combo.lastHit > window) {
            return 0;
        }
        return combo.count;
    }

    public boolean isTagged(Player player) {
        Long until = taggedUntil.get(player.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }

    public long getTagRemaining(Player player) {
        Long until = taggedUntil.get(player.getUniqueId());
        return until == null ? 0L : Math.max(0L, until - System.currentTimeMillis());
    }

    public void clear(Player player) {
        taggedUntil.remove(player.getUniqueId());
        combos.remove(player.getUniqueId());
        player.removeMetadata(COMBAT_TAG_KEY, plugin);
        player.removeMetadata(COMBO_KEY, plugin);
        player.removeMetadata(COMBO_UNTIL_KEY, plugin);
    }

    public void clearTag(Player player) {
        taggedUntil.remove(player.getUniqueId());
        player.removeMetadata(COMBAT_TAG_KEY, plugin);
    }

    private void cleanupPlayer(Player player) {
        long now = System.currentTimeMillis();
        Long until = taggedUntil.get(player.getUniqueId());
        if (until != null && until <= now) {
            taggedUntil.remove(player.getUniqueId());
            player.removeMetadata(COMBAT_TAG_KEY, plugin);
        }

        Combo combo = combos.get(player.getUniqueId());
        if (combo != null && now - combo.lastHit > 10_000L) {
            combos.remove(player.getUniqueId());
            player.removeMetadata(COMBO_KEY, plugin);
            player.removeMetadata(COMBO_UNTIL_KEY, plugin);
        }
    }

    public String describe() {
        return "&7Mode: " + mode.coloredName()
                + " &8| &7Cooldown: " + (mode.attackCooldown() ? "&aon" : "&coff")
                + " &8| &7Sweep: " + (mode.sweepAttack() ? "&aon" : "&coff")
                + " &8| &7Shields: " + (mode.shields() ? "&aon" : "&coff");
    }

    public String status(Player player) {
        if (!isTagged(player)) {
            return "&aSafe";
        }
        return "&c" + Text.duration(getTagRemaining(player));
    }

    private static final class Combo {
        private int count;
        private long lastHit;
        private UUID lastVictim;
    }
}
