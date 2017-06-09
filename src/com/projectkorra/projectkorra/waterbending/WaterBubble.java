package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.TempBlock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

public class WaterBubble extends WaterAbility {

	private static long clickDuration; //How long the click variant lasts
	private static double maxRadius;
	private static double speed;
	private static boolean requireAir = false;
	
	private boolean isShift;
	private double radius;
	private boolean removing = false; //Is true when the radius is shrinking
	private Map<Block, MaterialData> waterOrigins = new ConcurrentHashMap<Block, MaterialData>();
	private Location location;
	private long lastActivation; //When the last click happened
	
	public WaterBubble(Player player, boolean isShift) {
		super(player);
		
		setFields();
		
		if (CoreAbility.hasAbility(player, this.getClass())) {
			WaterBubble bubble = CoreAbility.getAbility(player, this.getClass());
			
			if (bubble.location.getWorld().equals(player.getWorld())) {
				if (bubble.location.distanceSquared(player.getLocation()) < maxRadius * maxRadius) {
					if (bubble.removing) {
						bubble.removing = false;
					}
					
					bubble.location = player.getLocation();
					bubble.isShift = isShift;
					bubble.lastActivation = System.currentTimeMillis();
					return;
				}
			}
			bubble.removing = true;
		} else if (requireAir && !(!player.getEyeLocation().getBlock().getType().isSolid() && !player.getEyeLocation().getBlock().isLiquid())) {
			return;
		}
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		this.radius = 0;
		this.isShift = isShift;
		this.location = player.getLocation();
		this.lastActivation = System.currentTimeMillis();
		
		start();
	}
	
	public void setFields() {
		clickDuration = ConfigManager.defaultConfig.get().getLong("Abilities.Water.WaterBubble.ClickDuration");
		maxRadius = ConfigManager.defaultConfig.get().getDouble("Abilities.Water.WaterBubble.Radius");
		speed = ConfigManager.defaultConfig.get().getDouble("Abilities.Water.WaterBubble.Speed");
		requireAir = ConfigManager.defaultConfig.get().getBoolean("Abilities.Water.WaterBubble.MustStartAboveWater");
	}

	@Override
	public String getName() {
		return "WaterBubble";
	}

	@SuppressWarnings("deprecation")
	@Override
	public void progress() {
		if (!bPlayer.canBend(this) || (isShift && !player.isSneaking()) || !this.location.getWorld().equals(player.getWorld())) {
			this.removing = true;
		}
		
		if (System.currentTimeMillis() - this.lastActivation > clickDuration && !isShift) {
			this.removing = true;
		}
		
		if (removing) {
			this.radius -= speed;
			
			if (radius <= 0.1) {
				radius = 0.1;
				remove();
			}
		} else {
			this.radius += speed;

			if (this.radius > maxRadius) {
				this.radius = maxRadius;
			}
		}

		List<Block> list = new ArrayList<Block>();
		
		if (this.radius < maxRadius || !this.location.getBlock().equals(player.getLocation().getBlock())) {
			
			for (double x = -radius; x < radius; x+= 0.5) {
				for (double y = -radius; y < radius; y+=0.5) {
					for (double z = -radius; z < radius; z+=0.5) {
						if (x * x + y * y + z * z <= radius * radius) {
							Block b = location.add(x, y, z).getBlock();
							
							if (!waterOrigins.containsKey(b)) {
								if (b.getType() == Material.STATIONARY_WATER || b.getType() == Material.WATER) {
									if (!TempBlock.isTempBlock(b)) {
										waterOrigins.put(b, b.getState().getData());
									}
									b.setType(Material.AIR);
								}
							}
							list.add(b); //Store it to say that it should be there
							location.subtract(x, y, z);
						}
					}
				}
			}
			
			//Remove all blocks that shouldn't be there
			Set<Block> set = new HashSet<Block>();
			set.addAll(waterOrigins.keySet());
			set.removeAll(list);
				
			for (Block b : set) {
				b.setType(waterOrigins.get(b).getItemType());
				b.setData(waterOrigins.get(b).getData());
				waterOrigins.remove(b);
			}
		}
		
		this.location = player.getLocation();
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void remove() {
		super.remove();
		
		for (Block b : waterOrigins.keySet()) {
			b.setType(waterOrigins.get(b).getItemType());
			b.setData(waterOrigins.get(b).getData());
		}
	}
	
	/**
	 * Returns whether the block provided is one of the air blocks used by WaterBubble
	 * 
	 * @param block The block being tested
	 * @return True if it's in use 
	 */
	public static boolean isAir(Block block) {
		for (WaterBubble bubble : CoreAbility.getAbilities(WaterBubble.class)) {
			if (bubble.waterOrigins.containsKey(block)) {
				return false;
			}
		}
		return true;
	}

}
