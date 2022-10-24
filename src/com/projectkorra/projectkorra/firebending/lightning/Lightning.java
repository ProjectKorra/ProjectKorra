package com.projectkorra.projectkorra.firebending.lightning;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.firebending.FireJet;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.LightningAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.MovementHandler;

public class Lightning extends LightningAbility {

	public static enum State {
		START, STRIKE, MAINBOLT
	}

	private static final int POINT_GENERATION = 5;

	@Attribute("Charged")
	private boolean charged;
	private boolean hitWater;
	private boolean hitIce;
	private boolean hitCopper;
	private boolean selfHitWater;
	private boolean selfHitClose;
	private boolean allowOnFireJet;
	private boolean transformMobs;
	private boolean chargeCreeper;
	private boolean chainLightningRods;
	@Attribute("ArcOnIce")
	private boolean arcOnIce;
	@Attribute("ArcOnCopper")
	private boolean arcOnCopper;
	private int waterArcs;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.CHARGE_DURATION)
	private double chargeTime;
	@Attribute("SubArcChance")
	private double subArcChance;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute("MaxChainArcs")
	private double maxChainArcs;
	@Attribute("Chain" + Attribute.RANGE)
	private double chainRange;
	@Attribute("WaterArc" + Attribute.RANGE)
	private double waterArcRange;
	@Attribute("ChainArcChance")
	private double chainArcChance;
	@Attribute("StunChance")
	private double stunChance;
	@Attribute("Stun" + Attribute.DURATION)
	private double stunDuration;
	@Attribute("MaxArcAngle")
	private double maxArcAngle;
	private double particleRotation;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private State state;
	private Location origin;
	private Location destination;
	private ArrayList<Entity> affectedEntities;
	private ArrayList<Arc> arcs;
	private ArrayList<BukkitRunnable> tasks;
	private ArrayList<Location> locations;
	private static final Set<EntityType> LIGHTNING_AFFECTED = Set.of(EntityType.CREEPER, EntityType.VILLAGER,
			EntityType.PIG, EntityType.MUSHROOM_COW, EntityType.TURTLE, EntityType.SKELETON_HORSE
	);

	public Lightning(final Player player) {
		super(player);

		if (!this.bPlayer.canBend(this)) {
			return;
		}
		if (hasAbility(player, Lightning.class)) {
			if (!getAbility(player, Lightning.class).isCharged()) {
				return;
			}
		}

		this.charged = false;
		this.hitWater = false;
		this.hitIce = false;
		this.hitCopper = false;
		this.state = State.START;
		this.affectedEntities = new ArrayList<>();
		this.arcs = new ArrayList<>();
		this.tasks = new ArrayList<>();
		this.locations = new ArrayList<>();

		this.selfHitWater = getConfig().getBoolean("Abilities.Fire.Lightning.SelfHitWater");
		this.selfHitClose = getConfig().getBoolean("Abilities.Fire.Lightning.SelfHitClose");
		this.arcOnIce = getConfig().getBoolean("Abilities.Fire.Lightning.ArcOnIce");
		this.arcOnCopper = getConfig().getBoolean("Abilities.Fire.Lightning.ArcOnCopper");
		this.range = applyModifiersRange(getConfig().getDouble("Abilities.Fire.Lightning.Range"));
		this.damage = applyModifiersDamage(getConfig().getDouble("Abilities.Fire.Lightning.Damage"));
		this.maxArcAngle = getConfig().getDouble("Abilities.Fire.Lightning.MaxArcAngle");
		this.subArcChance = getConfig().getDouble("Abilities.Fire.Lightning.SubArcChance");
		this.chainRange = applyModifiersRange(getConfig().getDouble("Abilities.Fire.Lightning.ChainArcRange"));
		this.chainArcChance = applyModifiers(getConfig().getDouble("Abilities.Fire.Lightning.ChainArcChance"));
		this.waterArcRange = applyModifiers(getConfig().getDouble("Abilities.Fire.Lightning.WaterArcRange"));
		this.stunChance = applyModifiers(getConfig().getDouble("Abilities.Fire.Lightning.StunChance"));
		this.stunDuration = applyModifiers(getConfig().getDouble("Abilities.Fire.Lightning.StunDuration"));
		this.maxChainArcs = applyModifiers(getConfig().getInt("Abilities.Fire.Lightning.MaxChainArcs"));
		this.waterArcs = (int) applyModifiers(getConfig().getInt("Abilities.Fire.Lightning.WaterArcs"));
		this.chargeTime = applyInverseModifiers(getConfig().getLong("Abilities.Fire.Lightning.ChargeTime"));
		this.cooldown = applyModifiersCooldown(getConfig().getLong("Abilities.Fire.Lightning.Cooldown"));
		this.allowOnFireJet = getConfig().getBoolean("Abilities.Fire.Lightning.AllowOnFireJet");
		this.transformMobs = getConfig().getBoolean("Abilities.Fire.Lightning.TransformMobs");
		this.chargeCreeper = getConfig().getBoolean("Abilities.Fire.Lightning.ChargeCreeper");
		this.chainLightningRods = getConfig().getBoolean("Abilities.Fire.Lightning.ChainLightningRods");

		/*this.range = this.getDayFactor(this.range);
		this.subArcChance = this.getDayFactor(this.subArcChance);
		this.damage = this.getDayFactor(this.damage);
		this.maxChainArcs = this.getDayFactor(this.maxChainArcs);
		this.chainArcChance = this.getDayFactor(this.chainArcChance);
		this.chainRange = this.getDayFactor(this.chainRange);
		this.waterArcRange = this.getDayFactor(this.waterArcRange);
		this.stunChance = this.getDayFactor(this.stunChance);
		this.stunDuration = this.getDayFactor(this.stunDuration);*/

		if (this.bPlayer.isAvatarState()) {
			this.chargeTime = getConfig().getLong("Abilities.Avatar.AvatarState.Fire.Lightning.ChargeTime");
			this.cooldown = getConfig().getLong("Abilities.Avatar.AvatarState.Fire.Lightning.Cooldown");
			this.damage = getConfig().getDouble("Abilities.Avatar.AvatarState.Fire.Lightning.Damage");
		}

		this.start();
	}

	/**
	 * Damages an entity, and may cause paralysis depending on the config.
	 *
	 * @param lent The LivingEntity that is being damaged
	 */
	public void electrocute(final LivingEntity lent) {
		playLightningbendingSound(lent.getLocation());
		playLightningbendingSound(this.player.getLocation());
		playLightningbendingHitSound(lent.getLocation());
		playLightningbendingHitSound(this.player.getLocation());
		DamageHandler.damageEntity(lent, this.damage, this);
		if (ThreadLocalRandom.current().nextDouble() <= this.stunChance) {
			final MovementHandler mh = new MovementHandler(lent, this);
			mh.stopWithDuration((long) this.stunDuration, Element.LIGHTNING.getColor() + "* Electrocuted *");
		}
	}

	/**
	 * Checks if a block is transparent, also considers the ARC_ON_ICE config
	 * option.
	 *
	 * @param player the player that is viewing the block
	 * @param block the block
	 * @return true if the block is transparent
	 */
	private boolean isTransparentForLightning(final Player player, final Block block) {
		if (this.isTransparent(block)) {
			if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
				return false;
			} else if (isIce(block)) {
				return this.arcOnIce;
			} else {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Transforms mobs as vanilla Minecraft lightning does, considers LIGHTNING_AFFECTED mobs
	 *
	 * @param entity Entity to transform
	 */
	private void transformMobs(final LivingEntity entity) {
		if ((!this.transformMobs && !this.chargeCreeper) || !LIGHTNING_AFFECTED.contains(entity.getType())) {
			return;
		} else if (!this.transformMobs && this.chargeCreeper && entity.getType() == EntityType.CREEPER) {
			((Creeper) entity).setPowered(true);
			return;
		} else if (this.transformMobs && LIGHTNING_AFFECTED.contains(entity.getType())) {
			switch (entity.getType()) {
				case CREEPER:
					((Creeper) entity).setPowered(true);
					break;
				case VILLAGER:
					entity.getWorld().spawnEntity(entity.getLocation(), EntityType.WITCH);
					entity.remove();
					break;
				case PIG:
					entity.getWorld().spawnEntity(entity.getLocation(), EntityType.ZOMBIFIED_PIGLIN);
					entity.remove();
					break;
				case MUSHROOM_COW:
					MushroomCow cow = (MushroomCow) entity;
					cow.setVariant(cow.getVariant() == MushroomCow.Variant.RED ? MushroomCow.Variant.BROWN : MushroomCow.Variant.RED);
					break;
				case TURTLE:
					entity.getWorld().dropItem(entity.getLocation(), new ItemStack(Material.BOWL, 1));
					entity.setHealth(0);
					break;
				default:
					break;
			}
			return;
		}
	}
	
	/**
	 * Charges lightning rods. If ChainLightningRods is enabled, it will power all lightning rods
	 * below the block that was hit
	 *
	 * @param block
	 */
	private void powerLightningRods(final Block block) {
		if (isLightningRod(block)) {
			block.getWorld().spawnParticle(Particle.valueOf("ELECTRIC_SPARK"), block.getLocation().clone().add(0.5, 0.5, 0.5), 6, 0.125, 0.125, 0.125, 0.05);
			
			List<Block> blocks = new ArrayList<>();
			
			if (this.chainLightningRods) {
				Block down = block.getRelative(BlockFace.DOWN);
				
				if (isLightningRod(down)) {
					while (isLightningRod(down)) {
						updateLightningRod(down, true);
						
						blocks.add(down);
						
						down = down.getRelative(BlockFace.DOWN);
					}
				} else {
					updateLightningRod(block, true);
				}
			}
			Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, () -> {
				if (blocks.isEmpty()) {
					updateLightningRod(block, false);
					return;
				}
				for (Block powerable : blocks) {
					updateLightningRod(powerable, false);
				}
			}, 8);
		}
	}
	
	private void updateLightningRod(final Block block, final boolean powered) {
		Powerable powerable = (Powerable) block.getBlockData();
		powerable.setPowered(powered);
		block.setBlockData(powerable);
	}
	
	private boolean isCopper(final Block block) {
		return block.getType().name().contains("COPPER");
	}
	
	private boolean isLightningRod(final Block block) {
		return GeneralMethods.getMCVersion() >= 1170 && block.getType() == Material.valueOf("LIGHTNING_ROD");
	}

	/**
	 * Progresses the instance of this ability by 1 tick. This is the heart of
	 * the ability, it checks if it needs to remove itself, and handles the
	 * initial Lightning Arc generation.
	 *
	 * Once all of the arcs have been created then this ability instance gets
	 * removed, but the BukkitRunnables continue until they remove themselves.
	 **/
	@Override
	public void progress() {
		if (this.player.isDead() || !this.player.isOnline()) {
			this.removeWithTasks();
			return;
		} else if (!this.bPlayer.canBendIgnoreCooldowns(this)) {
			this.remove();
			return;
		} else if (CoreAbility.hasAbility(player, FireJet.class) && !allowOnFireJet){
			this.removeWithTasks();
			return;
		}

		this.locations.clear();

		if (this.state == State.START) {
			if (this.bPlayer.isOnCooldown(this)) {
				this.remove();
				return;
			} else if (System.currentTimeMillis() - this.getStartTime() > this.chargeTime) {
				this.charged = true;
			}

			if (this.charged) {
				if (this.player.isSneaking()) {
					final Location loc = this.player.getEyeLocation().add(this.player.getEyeLocation().getDirection().normalize().multiply(1.2));
					loc.add(0, 0.3, 0);
					playLightningbendingParticle(loc, 0.2F, 0.2F, 0.2F);
					if (ThreadLocalRandom.current().nextDouble() < .2) {
						playLightningbendingChargingSound(loc);
					}
					
				} else {
					this.state = State.MAINBOLT;
					this.bPlayer.addCooldown(this);
					final Entity target = GeneralMethods.getTargetedEntity(this.player, this.range);
					this.origin = this.player.getEyeLocation();

					if (target != null) {
						this.destination = target.getLocation();
					} else {
						Block targetBlock = GeneralMethods.getTargetedLocation(this.player, this.range, false).getBlock();
						boolean foundRod = isLightningRod(targetBlock);
						
						if (!foundRod) {
							for (Block block : GeneralMethods.getBlocksAroundPoint(targetBlock.getLocation(), 1.25)) {
								if (isLightningRod(block)) {
									foundRod = true;
									targetBlock = block;
									break;
								}
							}
						}
						if (foundRod) {
							this.destination = targetBlock.getLocation().clone().add(0.5, 0.5, 0.5);
						} else {
							this.destination = this.player.getEyeLocation().add(this.player.getEyeLocation().getDirection().normalize().multiply(this.range));
						}
					}
				}
			} else {
				if (!this.player.isSneaking()) {
					this.remove();
					return;
				}

				final Location localLocation1 = this.player.getLocation();
				final double d1 = 0.1570796326794897D;
				final double d2 = 0.06283185307179587D;
				final double d3 = 1.0D;
				final double d4 = 1.0D;
				final double d5 = d1 * this.particleRotation;
				final double d6 = d2 * this.particleRotation;
				final double d7 = localLocation1.getX() + d4 * Math.cos(d5);
				final double d8 = localLocation1.getZ() + d4 * Math.sin(d5);
				final double newY = (localLocation1.getY() + 1.0D + d4 * Math.cos(d6));
				final Location localLocation2 = new Location(this.player.getWorld(), d7, newY, d8);
				playLightningbendingParticle(localLocation2);
				this.particleRotation += 1.0D / d3;
				if (ThreadLocalRandom.current().nextDouble() < .2) {
					playLightningbendingChargingSound(this.player.getLocation());
				}
			}

		} else if (this.state == State.MAINBOLT) {
			final Arc mainArc = new Arc(this.origin, this.destination);
			mainArc.generatePoints(POINT_GENERATION);
			this.arcs.add(mainArc);
			final ArrayList<Arc> subArcs = mainArc.generateArcs(this.subArcChance, this.range / 2.0, this.maxArcAngle);
			this.arcs.addAll(subArcs);
			this.state = State.STRIKE;
		} else if (this.state == State.STRIKE) {
			for (int i = 0; i < this.arcs.size(); i++) {
				final Arc arc = this.arcs.get(i);
				for (int j = 0; j < arc.getAnimationLocations().size() - 1; j++) {
					final Location iterLoc = arc.getAnimationLocations().get(j).getLocation().clone();
					final Location dest = arc.getAnimationLocations().get(j + 1).getLocation().clone();
					if (this.selfHitClose && this.player.getLocation().distanceSquared(iterLoc) < 9 && !this.isTransparentForLightning(this.player, iterLoc.getBlock()) && !this.affectedEntities.contains(this.player)) {
						this.affectedEntities.add(this.player);
						this.electrocute(this.player);
					}

					while (iterLoc.distanceSquared(dest) > 0.15 * 0.15) {
						final BukkitRunnable task = new LightningParticle(arc, iterLoc.clone(), this.selfHitWater, this.waterArcs);
						final double timer = arc.getAnimationLocations().get(j).getAnimCounter() / 2;
						task.runTaskTimer(ProjectKorra.plugin, (long) timer, 1);
						this.tasks.add(task);
						iterLoc.add(GeneralMethods.getDirection(iterLoc, dest).normalize().multiply(0.15));
					}
				}
				this.arcs.remove(i);
				i--;
			}
			if (this.tasks.size() == 0) {
				this.remove();
				return;
			}
		}
	}

	/**
	 * Removes the instance of this ability and cancels any current runnables
	 */
	public void removeWithTasks() {
		for (int i = 0; i < this.tasks.size(); i++) {
			this.tasks.get(i).cancel();
			i--;
		}
		this.remove();
	}

	/**
	 * Represents a Lightning Arc Point particle animation. This basically just
	 * holds a location and counts the amount of times that a particle has been
	 * animated.
	 **/
	public class AnimationLocation {
		private Location location;
		private int animationCounter;

		public AnimationLocation(final Location loc, final int animationCounter) {
			this.location = loc;
			this.animationCounter = animationCounter;
		}

		public int getAnimCounter() {
			return this.animationCounter;
		}

		public Location getLocation() {
			return this.location;
		}

		public void setAnimationCounter(final int animationCounter) {
			this.animationCounter = animationCounter;
		}

		public void setLocation(final Location location) {
			this.location = location;
		}
	}

	/**
	 * An Arc represents a Lightning arc for the specific ability. These Arcs
	 * contain a list of Particles that are used to display the entire arc. Arcs
	 * can also generate a list of subarcs that chain off of their own instance.
	 **/
	public class Arc {
		private int animationCounter;
		private Vector direction;
		private final ArrayList<Location> points;
		private final ArrayList<AnimationLocation> animationLocations;
		private final ArrayList<LightningParticle> particles;
		private final ArrayList<Arc> subArcs;

		public Arc(final Location startPoint, final Location endPoint) {
			this.points = new ArrayList<>();
			this.points.add(startPoint.clone());
			this.points.add(endPoint.clone());
			this.direction = GeneralMethods.getDirection(startPoint, endPoint);
			this.particles = new ArrayList<>();
			this.subArcs = new ArrayList<>();
			this.animationLocations = new ArrayList<>();
			this.animationCounter = 0;
		}

		/**
		 * Stops this Arc from further animating or doing damage.
		 */
		public void cancel() {
			for (int i = 0; i < this.particles.size(); i++) {
				this.particles.get(i).cancel();
			}

			for (final Arc subArc : this.subArcs) {
				subArc.cancel();
			}
		}

		/**
		 * Randomly generates subarcs off of this arc.
		 *
		 * @param chance The chance that an arc will be generated for each
		 *            specific point in the arc. Note: if you generate a lot of
		 *            points then chance will need to be lowered.
		 * @param range The length of each subarc.
		 *
		 **/
		public ArrayList<Arc> generateArcs(final double chance, final double range, final double maxArcAngle) {
			final ArrayList<Arc> arcs = new ArrayList<>();

			for (int i = 0; i < this.animationLocations.size(); i++) {
				if (ThreadLocalRandom.current().nextDouble() < chance) {
					final Location loc = this.animationLocations.get(i).getLocation();
					final double angle = (ThreadLocalRandom.current().nextDouble() - 0.5) * maxArcAngle * 2;
					final Vector dir = GeneralMethods.rotateXZ(this.direction.clone(), angle);
					final double randRange = (ThreadLocalRandom.current().nextDouble() * range) + (range / 3.0);

					final Location loc2 = loc.clone().add(dir.normalize().multiply(randRange));
					final Arc arc = new Arc(loc, loc2);

					this.subArcs.add(arc);
					arc.setAnimationCounter(this.animationLocations.get(i).getAnimCounter());
					arc.generatePoints(POINT_GENERATION);
					arcs.add(arc);
					arcs.addAll(arc.generateArcs(chance / 2.0, range / 2.0, maxArcAngle));
				}
			}
			return arcs;
		}

		/**
		 * Runs an arc generation algorithm by first creating two points, the
		 * starting point and the ending point. Next, it creates a point in the
		 * middle that has an offset relative to the beginning and end points.
		 * Now that the arc is split into 3 points, we continue this processes
		 * by generating middle points in the two halfs of this arc. This
		 * process continues the amount of times specified.
		 *
		 * @param times The amount of times that the arc will be split in half
		 *            causes O(n^2) complexity
		 **/
		public void generatePoints(final int times) {
			for (int i = 0; i < times; i++) {
				for (int j = 0; j < this.points.size() - 1; j += 2) {
					final Location loc1 = this.points.get(j);
					final Location loc2 = this.points.get(j + 1);
					double adjac = 0;
					if (loc1.getWorld().equals(loc2.getWorld())) {
						adjac = loc1.distance(loc2) / 2;
					}

					double angle = (ThreadLocalRandom.current().nextDouble() - 0.5) * Lightning.this.maxArcAngle;

					angle += angle >= 0 ? 10 : -10;

					final double radians = Math.toRadians(angle);
					final double hypot = adjac / Math.cos(radians);
					final Vector dir = GeneralMethods.rotateXZ(this.direction.clone(), angle);
					final Location newLoc = loc1.clone().add(dir.normalize().multiply(hypot));

					newLoc.add(0, (ThreadLocalRandom.current().nextDouble() - 0.5) / 2.0, 0);
					this.points.add(j + 1, newLoc);
				}
			}
			for (int i = 0; i < this.points.size(); i++) {
				this.animationLocations.add(new AnimationLocation(this.points.get(i), this.animationCounter));
				this.animationCounter++;
			}
		}

		public int getAnimationCounter() {
			return this.animationCounter;
		}

		public void setAnimationCounter(final int animationCounter) {
			this.animationCounter = animationCounter;
		}

		public Vector getDirection() {
			return this.direction;
		}

		public void setDirection(final Vector direction) {
			this.direction = direction;
		}

		public ArrayList<Location> getPoints() {
			return this.points;
		}

		public ArrayList<AnimationLocation> getAnimationLocations() {
			return this.animationLocations;
		}

		public ArrayList<LightningParticle> getParticles() {
			return this.particles;
		}

		public ArrayList<Arc> getSubArcs() {
			return this.subArcs;
		}

	}

	/**
	 * A Runnable Particle that continuously displays itself until it reaches a
	 * certain time limit.
	 *
	 * These LightningParticles do the actual checking for player collision and
	 * handle damaging any entities. These Runnables also check to see if they
	 * reach water, in which case they will generate subarcs to branch out.
	 **/
	public class LightningParticle extends BukkitRunnable {
		private boolean selfHitWater;
		private int count = 0;
		private int waterArcs;
		private Arc arc;
		private Location location;

		public LightningParticle(final Arc arc, final Location location, final boolean selfHitWater, final int waterArcs) {
			this.arc = arc;
			this.location = location;
			this.selfHitWater = selfHitWater;
			this.waterArcs = waterArcs;
			arc.particles.add(this);
		}

		/** Cancels this Runnable **/
		@Override
		public void cancel() {
			super.cancel();
			Lightning.this.tasks.remove(this);
		}

		/**
		 * Animates the Location, checks for water/player collision and also
		 * deals with any chain subarcs.
		 */
		@Override
		public void run() {
			playLightningbendingParticle(this.location, 0F, 0F, 0F);
			this.count++;
			if (this.count > 5) {
				this.cancel();
			} else if (this.count == 1) {
				if (ThreadLocalRandom.current().nextDouble() < .1) {
					playLightningbendingSound(location);
				}

				if (!Lightning.this.isTransparentForLightning(Lightning.this.player, this.location.getBlock()) && !isCopper(this.location.getBlock())) {
					powerLightningRods(this.location.getBlock());
					this.arc.cancel();
					return;
				}
				final Block block = this.location.getBlock();
				// We only want to consider this particle as part of the location on the its first tick, when it actually does the electrocution.
				// The later ticks are just for visual purposes.
				Lightning.this.locations.add(block.getLocation());

				// Handle Water and Copper electrocution.
				if ((!Lightning.this.hitWater && isWater(block) || (!Lightning.this.hitCopper && Lightning.this.arcOnCopper && isCopper(block)) || (Lightning.this.arcOnIce && isIce(block)))) {
					if (isWater(block) || isIce(block)) {
						Lightning.this.hitWater = true;
						if (isIce(block)) {
							Lightning.this.hitIce = true;
						}
					} else if (isCopper(block)) {
						Lightning.this.hitCopper = true;
						Lightning.this.waterArcRange = 2;
					}
					powerLightningRods(block);

					for (int i = 0; i < this.waterArcs; i++) {
						final Location origin = this.location.clone();
						origin.add(new Vector((ThreadLocalRandom.current().nextDouble() - 0.5) * 2, 0, (ThreadLocalRandom.current().nextDouble() - 0.5) * 2));
						Lightning.this.destination = origin.clone().add(new Vector((ThreadLocalRandom.current().nextDouble() - 0.5) * Lightning.this.waterArcRange, ThreadLocalRandom.current().nextDouble() - 0.7, (ThreadLocalRandom.current().nextDouble() - 0.5) * Lightning.this.waterArcRange));
						final Arc newArc = new Arc(origin, Lightning.this.destination);
						newArc.generatePoints(POINT_GENERATION);
						Lightning.this.arcs.add(newArc);
					}
				}

				for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, 2.5)) {

					// If the player is in water we will electrocute them only if they are standing in water. If the lightning hit ice we can electrocute them all the time.
					if (entity.equals(Lightning.this.player) && !(this.selfHitWater && Lightning.this.hitWater && isWater(Lightning.this.player.getLocation().getBlock())) && !(this.selfHitWater && Lightning.this.hitCopper) && !(this.selfHitWater && Lightning.this.hitIce)) {
						continue;
					}

					if (entity instanceof LivingEntity && !Lightning.this.affectedEntities.contains(entity)) {
						Lightning.this.affectedEntities.add(entity);
						final LivingEntity lent = (LivingEntity) entity;
						if (lent instanceof Player) {
							playLightningbendingSound(lent.getLocation());
							playLightningbendingSound(Lightning.this.player.getLocation());
							final Player p = (Player) lent;
							final Lightning light = getAbility(p, Lightning.class);
							if (light != null && light.state == State.START) {
								light.charged = true;
								Lightning.this.remove();
								return;
							}
						}
						
						Lightning.this.transformMobs(lent);
						Lightning.this.electrocute(lent);

						// Handle Chain Lightning.
						if (Lightning.this.maxChainArcs >= 1 && ThreadLocalRandom.current().nextDouble() <= Lightning.this.chainArcChance) {
							Lightning.this.maxChainArcs--;
							for (final Entity ent : GeneralMethods.getEntitiesAroundPoint(lent.getLocation(), Lightning.this.chainRange)) {
								if (!ent.equals(Lightning.this.player) && !ent.equals(lent) && ent instanceof LivingEntity && !Lightning.this.affectedEntities.contains(ent)) {
									Lightning.this.origin = lent.getLocation().add(0, 1, 0);
									Lightning.this.destination = ent.getLocation().add(0, 1, 0);
									final Arc newArc = new Arc(Lightning.this.origin, Lightning.this.destination);
									newArc.generatePoints(POINT_GENERATION);
									Lightning.this.arcs.add(newArc);
									this.cancel();
									return;
								}
							}
						}
					}
				}
			}
		}

		public boolean isSelfHitWater() {
			return this.selfHitWater;
		}

		public void setSelfHitWater(final boolean selfHitWater) {
			this.selfHitWater = selfHitWater;
		}

		public int getCount() {
			return this.count;
		}

		public void setCount(final int count) {
			this.count = count;
		}

		public int getWaterArcs() {
			return this.waterArcs;
		}

		public void setWaterArcs(final int waterArcs) {
			this.waterArcs = waterArcs;
		}

		public Arc getArc() {
			return this.arc;
		}

		public void setArc(final Arc arc) {
			this.arc = arc;
		}

		public Location getLocation() {
			return this.location;
		}

		public void setLocation(final Location location) {
			this.location = location;
		}
	}

	@Override
	public String getName() {
		return "Lightning";
	}

	@Override
	public Location getLocation() {
		return this.origin;
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

	@Override
	public boolean isCollidable() {
		return this.arcs.size() > 0;
	}

	@Override
	public List<Location> getLocations() {
		return this.locations;
	}

	public boolean isCharged() {
		return this.charged;
	}

	public void setCharged(final boolean charged) {
		this.charged = charged;
	}

	public boolean isHitWater() {
		return this.hitWater;
	}

	public void setHitWater(final boolean hitWater) {
		this.hitWater = hitWater;
	}

	public boolean isHitIce() {
		return this.hitIce;
	}

	public void setHitIce(final boolean hitIce) {
		this.hitIce = hitIce;
	}
	
	public boolean isHitCopper() {
		return this.hitCopper;
	}
	
	public void setHitCopper(final boolean hitCopper) {
		this.hitCopper = hitCopper;
	}

	public boolean isSelfHitWater() {
		return this.selfHitWater;
	}

	public void setSelfHitWater(final boolean selfHitWater) {
		this.selfHitWater = selfHitWater;
	}

	public boolean isSelfHitClose() {
		return this.selfHitClose;
	}

	public void setSelfHitClose(final boolean selfHitClose) {
		this.selfHitClose = selfHitClose;
	}

	public boolean isArcOnIce() {
		return this.arcOnIce;
	}

	public void setArcOnIce(final boolean arcOnIce) {
		this.arcOnIce = arcOnIce;
	}
	
	public boolean isArcOnCopper() {
		return this.arcOnCopper;
	}
	
	public void setArcOnCopper(final boolean arcOnCopper) {
		this.arcOnCopper = arcOnCopper;
	}

	public int getWaterArcs() {
		return this.waterArcs;
	}

	public void setWaterArcs(final int waterArcs) {
		this.waterArcs = waterArcs;
	}

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	public double getChargeTime() {
		return this.chargeTime;
	}

	public void setChargeTime(final double chargeTime) {
		this.chargeTime = chargeTime;
	}

	public double getSubArcChance() {
		return this.subArcChance;
	}

	public void setSubArcChance(final double subArcChance) {
		this.subArcChance = subArcChance;
	}

	public double getDamage() {
		return this.damage;
	}

	public void setDamage(final double damage) {
		this.damage = damage;
	}

	public double getMaxChainArcs() {
		return this.maxChainArcs;
	}

	public void setMaxChainArcs(final double maxChainArcs) {
		this.maxChainArcs = maxChainArcs;
	}

	public double getChainRange() {
		return this.chainRange;
	}

	public void setChainRange(final double chainRange) {
		this.chainRange = chainRange;
	}

	public double getWaterArcRange() {
		return this.waterArcRange;
	}

	public void setWaterArcRange(final double waterArcRange) {
		this.waterArcRange = waterArcRange;
	}

	public double getChainArcChance() {
		return this.chainArcChance;
	}

	public void setChainArcChance(final double chainArcChance) {
		this.chainArcChance = chainArcChance;
	}

	public double getStunChance() {
		return this.stunChance;
	}

	public void setStunChance(final double stunChance) {
		this.stunChance = stunChance;
	}

	public double getStunDuration() {
		return this.stunDuration;
	}

	public void setStunDuration(final double stunDuration) {
		this.stunDuration = stunDuration;
	}

	public double getMaxArcAngle() {
		return this.maxArcAngle;
	}

	public void setMaxArcAngle(final double maxArcAngle) {
		this.maxArcAngle = maxArcAngle;
	}

	public double getParticleRotation() {
		return this.particleRotation;
	}

	public void setParticleRotation(final double particleRotation) {
		this.particleRotation = particleRotation;
	}

	public State getState() {
		return this.state;
	}

	public void setState(final State state) {
		this.state = state;
	}

	public Location getOrigin() {
		return this.origin;
	}

	public void setOrigin(final Location origin) {
		this.origin = origin;
	}

	public Location getDestination() {
		return this.destination;
	}

	public void setDestination(final Location destination) {
		this.destination = destination;
	}

	public static int getPointGeneration() {
		return POINT_GENERATION;
	}

	public ArrayList<Entity> getAffectedEntities() {
		return this.affectedEntities;
	}

	public ArrayList<Arc> getArcs() {
		return this.arcs;
	}

	public ArrayList<BukkitRunnable> getTasks() {
		return this.tasks;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

}
