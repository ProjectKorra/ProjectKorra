package com.projectkorra.ProjectKorra.airbending;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Flight;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Utilities.ParticleEffect;

public class SpiritualProjection {

	public static ConcurrentHashMap<Player, SpiritualProjection> instances = new ConcurrentHashMap<Player, SpiritualProjection>();

	private long meditate = ProjectKorra.plugin.getConfig().getLong("Abilities.Air.SpiritualProjection.MeditateDuration");
	private long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Air.SpiritualProjection.Cooldown");
	private long projectionDur = ProjectKorra.plugin.getConfig().getLong("Abilities.Air.SpiritualProjection.ProjectionDuration");
	private int maxWallThickness = ProjectKorra.plugin.getConfig().getInt("Abilities.Air.SpiritualProjection.MaxNoClipWallThickness");
	private int spiritRange = ProjectKorra.plugin.getConfig().getInt("Abilities.Air.SpiritualProjection.ProjectionRange");
	private int rangeBuffer = ProjectKorra.plugin.getConfig().getInt("Abilities.Air.SpiritualProjection.ProjectionRangeBuffer");
	private Material[] nonoclip = {Material.BARRIER, Material.BEDROCK};

	private float speed;
	private Player player;
	private ArmorStand npcSeat;
	private Zombie npc;
	private int step;
	private double velocity = 0.15;
	private long time;
	private boolean isSpiritState;
	private ItemStack[] armor;
	private ItemStack[] inventory;

	public SpiritualProjection(Player player) {
		if (GeneralMethods.getBendingPlayer(player.getName()).isOnCooldown("SpiritualProjection")) {
			return;
		}
		if (instances.containsKey(player)) {
			Entity entity = GeneralMethods.getTargetedEntity(player, 8, new ArrayList<Entity>());
			if (entity != null && entity instanceof LivingEntity && entity == instances.get(player).npc) {
				instances.get(player).cancel();
			}
			return;
		}

		this.player = player;
		time = System.currentTimeMillis();
		instances.put(player, this);
		//WaterMethods.nonBloodbendable.add(player.getName());
	}

	@SuppressWarnings("deprecation")
	private void progress() {
		if (!instances.containsKey(player)) {
			return;
		}
		if (player.isDead() || !player.isOnline()) {
			cancel();
			return;
		}
		if(GeneralMethods.getBoundAbility(player) == null){
			cancel();
			return;
		}
		if(!GeneralMethods.getBoundAbility(player).equalsIgnoreCase("SpiritualProjection")){
			cancel();
			return;
		}
		if (!GeneralMethods.canBend(player.getName(), "SpiritualProjection")) {
			cancel();
			return;
		}	
		if (!isSpiritState) {
			if (System.currentTimeMillis() > time + meditate) {
				ParticleEffect.INSTANT_SPELL.display((float) Math.random()/3, (float) Math.random()/3, (float) Math.random()/3, 0.0F, 5, player.getLocation(), 257D);
				if (player.isOnGround() && !player.isSneaking()) {
					spawnNPC(player.getLocation());
					isSpiritState = true;
					speed = player.getFlySpeed();
					armor = player.getInventory().getArmorContents();
					inventory = player.getInventory().getContents();
					ItemStack[] empty = {};
					player.getInventory().setArmorContents(null);
					player.getInventory().setContents(empty);
					player.setCanPickupItems(false);
					new Flight(player);
					allowFlight();
					time = System.currentTimeMillis();
				}
			} else if (!player.isSneaking() || !player.isOnGround()) {
				cancel();
				return;
			} else {
				ParticleEffect.MOB_SPELL_AMBIENT.display((float) Math.random()/3, (float) Math.random()/3, (float) Math.random()/3, 0.0F, 5, player.getLocation(), 257D);
			}
		} else if (isSpiritState) {
			if (System.currentTimeMillis() > time + projectionDur) {
				cancel();
				return;
			}
			allowFlight();
			manageNPC();
			player.removePotionEffect(PotionEffectType.INVISIBILITY);
			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 1));
			playParticles(player.getLocation());
			noclipWall(player.getLocation());
			noclipWall(player.getEyeLocation());
			manageDistance();
		}
	}

	private void spawnNPC(Location location) {
		npcSeat = (ArmorStand) location.getWorld().spawnEntity(location.subtract(0, 1.5, 0), EntityType.ARMOR_STAND);
		npcSeat.setVisible(false);
		npcSeat.setGravity(false);
		npcSeat.setCanPickupItems(false);
		npcSeat.setSmall(true);
		npcSeat.setRemoveWhenFarAway(false);
		npc = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
		npc.setTarget(null);
		npc.setRemoveWhenFarAway(false);
		npc.setCustomName(player.getName());
		npc.setCustomNameVisible(true);
		npc.setCanPickupItems(false);
		npc.setBaby(false);
		npc.setHealth(player.getHealth());
		npc.setVillager(false);
		npc.setMetadata("spirit", new FixedMetadataValue(ProjectKorra.plugin, "1"));
		npcSeat.setPassenger(npc);
		
		for (Entity entity : player.getNearbyEntities(20, 20, 20)) {
			if (entity instanceof LivingEntity && entity instanceof Creature) {
				Creature creature = (Creature) entity;
				if (creature.getTarget() != null && creature.getTarget().getEntityId() == player.getEntityId()) {
					creature.setTarget(npc);
				}
			}
		}
		
		player.removePotionEffect(PotionEffectType.BLINDNESS);
		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1));
	}

	private void manageNPC() {
		if (npc.isDead()) {
			player.setHealth(0);
			cancel();
			return;
		}
		for (PotionEffect effect : npc.getActivePotionEffects()) {
			npc.removePotionEffect(effect.getType());
		}
		player.setHealth(npc.getHealth());
		npc.setFireTicks(0);
		npcSeat.setPassenger(npc);
		npc.setTarget(null);
	}

	private void removeNPC() {
		npcSeat.remove();
		npc.remove();
	}

	@SuppressWarnings("deprecation")
	private void noclipWall(Location location) {
		if (GeneralMethods.getBendingPlayer(player.getName()).isOnCooldown("spnoclip"))
			return;
		Block block = location.getBlock();
		BlockFace face = GeneralMethods.getCardinalDirection(location.getDirection());
		float pitch = location.getPitch();
		float yaw = location.getYaw();
		double y = location.getY();
		Vector v = player.getVelocity().clone();
		if (GeneralMethods.isSolid(block.getRelative(BlockFace.DOWN)) && player.isOnGround()) {
			face = BlockFace.DOWN;
		}else if (GeneralMethods.isSolid(block.getRelative(BlockFace.UP)) 
				&& player.getEyeLocation().distance(block.getRelative(BlockFace.UP).getLocation()) < 0.85) {
			face = BlockFace.UP;
		}
		if (GeneralMethods.isSolid(block.getRelative(face)) && !Arrays.asList(nonoclip).contains(block.getRelative(face).getType())) {
			for (int j = 1; j < maxWallThickness; j++) {
				if (GeneralMethods.isSolid(block.getRelative(face, j)) 
						&& Arrays.asList(nonoclip).contains(block.getRelative(face, j).getType())
						|| block.getRelative(face, j).getLocation().distance(npc.getLocation()) >= spiritRange) {
					break;
				}

				if (GeneralMethods.isSolid(block.getRelative(face, j).getRelative(BlockFace.UP)) 
						&& Arrays.asList(nonoclip).contains(block.getRelative(face, j).getRelative(BlockFace.UP).getType())
						|| block.getRelative(face, j).getRelative(BlockFace.UP).getLocation().distance(npc.getLocation()) >= spiritRange) {
					break;
				}

				if (!GeneralMethods.isSolid(block.getRelative(face, j)) && !GeneralMethods.isSolid(block.getRelative(face, j).getRelative(BlockFace.UP))) {

					GeneralMethods.getBendingPlayer(player.getName()).addCooldown("spnoclip", 50);
					Location result = block.getRelative(face, j).getLocation().add(0.5, 0, 0.5);
					if (face != BlockFace.DOWN) {
						result.setY(y);
					}
					if (face == BlockFace.DOWN) {
						result.setY(result.getY() - 0.5);
					} else if (face == BlockFace.UP) {
						result.setY(result.getY() + 2);
					}
					result.setPitch(pitch);
					result.setYaw(yaw);
					player.teleport(result);
					player.setVelocity(v);
					break;
				}
			}
		}
	}

	private void manageDistance() {
		if (player.getLocation().distance(npc.getLocation()) >= spiritRange - rangeBuffer) {
			player.setVelocity(new Vector(0, 0, 0));
		}
		if (player.getLocation().distance(npc.getLocation()) >= spiritRange) {
			cancel();
			return;
		}
	}

	private void playParticles(Location location) {
		Location l = location.clone().add(0, 1, 0);

		for (int i = 0; i < 3; i++) {
			double angle = this.step * this.velocity;
			double xRotation = Math.PI / 2.5 * 2.1;
			Vector v1 = new Vector(Math.cos(angle), Math.sin(angle), 0.5D).multiply(0.5);
			Vector v2 = new Vector(Math.cos(angle), Math.sin(angle), 0.0D).multiply(0.5);
			rotateAroundAxisX(v1, xRotation);
			rotateAroundAxisX(v2, -xRotation);
			rotateAroundAxisY(v1, -((this.step * Math.PI / 180)-1.575));
			rotateAroundAxisY(v2, (((this.step + 180) * Math.PI / 90)-2.575));

			GeneralMethods.displayColoredParticle(l.clone().add(v1), "C2C2C2");
			GeneralMethods.displayColoredParticle(l.clone().add(v2), "CBCBCB");

			double x = 0.5 * Math.cos(angle);
			double z = 0.5 * Math.sin(angle);
			GeneralMethods.displayColoredParticle(l.clone().add(x, i/10, z), "C2C2C2");

			this.step++;
		}
	}

	private Vector rotateAroundAxisX(Vector v, double angle) {
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		double y = v.getY() * cos - v.getZ() * sin;
		double z = v.getY() * sin + v.getZ() * cos;
		return v.setY(y).setZ(z);
	}

	private Vector rotateAroundAxisY(Vector v, double angle) {
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		double x = v.getX() * cos + v.getZ() * sin;
		double z = v.getX() * -sin + v.getZ() * cos;
		return v.setX(x).setZ(z);
	}

	public static ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<Player>();
		players.addAll(instances.keySet());
		return players;
	}
	
	public static boolean isSpiritualProjected(Entity entity) {
		if (entity instanceof Player) {
			return isSpiritualProjected((Player) entity);
		}
		return false;
	}

	public static boolean isSpiritualProjected(Player player) {
		if (instances.containsKey(player)) {
			return true;
		}
		return false;
	}

	private void allowFlight() {
		player.setAllowFlight(true);
		player.setFlying(true);
		player.setFlySpeed(0.1F);
	}

	private void removeFlight() {
		player.setAllowFlight(false);
		player.setFlying(false);
		player.setFlySpeed(speed);
	}

	public static void cancel(Player player) {
		if (instances.containsKey(player)) {
			instances.get(player).cancel();
		}
	}

	private void cancel() {
		if (isSpiritState) {
			if (player.isOnline()) {
				GeneralMethods.getBendingPlayer(player.getName()).addCooldown("SpiritualProjection", cooldown);
			}
			Location npcLoc = npc.getLocation();
			removeNPC();
			removeFlight();
			player.teleport(npcLoc.add(0, 1.5, 0));
			player.removePotionEffect(PotionEffectType.INVISIBILITY);
			player.removePotionEffect(PotionEffectType.BLINDNESS);
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1));
			player.getInventory().setArmorContents(armor);
			player.getInventory().setContents(inventory);
			player.setCanPickupItems(true);
		}
		//WaterMethods.nonBloodbendable.remove(player.getName());
		instances.remove(player);
	}

	public static void progressAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).progress();
		}
	}

	public static void removeAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).cancel();
		}
		instances.clear();
	}

	public Player getPlayer() {
		return player;
	}

	public long getCooldown() {
		return cooldown;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public long getProjectionDuration() {
		return projectionDur;
	}

	public void setProjectionDuration(long projectionDur) {
		this.projectionDur = projectionDur;
	}

	public long getMeditateCharge() {
		return meditate;
	}

	public void setMeditateCharge(long meditate) {
		this.meditate = meditate;
	}

	public int getProjectionRange() {
		return spiritRange;
	}

	public void setProjectionRange(int spiritRange) {
		this.spiritRange = spiritRange;
	}
}