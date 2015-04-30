package com.projectkorra.ProjectKorra;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

public class Information {
    
    private Player player;
    private long time;
    private Block block;
    private Location location;
    private Material type;
    private int integer;
    private double value;
    private byte data;
    private String string;
    private BlockState state;
    
    private static int ID = Integer.MIN_VALUE;
    private int id;
    
    public Information() {
        id = ID++;
        if (ID >= Integer.MAX_VALUE) {
            ID = Integer.MIN_VALUE;
        }
    }
    
    public int getID() {
        return id;
    }
    
    public void setState(BlockState state) {
        this.state = state;
    }
    
    public BlockState getState() {
        return state;
    }
    
    public void setString(String string) {
        this.string = string;
    }
    
    public String getString() {
        return string;
    }
    
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public void setTime(long time) {
        this.time = time;
    }
    
    public long getTime() {
        return time;
    }
    
    public void setBlock(Block block) {
        this.block = block;
    }
    
    public Block getBlock() {
        return block;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setType(Material type) {
        this.type = type;
    }
    
    public Material getType() {
        return type;
    }
    
    public void setInteger(int integer) {
        this.integer = integer;
    }
    
    public int getInteger() {
        return integer;
    }
    
    public void setDouble(double value) {
        this.value = value;
    }
    
    public double getDouble() {
        return value;
    }
    
    public void setData(byte data) {
        this.data = data;
    }
    
    public byte getData() {
        return data;
    }
    
}