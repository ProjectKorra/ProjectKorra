package com.projectkorra.projectkorra.firebending;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.SurgeWave;
import com.projectkorra.projectkorra.waterbending.Torrent;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;
import com.projectkorra.projectkorra.waterbending.WaterSpoutWave;
import com.projectkorra.projectkorra.waterbending.combo.IceWave;
import com.projectkorra.projectkorra.waterbending.ice.PhaseChange;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArmsSpear;

public class HeatControl extends FireAbility {

	public enum HeatControlType {
		COOK, EXTINGUISH, MELT, SOLIDIFY
	}

	private static final Material[] COOKABLE_MATERIALS = { Material.RAW_BEEF, Material.RAW_CHICKEN, Material.RAW_FISH, Material.PORK, Material.POTATO_ITEM, Material.RABBIT, Material.MUTTON };

	private HeatControlType heatControlType;

	// HeatControl Cook variables
	private long cookTime;
	private long cookInterval;

	//HeatControl Extinguish variables
	private long extinguishCooldown;
	private double extinguishRadius;

	//HeatControl Melt variables
	private double meltRange;
	private double meltRadius;
	private Location meltLocation;
	private static final Map<Block, TempBlock> MELTED_BLOCKS = new HashMap<>();

	//HeatControl Solidify variables
	private int solidifyRadius;
	private long solidifyDelay;
	private long solidifyLastBlockTime;
	private long solidifyRevertTime;
	private double solidifyMaxRadius;
	private double solidifyRange;
	private boolean solidifyRevert;
	private boolean solidifying;
	private Location solidifyLocation;
	private Random randy;

	public HeatControl(Player player, HeatControlType heatControlType) {
		super(player);

		this.heatControlType = heatControlType;
		setFields();

		if (this.heatControlType == HeatControlType.COOK) {
			if (!isCookable(player.getInventory().getItemInMainHand().getType())) {
				remove();
				new HeatControl(player, HeatControlType.SOLIDIFY);
				return;
			}
			start();

		} else if (this.heatControlType == HeatControlType.EXTINGUISH) {
			if (bPlayer.isOnCooldown(getName() + "Extinguish")) {
				remove();
				return;
			}

			start();

		} else if (this.heatControlType == HeatControlType.MELT) {
			meltLocation = GeneralMethods.getTargetedLocation(player, meltRange);
			for (Block block : GeneralMethods.getBlocksAroundPoint(meltLocation, meltRadius)) {

				if (isMeltable(block)) {
					melt(player, block);
				}
			}

		} else if (this.heatControlType == HeatControlType.SOLIDIFY) {
			if (!bPlayer.canBend(this)) {
				return;
			} else if (getLavaBlock(player, solidifyRange) == null) {
				remove();
				new HeatControl(player, HeatControlType.EXTINGUISH);
				return;
			}

			solidifyLastBlockTime = System.currentTimeMillis();
			start();
		}

	}

	public void setFields() {
		if (this.heatControlType == HeatControlType.COOK) {
			this.cookTime = System.currentTimeMillis();
			this.cookInterval = getConfig().getLong("Abilities.Fire.HeatControl.Cook.Interval");
		} else if (this.heatControlType == HeatControlType.EXTINGUISH) {
			this.extinguishCooldown = getConfig().getLong("Abilities.Fire.HeatControl.Extinguish.Cooldown");
			this.extinguishRadius = getConfig().getLong("Abilities.Fire.HeatControl.Extinguish.Radius");
			this.extinguishRadius = getDayFactor(this.extinguishRadius);
		} else if (this.heatControlType == HeatControlType.MELT) {
			this.meltRange = getConfig().getDouble("Abilities.Fire.HeatControl.Melt.Range");
			this.meltRadius = getConfig().getDouble("Abilities.Fire.HeatControl.Melt.Radius");
			this.meltRange = getDayFactor(this.meltRange);
			this.meltRadius = getDayFactor(this.meltRadius);
		} else if (this.heatControlType == HeatControlType.SOLIDIFY) {
			this.solidifyRadius = 1;
			this.solidifyDelay = 50;
			this.solidifyLastBlockTime = 0;
			this.solidifyMaxRadius = getConfig().getDouble("Abilities.Fire.HeatControl.Solidify.MaxRadius");
			this.solidifyRange = getConfig().getDouble("Abilities.Fire.HeatControl.Solidify.Range");
			this.solidifyRevert = getConfig().getBoolean("Abilities.Fire.HeatControl.Solidify.Revert");
			this.solidifyRevertTime = getConfig().getLong("Abilities.Fire.HeatControl.Solidify.RevertTime");
			this.randy = new Random();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void progress() {

		if (!bPlayer.canBend(this)) {
			remove();
			return;
		}

		if (this.heatControlType == HeatControlType.COOK) {

			if (!player.isSneaking()) {
				remove();
				return;
			}

			if (!isCookable(player.getInventory().getItemInMainHand().getType())) {
				remove();
				return;
			}

			if (System.currentTimeMillis() - cookTime > cookInterval) {
				cook();
				cookTime = System.currentTimeMillis();
				return;
			}

			displayCookParticles();

		} else if (this.heatControlType == HeatControlType.EXTINGUISH) {

			if (!player.isSneaking()) {
				bPlayer.addCooldown(getName() + "Extinguish", extinguishCooldown);
				remove();
				return;
			}

			Set<Material> blocks = new HashSet<>();
			for (int material : GeneralMethods.NON_OPAQUE) {
				blocks.add(Material.getMaterial(material));
			}

			for (Block block : GeneralMethods.getBlocksAroundPoint(player.getLocation(), extinguishRadius)) {
				Material material = block.getType();
				if (material == Material.FIRE && !GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {

					block.setType(Material.AIR);
					block.getWorld().playEffect(block.getLocation(), Effect.EXTINGUISH, 0);
				}
			}

		} else if (this.heatControlType == HeatControlType.SOLIDIFY) {

			if (solidifyRadius >= solidifyMaxRadius) {
				remove();
				return;
			}

			if (!player.isSneaking()) {
				remove();
				return;
			}

			if (!solidifying) {
				solidifying = true;
			}

			Location targetLocation = GeneralMethods.getTargetedLocation(player, solidifyRange);
			//if (isLava(targetLocation.getBlock())) {
			//	remove();
			//	new HeatControl(player, HeatControlType.EXTINGUISH);
			//}
			resetLocation(targetLocation);
			List<Location> area = GeneralMethods.getCircle(solidifyLocation, solidifyRadius, 3, true, true, 0);
			solidify(area);
		}

	}

	private void cook() {
		ItemStack cooked = getCooked(player.getInventory().getItemInMainHand());
		HashMap<Integer, ItemStack> cantFit = player.getInventory().addItem(cooked);
		for (int id : cantFit.keySet()) {
			player.getWorld().dropItem(player.getEyeLocation(), cantFit.get(id));
		}

		int amount = player.getInventory().getItemInMainHand().getAmount();
		if (amount == 1) {
			player.getInventory().clear(player.getInventory().getHeldItemSlot());
		} else {
			player.getInventory().getItemInMainHand().setAmount(amount - 1);
		}
	}

	private ItemStack getCooked(ItemStack is) {
		ItemStack cooked = new ItemStack(Material.AIR);
		Material material = is.getType();

		switch (material) {
			case RAW_BEEF:
				cooked = new ItemStack(Material.COOKED_BEEF, 1);
				break;
			case RAW_FISH:
				ItemStack salmon = new ItemStack(Material.RAW_FISH, 1, (short) 1);
				if (is.getDurability() == salmon.getDurability()) {
					cooked = new ItemStack(Material.COOKED_FISH, 1, (short) 1);
				} else {
					cooked = new ItemStack(Material.COOKED_FISH, 1);
				}
				break;
			case RAW_CHICKEN:
				cooked = new ItemStack(Material.COOKED_CHICKEN, 1);
				break;
			case PORK:
				cooked = new ItemStack(Material.GRILLED_PORK, 1);
				break;
			case POTATO_ITEM:
				cooked = new ItemStack(Material.BAKED_POTATO, 1);
				break;
			case MUTTON:
				cooked = new ItemStack(Material.COOKED_MUTTON);
				break;
			case RABBIT:
				cooked = new ItemStack(Material.COOKED_RABBIT);
				break;
			default:
				break;
		}

		return cooked;
	}

	public void displayCookParticles() {
		ParticleEffect.FLAME.display(player.getLocation().clone().add(0, 1, 0), 0.5F, 0.5F, 0.5F, 0, 3);
		ParticleEffect.SMOKE.display(player.getLocation().clone().add(0, 1, 0), 0.5F, 0.5F, 0.5F, 0, 2);
	}

	public static boolean isCookable(Material material) {
		return Arrays.asList(COOKABLE_MATERIALS).contains(material);
	}

	public static boolean canBurn(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return true;
		} else if (bPlayer.getBoundAbilityName().equals("HeatControl") || hasAbility(player, FireJet.class)) {
			player.setFireTicks(-1);
			return false;
		} else if (player.getFireTicks() > 80 && bPlayer.canBendPassive(Element.FIRE)) {
			player.setFireTicks(80);
		}
		return true;
	}

	public static void melt(Player player, final Block block) {
		if (GeneralMethods.isRegionProtectedFromBuild(player, "HeatControl", block.getLocation())) {
			return;
		} else if (!SurgeWave.canThaw(block)) {
			SurgeWave.thaw(block);
			return;
		} else if (!Torrent.canThaw(block)) {
			Torrent.thaw(block);
			return;
		} else if (WaterArmsSpear.canThaw(block)) {
			WaterArmsSpear.thaw(block);
			return;
		}

		if (TempBlock.isTempBlock(block)) {
			TempBlock tb = TempBlock.get(block);
			if (PhaseChange.getFrozenBlocksAsTempBlock().contains(tb)) {
				PhaseChange.thaw(tb);
			}
		}

		WaterSpoutWave.thaw(block);
		IceWave.thaw(block);

		if (isMeltable(block) && !TempBlock.isTempBlock(block) && WaterManipulation.canPhysicsChange(block)) {
			if (block.getType() == Material.SNOW) {
				block.setType(Material.AIR);
				return;
			} else {
				TempBlock tb = new TempBlock(block, Material.WATER, (byte) 0);
				MELTED_BLOCKS.put(block, tb);

				new BukkitRunnable() {
					@Override
					public void run() {
						MELTED_BLOCKS.get(block).revertBlock();
						MELTED_BLOCKS.remove(block);
					}
				}.runTaskLater(ProjectKorra.plugin, 5 * 20 * 60);
			}
		}
	}

	public void solidify(List<Location> area) {
		if (System.currentTimeMillis() < solidifyLastBlockTime + solidifyDelay) {
			return;
		}

		List<Block> lava = new ArrayList<Block>();
		for (Location l : area) {
			if (isLava(l.getBlock())) {
				lava.add(l.getBlock());
			}
		}

		solidifyLastBlockTime = System.currentTimeMillis();
		if (lava.size() == 0) {
			solidifyRadius++;
			return;
		}

		Block b = lava.get(randy.nextInt(lava.size()));

		final TempBlock tempBlock;
		if (TempBlock.isTempBlock(b)) {
			tempBlock = TempBlock.get(b);
			tempBlock.setType(Material.MAGMA, (byte) 0);
		} else {
			tempBlock = new TempBlock(b, Material.MAGMA, (byte) 0);
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				if (tempBlock != null) {
					boolean bool = Math.random() > .5 ? true : false;
					if (solidifyRevert) {
						if (bool) {
							tempBlock.setType(Material.STONE, (byte) 0);
						} else {
							tempBlock.setType(Material.COBBLESTONE, (byte) 0);
						}
						tempBlock.setRevertTime(solidifyRevertTime);
					} else {
						tempBlock.revertBlock();
						if (bool) {
							tempBlock.getBlock().setType(Material.STONE);
						} else {
							tempBlock.getBlock().setType(Material.COBBLESTONE);
						}
					}

					ParticleEffect.SMOKE.display(tempBlock.getBlock().getLocation().clone().add(0.5, 1, 0.5), 0.1F, 0.1F, 0.1F, 0.01F, 3);
					if (randy.nextInt(3) == 0) {
						tempBlock.getBlock().getWorld().playSound(tempBlock.getBlock().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5F, 1);
					}
				}
			}
		}.runTaskLater(ProjectKorra.plugin, 20);
	}

	public void resetLocation(Location loc) {
		if (solidifyLocation == null) {
			solidifyLocation = loc;
			return;
		}

		if (!loc.equals(solidifyLocation)) {
			solidifyRadius = 1;
			solidifyLocation = loc;
		}
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		if (this.heatControlType != null) {
			return this.heatControlType.equals(HeatControlType.COOK);
		} else {
			return false;
		}
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public String getName() {
		return "HeatControl";
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@SuppressWarnings("deprecation")
	public static Block getLavaBlock(Player player, double range) {
		Location location = player.getEyeLocation();
		Vector vector = location.getDirection().clone().normalize();

		for (double i = 0; i <= range; i++) {
			Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if (GeneralMethods.isRegionProtectedFromBuild(player, location)) {
				continue;
			}
			if (isLava(block)) {
				if (TempBlock.isTempBlock(block)) {
					TempBlock tb = TempBlock.get(block);
					byte full = 0x0;
					if (tb.getState().getRawData() != full && !isLava(tb.getState().getType())) {
						continue;
					}
				}
				return block;
			}
		}
		return null;
	}
	
	public static Collection<TempBlock> getMeltedBlocks() {
		return MELTED_BLOCKS.values();
		
	}

}
