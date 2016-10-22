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
	
	public enum Function {
		COOK, EXTINGUISH, MELT, SOLIDIFY
	}
	
	private static final Material[] COOKABLE_MATERIALS = { Material.RAW_BEEF, Material.RAW_CHICKEN, 
			Material.RAW_FISH, Material.PORK, Material.POTATO_ITEM, Material.RABBIT, Material.MUTTON };
	
	public static ConcurrentHashMap<TempBlock, Long> SOLIDIFY_STONE = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<TempBlock, Long> SOLIDIFY_REVERT = new ConcurrentHashMap<>();
	public static List<TempBlock> BLOCKS = new ArrayList<>();
	
	private Function function;
	
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
	private long solidifyLastParticleTime;
	private double solidifyMaxRadius;
	private double solidifyRange;
	private Location solidifyLocation;
	private Random randy;
	

	public HeatControl(Player player, Function function) {
		super(player);
		
		this.function = function;
		setFields(function);
		
		if (function == Function.COOK) {
			
			start();
		}
		
		if (function == Function.EXTINGUISH) {
			
			if (bPlayer.isOnCooldown(getName() + "Extinguish")) {
				remove();
				return;
			}
			
			start();
		}
		
		if (function == Function.MELT) {
			
			meltLocation = GeneralMethods.getTargetedLocation(player, meltRange);
			for (Block block : GeneralMethods.getBlocksAroundPoint(meltLocation, meltRadius)) {
				
				if (isMeltable(block)) {
					PhaseChangeMelt.melt(player, block);
				}/* else if (isHeatable(block)) {
					heat(block);
				}*/
			}
		}
		
		if (function == Function.SOLIDIFY) {
			
			if (!bPlayer.canBend(this)) {
				return;
			} else if (EarthAbility.getLavaSourceBlock(player, solidifyRange) == null) {
				remove();
				new HeatControl(player, Function.COOK);
				return;
			}
			
			solidifyLastBlockTime = System.currentTimeMillis();
			start();
		}
		
	}
	
	public void setFields(Function function) {
		
		if (function == Function.COOK) {
			
			this.cookTime = System.currentTimeMillis();
			this.cookInterval = getConfig().getLong("Abilities.Fire.HeatControl.Cook.Interval");
			this.function = Function.COOK;
		}
		
		if (function == Function.EXTINGUISH) {

			this.extinguishCooldown = getConfig().getLong("Abilities.Fire.HeatControl.Extinguish.Cooldown");
			this.extinguishRadius = getConfig().getLong("Abilities.Fire.HeatControl.Extinguish.Radius");

			this.extinguishRadius = getDayFactor(this.extinguishRadius);
		}
		
		if (function == Function.MELT) {

			this.meltRange = getConfig().getDouble("Abilities.Fire.HeatControl.Melt.Range");
			this.meltRadius = getConfig().getDouble("Abilities.Fire.HeatControl.Melt.Radius");

			this.meltRange = getDayFactor(this.meltRange);
			this.meltRadius = getDayFactor(this.meltRadius);
		}
		
		if (function == Function.SOLIDIFY) {
			
			this.solidifyRadius = 1;
			this.solidifyDelay = 50;
			this.solidifyLastBlockTime = 0;
			this.solidifyLastParticleTime = 0;
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
		
		if (this.function == Function.COOK) {
			
			if (!player.isSneaking()) {
				remove();
				return;
			}
			
			if (!isCookable(player.getInventory().getItemInMainHand().getType())) {
				remove();
				new HeatControl(player, Function.EXTINGUISH);
				return;
			}
			
			if (System.currentTimeMillis() - cookTime > cookInterval) {
				cook();
				cookTime = System.currentTimeMillis();
				return;
			}

			displayCookParticles();
		}
		
		if (this.function == Function.EXTINGUISH) {
			
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
		
		if (this.function == Function.SOLIDIFY) {
			
			if (solidifyRadius >= solidifyMaxRadius) {
				remove();
				return;
			}
			
			if (!player.isSneaking()) {
				remove();
				return;
			}
			
			Location targetLocation = GeneralMethods.getTargetedLocation(player, solidifyRange);
			resetLocation(targetLocation);
			List<Location> area = GeneralMethods.getCircle(solidifyLocation, solidifyRadius, 3, true, true, 0);
			particles(area);
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
	
	@SuppressWarnings("deprecation")
	public void solidify(List<Location> area) {
		if (System.currentTimeMillis() < solidifyLastBlockTime + solidifyDelay) {
			return;
		}

		List<Block> lava = new ArrayList<Block>();
		for (Location l : area) {
			if (isLava(l.getBlock()) && l.getBlock().getData() == (byte) 0) {
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
		
		SOLIDIFY_STONE.put(tempBlock, System.currentTimeMillis());
		BLOCKS.add(tempBlock);
	}
	
	public static void revert(TempBlock block) {
		
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
	
	public void particles(List<Location> area) {
		if (System.currentTimeMillis() < solidifyLastParticleTime + 300) {
			return;
		}

		solidifyLastParticleTime = System.currentTimeMillis();
		for (Location l : area) {
			if (isLava(l.getBlock())) {
				ParticleEffect.SMOKE.display(l, 0, 0, 0, 0.1f, 2);
			}
		}
	}
	
	public static void manageSolidify() {
		
		for (TempBlock tempBlock : SOLIDIFY_STONE.keySet()) {
			
			if (System.currentTimeMillis() - SOLIDIFY_STONE.get(tempBlock) > 1000) {
				
				if (getConfig().getBoolean("Abilities.Fire.HeatControl.Solidify.Revert")) {
					
					tempBlock.setType(Material.STONE, (byte) 0);
					SOLIDIFY_REVERT.put(tempBlock, System.currentTimeMillis());
				} else {
					
					tempBlock.revertBlock();
					tempBlock.getBlock().setType(Material.STONE);
				}
				
				ParticleEffect.SMOKE.display(tempBlock.getBlock().getLocation().clone().add(0.5, 1, 0.5), 0.1F, 0.1F, 0.1F, 0.01F, 3);
				tempBlock.getBlock().getWorld().playSound(tempBlock.getBlock().getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1, 1);
				SOLIDIFY_STONE.remove(tempBlock);
			}
		}
			
		for (TempBlock tempBlock : SOLIDIFY_REVERT.keySet()) {
			
			if (System.currentTimeMillis() - SOLIDIFY_REVERT.get(tempBlock) > getConfig().getLong("Abilities.Fire.HeatControl.Solidify.RevertTime")) {
				
				revert(tempBlock);
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
