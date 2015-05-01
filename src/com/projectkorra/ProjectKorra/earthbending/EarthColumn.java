package com.projectkorra.ProjectKorra.earthbending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class EarthColumn {
    
    public static ConcurrentHashMap<Integer, EarthColumn> instances = new ConcurrentHashMap<Integer, EarthColumn>();
    private static ConcurrentHashMap<Block, Block> alreadydoneblocks = new ConcurrentHashMap<Block, Block>();
    private static ConcurrentHashMap<Block, Integer> baseblocks = new ConcurrentHashMap<Block, Integer>();
    
    public static final int standardheight = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.RaiseEarth.Column.Height");
    private static int ID = Integer.MIN_VALUE;
    
    private static double range = 20;
    private static double speed = 8;
    private static final Vector direction = new Vector(0, 1, 0);
    private static long interval = (long) (1000. / speed);
    
    private Location origin;
    private Location location;
    private Block block;
    private int distance;
    private Player player;
    private int id;
    private long time;
    private int height = standardheight;
    private ConcurrentHashMap<Block, Block> affectedblocks = new ConcurrentHashMap<Block, Block>();
    
    public EarthColumn(Player player) {
        BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());
        
        if (bPlayer.isOnCooldown("RaiseEarth"))
            return;
        
        try {
            block = Methods.getEarthSourceBlock(player, range);
            if (block == null)
                return;
            origin = block.getLocation();
            location = origin.clone();
            distance = Methods.getEarthbendableBlocksLength(player, block, direction.clone().multiply(-1), height);
        } catch (IllegalStateException e) {
            return;
        }
        
        this.player = player;
        
        loadAffectedBlocks();
        
        if (distance != 0) {
            if (canInstantiate()) {
                id = ID;
                instances.put(id, this);
                bPlayer.addCooldown("RaiseEarth", Methods.getGlobalCooldown());
                if (ID >= Integer.MAX_VALUE) {
                    ID = Integer.MIN_VALUE;
                }
                ID++;
                time = System.currentTimeMillis() - interval;
            }
        }
    }
    
    public EarthColumn(Player player, Location origin) {
        this.origin = origin;
        location = origin.clone();
        block = location.getBlock();
        this.player = player;
        distance = Methods.getEarthbendableBlocksLength(player, block, direction.clone().multiply(-1), height);
        
        loadAffectedBlocks();
        
        if (distance != 0) {
            if (canInstantiate()) {
                id = ID;
                instances.put(id, this);
                if (ID >= Integer.MAX_VALUE) {
                    ID = Integer.MIN_VALUE;
                }
                ID++;
                time = System.currentTimeMillis() - interval;
            }
        }
    }
    
    public EarthColumn(Player player, Location origin, int height) {
        this.height = height;
        this.origin = origin;
        location = origin.clone();
        block = location.getBlock();
        this.player = player;
        distance = Methods.getEarthbendableBlocksLength(player, block, direction.clone().multiply(-1), height);
        
        loadAffectedBlocks();
        
        if (distance != 0) {
            if (canInstantiate()) {
                id = ID;
                instances.put(id, this);
                if (ID >= Integer.MAX_VALUE) {
                    ID = Integer.MIN_VALUE;
                }
                ID++;
                time = System.currentTimeMillis() - interval;
            }
        }
    }
    
    private void loadAffectedBlocks() {
        affectedblocks.clear();
        Block thisblock;
        for (int i = 0; i <= distance; i++) {
            thisblock = block.getWorld().getBlockAt(location.clone().add(direction.clone().multiply(-i)));
            affectedblocks.put(thisblock, thisblock);
            if (CompactColumn.blockInAllAffectedBlocks(thisblock))
                CompactColumn.revertBlock(thisblock);
        }
    }
    
    private boolean blockInAffectedBlocks(Block block) {
        if (affectedblocks.containsKey(block)) {
            return true;
        }
        return false;
    }
    
    public static boolean blockInAllAffectedBlocks(Block block) {
        for (int ID : instances.keySet()) {
            if (instances.get(ID).blockInAffectedBlocks(block))
                return true;
        }
        return false;
    }
    
    public static void revertBlock(Block block) {
        for (int ID : instances.keySet()) {
            if (instances.get(ID).blockInAffectedBlocks(block)) {
                instances.get(ID).affectedblocks.remove(block);
            }
        }
    }
    
    private boolean canInstantiate() {
        for (Block block : affectedblocks.keySet()) {
            if (blockInAllAffectedBlocks(block) || alreadydoneblocks.containsKey(block)) {
                return false;
            }
        }
        return true;
    }
    
    public static void progressAll() {
        for (int ID : instances.keySet()) {
            instances.get(ID).progress();
        }
    }
    
    private boolean progress() {
        if (System.currentTimeMillis() - time >= interval) {
            time = System.currentTimeMillis();
            if (!moveEarth()) {
                instances.remove(id);
                for (Block block : affectedblocks.keySet()) {
                    alreadydoneblocks.put(block, block);
                }
                baseblocks.put(location.clone().add(direction.clone().multiply(-1 * (distance - 1)))
                        .getBlock(), (distance - 1));
                
                return false;
            }
        }
        return true;
    }
    
    private boolean moveEarth() {
        Block block = location.getBlock();
        location = location.add(direction);
        Methods.moveEarth(player, block, direction, distance);
        loadAffectedBlocks();
        
        if (location.distance(origin) >= distance) {
            return false;
        }
        
        return true;
    }
    
    public static boolean blockIsBase(Block block) {
        if (baseblocks.containsKey(block)) {
            return true;
        }
        return false;
    }
    
    public static void removeBlockBase(Block block) {
        if (baseblocks.containsKey(block)) {
            baseblocks.remove(block);
        }
        
    }
    
    public static void removeAll() {
        for (int id : instances.keySet()) {
            instances.remove(id);
        }
    }
    
    public static void resetBlock(Block block) {
        
        if (alreadydoneblocks.containsKey(block)) {
            alreadydoneblocks.remove(block);
        }
        
    }
    
    public static String getDescription() {
        return "To use, simply left-click on an earthbendable block. "
                + "A column of earth will shoot upwards from that location. "
                + "Anything in the way of the column will be brought up with it, "
                + "leaving talented benders the ability to trap brainless entities up there. "
                + "Additionally, simply sneak (default shift) looking at an earthbendable block. "
                + "A wall of earth will shoot upwards from that location. "
                + "Anything in the way of the wall will be brought up with it. ";
    }
    
}