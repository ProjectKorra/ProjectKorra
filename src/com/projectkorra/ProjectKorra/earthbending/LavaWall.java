package com.projectkorra.ProjectKorra.earthbending;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.firebending.FireBlast;

public class LavaWall {
    public static ConcurrentHashMap<Integer, LavaWall> instances = new ConcurrentHashMap<Integer, LavaWall>();
    public static ConcurrentHashMap<Block, Block> affectedblocks = new ConcurrentHashMap<Block, Block>();
    public static ConcurrentHashMap<Block, Player> wallblocks = new ConcurrentHashMap<Block, Player>();
    private static double range = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.Surge.Wall.Range");
    private static final double defaultradius = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.Surge.Wall.Radius");
    private static final long interval = 30;
    @SuppressWarnings("unused")
    private static final byte full = 0x0;
    Player player;
    private Location location = null;
    private Block sourceblock = null;
    private Location firstdestination = null;
    private Location targetdestination = null;
    private Vector firstdirection = null;
    private Vector targetdirection = null;
    private boolean progressing = false;
    private boolean settingup = false;
    private boolean forming = false;
    private long time;
    private double radius = defaultradius;
    
    public LavaWall(Player player) {
        this.player = player;
        
        if (LavaWave.instances.containsKey(player.getEntityId())) {
            LavaWave wave = LavaWave.instances.get(player.getEntityId());
            if (!wave.progressing) {
                LavaWave.launch(player);
                return;
            }
        }
        
        if (AvatarState.isAvatarState(player)) {
            radius = AvatarState.getValue(radius);
        }
        
        BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());
        if (bPlayer.isOnCooldown("LavaSurge"))
            return;
        
    }
    
    public boolean prepare() {
        cancelPrevious();
        Block block = Methods.getLavaSourceBlock(player, range);
        if (block != null) {
            sourceblock = block;
            focusBlock();
            return true;
        }
        return false;
    }
    
    private void cancelPrevious() {
        if (instances.containsKey(player.getEntityId())) {
            LavaWall old = instances.get(player.getEntityId());
            if (old.progressing) {
                old.removeLava(old.sourceblock);
            } else {
                old.cancel();
            }
        }
    }
    
    public void cancel() {
        unfocusBlock();
    }
    
    private void focusBlock() {
        location = sourceblock.getLocation();
    }
    
    private void unfocusBlock() {
        instances.remove(player.getEntityId());
    }
    
    @SuppressWarnings("deprecation")
    public void moveLava() {
        if (sourceblock != null) {
            targetdestination = player.getTargetBlock(Methods.getTransparentEarthbending(), (int) range).getLocation();
            if (targetdestination.distance(location) <= 1) {
                progressing = false;
                targetdestination = null;
            } else {
                progressing = true;
                settingup = true;
                firstdestination = getToEyeLevel();
                firstdirection = getDirection(sourceblock.getLocation(), firstdestination);
                targetdirection = getDirection(firstdestination, targetdestination);
                
                if (!Methods.isAdjacentToThreeOrMoreSources(sourceblock)) {
                    sourceblock.setType(Material.AIR);
                }
                addLava(sourceblock);
            }
        }
    }
    
    private Location getToEyeLevel() {
        Location loc = sourceblock.getLocation().clone();
        loc.setY(targetdestination.getY());
        return loc;
    }
    
    private Vector getDirection(Location location, Location destination) {
        double x1, y1, z1;
        double x0, y0, z0;
        x1 = destination.getX();
        y1 = destination.getY();
        z1 = destination.getZ();
        x0 = location.getX();
        y0 = location.getY();
        z0 = location.getZ();
        return new Vector(x1 - x0, y1 - y0, z1 - z0);
    }
    
    public static void progressAll() {
        for (int ID : instances.keySet()) {
            instances.get(ID).progress();
        }
    }
    
    private boolean progress() {
        if (player.isDead() || !player.isOnline()) {
            breakBlock();
            // instances.remove(player.getEntityId());
            return false;
        }
        if (!Methods.canBend(player.getName(), "LavaSurge")) {
            if (!forming)
                breakBlock();
            unfocusBlock();
            return false;
        }
        if (System.currentTimeMillis() - time >= interval) {
            time = System.currentTimeMillis();
            if (!forming) {
            }
            if (Methods.getBoundAbility(player) == null) {
                unfocusBlock();
                return false;
            }
            if (!progressing
                    && !Methods.getBoundAbility(player).equalsIgnoreCase("LavaSurge")) {
                unfocusBlock();
                return false;
            }
            if (progressing
                    && (!player.isSneaking() || !Methods.getBoundAbility(player).equalsIgnoreCase("LavaSurge"))) {
                breakBlock();
                return false;
            }
            if (!progressing) {
                sourceblock.getWorld().playEffect(location, Effect.SMOKE, 4, (int) range);
                return false;
            }
            if (forming) {
                ArrayList<Block> blocks = new ArrayList<Block>();
                Location loc = Methods.getTargetedLocation(player, (int) range, 8, 9, 79);
                location = loc.clone();
                Vector dir = player.getEyeLocation().getDirection();
                Vector vec;
                Block block;
                for (double i = 0; i <= radius; i += 0.5) {
                    for (double angle = 0; angle < 360; angle += 10) {
                        vec = Methods.getOrthogonalVector(dir.clone(), angle, i);
                        block = loc.clone().add(vec).getBlock();
                        if (Methods.isRegionProtectedFromBuild(player, "LavaSurge", block.getLocation()))
                            continue;
                        if (wallblocks.containsKey(block)) {
                            blocks.add(block);
                        } else if (!blocks.contains(block)
                                && (block.getType() == Material.AIR
                                        || block.getType() == Material.FIRE
                                        || Methods.isLavabendable(block, player))) {
                            wallblocks.put(block, player);
                            addWallBlock(block);
                            blocks.add(block);
                            FireBlast.removeFireBlastsAroundPoint(block.getLocation(), 2);
                        }
                    }
                }
                for (Block blocki : wallblocks.keySet()) {
                    if (wallblocks.get(blocki) == player && !blocks.contains(blocki)) {
                        finalRemoveLava(blocki);
                    }
                }
                return true;
            }
            if (sourceblock.getLocation().distance(firstdestination) < .5 && settingup) {
                settingup = false;
            }
            Vector direction;
            if (settingup) {
                direction = firstdirection;
            } else {
                direction = targetdirection;
            }
            location = location.clone().add(direction);
            Block block = location.getBlock();
            if (block.getLocation().equals(sourceblock.getLocation())) {
                location = location.clone().add(direction);
                block = location.getBlock();
            }
            if (block.getType() != Material.AIR) {
                breakBlock();
                return false;
            }
            if (!progressing) {
                breakBlock();
                return false;
            }
            addLava(block);
            removeLava(sourceblock);
            sourceblock = block;
            if (location.distance(targetdestination) < 1) {
                removeLava(sourceblock);
                forming = true;
            }
            return true;
        }
        return false;
    }
    
    private void addWallBlock(Block block) {
        new TempBlock(block, Material.STATIONARY_LAVA, (byte) 8);
    }
    
    private void breakBlock() {
        finalRemoveLava(sourceblock);
        for (Block block : wallblocks.keySet()) {
            if (wallblocks.get(block) == player) {
                finalRemoveLava(block);
            }
        }
        instances.remove(player.getEntityId());
    }
    
    private void removeLava(Block block) {
        if (block != null) {
            if (affectedblocks.containsKey(block)) {
                if (!Methods.isAdjacentToThreeOrMoreSources(block)) {
                    TempBlock.revertBlock(block, Material.AIR);
                }
                affectedblocks.remove(block);
            }
        }
    }
    
    private static void finalRemoveLava(Block block) {
        if (affectedblocks.containsKey(block)) {
            TempBlock.revertBlock(block, Material.AIR);
            affectedblocks.remove(block);
        }
        if (wallblocks.containsKey(block)) {
            TempBlock.revertBlock(block, Material.AIR);
            wallblocks.remove(block);
        }
    }
    
    private void addLava(Block block) {
        if (Methods.isRegionProtectedFromBuild(player, "LavaSurge", block.getLocation()))
            return;
        if (!TempBlock.isTempBlock(block)) {
            new TempBlock(block, Material.STATIONARY_LAVA, (byte) 8);
            affectedblocks.put(block, block);
        }
    }
    
    public static void moveLava(Player player) {
        if (instances.containsKey(player.getEntityId())) {
            instances.get(player.getEntityId()).moveLava();
        }
    }
    
    @SuppressWarnings("deprecation")
    public static void form(Player player) {
        if (!instances.containsKey(player.getEntityId())) {
            new LavaWave(player);
            return;
        } else {
            if (Methods.isLavabendable(player.getTargetBlock((HashSet<Byte>) null, (int) LavaWave.defaultrange), player)) {
                new LavaWave(player);
                return;
            }
        }
        moveLava(player);
    }
    
    public static void removeAll() {
        for (Block block : affectedblocks.keySet()) {
            TempBlock.revertBlock(block, Material.AIR);
            affectedblocks.remove(block);
            wallblocks.remove(block);
        }
        for (Block block : wallblocks.keySet()) {
            TempBlock.revertBlock(block, Material.AIR);
            affectedblocks.remove(block);
            wallblocks.remove(block);
        }
    }
    
    public static boolean wasBrokenFor(Player player, Block block) {
        if (instances.containsKey(player.getEntityId())) {
            LavaWall wall = instances.get(player.getEntityId());
            if (wall.sourceblock == null)
                return false;
            if (wall.sourceblock.equals(block))
                return true;
        }
        return false;
    }
    
}