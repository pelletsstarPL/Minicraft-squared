package minicraft.item;

import java.util.ArrayList;

public class Recipes {
	
	public static final ArrayList<Recipe> anvilRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> ovenRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> furnaceRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> workbenchRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> stonecutterRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> enchantRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> craftRecipes = new ArrayList<>();
	public static final ArrayList<Recipe> loomRecipes = new ArrayList<>();
	public static ArrayList<Recipe> shardForgeRecipes = new ArrayList<>();

	static {
		stonecutterRecipes.add(new Recipe("Stone Brick_2", "Stone_2"));
		stonecutterRecipes.add(new Recipe("Stone Door_1", "Stone Brick_5"));
		stonecutterRecipes.add(new Recipe("Dungeon brick_1", "Stone Brick_1","Moss_1"));
		stonecutterRecipes.add(new Recipe("Obsidian Brick_2", "Obsidian_6"));
		stonecutterRecipes.add(new Recipe("Obsidian Door_1", "Obsidian Brick_5"));
		stonecutterRecipes.add(new Recipe("Ornate stone_1", "Stone_2"));
		stonecutterRecipes.add(new Recipe("Rocky stone_1","Stone_2"));
		stonecutterRecipes.add(new Recipe("Ornate obsidian_1", "Obsidian_6"));
		stonecutterRecipes.add(new Recipe("Cross obsidian_1", "Obsidian_6"));

		craftRecipes.add(new Recipe("Workbench_1", "Wood_10"));
		craftRecipes.add(new Recipe("Crude Axe_1", "Stone_2","Stick_3","String_1"));
		craftRecipes.add(new Recipe("Crude Hoe_1", "Stone_2","Stick_4","String_1"));
		craftRecipes.add(new Recipe("Crude Pickaxe_1", "Stone_3","Stick_3","String_1"));
		craftRecipes.add(new Recipe("Crude Shovel_1", "Stone_2","Stick_4","String_1"));
		craftRecipes.add(new Recipe("String_1", "Plant Fiber_3"));
		craftRecipes.add(new Recipe("Torch_2", "Stick_2", "coal_1"));
		craftRecipes.add(new Recipe("Torch_2", "Stick_2", "charcoal_1"));
		craftRecipes.add(new Recipe("plank_2", "Wood_1"));
		craftRecipes.add(new Recipe("Plank Wall_1", "plank_3"));
		craftRecipes.add(new Recipe("Wood Door_1", "plank_5"));
		craftRecipes.add(new Recipe("Stick_4", "Wood_1"));
		craftRecipes.add(new Recipe("Sugar_1", "Reed_2"));
		craftRecipes.add(new Recipe("Bonemeal_2", "Bone_1"));
		craftRecipes.add(new Recipe("Purified water_1", "Coal filter_1","Dirty water_1"));
		craftRecipes.add(new Recipe("Coarse dirt_1", "dirt_1","stone_1"));

		workbenchRecipes.add(new Recipe("Torch_2", "Stick_2", "coal_1"));
		workbenchRecipes.add(new Recipe("Torch_2", "Stick_2", "charcoal_1"));
		workbenchRecipes.add(new Recipe("Lantern_1", "Wood_8", "slime_4", "glass_4"));
		workbenchRecipes.add(new Recipe("Stone Wall_1", "Stone Brick_3"));

		workbenchRecipes.add(new Recipe("Obsidian Wall_1", "Obsidian Brick_3"));

		workbenchRecipes.add(new Recipe("Oven_1", "Stone_15"));
		workbenchRecipes.add(new Recipe("Furnace_1", "Stone_20"));
		workbenchRecipes.add(new Recipe("Enchanter_1", "Wood_5", "String_2", "Lapis_15"));
		workbenchRecipes.add(new Recipe("Chest_1", "Wood_20"));
		workbenchRecipes.add(new Recipe("Patch vase_1", "Wood_15"));
		workbenchRecipes.add(new Recipe("Anvil_1", "iron_5"));
		workbenchRecipes.add(new Recipe("Tnt_1", "Gunpowder_10", "Sand_8"));
		workbenchRecipes.add(new Recipe("Loom_1", "Wood_10", "Wool_5"));
		workbenchRecipes.add(new Recipe("Stonecutter_1", "Wood_10", "Stone_5","Iron_3"));
		workbenchRecipes.add(new Recipe("Coal filter_1", "Paper_4", "Coal_1"));
		workbenchRecipes.add(new Recipe("Coal filter_1", "Paper_4", "charcoal_1"));
		workbenchRecipes.add(new Recipe("Paper_3", "Reed_8"));
		workbenchRecipes.add(new Recipe("Bonemeal_2", "Bone_1"));
		workbenchRecipes.add(new Recipe("Fungus spores_1", "Fungus_5","Wheat seeds_2","Plant fiber_1"));
		workbenchRecipes.add(new Recipe("Wood Fishing Rod_1", "Wood_5","Stick_2", "String_3"));
		workbenchRecipes.add(new Recipe("Iron Fishing Rod_1", "Iron_10","Stick_3", "String_3"));
		workbenchRecipes.add(new Recipe("Gold Fishing Rod_1", "Gold_10","Stick_3", "String_3"));
		workbenchRecipes.add(new Recipe("Gem Fishing Rod_1", "Gem_10","Stick_3", "String_3"));


		workbenchRecipes.add(new Recipe("Wood Bow_1", "Wood_6","Stick_3","string_2"));
		workbenchRecipes.add(new Recipe("Wood Sword_1", "Wood_4","Stick_1","string_1"));
		workbenchRecipes.add(new Recipe("Wood Claymore_1", "Wood sword_1","string_1","Blood shard_4"));
		workbenchRecipes.add(new Recipe("Rock Sword_1", "Wood_3","Stick_3","Stone_5"));
		workbenchRecipes.add(new Recipe("Rock Claymore_1", "Rock sword_1","Blood shard_15"));
		workbenchRecipes.add(new Recipe("Rock Axe_1", "Wood_3","Stick_3","Stone_5"));
		workbenchRecipes.add(new Recipe("Rock Hoe_1","Wood_3","Stick_3","Stone_5"));
		workbenchRecipes.add(new Recipe("Rock Pickaxe_1", "Wood_3","Stick_3","Stone_5"));
		workbenchRecipes.add(new Recipe("Rock Shovel_1", "Wood_3","Stick_3","Stone_5"));
		workbenchRecipes.add(new Recipe("Rock Bow_1", "Stick_3","Stone_6", "string_2"));

		workbenchRecipes.add(new Recipe("arrow_3", "Stick_4", "Stone_2"));
		workbenchRecipes.add(new Recipe("Leather Armor_1", "leather_10"));
		workbenchRecipes.add(new Recipe("Snake Armor_1", "scale_15"));
		//Juices/waters
		workbenchRecipes.add(new Recipe("Sugar_2", "Reed_4"));
		workbenchRecipes.add(new Recipe("bottle_1", "glass_1"));
		workbenchRecipes.add(new Recipe("Apple juice_1","Water bottle_1","apple_2","Sugar_1"));
		workbenchRecipes.add(new Recipe("Cactus juice_1","Water bottle_1","cactus_3","Sugar_1"));
		workbenchRecipes.add(new Recipe("Gold apple juice_1", "Water bottle_1","Gold apple_1","Sugar_3"));
		workbenchRecipes.add(new Recipe("Herb tea_1", "Water bottle_1","flower_1","rose_1","Wheat Seeds_1","Sunflower_1"));
		workbenchRecipes.add(new Recipe("Carrot juice_1", "Water bottle_1","carrot_2","Sugar_3"));
		workbenchRecipes.add(new Recipe("Beetroot juice_1", "Water bottle_1","Beetroot_4"));
		//layers
		workbenchRecipes.add(new Recipe("Snow layer_1", "snow_2"));
		workbenchRecipes.add(new Recipe("Moss layer_1", "moss_2"));

		loomRecipes.add(new Recipe("String_2", "Wool_1"));
		loomRecipes.add(new Recipe("Bed_1", "Wood_5", "Wool_3"));
		loomRecipes.add(new Recipe("red wool_1", "Wool_1", "rose_1"));
		loomRecipes.add(new Recipe("orange wool_1", "Wool_1", "rose_1","Flower_1"));
		loomRecipes.add(new Recipe("orange wool_1", "Wool_1", "small rose_2","small flower_2"));
		loomRecipes.add(new Recipe("yellow wool_1", "Wool_1", "Flower_1"));
		loomRecipes.add(new Recipe("blue wool_1", "Wool_1", "Lapis_1"));
		loomRecipes.add(new Recipe("light blue wool_1", "blue wool_1", "bonemeal_2"));
		loomRecipes.add(new Recipe("green wool_1", "Wool_1", "Cactus_1"));
		loomRecipes.add(new Recipe("lime wool_1", "green wool_1", "bonemeal_1"));
		loomRecipes.add(new Recipe("black wool_1", "Wool_1", "coal_1"));
		loomRecipes.add(new Recipe("gray wool_1", "black wool_1", "bonemeal_2"));
		loomRecipes.add(new Recipe("light gray wool_1", "Wool_1", "bonemeal_1","coal_1"));
		loomRecipes.add(new Recipe("pink wool_1", "red wool_1", "bonemeal_2"));
		loomRecipes.add(new Recipe("magenta wool_1", "Wool_1", "bonemeal_1","rose_1","lapis_1"));
		loomRecipes.add(new Recipe("purple wool_1", "Wool_1","rose_1","lapis_1"));
		loomRecipes.add(new Recipe("cyan wool_1", "Wool_1","Cactus_1","Lapis_1"));

		loomRecipes.add(new Recipe("blue clothes_1", "cloth_5", "Lapis_1"));
		loomRecipes.add(new Recipe("l. blue clothes_1", "cloth_5", "Lapis_1","bonemeal_2"));
		loomRecipes.add(new Recipe("green clothes_1", "cloth_5", "Cactus_1"));
		loomRecipes.add(new Recipe("lime clothes_1", "cloth_5", "Cactus_1","bonemeal_2"));
		loomRecipes.add(new Recipe("yellow clothes_1", "cloth_5", "Flower_1"));
		loomRecipes.add(new Recipe("yellow clothes_1", "cloth_5", "small Flower_3"));
		loomRecipes.add(new Recipe("red clothes_1", "cloth_5", "rose_1"));
		loomRecipes.add(new Recipe("red clothes_1", "cloth_5", "small rose_3"));
		loomRecipes.add(new Recipe("black clothes_1", "cloth_5", "coal_1"));
		loomRecipes.add(new Recipe("gray clothes_1", "cloth_5", "coal_1","bonemeal_1"));
		loomRecipes.add(new Recipe("l. gray clothes_1", "cloth_5", "coal_1","bonemeal_2"));
		loomRecipes.add(new Recipe("l. gray clothes_1", "white clothes_1", "coal_1","bonemeal_1"));
		loomRecipes.add(new Recipe("white clothes_1", "cloth_5", "bonemeal_4"));
		loomRecipes.add(new Recipe("orange clothes_1", "cloth_5", "rose_1", "Flower_1"));
		loomRecipes.add(new Recipe("purple clothes_1", "cloth_5", "Lapis_1", "rose_1"));
		loomRecipes.add(new Recipe("pink clothes_1", "cloth_5", "bonemeal_2", "rose_1"));
		loomRecipes.add(new Recipe("cyan clothes_1", "cloth_5", "Lapis_1", "Cactus_1"));
		loomRecipes.add(new Recipe("reg clothes_1", "cloth_5"));
		
		loomRecipes.add(new Recipe("Leather Armor_1", "leather_10"));
		
		anvilRecipes.add(new Recipe("Iron Armor_1", "iron_10"));
		anvilRecipes.add(new Recipe("Gold Armor_1", "gold_10"));
		anvilRecipes.add(new Recipe("Gem Armor_1", "gem_65"));
		anvilRecipes.add(new Recipe("Empty Bucket_1", "iron_5"));
		anvilRecipes.add(new Recipe("Iron Lantern_1", "iron_8", "slime_5", "glass_4"));
		anvilRecipes.add(new Recipe("Gold Lantern_1", "gold_10", "slime_5", "glass_4"));
		anvilRecipes.add(new Recipe("Gem Lantern_1", "gold lantern_1", "gold_1", "gem_40"));
		anvilRecipes.add(new Recipe("Iron Sword_1", "iron_5","Stick_6"));

		anvilRecipes.add(new Recipe("Iron Axe_1", "Stick_6", "iron_5"));
		anvilRecipes.add(new Recipe("Iron Hoe_1", "Stick_6", "iron_5"));
		anvilRecipes.add(new Recipe("Iron Pickaxe_1", "Stick_6", "iron_5"));
		anvilRecipes.add(new Recipe("Iron Shovel_1", "Stick_6", "iron_5"));
		anvilRecipes.add(new Recipe("Iron Bow_1",  "iron_5", "string_2"));
		anvilRecipes.add(new Recipe("Gold Sword_1", "Stick_6", "gold_5"));

		anvilRecipes.add(new Recipe("Gold Axe_1", "Stick_6", "gold_5"));
		anvilRecipes.add(new Recipe("Gold Hoe_1", "Stick_6", "gold_5"));
		anvilRecipes.add(new Recipe("Gold Pickaxe_1", "Stick_6", "gold_5"));
		anvilRecipes.add(new Recipe("Gold Shovel_1", "Stick_6", "gold_5"));
		anvilRecipes.add(new Recipe("Gold Bow_1",  "gold_5", "string_2"));
		anvilRecipes.add(new Recipe("Gem Sword_1", "Stick_6", "gem_50"));

		anvilRecipes.add(new Recipe("Gem Axe_1", "Stick_6", "gem_50"));
		anvilRecipes.add(new Recipe("Gem Hoe_1", "Stick_6", "gem_50"));
		anvilRecipes.add(new Recipe("Gem Pickaxe_1", "Stick_6", "gem_50"));
		anvilRecipes.add(new Recipe("Gem Shovel_1", "Stick_6", "gem_50"));
		anvilRecipes.add(new Recipe("Gem Bow_1",  "gem_50", "string_2"));
		anvilRecipes.add(new Recipe("Obsidium Sword_1", "Stick_6", "obsidium_6"));

		anvilRecipes.add(new Recipe("Obsidium Axe_1", "Stick_6", "obsidium_6"));
		anvilRecipes.add(new Recipe("Obsidium Hoe_1", "Stick_6", "obsidium_6"));
		anvilRecipes.add(new Recipe("Obsidium Pickaxe_1", "Stick_6", "obsidium_6"));
		anvilRecipes.add(new Recipe("Obsidium Shovel_1", "Stick_6", "obsidium_6"));
		anvilRecipes.add(new Recipe("Obsidium Bow_1", "Stick_6", "obsidium_6", "string_2"));
		anvilRecipes.add(new Recipe("Iron Shears_1", "Iron_4"));
		anvilRecipes.add(new Recipe("Gold Shears_1", "Iron_1","Gold_3"));
		anvilRecipes.add(new Recipe("Gem Shears_1", "Iron_1","gem_20"));
		anvilRecipes.add(new Recipe("Obsidium Shears_1", "Iron_1","Obsidium_3"));
		anvilRecipes.add(new Recipe("Lapis Block_1", "Lapis_25"));
		anvilRecipes.add(new Recipe("Iron Block_1", "Iron_15"));
		anvilRecipes.add(new Recipe("Gold Block_1", "Gold_15"));
		anvilRecipes.add(new Recipe("Gem Block_1", "Gem_75"));
		anvilRecipes.add(new Recipe("Obsidium Block_1", "Obsidium_15"));
		anvilRecipes.add(new Recipe("Coal Block_1", "Coal_35"));
		anvilRecipes.add(new Recipe("Lapis_24", "Lapis Block_1"));
		anvilRecipes.add(new Recipe("Iron_14", "Iron Block_1"));
		anvilRecipes.add(new Recipe("Gem_74", "Gem Block_1"));
		anvilRecipes.add(new Recipe("Obsidium_15", "Obsidium Block_1"));
		anvilRecipes.add(new Recipe("Coal_34", "Coal Block_1"));

		anvilRecipes.add(new Recipe("Shardforge_1", "Obsidium_10","anvil_1","furnace_1","shard_10"));

		furnaceRecipes.add(new Recipe("iron_1", "iron Ore_4", "coal_1"));
		furnaceRecipes.add(new Recipe("iron_1", "iron Ore_4", "charcoal_1"));
		furnaceRecipes.add(new Recipe("gold_1", "gold Ore_4", "coal_1"));
		furnaceRecipes.add(new Recipe("gold_1", "gold Ore_4", "charcoal_1"));
		furnaceRecipes.add(new Recipe("glass_1", "sand_4", "coal_1"));
		furnaceRecipes.add(new Recipe("glass_1", "sand_4", "charcoal_1"));
		furnaceRecipes.add(new Recipe("charcoal_1", "wood_2"));
		furnaceRecipes.add(new Recipe("obsidium_1", "iron_1","obsidium ore_6", "coal_4"));
		furnaceRecipes.add(new Recipe("obsidium_1", "iron_1","obsidium ore_6", "charcoal_4"));

		ovenRecipes.add(new Recipe("cooked pork_1", "raw pork_1", "coal_1"));
		ovenRecipes.add(new Recipe("cooked pork_1", "raw pork_1", "charcoal_1"));
		ovenRecipes.add(new Recipe("steak_1", "raw beef_1", "coal_1"));
		ovenRecipes.add(new Recipe("steak_1", "raw beef_1", "charcoal_1"));
		ovenRecipes.add(new Recipe("cooked fish_1", "raw fish_1", "coal_1"));
		ovenRecipes.add(new Recipe("cooked fish_1", "raw fish_1", "charcoal_1"));
		ovenRecipes.add(new Recipe("bread_1", "wheat_4"));
		ovenRecipes.add(new Recipe("Baked Potato_1", "Potato_1"));
		ovenRecipes.add(new Recipe("Cooked Carrot_1", "Carrot_1"));
		ovenRecipes.add(new Recipe("Water bottle_1", "Purified water_1"));

		enchantRecipes.add(new Recipe("Gold Apple_1", "apple_1", "gold_8"));
		enchantRecipes.add(new Recipe("potion_1", "bottle_1", "Lapis_3"));
		enchantRecipes.add(new Recipe("speed potion_1", "potion_1", "Cactus_5"));
		enchantRecipes.add(new Recipe("light potion_1", "potion_1", "slime_5"));
		enchantRecipes.add(new Recipe("swim potion_1", "potion_1", "raw fish_5"));
		enchantRecipes.add(new Recipe("haste potion_1", "potion_1", "Wood_5", "Stone_5"));
		enchantRecipes.add(new Recipe("time potion_1", "potion_1", "cactus juice_1","gem_1","slime_1"));
		enchantRecipes.add(new Recipe("lava potion_1", "potion_1", "Lava Bucket_1"));
		enchantRecipes.add(new Recipe("energy potion_1", "potion_1", "gem_25"));
		enchantRecipes.add(new Recipe("regen potion_1", "potion_1", "Gold Apple_1"));
		enchantRecipes.add(new Recipe("Health Potion_1", "potion_1", "GunPowder_2", "Leather Armor_1"));
		enchantRecipes.add(new Recipe("Power Potion_1", "potion_1", "cactus_3", "blood shard_2","carrot_1"));
		enchantRecipes.add(new Recipe("Escape Potion_1", "potion_1", "GunPowder_3", "Lapis_7"));
		enchantRecipes.add(new Recipe("Air Totem_1", "gold_10", "cloud shard_30", "gem_1","Lapis_3","Demonicolon_1","Cloud cactus_3"));
		enchantRecipes.add(new Recipe("Moonlight Totem_1", "gold_5", "Gold apple_1","Lapis_30","Demonicolon_1"));
		enchantRecipes.add(new Recipe("Time Totem_1", "time potion_1","iron_3","lapis_2","cloud shard_1"));

		shardForgeRecipes.add(new Recipe("Iron Claymore_1", "Iron Sword_1", "shard_15","lava bucket_1"));
		shardForgeRecipes.add(new Recipe("Iron Claymore_1", "Iron Sword_1", "blood shard_20","lava bucket_1"));
		shardForgeRecipes.add(new Recipe("Iron Hammer_1", "Iron pickaxe_1","Iron axe_1", "shard_15","lava bucket_1"));
		shardForgeRecipes.add(new Recipe("Iron Hammer_1", "Iron pickaxe_1","Iron axe_1", "blood shard_25","lava bucket_1"));
		shardForgeRecipes.add(new Recipe("Gold Claymore_1", "Gold Sword_1", "shard_15","lava bucket_1"));
		shardForgeRecipes.add(new Recipe("Gold Claymore_1", "Gold Sword_1", "blood shard_35","lava bucket_1"));
		shardForgeRecipes.add(new Recipe("Gold Hammer_1", "Gold pickaxe_1","Gold axe_1", "blood shard_40","lava bucket_1"));
		shardForgeRecipes.add(new Recipe("Gold Hammer_1", "Gold pickaxe_1","Gold axe_1", "shard_15","lava bucket_1"));
		shardForgeRecipes.add(new Recipe("Gem Claymore_1", "Gem Sword_1", "shard_15","lava bucket_1"));
		shardForgeRecipes.add(new Recipe("Gem Hammer_1", "Gold Pickaxe_1","Gem axe_1", "shard_15","lava bucket_1"));
		shardForgeRecipes.add(new Recipe("Obsidium Claymore_1", "Obsidium Sword_1", "shard_15","lava bucket_1"));
		shardForgeRecipes.add(new Recipe("Obsidium Hammer_1", "Obsidium Pickaxe_1","Obsidium Axe_1", "shard_15","lava bucket_1"));
	}

}
