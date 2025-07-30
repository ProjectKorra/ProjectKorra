package com.projectkorra.projectkorra.earthbending;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.projectkorra.projectkorra.region.RegionProtection;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempArmor;
import com.projectkorra.projectkorra.util.TempBlock;

public class EarthArmor extends EarthAbility {

	public static final Map<String, Integer> COLORS = new HashMap<>();

	private boolean formed;
	private Material headMaterial;
	private Material legsMaterial;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private long interval;
	@Attribute(Attribute.DURATION)
	private long maxDuration;
	@Attribute(Attribute.SELECT_RANGE)
	private double selectRange;
	private Block headBlock;
	private Block legsBlock;
	private Location headBlockLocation;
	private Location legsBlockLocation;
	private boolean active;
	private PotionEffect oldAbsorbtion = null;
	private double goldHearts;
	@Attribute("GoldHearts")
	private int maxGoldHearts;
	private TempArmor armor;

	public EarthArmor(final Player player) {
		super(player);
		if (hasAbility(player, EarthArmor.class) || !this.canBend()) {
			return;
		}

		this.formed = false;
		this.active = true;
		this.interval = 2000;
		this.goldHearts = 0;
		this.cooldown = getConfig().getLong("Abilities.Earth.EarthArmor.Cooldown");
		this.maxDuration = getConfig().getLong("Abilities.Earth.EarthArmor.MaxDuration");
		this.selectRange = getConfig().getDouble("Abilities.Earth.EarthArmor.SelectRange");
		this.maxGoldHearts = getConfig().getInt("Abilities.Earth.EarthArmor.GoldHearts");

		if (COLORS.isEmpty()) defineColors();

		this.headBlock = this.getTargetEarthBlock((int) this.selectRange);
		if (!GeneralMethods.isRegionProtectedFromBuild(this, this.headBlock.getLocation()) && this.getEarthbendableBlocksLength(this.headBlock, new Vector(0, -1, 0), 2) >= 2) {
			this.legsBlock = this.headBlock.getRelative(BlockFace.DOWN);
			this.headMaterial = this.headBlock.getType();
			this.legsMaterial = this.legsBlock.getType();
			this.headBlockLocation = this.headBlock.getLocation();
			this.legsBlockLocation = this.legsBlock.getLocation();

			final Block oldHeadBlock = this.headBlock;
			final Block oldLegsBlock = this.legsBlock;

			if (!this.moveBlocks()) {
				return;
			}
			if ((TempBlock.isTempBlock(oldHeadBlock) && !isBendableEarthTempBlock(oldHeadBlock))
					|| (TempBlock.isTempBlock(oldLegsBlock) && !isBendableEarthTempBlock(oldLegsBlock))) {
				return;
			}
			if (isEarthRevertOn()) {
				addTempAirBlock(oldHeadBlock);
				addTempAirBlock(oldLegsBlock);
			} else {
				GeneralMethods.removeBlock(oldHeadBlock);
				GeneralMethods.removeBlock(oldLegsBlock);
			}

			playEarthbendingSound(this.headBlock.getLocation());
			this.start();
		}
	}

	private void formArmor() {
		if (TempBlock.isTempBlock(this.headBlock)) {
			TempBlock.revertBlock(this.headBlock, Material.AIR);
		}
		if (TempBlock.isTempBlock(this.legsBlock)) {
			TempBlock.revertBlock(this.legsBlock, Material.AIR);
		}

		final ItemStack head = new ItemStack(Material.LEATHER_HELMET, 1);
		final ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		final ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		final ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);

		final LeatherArmorMeta metaHead = (LeatherArmorMeta) head.getItemMeta();
		final LeatherArmorMeta metaChest = (LeatherArmorMeta) chestplate.getItemMeta();
		final LeatherArmorMeta metaLegs = (LeatherArmorMeta) leggings.getItemMeta();
		final LeatherArmorMeta metaBottom = (LeatherArmorMeta) boots.getItemMeta();

		metaHead.setColor(Color.fromRGB(getColor(this.headMaterial)));
		metaChest.setColor(Color.fromRGB(getColor(this.headMaterial)));
		metaLegs.setColor(Color.fromRGB(getColor(this.legsMaterial)));
		metaBottom.setColor(Color.fromRGB(getColor(this.legsMaterial)));

		head.setItemMeta(metaHead);
		chestplate.setItemMeta(metaChest);
		leggings.setItemMeta(metaLegs);
		boots.setItemMeta(metaBottom);

		final ItemStack armors[] = { boots, leggings, chestplate, head };
		this.armor = new TempArmor(this.player, 72000000L, this, armors); // Duration of 2 hours.
		this.armor.setRemovesAbilityOnForceRevert(true);
		this.formed = true;

		for (final PotionEffect effect : this.player.getActivePotionEffects()) {
			if (effect.getType() == PotionEffectType.ABSORPTION) {
				this.oldAbsorbtion = effect;
				this.player.removePotionEffect(PotionEffectType.ABSORPTION);
				break;
			}
		}
		final int level = this.maxGoldHearts / 2 - 1 + (this.maxGoldHearts % 2);
		this.player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, level, true, false));

		this.goldHearts = this.maxGoldHearts * 2;
		this.player.setAbsorptionAmount(this.goldHearts);
	}

	private boolean inPosition() {
		return this.headBlock.equals(this.player.getEyeLocation().getBlock()) && this.legsBlock.equals(this.player.getLocation().getBlock());
	}

	private boolean moveBlocks() {
		if (!this.player.getWorld().equals(this.headBlock.getWorld())) {
			this.remove();
			return false;
		}

		final Location headLocation = this.player.getEyeLocation();
		final Location legsLocation = this.player.getLocation();
		Vector headDirection = headLocation.toVector().subtract(this.headBlockLocation.toVector()).normalize().multiply(.5);

		Block newHeadBlock = this.headBlock;
		Block newLegsBlock = this.legsBlock;

		final int yDiff = this.player.getEyeLocation().getBlockY() - this.headBlock.getY();

		if (yDiff != 0) {
			final Block checkBlock = yDiff > 0 ? this.headBlock.getRelative(BlockFace.UP) : this.legsBlock.getRelative(BlockFace.DOWN);

			if (this.isTransparent(checkBlock) && !checkBlock.isLiquid()) {
				GeneralMethods.breakBlock(checkBlock); // Destroy any minor blocks that are in the way.

				headDirection = new Vector(0, yDiff > 0 ? 0.5 : -0.5, 0);
			}
		}

		if (!headLocation.getBlock().equals(this.headBlock)) {
			this.headBlockLocation = this.headBlockLocation.clone().add(headDirection);
			newHeadBlock = this.headBlockLocation.getBlock();
		}
		if (!legsLocation.getBlock().equals(this.legsBlock)) {
			this.legsBlockLocation = this.headBlockLocation.clone().add(0, -1, 0);
			newLegsBlock = newHeadBlock.getRelative(BlockFace.DOWN);
		}

		if (this.isTransparent(newHeadBlock) && !newHeadBlock.isLiquid()) {
			GeneralMethods.breakBlock(newHeadBlock);
		} else if (!this.isEarthbendable(newHeadBlock) && !newHeadBlock.isLiquid() && !ElementalAbility.isAir(newHeadBlock.getType())) {
			ParticleEffect.BLOCK_CRACK.display(newHeadBlock.getLocation(), 8, 0.5, 0.5, 0.5, newHeadBlock.getBlockData());
			this.remove();
			return false;
		}

		if (this.isTransparent(newLegsBlock) && !newLegsBlock.isLiquid()) {
			GeneralMethods.breakBlock(newLegsBlock);
		} else if (!this.isEarthbendable(newLegsBlock) && !newLegsBlock.isLiquid() && !ElementalAbility.isAir(newLegsBlock.getType())) {
			newLegsBlock.getLocation().getWorld().playSound(newLegsBlock.getLocation(), Sound.BLOCK_GRASS_BREAK, 1, 1);
			ParticleEffect.BLOCK_CRACK.display(newHeadBlock.getLocation(), 8, 0.5, 0.5, 0.5, newLegsBlock.getBlockData());
			this.remove();
			return false;
		}

		if (this.headBlock.getLocation().distanceSquared(this.player.getEyeLocation()) > this.selectRange * this.selectRange) {
			this.remove();
			return false;
		}

		if (!newHeadBlock.equals(this.headBlock)) {
			new TempBlock(newHeadBlock, this.headMaterial);
			if (TempBlock.isTempBlock(this.headBlock)) {
				TempBlock.revertBlock(this.headBlock, Material.AIR);
			}
		}

		if (!newLegsBlock.equals(this.legsBlock)) {
			new TempBlock(newLegsBlock, this.legsMaterial);
			if (TempBlock.isTempBlock(this.legsBlock)) {
				TempBlock.revertBlock(this.legsBlock, Material.AIR);
			}
		}
		this.headBlock = newHeadBlock;
		this.legsBlock = newLegsBlock;
		return true;
	}

	@Override
	public void progress() {
		if (!this.canBend()) {
			this.remove();
			return;
		}

		if (System.currentTimeMillis() - this.getStartTime() > this.maxDuration) {
			this.player.getLocation().getWorld().playSound(this.player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);
			this.player.getLocation().getWorld().playSound(this.player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);
			this.player.getLocation().getWorld().playSound(this.player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);

			ParticleEffect.BLOCK_CRACK.display(this.player.getEyeLocation(), 8, 0.1, 0.1, 0.1, this.headMaterial.createBlockData());
			ParticleEffect.BLOCK_CRACK.display(this.player.getLocation(), 8, 0.1F, 0.1F, 0.1F, this.legsMaterial.createBlockData());

			this.bPlayer.addCooldown(this);
			this.remove();
			return;
		}

		if (this.formed) {
			if (!this.player.hasPotionEffect(PotionEffectType.ABSORPTION)) {
				this.player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 1, true, false));
				this.player.setAbsorptionAmount(this.goldHearts);
			}

			if (!this.active) {
				this.bPlayer.addCooldown(this);
				this.remove();
				return;
			}

			this.player.setFireTicks(0);
		} else {
			if (!this.moveBlocks()) {
				return;
			}
			if (this.inPosition()) {
				this.formArmor();
			}
		}
	}

	@Override
	public void remove() {
		super.remove();
		if (isEarthRevertOn()) {
			if (TempBlock.isTempBlock(this.headBlock)) {
				TempBlock.revertBlock(this.headBlock, Material.AIR);
			}
			if (TempBlock.isTempBlock(this.legsBlock)) {
				TempBlock.revertBlock(this.legsBlock, Material.AIR);
			}
		} else {
			this.headBlock.breakNaturally();
			this.legsBlock.breakNaturally();
		}

		if (TempArmor.getTempArmorList(this.player).contains(this.armor)) {
			this.armor.revert();
		}

		this.player.removePotionEffect(PotionEffectType.ABSORPTION);

		if (this.oldAbsorbtion != null) {
			this.player.addPotionEffect(this.oldAbsorbtion);
		}

	}

	public void updateAbsorbtion() {
		final EarthArmor abil = this;
		new BukkitRunnable() {
			@Override
			public void run() {
				abil.goldHearts = EarthArmor.this.player.getAbsorptionAmount();
				if (abil.formed && abil.goldHearts < 0.9F) {
					abil.bPlayer.addCooldown(abil);

					abil.player.getLocation().getWorld().playSound(abil.player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);
					abil.player.getLocation().getWorld().playSound(abil.player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);
					abil.player.getLocation().getWorld().playSound(abil.player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);

					ParticleEffect.BLOCK_CRACK.display(abil.player.getEyeLocation(), 8, 0.1, 0.1, 0.1, abil.headMaterial.createBlockData());
					ParticleEffect.BLOCK_CRACK.display(abil.player.getLocation(), 8, 0.1F, 0.1F, 0.1F, abil.legsMaterial.createBlockData());

					abil.remove();
				}
			}
		}.runTaskLater(ProjectKorra.plugin, 1L);

	}

	/**
	 * Creates colors for the armor. These hex colors were the averages of the textures.
	 * https://github.com/StrangeOne101/TextureToRGB was used to generate these them
	 */
	public static void defineColors() {
		COLORS.put("amethyst_block", 0x8561bf);
		COLORS.put("ancient_debris", 0x5f3f37);
		COLORS.put("andesite", 0x888888);
		COLORS.put("anvil", 0x444444);
		COLORS.put("basalt", 0x49484d);
		COLORS.put("bedrock", 0x555555);
		COLORS.put("blackstone", 0x2a2328);
		COLORS.put("black_concrete", 0x080a0f);
		COLORS.put("black_concrete_powder", 0x191a1f);
		COLORS.put("black_glazed_terracotta", 0x431e20);
		COLORS.put("black_terracotta", 0x251610);
		COLORS.put("blue_concrete", 0x2c2e8f);
		COLORS.put("blue_concrete_powder", 0x4649a6);
		COLORS.put("blue_glazed_terracotta", 0x2f408b);
		COLORS.put("blue_terracotta", 0x4a3b5b);
		COLORS.put("bricks", 0x966153);
		COLORS.put("brown_concrete", 0x603b1f);
		COLORS.put("brown_concrete_powder", 0x7d5435);
		COLORS.put("brown_glazed_terracotta", 0x776a55);
		COLORS.put("brown_terracotta", 0x4d3323);
		COLORS.put("calcite", 0xdfe0dc);
		COLORS.put("cauldron", 0x4a494a);
		COLORS.put("chain", 0x0b0c10);
		COLORS.put("chiseled_copper", 0xb86449);
		COLORS.put("chiseled_deepslate", 0x363636);
		COLORS.put("chiseled_nether_bricks", 0x2f171c);
		COLORS.put("chiseled_polished_blackstone", 0x353038);
		COLORS.put("chiseled_quartz_block", 0xe7e2da);
		COLORS.put("chiseled_red_sandstone", 0xb7601b);
		COLORS.put("chiseled_sandstone", 0xd8ca9b);
		COLORS.put("chiseled_stone_bricks", 0x777677);
		COLORS.put("chiseled_tuff_bricks", 0x62675f);
		COLORS.put("clay", 0xa0a6b3);
		COLORS.put("coal_block", 0x100f0f);
		COLORS.put("coal_ore", 0x696969);
		COLORS.put("coarse_dirt", 0x77553b);
		COLORS.put("cobbled_deepslate", 0x4d4d50);
		COLORS.put("cobblestone", 0x7f7f7f);
		COLORS.put("copper_block", 0xc06b4f);
		COLORS.put("copper_grate", 0x864b37);
		COLORS.put("copper_ore", 0x7c7d78);
		COLORS.put("cracked_deepslate_bricks", 0x404041);
		COLORS.put("cracked_deepslate_tiles", 0x343434);
		COLORS.put("cracked_nether_bricks", 0x281417);
		COLORS.put("cracked_polished_blackstone_bricks", 0x2c252b);
		COLORS.put("cracked_stone_bricks", 0x767576);
		COLORS.put("crimson_nylium", 0x6b1a1a);
		COLORS.put("crying_obsidian", 0x200a3c);
		COLORS.put("cut_copper", 0xbf6a50);
		COLORS.put("cut_red_sandstone", 0xbd651f);
		COLORS.put("cut_sandstone", 0xd9ce9f);
		COLORS.put("cyan_concrete", 0x157788);
		COLORS.put("cyan_concrete_powder", 0x24939d);
		COLORS.put("cyan_glazed_terracotta", 0x34767d);
		COLORS.put("cyan_terracotta", 0x565b5b);
		COLORS.put("dark_prismarine", 0x335b4b);
		COLORS.put("deepslate", 0x505052);
		COLORS.put("deepslate_bricks", 0x464647);
		COLORS.put("deepslate_coal_ore", 0x4a4a4c);
		COLORS.put("deepslate_copper_ore", 0x5c5d59);
		COLORS.put("deepslate_diamond_ore", 0x536a6a);
		COLORS.put("deepslate_emerald_ore", 0x4e6857);
		COLORS.put("deepslate_gold_ore", 0x73664e);
		COLORS.put("deepslate_iron_ore", 0x6a635e);
		COLORS.put("deepslate_lapis_ore", 0x4f5a73);
		COLORS.put("deepslate_redstone_ore", 0x68494a);
		COLORS.put("deepslate_tiles", 0x363637);
		COLORS.put("diamond_block", 0x62ede4);
		COLORS.put("diamond_ore", 0x798d8c);
		COLORS.put("diorite", 0xbcbcbc);
		COLORS.put("dirt", 0x866043);
		COLORS.put("dirt_path", 0x805e3e);
		COLORS.put("dragon_egg", 0x0c090f);
		COLORS.put("dripstone_block", 0x866b5c);
		COLORS.put("emerald_block", 0x2acb57);
		COLORS.put("emerald_ore", 0x6c8873);
		COLORS.put("end_stone", 0xdbde9e);
		COLORS.put("end_stone_bricks", 0xdae0a2);
		COLORS.put("exposed_chiseled_copper", 0x9a7764);
		COLORS.put("exposed_copper", 0xa17d67);
		COLORS.put("exposed_cut_copper", 0x9a7965);
		COLORS.put("exposed_copper_grate", 0x715849);
		COLORS.put("farmland", 0x8f6646);
		COLORS.put("gilded_blackstone", 0x372a26);
		COLORS.put("glowstone", 0xab8354);
		COLORS.put("gold_block", 0xf6d03d);
		COLORS.put("gold_ore", 0x91856a);
		COLORS.put("granite", 0x956755);
		COLORS.put("grass_block", 0x738A4E);
		COLORS.put("gravel", 0x837f7e);
		COLORS.put("gray_concrete", 0x36393d);
		COLORS.put("gray_concrete_powder", 0x4c5154);
		COLORS.put("gray_glazed_terracotta", 0x535a5d);
		COLORS.put("gray_terracotta", 0x392a23);
		COLORS.put("green_concrete", 0x495b24);
		COLORS.put("green_concrete_powder", 0x61772c);
		COLORS.put("green_glazed_terracotta", 0x758e43);
		COLORS.put("green_terracotta", 0x4c532a);
		COLORS.put("hopper", 0x424144);
		COLORS.put("iron_bars", 0x3e3f3d);
		COLORS.put("iron_block", 0xdcdcdc);
		COLORS.put("iron_ore", 0x88817a);
		COLORS.put("lapis_block", 0x1e438c);
		COLORS.put("lapis_ore", 0x6b758d);
		COLORS.put("lava", 0xcf5b13);
		COLORS.put("lightning_rod", 0x1e110c);
		COLORS.put("light_blue_concrete", 0x2389c6);
		COLORS.put("light_blue_concrete_powder", 0x4ab4d5);
		COLORS.put("light_blue_glazed_terracotta", 0x5ea4d0);
		COLORS.put("light_blue_terracotta", 0x716c89);
		COLORS.put("light_gray_concrete", 0x7d7d73);
		COLORS.put("light_gray_concrete_powder", 0x9a9a94);
		COLORS.put("light_gray_glazed_terracotta", 0x90a6a7);
		COLORS.put("light_gray_terracotta", 0x876a61);
		COLORS.put("lime_concrete", 0x5ea818);
		COLORS.put("lime_concrete_powder", 0x7dbd29);
		COLORS.put("lime_glazed_terracotta", 0xa2c537);
		COLORS.put("lime_terracotta", 0x677534);
		COLORS.put("magenta_concrete", 0xa9309f);
		COLORS.put("magenta_concrete_powder", 0xc053b8);
		COLORS.put("magenta_glazed_terracotta", 0xd064bf);
		COLORS.put("magenta_terracotta", 0x95586c);
		COLORS.put("magma", 0x8e3f1f);
		COLORS.put("mossy_cobblestone", 0x6e765e);
		COLORS.put("mossy_stone_bricks", 0x737969);
		COLORS.put("moss_block", 0x596d2d);
		COLORS.put("mud", 0x3c393c);
		COLORS.put("muddy_mangrove_roots", 0x443a30);
		COLORS.put("mud_bricks", 0x89674f);
		COLORS.put("mushroom_stem", 0xcbc4b9);
		COLORS.put("mycelium", 0x6f6265);
		COLORS.put("netherite_block", 0x423d3f);
		COLORS.put("netherrack", 0x612626);
		COLORS.put("nether_bricks", 0x2c151a);
		COLORS.put("nether_gold_ore", 0x73362a);
		COLORS.put("nether_quartz_ore", 0x75413e);
		COLORS.put("nether_wart_block", 0x720202);
		COLORS.put("obsidian", 0x0f0a18);
		COLORS.put("orange_concrete", 0xe06100);
		COLORS.put("orange_concrete_powder", 0xe3831f);
		COLORS.put("orange_glazed_terracotta", 0x9a935b);
		COLORS.put("orange_terracotta", 0xa15325);
		COLORS.put("oxidized_copper", 0x52a284);
		COLORS.put("oxidized_copper_grate", 0x39705b);
		COLORS.put("oxidized_chiseled_copper", 0x53a184);
		COLORS.put("oxidized_cut_copper", 0x4f997e);
		COLORS.put("packed_mud", 0x8e6a4f);
		COLORS.put("pink_concrete", 0xd5658e);
		COLORS.put("pink_concrete_powder", 0xe499b5);
		COLORS.put("pink_glazed_terracotta", 0xeb9ab5);
		COLORS.put("pink_terracotta", 0xa14e4e);
		COLORS.put("podzol", 0x7a5739);
		COLORS.put("pointed_dripstone", 0x745c50);
		COLORS.put("polished_andesite", 0x848685);
		COLORS.put("polished_basalt", 0x58585b);
		COLORS.put("polished_blackstone", 0x353038);
		COLORS.put("polished_blackstone_bricks", 0x302a31);
		COLORS.put("polished_deepslate", 0x484849);
		COLORS.put("polished_diorite", 0xc0c1c2);
		COLORS.put("polished_granite", 0x9a6a59);
		COLORS.put("polished_tuff", 0x616863);
		COLORS.put("prismarine", 0x639c97);
		COLORS.put("prismarine_bricks", 0x63ab9e);
		COLORS.put("purple_concrete", 0x641f9c);
		COLORS.put("purple_concrete_powder", 0x8337b1);
		COLORS.put("purple_glazed_terracotta", 0x6d3098);
		COLORS.put("purple_terracotta", 0x764656);
		COLORS.put("purpur_block", 0xa97da9);
		COLORS.put("purpur_pillar", 0xab81ab);
		COLORS.put("quartz_block", 0xebe5de);
		COLORS.put("quartz_bricks", 0xeae5dd);
		COLORS.put("quartz_pillar", 0xebe6e0);
		COLORS.put("raw_copper_block", 0x9a694f);
		COLORS.put("raw_gold_block", 0xdda92e);
		COLORS.put("raw_iron_block", 0xa6876b);
		COLORS.put("redstone_block", 0xaf1805);
		COLORS.put("redstone_ore", 0x8c6d6d);
		COLORS.put("red_concrete", 0x8e2020);
		COLORS.put("red_concrete_powder", 0xa83632);
		COLORS.put("red_glazed_terracotta", 0xb53b35);
		COLORS.put("red_mushroom_block", 0xc82e2d);
		COLORS.put("red_nether_bricks", 0x450709);
		COLORS.put("red_sand", 0xbe6621);
		COLORS.put("red_sandstone", 0xba631d);
		COLORS.put("red_terracotta", 0x8f3d2e);
		COLORS.put("reinforced_deepslate", 0x666d64);
		COLORS.put("rooted_dirt", 0x90674c);
		COLORS.put("sand", 0xdbcfa3);
		COLORS.put("sandstone", 0xd8cb9b);
		COLORS.put("sculk", 0x0c1d24);
		COLORS.put("sculk_catalyst", 0x0f1f26);
		COLORS.put("sculk_sensor", 0x074654);
		COLORS.put("sculk_shrieker", 0x3f534e);
		COLORS.put("sculk_vein", 0x052128);
		COLORS.put("sea_lantern", 0xacc7be);
		COLORS.put("shroomlight", 0xf09246);
		COLORS.put("smooth_basalt", 0x48484e);
		COLORS.put("smooth_stone", 0x9e9e9e);
		COLORS.put("soul_sand", 0x513e32);
		COLORS.put("soul_soil", 0x4b392e);
		COLORS.put("stone", 0x7d7d7d);
		COLORS.put("stone_bricks", 0x7a797a);
		COLORS.put("terracotta", 0x985e43);
		COLORS.put("tuff", 0x6c6d66);
		COLORS.put("tuff_bricks", 0x62665f);
		COLORS.put("warped_nylium", 0x2b7265);
		COLORS.put("warped_wart_block", 0x167779);
		COLORS.put("weathered_chiseled_copper", 0x68966f);
		COLORS.put("weathered_copper", 0x6c996e);
		COLORS.put("weathered_cut_copper", 0x6d916b);
		COLORS.put("weathered_copper_grate", 0x4a6a4d);
		COLORS.put("white_concrete", 0xcfd5d6);
		COLORS.put("white_concrete_powder", 0xe1e3e3);
		COLORS.put("white_glazed_terracotta", 0xbcd4ca);
		COLORS.put("white_terracotta", 0xd1b2a1);
		COLORS.put("yellow_concrete", 0xf0af15);
		COLORS.put("yellow_concrete_powder", 0xe8c736);
		COLORS.put("yellow_glazed_terracotta", 0xeac058);
		COLORS.put("yellow_terracotta", 0xba8523);
	}

	/**
	 * Gets the color of the provided material
	 * @param material The material
	 * @return The color in RGB/int
	 */
	public static int getColor(Material material) {
		String mat = material.name().toLowerCase();

		if (mat.startsWith("waxed_")) { //For copper
			mat = mat.substring(6);
		} else if (mat.startsWith("smooth_") && !mat.equals("smooth_stone") && !mat.equals("smooth_basalt")) { //For smoothed block variants
			mat = mat.substring(7);
		}
		if (mat.endsWith("_slab") || mat.endsWith("_wall")) {
			mat = mat.substring(0, mat.length() - 5);
		} else if (mat.endsWith("_stairs")) {
			mat = mat.substring(0, mat.length() - 7);
		} else if (mat.endsWith("_trap_door")) {
			mat = mat.substring(0, mat.length() - 10) + "_block";
		} else if (mat.endsWith("_door")) {
			mat = mat.substring(0, mat.length() - 5) + "_block";
		} else if (mat.equals("chipped_anvil") || mat.equals("damaged_anvil")) {
			mat = "anvil";
		} else if (mat.equals("grass_path")) {
			mat = "dirt_path";
		}

		if (COLORS.containsKey(mat)) {
			return COLORS.get(mat);
		}

		return 0x7d7d7d; //Stone
	}

	public void click() {
		if (!this.player.isSneaking()) {
			return;
		}

		this.player.getLocation().getWorld().playSound(this.player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);
		this.player.getLocation().getWorld().playSound(this.player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);
		this.player.getLocation().getWorld().playSound(this.player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);

		ParticleEffect.BLOCK_CRACK.display(this.player.getEyeLocation(), 8, 0.1, 0.1, 0.1, this.headMaterial.createBlockData());
		ParticleEffect.BLOCK_CRACK.display(this.player.getLocation(), 8, 0.1F, 0.1F, 0.1F, this.legsMaterial.createBlockData());

		this.bPlayer.addCooldown(this);
		this.remove();
	}

	private boolean canBend() {

		final List<String> disabledWorlds = getConfig().getStringList("Properties.DisabledWorlds");
		final Location playerLoc = this.player.getLocation();

		if (!this.player.isOnline() || this.player.isDead()) {
			return false;

		} else if (this.bPlayer.isOnCooldown("EarthArmor")) {
			return false;
		} else if (!this.bPlayer.canBind(this)) {
			return false;
		} else if (this.getPlayer() != null && this.getLocation() != null && !this.getLocation().getWorld().equals(this.player.getWorld())) {
			return false;
		} else if (disabledWorlds != null && disabledWorlds.contains(this.player.getWorld().getName())) {
			return false;
		} else if (Commands.isToggledForAll || !this.bPlayer.isToggled() || !this.bPlayer.isElementToggled(this.getElement())) {
			return false;
		} else if (this.player.getGameMode() == GameMode.SPECTATOR) {
			return false;
		}

		if (RegionProtection.isRegionProtected(this.player, playerLoc, this.getName())) {
			return false;
		}

		return true;
	}

	@Override
	public String getName() {
		return "EarthArmor";
	}

	@Override
	public Location getLocation() {
		return this.headBlockLocation;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return this.player != null;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public boolean isFormed() {
		return this.formed;
	}

	public void setFormed(final boolean formed) {
		this.formed = formed;
	}

	public Material getHeadMaterial() {
		return this.headMaterial;
	}

	public void setHeadMaterial(final Material material) {
		this.headMaterial = material;
	}

	public Material getLegsMaterial() {
		return this.legsMaterial;
	}

	public void setLegsMaterial(final Material material) {
		this.legsMaterial = material;
	}

	public double getSelectRange() {
		return this.selectRange;
	}

	public void setSelectRange(final double selectRange) {
		this.selectRange = selectRange;
	}

	public long getInterval() {
		return this.interval;
	}

	public void setInterval(final long interval) {
		this.interval = interval;
	}

	@Override
	public Block getSourceBlock() { return getHeadBlock(); }

	public Block getHeadBlock() {
		return this.headBlock;
	}

	public void setHeadBlock(final Block headBlock) {
		this.headBlock = headBlock;
	}

	public Block getLegsBlock() {
		return this.legsBlock;
	}

	public void setLegsBlock(final Block legsBlock) {
		this.legsBlock = legsBlock;
	}

	public Location getHeadBlockLocation() {
		return this.headBlockLocation;
	}

	public void setHeadBlockLocation(final Location headBlockLocation) {
		this.headBlockLocation = headBlockLocation;
	}

	public Location getLegsBlockLocation() {
		return this.legsBlockLocation;
	}

	public void setLegsBlockLocation(final Location legsBlockLocation) {
		this.legsBlockLocation = legsBlockLocation;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	public double getGoldHearts() {
		return this.goldHearts;
	}

	public int getMaxGoldHearts() {
		return this.maxGoldHearts;
	}

	public void setGoldHearts(final double goldHearts) {
		this.goldHearts = goldHearts;
	}

	public void setMaxGoldHearts(final int maxGoldHearts) {
		this.maxGoldHearts = maxGoldHearts;
	}

}
