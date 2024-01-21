package minicraft.item;

import java.util.ArrayList;

public class Recipes {

	public static final ArrayList<Recipe> anvilRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> ovenRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> furnaceRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> workbenchRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> enchantRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> craftRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> loomRecipes = new ArrayList<>();

	static {
		craftRecipes.add(new Recipe("Workbench_1", "Wood_10"));
		craftRecipes.add(new Recipe("Torch_2", "Wood_1", "coal_1"));
		craftRecipes.add(new Recipe("plank_2", "Wood_1"));
		craftRecipes.add(new Recipe("Plank Wall_1", "plank_3"));
		craftRecipes.add(new Recipe("Wood Door_1", "plank_5"));

		workbenchRecipes.add(new Recipe("Workbench_1", "Wood_10"));
		workbenchRecipes.add(new Recipe("Torch_2", "Wood_1", "coal_1"));
		workbenchRecipes.add(new Recipe("plank_2", "Wood_1"));
		workbenchRecipes.add(new Recipe("Plank Wall_1", "plank_3"));
		workbenchRecipes.add(new Recipe("Wood Door_1", "plank_5"));
		workbenchRecipes.add(new Recipe("Lantern_1", "Wood_8", "slime_4", "glass_3"));
		workbenchRecipes.add(new Recipe("Stone Brick_1", "Stone_2"));
		workbenchRecipes.add(new Recipe("Ornate Stone_1", "Stone_2"));
		workbenchRecipes.add(new Recipe("Stone Wall_1", "Stone Brick_3"));
		workbenchRecipes.add(new Recipe("Stone Door_1", "Stone Brick_5"));
		workbenchRecipes.add(new Recipe("Obsidian Brick_1", "Raw Obsidian_2"));
		workbenchRecipes.add(new Recipe("Ornate Obsidian_1", "Raw Obsidian_2"));
		workbenchRecipes.add(new Recipe("Obsidian Wall_1", "Obsidian Brick_3"));
		workbenchRecipes.add(new Recipe("Obsidian Door_1", "Obsidian Brick_5"));
		workbenchRecipes.add(new Recipe("Oven_1", "Stone_15"));
		workbenchRecipes.add(new Recipe("Furnace_1", "Stone_20"));
		workbenchRecipes.add(new Recipe("Enchanter_1", "Wood_5", "String_2", "Lapis_10"));
		workbenchRecipes.add(new Recipe("Chest_1", "Wood_20"));
		workbenchRecipes.add(new Recipe("Anvil_1", "iron_5"));
		workbenchRecipes.add(new Recipe("Tnt_1", "Gunpowder_10", "Sand_8"));
		workbenchRecipes.add(new Recipe("Loom_1", "Wood_10", "Wool_5"));
		workbenchRecipes.add(new Recipe("Wood Fishing Rod_1", "Wood_10", "String_3"));
		workbenchRecipes.add(new Recipe("Iron Fishing Rod_1", "Iron_10", "String_3"));
		workbenchRecipes.add(new Recipe("Gold Fishing Rod_1", "Gold_10", "String_3"));
		workbenchRecipes.add(new Recipe("Gem Fishing Rod_1", "Gem_10", "String_3"));

		workbenchRecipes.add(new Recipe("Wood Sword_1", "Wood_5"));
		workbenchRecipes.add(new Recipe("Wood Axe_1", "Wood_5"));
		workbenchRecipes.add(new Recipe("Wood Hoe_1", "Wood_5"));
		workbenchRecipes.add(new Recipe("Wood Pickaxe_1", "Wood_5"));
		workbenchRecipes.add(new Recipe("Wood Shovel_1", "Wood_5"));
		workbenchRecipes.add(new Recipe("Wood Bow_1", "Wood_5", "string_2"));
		workbenchRecipes.add(new Recipe("Rock Sword_1", "Wood_5", "Stone_5"));
		workbenchRecipes.add(new Recipe("Rock Axe_1", "Wood_5", "Stone_5"));
		workbenchRecipes.add(new Recipe("Rock Hoe_1", "Wood_5", "Stone_5"));
		workbenchRecipes.add(new Recipe("Rock Pickaxe_1", "Wood_5", "Stone_5"));
		workbenchRecipes.add(new Recipe("Rock Shovel_1", "Wood_5", "Stone_5"));
		workbenchRecipes.add(new Recipe("Rock Bow_1", "Wood_5", "Stone_5", "string_2"));

		workbenchRecipes.add(new Recipe("arrow_3", "Wood_2", "Stone_2"));
		workbenchRecipes.add(new Recipe("Leather Armor_1", "leather_10"));
		workbenchRecipes.add(new Recipe("Snake Armor_1", "scale_15"));

		loomRecipes.add(new Recipe("String_2", "Wool_1"));
		loomRecipes.add(new Recipe("red wool_1", "Wool_1", "rose_1"));
		loomRecipes.add(new Recipe("blue wool_1", "Wool_1", "Lapis_1"));
		loomRecipes.add(new Recipe("green wool_1", "Wool_1", "Cactus_1"));
		loomRecipes.add(new Recipe("yellow wool_1", "Wool_1", "Flower_1"));
		loomRecipes.add(new Recipe("black wool_1", "Wool_1", "coal_1"));
		loomRecipes.add(new Recipe("Bed_1", "Wood_5", "Wool_3"));

		loomRecipes.add(new Recipe("blue clothes_1", "cloth_5", "Lapis_1"));
		loomRecipes.add(new Recipe("green clothes_1", "cloth_5", "Cactus_1"));
		loomRecipes.add(new Recipe("yellow clothes_1", "cloth_5", "Flower_1"));
		loomRecipes.add(new Recipe("black clothes_1", "cloth_5", "coal_1"));
		loomRecipes.add(new Recipe("orange clothes_1", "cloth_5", "rose_1", "Flower_1"));
		loomRecipes.add(new Recipe("purple clothes_1", "cloth_5", "Lapis_1", "rose_1"));
		loomRecipes.add(new Recipe("cyan clothes_1", "cloth_5", "Lapis_1", "Cactus_1"));
		loomRecipes.add(new Recipe("reg clothes_1", "cloth_5"));

		loomRecipes.add(new Recipe("Leather Armor_1", "leather_10"));

		anvilRecipes.add(new Recipe("Iron Armor_1", "iron_10"));
		anvilRecipes.add(new Recipe("Gold Armor_1", "gold_10"));
		anvilRecipes.add(new Recipe("Gem Armor_1", "gem_65"));
		anvilRecipes.add(new Recipe("Empty Bucket_1", "iron_5"));
		anvilRecipes.add(new Recipe("Iron Lantern_1", "iron_8", "slime_5", "glass_4"));
		anvilRecipes.add(new Recipe("Gold Lantern_1", "gold_10", "slime_5", "glass_4"));
		anvilRecipes.add(new Recipe("Iron Sword_1", "Wood_5", "iron_5"));
		anvilRecipes.add(new Recipe("Iron Claymore_1", "Iron Sword_1", "shard_15"));
		anvilRecipes.add(new Recipe("Iron Axe_1", "Wood_5", "iron_5"));
		anvilRecipes.add(new Recipe("Iron Hoe_1", "Wood_5", "iron_5"));
		anvilRecipes.add(new Recipe("Iron Pickaxe_1", "Wood_5", "iron_5"));
		anvilRecipes.add(new Recipe("Iron Shovel_1", "Wood_5", "iron_5"));
		anvilRecipes.add(new Recipe("Iron Bow_1", "Wood_5", "iron_5", "string_2"));
		anvilRecipes.add(new Recipe("Gold Sword_1", "Wood_5", "gold_5"));
		anvilRecipes.add(new Recipe("Gold Claymore_1", "Gold Sword_1", "shard_15"));
		anvilRecipes.add(new Recipe("Gold Axe_1", "Wood_5", "gold_5"));
		anvilRecipes.add(new Recipe("Gold Hoe_1", "Wood_5", "gold_5"));
		anvilRecipes.add(new Recipe("Gold Pickaxe_1", "Wood_5", "gold_5"));
		anvilRecipes.add(new Recipe("Gold Shovel_1", "Wood_5", "gold_5"));
		anvilRecipes.add(new Recipe("Gold Bow_1", "Wood_5", "gold_5", "string_2"));
		anvilRecipes.add(new Recipe("Gem Sword_1", "Wood_5", "gem_50"));
		anvilRecipes.add(new Recipe("Gem Claymore_1", "Gem Sword_1", "shard_15"));
		anvilRecipes.add(new Recipe("Gem Axe_1", "Wood_5", "gem_50"));
		anvilRecipes.add(new Recipe("Gem Hoe_1", "Wood_5", "gem_50"));
		anvilRecipes.add(new Recipe("Gem Pickaxe_1", "Wood_5", "gem_50"));
		anvilRecipes.add(new Recipe("Gem Shovel_1", "Wood_5", "gem_50"));
		anvilRecipes.add(new Recipe("Gem Bow_1", "Wood_5", "gem_50", "string_2"));
		anvilRecipes.add(new Recipe("Shears_1", "Iron_4"));
		anvilRecipes.add(new Recipe("Watering Can_1", "Iron_3"));

		furnaceRecipes.add(new Recipe("iron_1", "iron Ore_4", "coal_1"));
		furnaceRecipes.add(new Recipe("gold_1", "gold Ore_4", "coal_1"));
		furnaceRecipes.add(new Recipe("glass_1", "sand_4", "coal_1"));
		furnaceRecipes.add(new Recipe("glass bottle_1", "glass_3"));

		ovenRecipes.add(new Recipe("cooked pork_1", "raw pork_1", "coal_1"));
		ovenRecipes.add(new Recipe("steak_1", "raw beef_1", "coal_1"));
		ovenRecipes.add(new Recipe("cooked fish_1", "raw fish_1", "coal_1"));
		ovenRecipes.add(new Recipe("bread_1", "wheat_4"));
		ovenRecipes.add(new Recipe("Baked Potato_1", "Potato_1"));

		enchantRecipes.add(new Recipe("Gold Apple_1", "apple_1", "gold_8"));
		enchantRecipes.add(new Recipe("awkward potion_1", "glass bottle_1", "Lapis_3"));
		enchantRecipes.add(new Recipe("speed potion_1", "awkward potion_1", "Cactus_5"));
		enchantRecipes.add(new Recipe("light potion_1", "awkward potion_1", "slime_5"));
		enchantRecipes.add(new Recipe("swim potion_1", "awkward potion_1", "raw fish_5"));
		enchantRecipes.add(new Recipe("haste potion_1", "awkward potion_1", "Wood_5", "Stone_5"));
		enchantRecipes.add(new Recipe("lava potion_1", "awkward potion_1", "Lava Bucket_1"));
		enchantRecipes.add(new Recipe("energy potion_1", "awkward potion_1", "gem_25"));
		enchantRecipes.add(new Recipe("regen potion_1", "awkward potion_1", "Gold Apple_1"));
		enchantRecipes.add(new Recipe("Health Potion_1", "awkward potion_1", "GunPowder_2", "Leather Armor_1"));
		enchantRecipes.add(new Recipe("Escape Potion_1", "awkward potion_1", "GunPowder_3", "Lapis_7"));
		enchantRecipes.add(new Recipe("Totem of Air_1", "gold_10", "gem_10", "Lapis_5", "Cloud Ore_5"));
		enchantRecipes.add(new Recipe("Obsidian Poppet_1", "gold_10", "gem_10", "Lapis_5", "Shard_15"));
		enchantRecipes.add(new Recipe("Arcane Fertilizer_3", "Lapis_6", "Bone_2"));
	}
}
