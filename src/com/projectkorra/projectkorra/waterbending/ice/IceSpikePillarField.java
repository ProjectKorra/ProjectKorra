package com.projectkorra.projectkorra.waterbending.ice;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IceSpikePillarField extends IceAbility {

	private double damage;
	private double radius;
	private int numberOfSpikes;
	private long cooldown;
	private Vector thrownForce;

	public IceSpikePillarField(Player player) {
		super(player);

		if (bPlayer.isOnCooldown("IceSpikePillarField")) {
			return;
		}

		this.damage = getConfig().getDouble("Abilities.Water.IceSpike.Field.Damage");
		this.radius = getConfig().getDouble("Abilities.Water.IceSpike.Field.Radius");
		this.numberOfSpikes = (int) (((radius * 2) * (radius * 2)) / 16);
		this.cooldown = getConfig().getLong("Abilities.Water.IceSpike.Field.Cooldown");
		this.thrownForce = new Vector(0, getConfig().getDouble("Abilities.Water.IceSpike.Field.Push"), 0);

		Random random = new Random();
		int locX = player.getLocation().getBlockX();
		int locY = player.getLocation().getBlockY();
		int locZ = player.getLocation().getBlockZ();
		List<Block> iceBlocks = new ArrayList<Block>();

		for (int x = (int) -(radius - 1); x <= (radius - 1); x++) {
			for (int z = (int) -(radius - 1); z <= (radius - 1); z++) {
				for (int y = -1; y <= 1; y++) {
					Block testBlock = player.getWorld().getBlockAt(locX + x, locY + y, locZ + z);

					if (WaterAbility.isIcebendable(player, testBlock.getType(), false) && testBlock.getRelative(BlockFace.UP).getType() == Material.AIR 
							&& !(testBlock.getX() == player.getEyeLocation().getBlock().getX() && testBlock.getZ() == player.getEyeLocation().getBlock().getZ())
							&& !TempBlock.isTempBlock(testBlock)) {
						iceBlocks.add(testBlock);
						for (int i = 0; i < iceBlocks.size() / 2 + 1; i++) {
							Random rand = new Random();
							if (rand.nextInt(5) == 0) {
								playIcebendingSound(iceBlocks.get(i).getLocation());
							}
						}
					}
				}
			}
		}

		List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(player.getLocation(), radius);
		for (int i = 0; i < numberOfSpikes; i++) {
			if (iceBlocks.isEmpty()) {
				return;
			}

			Entity target = null;
			Block targetBlock = null;
			for (Entity entity : entities) {
				if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId()) {
					for (Block block : iceBlocks) {
						if (block.getX() == entity.getLocation().getBlockX() && block.getZ() == entity.getLocation().getBlockZ()) {
							target = entity;
							targetBlock = block;
							break;
						}
					}
				} else {
					continue;
				}
			}

			if (target != null) {
				entities.remove(target);
			} else {
				targetBlock = iceBlocks.get(random.nextInt(iceBlocks.size()));
			}

			if (targetBlock.getRelative(BlockFace.UP).getType() != Material.ICE) {
				IceSpikePillar pillar = new IceSpikePillar(player, targetBlock.getLocation(), (int) damage, thrownForce, cooldown);
				pillar.inField = true;
				bPlayer.addCooldown("IceSpikePillarField", cooldown);
				iceBlocks.remove(targetBlock);
			}
		}
	}

	@Override
	public String getName() {
		return "IceSpike";
	}

	@Override
	public void progress() {
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public int getNumberOfSpikes() {
		return numberOfSpikes;
	}

	public void setNumberOfSpikes(int numberOfSpikes) {
		this.numberOfSpikes = numberOfSpikes;
	}

	public Vector getThrownForce() {
		return thrownForce;
	}

	public void setThrownForce(Vector thrownForce) {
		this.thrownForce = thrownForce;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

}
