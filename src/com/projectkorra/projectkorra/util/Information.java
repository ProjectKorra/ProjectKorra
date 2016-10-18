package com.projectkorra.projectkorra.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

public class Information {

	private static int ID = Integer.MIN_VALUE;
	private String string;
	private int id;
	private int integer;
	private long time;
	private double value;
	private byte data;

	private Block block;
	private BlockState state;
	private Location location;
	private Material type;
	private Player player;

	public Information() {
		id = ID++;
		if (ID >= Integer.MAX_VALUE) {
			ID = Integer.MIN_VALUE;
		}
	}

	public Block getBlock() {
		return block;
	}

	public byte getData() {
		return data;
	}

	public double getDouble() {
		return value;
	}

	public int getID() {
		return id;
	}

	public int getInteger() {
		return integer;
	}

	public Location getLocation() {
		return location;
	}

	public Player getPlayer() {
		return player;
	}

	public BlockState getState() {
		return state;
	}

	public String getString() {
		return string;
	}

	public long getTime() {
		return time;
	}

	public Material getType() {
		return type;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

	public void setData(byte data) {
		this.data = data;
	}

	public void setDouble(double value) {
		this.value = value;
	}

	public void setInteger(int integer) {
		this.integer = integer;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void setState(BlockState state) {
		this.state = state;
	}

	public void setString(String string) {
		this.string = string;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setType(Material type) {
		this.type = type;
	}

}
