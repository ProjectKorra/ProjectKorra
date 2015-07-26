package com.projectkorra.ProjectKorra.firebending;

import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.Utilities.ParticleEffect;
import com.projectkorra.ProjectKorra.earthbending.EarthMethods;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Carbogen on 11/02/15.
 */
public class HeatControl {
    public static ConcurrentHashMap<Integer, HeatControl> instances = new ConcurrentHashMap<>();

    public final double RANGE = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.HeatControl.Solidify.Range");
    public final int RADIUS = ProjectKorra.plugin.getConfig().getInt("Abilities.Fire.HeatControl.Solidify.Radius");
    public final int REVERT_TIME = ProjectKorra.plugin.getConfig().getInt("Abilities.Fire.HeatControl.Solidify.RevertTime");

    public static int ID = 1;

    private Player player;
    private int id;
    private int currentRadius = 1;
    private long delay = 50;
    private long lastBlockTime = 0;
    private long lastParticleTime = 0;
    private Location center;
    private List<TempBlock> tblocks = new ArrayList<>();

    public double range = RANGE;
    public int radius = RADIUS;
    public long revertTime = REVERT_TIME;

    public HeatControl(Player player) {

        if (!isEligible(player))
            return;


        if (EarthMethods.getLavaSourceBlock(player, getRange()) == null) {
            new Cook(player);
            return;
        }

        this.player = player;

        if (ID == Integer.MAX_VALUE - 1)
            ID = 0;

        this.id = ID;

        ID++;

        lastBlockTime = System.currentTimeMillis();

        instances.put(id, this);
    }

    public boolean isEligible(Player player) {
        return GeneralMethods.canBend(player.getName(), "HeatControl")
                && GeneralMethods.getBoundAbility(player) != null
                && GeneralMethods.getBoundAbility(player).equalsIgnoreCase("HeatControl");

    }

    @SuppressWarnings("deprecation")
    public void freeze(List<Location> area) {
        if (System.currentTimeMillis() < lastBlockTime + delay)
            return;

        List<Block> lava = area.stream()
                .filter(l -> EarthMethods.isLava(l.getBlock()))
                .map(Location::getBlock)
                .collect(Collectors.toList());

        lastBlockTime = System.currentTimeMillis();

        if (lava.size() == 0) {
            currentRadius++;
            return;
        }

        Block b = lava.get(GeneralMethods.rand.nextInt(lava.size()));

        TempBlock tb;

        if (TempBlock.isTempBlock(b)) {
            tb = TempBlock.get(b);
            tb.setType(Material.STONE);
        } else tb = new TempBlock(b, Material.STONE, b.getData());

        if (!tblocks.contains(tb))
            tblocks.add(tb);

    }

    public void particles(List<Location> area) {
        if (System.currentTimeMillis() < lastParticleTime + 300)
            return;

        lastParticleTime = System.currentTimeMillis();

        area.stream()
                .filter(l -> EarthMethods.isLava(l.getBlock()))
                .forEach(l -> ParticleEffect.SMOKE.display(l, 0, 0, 0, 0.1f, 2));
    }

    public void resetLocation(Location loc) {
        if (center == null) {
            center = loc;
            return;
        }

        if (!loc.equals(center)) {
            currentRadius = 1;
            center = loc;
        }
    }

    public void progress() {
        if (!player.isOnline() || player.isDead() || !isEligible(player) || !player.isSneaking()) {
            stop();
            return;
        }

        if (currentRadius >= getRadius()) {
            stop();
            return;
        }

        Location targetlocation = GeneralMethods.getTargetedLocation(player, range);

        resetLocation(targetlocation);

        List<Location> area = GeneralMethods.getCircle(center, currentRadius, 3, true, true, 0);

        particles(area);
        freeze(area);
    }

    public static void progressAll() {
        for (Integer id : instances.keySet()) {
            instances.get(id).progress();
        }
    }

    public void stop() {
        ProjectKorra.plugin.getServer().getScheduler().scheduleSyncDelayedTask(ProjectKorra.plugin, () -> {
            revertAll();
            if (instances.containsKey(id))
                instances.remove(id);
        }, getRevertTime());

    }

    public void revertAll() {
        tblocks.forEach(TempBlock::revertBlock);

        tblocks.clear();
    }

    public Player getPlayer() {
        return player;
    }

    public double getRange() {
        return range;
    }

    public int getRadius() {
        return radius;
    }

    public long getRevertTime() {
        return revertTime;
    }

    public void setRange(double value) {
        range = value;
    }

    public void setRadius(int value) {
        radius = value;
    }

    public void setRevertTime(long value) {
        revertTime = value;
    }
}
