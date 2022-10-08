package com.projectkorra.projectkorra.waterbending.ice;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
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

	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.RADIUS)
	private double radius;
	@Attribute("NumberOfSpikes")
	private int numberOfSpikes;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.KNOCKUP)
	private double knockup;
	private Vector thrownForce;

	public IceSpikePillarField(final Player player) {
		super(player);

		if (this.bPlayer.isOnCooldown("IceSpikePillarField")) {
			return;
		}

		this.damage = applyModifiers(getConfig().getDouble("Abilities.Water.IceSpike.Field.Damage"));
		this.radius = applyModifiers(getConfig().getDouble("Abilities.Water.IceSpike.Field.Radius"));
		this.cooldown = applyInverseModifiers(getConfig().getLong("Abilities.Water.IceSpike.Field.Cooldown"));
		this.knockup = applyModifiers(getConfig().getDouble("Abilities.Water.IceSpike.Field.Knockup"));

		if (this.bPlayer.isAvatarState()) {
			this.damage = getConfig().getDouble("Abilities.Avatar.AvatarState.Water.IceSpike.Field.Damage");
			this.radius = getConfig().getDouble("Abilities.Avatar.AvatarState.Water.IceSpike.Field.Radius");
		}

		this.numberOfSpikes = (int) (((this.radius) * (this.radius)) / 4);
		this.start();
	}

	@Override
	public String getName() {
		return "IceSpike";
	}

	@Override
	public void progress() {
		this.thrownForce = new Vector(0, this.knockup, 0);
		final Random random = new Random();
		final int locX = this.player.getLocation().getBlockX();
		final int locY = this.player.getLocation().getBlockY();
		final int locZ = this.player.getLocation().getBlockZ();
		final List<Block> iceBlocks = new ArrayList<Block>();

		for (int x = (int) -(this.radius - 1); x <= (this.radius - 1); x++) {
			for (int z = (int) -(this.radius - 1); z <= (this.radius - 1); z++) {
				for (int y = -1; y <= 1; y++) {
					final Block testBlock = this.player.getWorld().getBlockAt(locX + x, locY + y, locZ + z);

					if (((WaterAbility.isIcebendable(this.player, testBlock.getType(), false) && !TempBlock.isTempBlock(testBlock)) || (TempBlock.isTempBlock(testBlock) && WaterAbility.isBendableWaterTempBlock(testBlock))) && ElementalAbility.isAir(testBlock.getRelative(BlockFace.UP).getType()) && !(testBlock.getX() == this.player.getEyeLocation().getBlock().getX() && testBlock.getZ() == this.player.getEyeLocation().getBlock().getZ())) {
						iceBlocks.add(testBlock);
						for (int i = 0; i < iceBlocks.size() / 2 + 1; i++) {
							final Random rand = new Random();
							if (rand.nextInt(5) == 0) {
								playIcebendingSound(iceBlocks.get(i).getLocation());
							}
						}
					}
				}
			}
		}

		int pillars;

		final List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(this.player.getLocation(), this.radius);
		for (pillars = 0; pillars < this.numberOfSpikes; pillars++) {
			if (iceBlocks.isEmpty()) {
				break;
			}

			Entity target = null;
			Block targetBlock = null;
			for (final Entity entity : entities) {
				if (entity instanceof LivingEntity && entity.getEntityId() != this.player.getEntityId()) {
					for (final Block block : iceBlocks) {
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
				final IceSpikePillar pillar = new IceSpikePillar(this.player, targetBlock.getLocation(), (int) this.damage, this.thrownForce, this.cooldown);
				pillar.inField = true;
				iceBlocks.remove(targetBlock);
			} else {
				pillars--;
			}
		}

		if (pillars > 0) {
			this.bPlayer.addCooldown("IceSpikePillarField", this.cooldown);
		}
		this.remove();
	}

	@Override
	public Location getLocation() {
		return this.player != null ? this.player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
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
		return this.damage;
	}

	public void setDamage(final double damage) {
		this.damage = damage;
	}

	public double getRadius() {
		return this.radius;
	}

	public void setRadius(final double radius) {
		this.radius = radius;
	}

	public int getNumberOfSpikes() {
		return this.numberOfSpikes;
	}

	public void setNumberOfSpikes(final int numberOfSpikes) {
		this.numberOfSpikes = numberOfSpikes;
	}

	public Vector getThrownForce() {
		return this.thrownForce;
	}

	public void setThrownForce(final Vector thrownForce) {
		this.thrownForce = thrownForce;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

}
