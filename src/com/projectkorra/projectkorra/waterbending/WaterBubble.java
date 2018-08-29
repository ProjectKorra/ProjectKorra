package com.projectkorra.projectkorra.waterbending;

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

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.TempBlock;

public class WaterBubble extends WaterAbility {

	@Attribute("Click" + Attribute.DURATION)
	private long clickDuration; // How long the click variant lasts.
	@Attribute(Attribute.RADIUS)
	private double maxRadius;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute("RequireAir")
	private boolean requireAir;

	private boolean isShift;
	private double radius;
	private boolean removing = false; // Is true when the radius is shrinking.
	private final Map<Block, MaterialData> waterOrigins = new ConcurrentHashMap<Block, MaterialData>();
	private Location location;
	private long lastActivation; // When the last click happened.

	public WaterBubble(final Player player, final boolean isShift) {
		super(player);

		this.setFields();

		if (CoreAbility.hasAbility(player, this.getClass())) {
			final WaterBubble bubble = CoreAbility.getAbility(player, this.getClass());

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

		if (!this.bPlayer.canBend(this)) {
			return;
		}

		this.radius = 0;
		this.isShift = isShift;
		this.location = player.getLocation();
		this.lastActivation = System.currentTimeMillis();

		this.start();
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

	@Override
	public void progress() {
		if (!this.bPlayer.canBend(this) || (this.isShift && !this.player.isSneaking()) || !this.location.getWorld().equals(this.player.getWorld())) {
			this.removing = true;
		}

		if (System.currentTimeMillis() - this.lastActivation > clickDuration && !this.isShift) {
			this.removing = true;
		}

		if (this.removing) {
			this.radius -= speed;

			if (this.radius <= 0.1) {
				this.radius = 0.1;
				this.remove();
			}
		} else {
			this.radius += speed;

			if (this.radius > maxRadius) {
				this.radius = maxRadius;
			}
		}

		final List<Block> list = new ArrayList<Block>();

		if (this.radius < maxRadius || !this.location.getBlock().equals(this.player.getLocation().getBlock())) {

			for (double x = -this.radius; x < this.radius; x += 0.5) {
				for (double y = -this.radius; y < this.radius; y += 0.5) {
					for (double z = -this.radius; z < this.radius; z += 0.5) {
						if (x * x + y * y + z * z <= this.radius * this.radius) {
							final Block b = this.location.add(x, y, z).getBlock();

							if (!this.waterOrigins.containsKey(b)) {
								if (b.getType() == Material.STATIONARY_WATER || b.getType() == Material.WATER) {
									if (!TempBlock.isTempBlock(b)) {
										this.waterOrigins.put(b, b.getState().getData());
									}
									b.setType(Material.AIR);
								}
							}
							list.add(b); // Store it to say that it should be there.
							this.location.subtract(x, y, z);
						}
					}
				}
			}

			// Remove all blocks that shouldn't be there.
			final Set<Block> set = new HashSet<Block>();
			set.addAll(this.waterOrigins.keySet());
			set.removeAll(list);

			for (final Block b : set) {
				if (b.getType() == Material.AIR) {
					b.setType(this.waterOrigins.get(b).getItemType());
					b.setData(this.waterOrigins.get(b).getData());
				}
				this.waterOrigins.remove(b);
			}
		}

		this.location = this.player.getLocation();
	}

	@Override
	public Location getLocation() {
		return this.location;
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

	@Override
	public void remove() {
		super.remove();

		for (final Block b : this.waterOrigins.keySet()) {
			if (b.getType() == Material.AIR) {
				b.setType(this.waterOrigins.get(b).getItemType());
				b.setData(this.waterOrigins.get(b).getData());
			}
		}
	}

	/**
	 * Returns whether the block provided is one of the air blocks used by
	 * WaterBubble
	 *
	 * @param block The block being tested
	 * @return True if it's in use
	 */
	public static boolean isAir(final Block block) {
		for (final WaterBubble bubble : CoreAbility.getAbilities(WaterBubble.class)) {
			if (bubble.waterOrigins.containsKey(block)) {
				return false;
			}
		}
		return true;
	}

}
