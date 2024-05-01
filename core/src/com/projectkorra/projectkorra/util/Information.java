package com.projectkorra.projectkorra.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

public class Information {

	private static int ID = Integer.MIN_VALUE;
	private String string;
	private final int id;
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
		this.id = ID++;
		if (ID >= Integer.MAX_VALUE) {
			ID = Integer.MIN_VALUE;
		}
	}

	public Block getBlock() {
		return this.block;
	}

	public byte getData() {
		return this.data;
	}

	public double getDouble() {
		return this.value;
	}

	public int getID() {
		return this.id;
	}

	public int getInteger() {
		return this.integer;
	}

	public Location getLocation() {
		return this.location;
	}

	public Player getPlayer() {
		return this.player;
	}

	public BlockState getState() {
		return this.state;
	}

	public String getString() {
		return this.string;
	}

	public long getTime() {
		return this.time;
	}

	public Material getType() {
		return this.type;
	}

	public void setBlock(final Block block) {
		this.block = block;
	}

	public void setData(final byte data) {
		this.data = data;
	}

	public void setDouble(final double value) {
		this.value = value;
	}

	public void setInteger(final int integer) {
		this.integer = integer;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

	public void setPlayer(final Player player) {
		this.player = player;
	}

	public void setState(final BlockState state) {
		this.state = state;
	}

	public void setString(final String string) {
		this.string = string;
	}

	public void setTime(final long time) {
		this.time = time;
	}

	public void setType(final Material type) {
		this.type = type;
	}

}
