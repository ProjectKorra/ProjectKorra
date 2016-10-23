package com.projectkorra.projectkorra.firebending;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.PhaseChangeMelt;

public class HeatControl extends FireAbility {
	
	public enum HeatControlType {
		COOK, EXTINGUISH, MELT, SOLIDIFY
	}
	
	private static final Material[] COOKABLE_MATERIALS = { Material.RAW_BEEF, Material.RAW_CHICKEN, 
			Material.RAW_FISH, Material.PORK, Material.POTATO_ITEM, Material.RABBIT, Material.MUTTON };
	
	private HeatControlType heatControlType;
	
	/*
	 * HeatControl Cook variables
	 */
	private long cookTime;
	private long cookInterval;
	
	/*
	 * HeatControl Extinguish variables
	 */
	private long extinguishCooldown;
	private double extinguishRadius;
	
	/*
	 * HeatControl Melt variables
	 */
	private double meltRange;
	private double meltRadius;
	private Location meltLocation;
	
	/*
	 * HeatControl Solidify variables
	 */
	private int solidifyRadius;
	private long solidifyDelay;
	private long solidifyLastBlockTime;
	private double solidifyMaxRadius;
	private double solidifyRange;
	private boolean solidifying;
	private Location solidifyLocation;
	private Random randy;
	private ConcurrentHashMap<TempBlock, Long> solidify_stone = new ConcurrentHashMap<>();
	private ConcurrentHashMap<TempBlock, Long> solidify_revert = new ConcurrentHashMap<>();
	private List<TempBlock> BLOCKS = new ArrayList<>();
	

	public HeatControl(Player player, HeatControlType heatControlType) {
		super(player);
		
		this.heatControlType = heatControlType;
		setFields();
		
		if (heatControlType == HeatControlType.COOK) {
			start();
		}
		
		if (heatControlType == HeatControlType.EXTINGUISH) {
			if (bPlayer.isOnCooldown(getName() + "Extinguish")) {
				remove();
				return;
			}
			
			start();
		}
		
		if (heatControlType == HeatControlType.MELT) {
			meltLocation = GeneralMethods.getTargetedLocation(player, meltRange);
			for (Block block : GeneralMethods.getBlocksAroundPoint(meltLocation, meltRadius)) {
				
				if (isMeltable(block)) {
					PhaseChangeMelt.melt(player, block);
				}/* else if (isHeatable(block)) {
					heat(block);
				}*/
			}
		}
		
		if (heatControlType == HeatControlType.SOLIDIFY) {
			if (!bPlayer.canBend(this)) {
				return;
			} else if (EarthAbility.getLavaSourceBlock(player, solidifyRange) == null) {
				remove();
				new HeatControl(player, HeatControlType.COOK);
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
			this.heatControlType = HeatControlType.COOK;
		}
		
		else if (this.heatControlType == HeatControlType.EXTINGUISH) {
			this.extinguishCooldown = getConfig().getLong("Abilities.Fire.HeatControl.Extinguish.Cooldown");
			this.extinguishRadius = getConfig().getLong("Abilities.Fire.HeatControl.Extinguish.Radius");

			this.extinguishRadius = getDayFactor(this.extinguishRadius);
		}
		
		else if (this.heatControlType == HeatControlType.MELT) {
			this.meltRange = getConfig().getDouble("Abilities.Fire.HeatControl.Melt.Range");
			this.meltRadius = getConfig().getDouble("Abilities.Fire.HeatControl.Melt.Radius");

			this.meltRange = getDayFactor(this.meltRange);
			this.meltRadius = getDayFactor(this.meltRadius);
		}
		
		else if (this.heatControlType == HeatControlType.SOLIDIFY) {
			this.solidifyRadius = 1;
			this.solidifyDelay = 50;
			this.solidifyLastBlockTime = 0;
			this.solidifyMaxRadius = getConfig().getDouble("Abilities.Fire.HeatControl.Solidify.MaxRadius");
			this.solidifyRange = getConfig().getDouble("Abilities.Fire.HeatControl.Solidify.Range");
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
				new HeatControl(player, HeatControlType.EXTINGUISH);
				return;
			}
			
			if (System.currentTimeMillis() - cookTime > cookInterval) {
				cook();
				cookTime = System.currentTimeMillis();
				return;
			}

			displayCookParticles();
		}
		
		else if (this.heatControlType == HeatControlType.EXTINGUISH) {
			
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
		}
		
		else if (this.heatControlType == HeatControlType.SOLIDIFY) {
			
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
			resetLocation(targetLocation);
			List<Location> area = GeneralMethods.getCircle(solidifyLocation, solidifyRadius, 3, true, true, 0);
			solidify(area);
		}
		
	}
	
	/*
	 * 
	 * 		COOK METHODS
	 * 
	 */
	
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
	
	/*
	 * 
	 * 		EXTINGUISH METHODS
	 * 
	 */
	
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
	
	/*
	 * 
	 * 		MELT METHODS
	 * 
	 */
	
	@SuppressWarnings("deprecation")
	public static void heat(Block block) {
		if (block.getType() == Material.OBSIDIAN) {
			block.setType(Material.LAVA);
			block.setData((byte) 0x0);
		}
	}
	
	/*
	public static boolean isHeatable(Block block) {
		
		return false;
	}
	*/
	
	/*
	 * 
	 * 		SOLIDIFY METHODS
	 * 
	 */
	
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
		
		TempBlock tempBlock;
		if (TempBlock.isTempBlock(b)) {
			tempBlock = TempBlock.get(b);
			tempBlock.setType(Material.MAGMA, (byte) 0);
		} else {
			tempBlock = new TempBlock(b, Material.MAGMA, (byte) 0);
		}

		solidify_stone.put(tempBlock, System.currentTimeMillis());
		solidify_revert.put(tempBlock, System.currentTimeMillis() + 1000);
		BLOCKS.add(tempBlock);
	}
	
	@Override
	public void remove() {
		
		solidifying = false;
	}
	
	public void removeInstance() {
		
		super.remove();
	}
	
	public void revert(TempBlock block) {
		
		if (BLOCKS.contains(block)) {
			block.revertBlock();
			BLOCKS.remove(block);
		}
	}
	
	public void revertAll() {
		
		for (TempBlock tempBlock : BLOCKS) {
			tempBlock.revertBlock();
		}
		
		BLOCKS.clear();
	}
	
	public static void revertAllInstances() {
		
		for (HeatControl heatControl : getAbilities(HeatControl.class)) {
			heatControl.revertAll();
		}
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
	
	public static void manageSolidify() {

		for (HeatControl heatControl : getAbilities(HeatControl.class)) {
			
			for (TempBlock tempBlock : heatControl.solidify_stone.keySet()) {
				
				if (System.currentTimeMillis() - heatControl.solidify_stone.get(tempBlock) > 1000) {
					
					if (getConfig().getBoolean("Abilities.Fire.HeatControl.Solidify.Revert")) {
						
						tempBlock.setType(Material.STONE, (byte) 0);
						heatControl.solidify_revert.put(tempBlock, System.currentTimeMillis());
					} else {
						
						tempBlock.revertBlock();
						tempBlock.getBlock().setType(Material.STONE);
					}
					
					ParticleEffect.SMOKE.display(tempBlock.getBlock().getLocation().clone().add(0.5, 1, 0.5), 0.1F, 0.1F, 0.1F, 0.01F, 3);
					
					// TODO play the smoke in a line from the block to above the player's head.
					
					tempBlock.getBlock().getWorld().playSound(tempBlock.getBlock().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 1);
					heatControl.solidify_stone.remove(tempBlock);
				}
			}
			
			for (TempBlock tempBlock : heatControl.solidify_revert.keySet()) {
				
				if (System.currentTimeMillis() - heatControl.solidify_revert.get(tempBlock) > getConfig().getLong("Abilities.Fire.HeatControl.Solidify.RevertTime")) {
					
					heatControl.revert(tempBlock);
					heatControl.solidify_revert.remove(tempBlock);
				}
			}
			
			if (heatControl.solidify_stone.isEmpty() && heatControl.solidify_revert.isEmpty() && !heatControl.solidifying) {
				
				heatControl.removeInstance();
			}
		}
		
	}

	@Override
	public boolean isSneakAbility() {
		
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		
		return true;
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

}