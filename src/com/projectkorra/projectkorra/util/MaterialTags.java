package com.projectkorra.projectkorra.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;

/**
 * Represents a collection tags to identify materials that share common properties.
 * Will map to minecraft for missing tags, as well as custom ones that may be useful.
 */
public class MaterialTags {

	private static NamespacedKey keyFor(String key) {
		//noinspection deprecation
		return new NamespacedKey("projectkorra", key + "_settag");
	}
	public static final MaterialSetTag ARROWS = new MaterialSetTag(keyFor("arrows"))
			.endsWith("ARROW")
			.ensureSize("ARROWS", 3);

	/**
	 * Cover all 16 colors of beds.
	 */
	public static final MaterialSetTag BEDS = new MaterialSetTag(keyFor("beds"))
			.endsWith("_BED")
			.ensureSize("BEDS", 16);

	/**
	 * Covers all bucket items.
	 */
	public static final MaterialSetTag BUCKETS = new MaterialSetTag(keyFor("buckets"))
			.endsWith("BUCKET")
			.ensureSize("BUCKETS", 8);

	/**
	 * Covers coal and charcoal.
	 */
	public static final MaterialSetTag COALS = new MaterialSetTag(keyFor("coals"))
			.add(Material.COAL, Material.CHARCOAL);

	/**
	 * Covers both cobblestone wall variants.
	 */
	public static final MaterialSetTag COBBLESTONE_WALLS = new MaterialSetTag(keyFor("cobblestone_walls"))
			.endsWith("COBBLESTONE_WALL")
			.ensureSize("COBBLESTONE_WALLS", 2);

	/**
	 * Covers both cobblestone and mossy Cobblestone.
	 */
	public static final MaterialSetTag COBBLESTONES = new MaterialSetTag(keyFor("cobblestones"))
			.add(Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);

	/**
	 * Covers all 16 colors of concrete.
	 */
	public static final MaterialSetTag CONCRETES = new MaterialSetTag(keyFor("concretes"))
			.endsWith("_CONCRETE")
			.ensureSize("CONCRETES", 16);

	/**
	 * Covers all 16 colors of concrete powder.
	 */
	public static final MaterialSetTag CONCRETE_POWDER = new MaterialSetTag(keyFor("concrete_powder"))
			.endsWith("_CONCRETE_POWDER")
			.ensureSize("CONCRETE_POWDER", 16);

	/**
	 * Covers the two types of cooked fish.
	 */
	public static final MaterialSetTag COOKED_FISH = new MaterialSetTag(keyFor("cooked_fish"))
			.add(Material.COOKED_COD, Material.COOKED_SALMON);

	/**
	 * Covers all 16 dyes.
	 */
	public static final MaterialSetTag DYES = new MaterialSetTag(keyFor("dyes"))
			.endsWith("_DYE")
			.add(Material.BONE_MEAL,
					Material.CACTUS_GREEN,
					Material.COCOA_BEANS,
					Material.DANDELION_YELLOW,
					Material.INK_SAC,
					Material.LAPIS_LAZULI,
					Material.ROSE_RED
			)
			.ensureSize("DYES", 16);

	/**
	 * Covers all 6 wood variants of gates.
	 */
	public static final MaterialSetTag FENCE_GATES = new MaterialSetTag(keyFor("fence_gates"))
			.endsWith("_GATE")
			.ensureSize("FENCE_GATES", 6);

	/**
	 * Covers all 6 wood variants and nether brick fence.
	 */
	public static final MaterialSetTag FENCES = new MaterialSetTag(keyFor("fences"))
			.endsWith("_FENCE")
			.ensureSize("FENCES", 7);

	/**
	 * Covers all 4 variants of fish buckets.
	 */
	public static final MaterialSetTag FISH_BUCKETS = new MaterialSetTag(keyFor("fish_buckets"))
			.add(Material.COD_BUCKET, Material.PUFFERFISH_BUCKET, Material.SALMON_BUCKET, Material.TROPICAL_FISH_BUCKET);

	/**
	 * Covers the non-colored glass and 16 stained glass (not panes).
	 */
	public static final MaterialSetTag GLASS = new MaterialSetTag(keyFor("glass"))
			.endsWith("_GLASS")
			.add(Material.GLASS)
			.ensureSize("GLASS", 17);

	/**
	 * Covers the non-colored glass panes and 16 stained glass panes (panes only).
	 */
	public static final MaterialSetTag GLASS_PANES = new MaterialSetTag(keyFor("glass_panes"))
			.endsWith("GLASS_PANE")
			.ensureSize("GLASS_PANES", 17);

	/**
	 * Covers all 16 glazed terracotta blocks.
	 */
	public static final MaterialSetTag GLAZED_TERRACOTTA = new MaterialSetTag(keyFor("glazed_terracotta"))
			.endsWith("GLAZED_TERRACOTTA")
			.ensureSize("GLAZED_TERRACOTTA", 16);

	/**
	 * Covers the 16 colors of stained terracotta.
	 */
	public static final MaterialSetTag STAINED_TERRACOTTA = new MaterialSetTag(keyFor("stained_terracotta"))
			.endsWith("TERRACOTTA")
			.not(Material.TERRACOTTA)
			.notEndsWith("GLAZED_TERRACOTTA")
			.ensureSize("STAINED_TERRACOTTA", 16);

	/**
	 * Covers terracotta along with the 16 stained variants.
	 */
	public static final MaterialSetTag TERRACOTTA = new MaterialSetTag(keyFor("terracotta"))
			.endsWith("TERRACOTTA")
			.ensureSize("TERRACOTTA", 33);


	/**
	 * Covers both golden apples.
	 */
	public static final MaterialSetTag GOLDEN_APPLES = new MaterialSetTag(keyFor("golden_apples"))
			.endsWith("GOLDEN_APPLE")
			.ensureSize("GOLDEN_APPLES", 2);

	/**
	 * Covers the 3 variants of horse armor.
	 */
	public static final MaterialSetTag HORSE_ARMORS = new MaterialSetTag(keyFor("horse_armors"))
			.endsWith("_HORSE_ARMOR")
			.ensureSize("HORSE_ARMORS", 3);

	/**
	 * Covers the 6 variants of infested blocks.
	 */
	public static final MaterialSetTag INFESTED_BLOCKS = new MaterialSetTag(keyFor("infested_blocks"))
			.startsWith("INFESTED_")
			.ensureSize("INFESTED_BLOCKS", 6);

	/**
	 * Covers the 3 variants of mushroom blocks.
	 */
	public static final MaterialSetTag MUSHROOM_BLOCKS = new MaterialSetTag(keyFor("mushroom_blocks"))
			.endsWith("MUSHROOM_BLOCK")
			.add(Material.MUSHROOM_STEM)
			.ensureSize("MUSHROOM_BLOCKS", 3);

	/**
	 * Covers both mushrooms.
	 */
	public static final MaterialSetTag MUSHROOMS = new MaterialSetTag(keyFor("mushrooms"))
			.add(Material.BROWN_MUSHROOM, Material.RED_MUSHROOM);

	/**
	 * Covers all 12 music disc items.
	 */
	public static final MaterialSetTag MUSIC_DISCS = new MaterialSetTag(keyFor("music_discs"))
			.startsWith("MUSIC_DISC_")
			.ensureSize("MUSIC_DISCS", 12);

	/**
	 * Covers all 8 ores.
	 */
	public static final MaterialSetTag ORES = new MaterialSetTag(keyFor("ores"))
			.endsWith("_ORE")
			.ensureSize("ORES", 8);

	/**
	 * Covers all piston typed items and blocks including the piston head and moving piston.
	 */
	public static final MaterialSetTag PISTONS = new MaterialSetTag(keyFor("pistons"))
			.contains("PISTON")
			.ensureSize("PISTONS", 4);

	/**
	 * Covers all potato items.
	 */
	public static final MaterialSetTag POTATOES = new MaterialSetTag(keyFor("potatoes"))
			.endsWith("POTATO")
			.ensureSize("POTATOES", 3);

	/**
	 * Covers all 6 wooden pressure plates and the 2 weighted pressure plates and 1 stone pressure plate.
	 */
	public static final MaterialSetTag PRESSURE_PLATES = new MaterialSetTag(keyFor("pressure_plates"))
			.endsWith("_PRESSURE_PLATE")
			.ensureSize("PRESSURE_PLATES", 9);

	/**
	 * Covers the 3 variants of prismarine blocks.
	 */
	public static final MaterialSetTag PRISMARINE = new MaterialSetTag(keyFor("prismarine"))
			.add(Material.PRISMARINE, Material.PRISMARINE_BRICKS, Material.DARK_PRISMARINE);

	/**
	 * Covers the 3 variants of prismarine slabs.
	 */
	public static final MaterialSetTag PRISMARINE_SLABS = new MaterialSetTag(keyFor("prismarine_slabs"))
			.add(Material.PRISMARINE_SLAB, Material.PRISMARINE_BRICK_SLAB, Material.DARK_PRISMARINE_SLAB);

	/**
	 * Covers the 3 variants of prismarine stairs.
	 */
	public static final MaterialSetTag PRISMARINE_STAIRS = new MaterialSetTag(keyFor("prismarine_stairs"))
			.add(Material.PRISMARINE_STAIRS, Material.PRISMARINE_BRICK_STAIRS, Material.DARK_PRISMARINE_STAIRS);

	/**
	 * Covers the 3 variants of pumpkins.
	 */
	public static final MaterialSetTag PUMPKINS = new MaterialSetTag(keyFor("pumpkins"))
			.add(Material.CARVED_PUMPKIN, Material.JACK_O_LANTERN, Material.PUMPKIN);

	/**
	 * Covers the 4 variants of quartz blocks.
	 */
	public static final MaterialSetTag QUARTZ_BLOCKS = new MaterialSetTag(keyFor("quartz_blocks"))
			.add(Material.QUARTZ_BLOCK, Material.QUARTZ_PILLAR, Material.CHISELED_QUARTZ_BLOCK, Material.SMOOTH_QUARTZ);

	/**
	 * Covers all uncooked fish items.
	 */
	public static final MaterialSetTag RAW_FISH = new MaterialSetTag(keyFor("raw_fish"))
			.add(Material.COD, Material.PUFFERFISH, Material.SALMON, Material.TROPICAL_FISH);

	/**
	 * Covers the 4 variants of red sandstone blocks.
	 */
	public static final MaterialSetTag RED_SANDSTONES = new MaterialSetTag(keyFor("red_sandstones"))
			.endsWith("RED_SANDSTONE")
			.ensureSize("RED_SANDSTONES", 4);

	/**
	 * Covers the 4 variants of sandstone blocks.
	 */
	public static final MaterialSetTag SANDSTONES = new MaterialSetTag(keyFor("sandstones"))
			.add(Material.SANDSTONE, Material.CHISELED_SANDSTONE, Material.CUT_SANDSTONE, Material.SMOOTH_SANDSTONE);

	/**
	 * Covers sponge and wet sponge.
	 */
	public static final MaterialSetTag SPONGES = new MaterialSetTag(keyFor("sponges"))
			.endsWith("SPONGE")
			.ensureSize("SPONGES", 2);

	/**
	 * Covers the non-colored and 16 colored shulker boxes.
	 */
	public static final MaterialSetTag SHULKER_BOXES = new MaterialSetTag(keyFor("shulker_boxes"))
			.endsWith("SHULKER_BOX")
			.ensureSize("SHULKER_BOXES", 17);

	/**
	 * Covers zombie, creeper, skeleton, dragon, and player heads.
	 */
	public static final MaterialSetTag SKULLS = new MaterialSetTag(keyFor("skulls"))
			.endsWith("_HEAD")
			.endsWith("_SKULL")
			.not(Material.PISTON_HEAD)
			.ensureSize("SKULLS", 12);

	/**
	 * Covers all spawn egg items.
	 */
	public static final MaterialSetTag SPAWN_EGGS = new MaterialSetTag(keyFor("spawn_eggs"))
			.endsWith("_SPAWN_EGG")
			.ensureSize("SPAWN_EGGS", 51);

	/**
	 * Covers all 16 colors of stained glass.
	 */
	public static final MaterialSetTag STAINED_GLASS = new MaterialSetTag(keyFor("stained_glass"))
			.endsWith("_STAINED_GLASS")
			.ensureSize("STAINED_GLASS", 16);

	/**
	 * Covers all 16 colors of stained glass panes.
	 */
	public static final MaterialSetTag STAINED_GLASS_PANES = new MaterialSetTag(keyFor("stained_glass_panes"))
			.endsWith("STAINED_GLASS_PANE")
			.ensureSize("STAINED_GLASS_PANES", 16);

	/**
	 * Covers all 7 variants of trapdoors.
	 */
	public static final MaterialSetTag TRAPDOORS = new MaterialSetTag(keyFor("trapdoors"))
			.endsWith("_TRAPDOOR")
			.ensureSize("TRAPDOORS", 7);

	/**
	 * Covers all 6 wood variants of fences.
	 */
	public static final MaterialSetTag WOODEN_FENCES = new MaterialSetTag(keyFor("wooden_fences"))
			.endsWith("_FENCE")
			.not(Material.NETHER_BRICK_FENCE)
			.ensureSize("WOODEN_FENCES", 6);

	/**
	 * Covers all 6 wood variants of trapdoors.
	 */
	public static final MaterialSetTag WOODEN_TRAPDOORS = new MaterialSetTag(keyFor("wooden_trapdoors"))
			.endsWith("_TRAPDOOR")
			.not(Material.IRON_TRAPDOOR)
			.ensureSize("WOODEN_TRAPDOORS", 6);

	public static final MaterialSetTag WOODEN_GATES = new MaterialSetTag(keyFor("wooden_gates"))
			.endsWith("_GATE")
			.ensureSize("WOODEN_GATES", 6);

	public static final MaterialSetTag PURPUR = new MaterialSetTag(keyFor("purpur"))
			.startsWith("PURPUR_")
			.ensureSize("PURPUR", 4);

	public static final MaterialSetTag SIGNS = new MaterialSetTag(keyFor("signs"))
			.add(Material.SIGN, Material.WALL_SIGN)
			.ensureSize("SIGNS", 2);

	public static final MaterialSetTag TORCH = new MaterialSetTag(keyFor("torch"))
			.add(Material.TORCH, Material.WALL_TORCH)
			.ensureSize("TORCH", 2);

	public static final MaterialSetTag REDSTONE_TORCH = new MaterialSetTag(keyFor("restone_torch"))
			.add(Material.REDSTONE_TORCH, Material.REDSTONE_WALL_TORCH)
			.ensureSize("REDSTONE_TORCH", 2);

	@SuppressWarnings("unchecked")
	public static final MaterialSetTag COLORABLE = new MaterialSetTag(keyFor("colorable"))
			.add(Tag.WOOL, Tag.CARPETS).add(SHULKER_BOXES, STAINED_GLASS, STAINED_GLASS_PANES, CONCRETES, BEDS);
	//.ensureSize("COLORABLE", 81); unit test don't have the vanilla item tags, so counts don't line up for real
}
