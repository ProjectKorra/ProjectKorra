package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;

public class HeatControlCook extends FireAbility {
	
	private static final Material[] COOKABLE_MATERIALS = { Material.RAW_BEEF, Material.RAW_CHICKEN, 
			Material.RAW_FISH, Material.PORK, Material.POTATO_ITEM, Material.RABBIT, Material.MUTTON };
	
	private long time;
	private long cookTime;
	private ItemStack item;
		
	public HeatControlCook(Player player) {
		super(player);
		
		this.time = System.currentTimeMillis();
		this.cookTime = getConfig().getLong("Abilities.Fire.HeatControl.Cook.CookTime");
		this.item = player.getItemInHand();
		
		if (isCookable(item.getType())) {
			start();
		}
	}

	private void cook() {
		ItemStack cooked = getCooked(item);
		HashMap<Integer, ItemStack> cantFit = player.getInventory().addItem(cooked);
		for (int id : cantFit.keySet()) {
			player.getWorld().dropItem(player.getEyeLocation(), cantFit.get(id));
		}
		
		int amount = item.getAmount();
		if (amount == 1) {
			player.getInventory().clear(player.getInventory().getHeldItemSlot());
		} else {
			item.setAmount(amount - 1);
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

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		} else if (!item.equals(player.getItemInHand())) {
			time = System.currentTimeMillis();
			item = player.getItemInHand();
		}

		if (!isCookable(item.getType())) {
			remove();
			return;
		} else if (System.currentTimeMillis() > time + cookTime) {
			cook();
			time = System.currentTimeMillis();
		}
		
		ParticleEffect.FLAME.display(player.getEyeLocation(), 0.6F, 0.6F, 0.6F, 0, 3);
		ParticleEffect.SMOKE.display(player.getEyeLocation(), 0.6F, 0.6F, 0.6F, 0, 1);
	}

	public static boolean isCookable(Material material) {
		return Arrays.asList(COOKABLE_MATERIALS).contains(material);
	}

	@Override
	public String getName() {
		return "HeatControl";
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getCookTime() {
		return cookTime;
	}

	public void setCookTime(long cookTime) {
		this.cookTime = cookTime;
	}

	public ItemStack getItem() {
		return item;
	}

	public void setItem(ItemStack item) {
		this.item = item;
	}

	public static Material[] getCookableMaterials() {
		return COOKABLE_MATERIALS;
	}
		
}