package com.projectkorra.ProjectKorra.chiblocking;

import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChiComboManager {
    public enum ChiCombo {
        QuickStrike, SwiftKick
    }

    public static HashMap<Player, List<ChiCombo>> instances = new HashMap<>();
    public static List<List<ChiCombo>> knownCombos = new ArrayList<>();
    public static List<Entity> paralyzed = new ArrayList<>();
    public static HashMap<Entity, Location> paralyzedLocations = new HashMap<>();
    public static long paralysisDuration = ProjectKorra.plugin.getConfig().getLong("Abilities.Chi.ChiCombo.ParalyzeDuration");
    public static long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Chi.ChiCombo.Cooldown");

    public ChiComboManager() {
        List<ChiCombo> combo1 = new ArrayList<>();
        combo1.add(ChiCombo.QuickStrike);
        combo1.add(ChiCombo.SwiftKick);
        combo1.add(ChiCombo.QuickStrike);
        combo1.add(ChiCombo.QuickStrike);
        knownCombos.add(combo1);
    }

    public static void addCombo(Player player, ChiCombo combo) {
        if (!player.hasPermission("bending.ability.ChiCombo"))
            return;

        if (!instances.containsKey(player))
            instances.put(player, new ArrayList<>());
        instances.get(player).add(combo);

        if (instances.get(player).size() > 4)
            instances.put(player, shiftList(instances.get(player)));
        //ProjectKorra.log.info(instances.get(player).toString());

        checkForValidCombo(player);
    }

    public static List<ChiCombo> shiftList(List<ChiCombo> list) {
        List<ChiCombo> list2 = new ArrayList<>();

        for (int i = 1; i < list.size(); i++) {
            list2.add(list.get(i));
        }

        return list2;
    }

    public static boolean checkForValidCombo(Player player) {
        List<ChiCombo> combo = instances.get(player);

        for (List<ChiCombo> knownCombo : knownCombos) {
            int size = knownCombo.size();

            //ProjectKorra.log.info("Scanning " + knownCombo.toString());

            if (combo.size() < size)
                continue;

            boolean isValid = true;
            for (int i = 1; i <= size; i++) {
                if (combo.get(combo.size() - i) != (knownCombo.get(knownCombo.size() - i))) {
                    isValid = false;
                    break;
                }
            }

            if (isValid) {
                //ProjectKorra.log.info("Combo Matched for player "+player.getName());

                if (combo.size() == 4
                        && combo.get(0) == ChiCombo.QuickStrike
                        && combo.get(1) == ChiCombo.SwiftKick
                        && combo.get(2) == ChiCombo.QuickStrike
                        && combo.get(3) == ChiCombo.QuickStrike) {
                    if (!GeneralMethods.getBendingPlayer(player.getName()).isOnCooldown("Immobilize") && GeneralMethods.canBend(player.getDisplayName(), "Immobilize")) {
                        GeneralMethods.getBendingPlayer(player.getName()).addCooldown("Immobilize", cooldown);
                        paralyzeTarget(player, paralysisDuration);
                    }
                }

                instances.remove(player);
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("deprecation")
    public static void paralyzeTarget(Player player, long time) {
        Entity e = GeneralMethods.getTargetedEntity(player, 4, new ArrayList<>());

        if (e == null)
            return;

        if (e instanceof LivingEntity) {
            final LivingEntity le = (LivingEntity) e;
            paralyzed.add(le);
            paralyzedLocations.put(le, le.getLocation());

            ProjectKorra.plugin.getServer().getScheduler().scheduleSyncDelayedTask(ProjectKorra.plugin, new BukkitRunnable() {
                public void run() {
                    paralyzed.remove(le);
                    paralyzedLocations.remove(le);
                }
            }, (time / 1000) * 20);
        }
    }

    public static void addNewCombo(List<ChiCombo> combo) {
        knownCombos.add(combo);
    }

    public static boolean isParalyzed(Entity e) {
        return paralyzed.contains(e);
    }

    public static void handleParalysis() {
        paralyzed.stream().filter(e -> !(e instanceof Player))
                .forEach(e -> e.setVelocity(GeneralMethods.getDirection(e.getLocation(), paralyzedLocations.get(e))));
    }
}
