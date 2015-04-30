package com.projectkorra.ProjectKorra.waterbending;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.TempBlock;

public class WaterSourceGrabber {
    /*
     * Using an initial WaterSource block, this class animates the block up to a
     * specific height and then toward the players location.
     */
    public static enum AnimationState {
        RISING, TOWARD, FINISHED, FAILED
    }
    
    private Player player;
    private AnimationState state;
    @SuppressWarnings("unused")
    private Location origin, currentLoc;
    private double animSpeed;
    private Material mat;
    private Byte data;
    private ConcurrentHashMap<Block, TempBlock> affectedBlocks = new ConcurrentHashMap<Block, TempBlock>();
    
    public WaterSourceGrabber(Player player, Location origin, double animSpeed) {
        this.player = player;
        this.origin = origin;
        this.animSpeed = animSpeed;
        this.mat = Material.STATIONARY_WATER;
        this.data = 0x00;
        this.currentLoc = origin.clone();
        this.state = AnimationState.RISING;
    }
    
    public WaterSourceGrabber(Player player, Location origin) {
        this(player, origin, 1);
    }
    
    public void progress() {
        if (state == AnimationState.FAILED || state == AnimationState.FINISHED)
            return;
        
        if (state == AnimationState.RISING) {
            revertBlocks();
            double locDiff = player.getEyeLocation().getY() - currentLoc.getY();
            currentLoc.add(0, animSpeed * Math.signum(locDiff), 0);
            Block block = currentLoc.getBlock();
            if (!(Methods.isWaterbendable(block, player) || block.getType() == Material.AIR)
                    || Methods.isRegionProtectedFromBuild(player, "WaterSpout",
                            block.getLocation())) {
                remove();
                return;
            }
            createBlock(block, mat, data);
            if (Math.abs(locDiff) < 1)
                state = AnimationState.TOWARD;
        } else {
            revertBlocks();
            Location eyeLoc = player.getTargetBlock((HashSet<Material>) null, 2).getLocation();
            eyeLoc.setY(player.getEyeLocation().getY());
            Vector vec = Methods.getDirection(currentLoc, eyeLoc);
            currentLoc.add(vec.normalize().multiply(animSpeed));
            
            Block block = currentLoc.getBlock();
            if (!(Methods.isWaterbendable(block, player) || block.getType() == Material.AIR)
                    || Methods.isRegionProtectedFromBuild(player,
                            "WaterManipulation", block.getLocation())) {
                remove();
                return;
            }
            
            createBlock(block, mat, data);
            if (currentLoc.distance(eyeLoc) < 1.1) {
                state = AnimationState.FINISHED;
                revertBlocks();
            }
        }
    }
    
    public AnimationState getState() {
        return state;
    }
    
    public void remove() {
        state = AnimationState.FAILED;
    }
    
    public void revertBlocks() {
        Enumeration<Block> keys = affectedBlocks.keys();
        while (keys.hasMoreElements()) {
            Block block = keys.nextElement();
            affectedBlocks.get(block).revertBlock();
            affectedBlocks.remove(block);
        }
    }
    
    public void createBlock(Block block, Material mat) {
        createBlock(block, mat, (byte) 0);
    }
    
    public void createBlock(Block block, Material mat, byte data) {
        affectedBlocks.put(block, new TempBlock(block, mat, data));
    }
    
    public Material getMat() {
        return mat;
    }
    
    public void setMat(Material mat) {
        this.mat = mat;
    }
    
    public Byte getData() {
        return data;
    }
    
    public void setData(Byte data) {
        this.data = data;
    }
    
    public void setState(AnimationState state) {
        this.state = state;
    }
    
    public double getAnimSpeed() {
        return animSpeed;
    }
    
    public void setAnimSpeed(double animSpeed) {
        this.animSpeed = animSpeed;
    }
    
}
