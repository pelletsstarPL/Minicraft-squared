package minicraft.level.tile;

import java.util.ArrayList;

import minicraft.core.Game;
import minicraft.level.tile.farming.*;
import minicraft.level.tile.patch.*;

public final class Tiles {
	/// Idea: to save tile names while saving space, I could encode the names in base 64 in the save file...^M
    /// Then, maybe, I would just replace the id numbers with id names, make them all private, and then make a get(String) method, parameter is tile name.
	
	public static ArrayList<String> oldids = new ArrayList<>();
	
	private static ArrayList<Tile> tiles = new ArrayList<>();
	
	public static void initTileList() {
		if (Game.debug) System.out.println("Initializing tile list...");
		
		// 
		for (int i = 0; i < 512; i++)
			tiles.add(null);

		tiles.set(0, new GrassTile("Grass"));
		tiles.set(1, new DirtTile("Dirt"));
		tiles.set(2, new FlowerTile("Flower", FlowerTile.FlowerType.Flower));
		tiles.set(3, new HoleTile("Hole"));
		tiles.set(4, new StairsTile("Stairs Up", true, StairsTile.StairsType.Normal));
		tiles.set(5, new StairsTile("Stairs Down", false, StairsTile.StairsType.Normal));
		tiles.set(6, new WaterTile("Water"));
		// This is out of order because of lava buckets
		tiles.set(17, new LavaTile("Lava"));

		tiles.set(7, new RockTile("Rock", RockTile.RockType.Rock));
		tiles.set(8, new TreeTile(TreeTile.TreeType.Oak));
		tiles.set(9, new SaplingTile("Oak Sapling", SaplingTile.SaplingType.Oak));
		tiles.set(10, new SandTile("Sand"));
		tiles.set(11, new CactusTile("Cactus"));
		tiles.set(12, new SaplingTile("Cactus Sapling", SaplingTile.SaplingType.Cactus));
		tiles.set(13, new OreTile(OreTile.OreType.Iron));
		tiles.set(14, new OreTile(OreTile.OreType.Gold));
		tiles.set(15, new OreTile(OreTile.OreType.Gem));
		tiles.set(16, new OreTile(OreTile.OreType.Lapis));
		tiles.set(18, new LavaBrickTile("Lava Brick"));
		tiles.set(19, new ExplodedTile("Explode"));
		tiles.set(20, new FarmTile("Farmland"));
		tiles.set(21, new WheatTile("Wheat"));
		tiles.set(22, new HardRockTile("Hard Rock", HardRockTile.HRType.One));
		tiles.set(23, new InfiniteFallTile("Infinite Fall"));
		tiles.set(24, new CloudTile("Cloud"));
		tiles.set(25, new CloudCactusTile("Cloud Cactus"));
		tiles.set(26, new DoorTile(Tile.Material.Wood));
		tiles.set(27, new DoorTile(Tile.Material.Stone));
		tiles.set(28, new DoorTile(Tile.Material.Obsidian));
		tiles.set(29, new FloorTile(Tile.Material.Wood));
		tiles.set(30, new FloorTile(Tile.Material.Stone));
		tiles.set(31, new FloorTile(Tile.Material.Obsidian));
		tiles.set(32, new WallTile(Tile.Material.Wood));
		tiles.set(33, new WallTile(Tile.Material.Stone));
		tiles.set(34, new WallTile(Tile.Material.Obsidian));
		tiles.set(35, new WoolTile(WoolTile.WoolType.NORMAL));
		tiles.set(36, new PathTile("Path"));
		tiles.set(37, new WoolTile(WoolTile.WoolType.RED));
		tiles.set(38, new WoolTile(WoolTile.WoolType.BLUE));
		tiles.set(39, new WoolTile(WoolTile.WoolType.GREEN));
		tiles.set(40, new WoolTile(WoolTile.WoolType.YELLOW));
		tiles.set(41, new WoolTile(WoolTile.WoolType.BLACK));
		tiles.set(42, new PotatoTile("Potato"));
		tiles.set(43, new DoorTile(Tile.Material.Dungeon));

		tiles.set(44, new WallTile(Tile.Material.Dungeon));
		tiles.set(46, new FloorTile(Tile.Material.Dungeon));
		tiles.set(47, new FlowerTile("Rose", FlowerTile.FlowerType.Rose));
		tiles.set(48, new FlowerTile("Small flower", FlowerTile.FlowerType.SmallFlower));
		tiles.set(49, new FlowerTile("Small rose", FlowerTile.FlowerType.SmallRose));
		tiles.set(50, new FlowerTile("Sunflower", FlowerTile.FlowerType.Sunflower));
		tiles.set(51, new StairsTile("Obsidian Stairs Up", true, StairsTile.StairsType.Obsidian));
		tiles.set(52, new StairsTile("Obsidian Stairs Down", false, StairsTile.StairsType.Obsidian));
		tiles.set(53, new TreeTile(TreeTile.TreeType.Birch));
		tiles.set(54, new SaplingTile("Birch Sapling", SaplingTile.SaplingType.Birch));
		tiles.set(55, new RockTile("Deepslate", RockTile.RockType.Deepslate));
		tiles.set(56, new DeadTreeTile("Dead Tree", DeadTreeTile.DeadTreeType.Sand));
		tiles.set(57, new SmallTreeTile(SmallTreeTile.TreeType.Oak));
		tiles.set(58, new HardRockTile("Hard Rock II", HardRockTile.HRType.Two));
		tiles.set(59, new ReedTile("Reed"));
		tiles.set(60, new ObsidianTile("Raw Obsidian"));
		tiles.set(61, new CarrotTile("Carrot"));
		tiles.set(62, new MossTile("Moss"));
		tiles.set(63, new FlowerTile("Azalea", FlowerTile.FlowerType.Azalea));
		tiles.set(64, new StoneOreTile("Deepslate spiky stone-L", StoneOreTile.StoneOreType.Deepslate));
		tiles.set(65, new StoneOreTile("Deepslate spiky stone", StoneOreTile.StoneOreType.LavaDeepslate));
		tiles.set(66, new StoneOreTile("Spiky stone", StoneOreTile.StoneOreType.WaterStone));
		tiles.set(67, new StoneOreTile("Spiky stone-L", StoneOreTile.StoneOreType.Stone));
		tiles.set(68, new TreeTile(TreeTile.TreeType.BigFungus));
		tiles.set(69, new OreTile(OreTile.OreType.Obsidian));
		tiles.set(70, new OreTile(OreTile.OreType.IronNF));
		tiles.set(71, new OreTile(OreTile.OreType.GoldNF));
		tiles.set(72, new OreTile(OreTile.OreType.GemNF));
		tiles.set(73, new OreTile(OreTile.OreType.GemGNF));
		tiles.set(74, new OreTile(OreTile.OreType.GemBNF));
		tiles.set(75, new OreTile(OreTile.OreType.GemG));
		tiles.set(76, new OreTile(OreTile.OreType.GemB));
		tiles.set(77, new WallTile(Tile.Material.ObsidianD)); //this is for a wall
		tiles.set(78, new FlowerTile("Small stones", FlowerTile.FlowerType.DirtStones));
		tiles.set(79, new FlowerTile("Grass small stones", FlowerTile.FlowerType.GrassStones));
		tiles.set(80, new BeetrootTile("Beetroot"));
		tiles.set(81, new FlowerTile("Tall grass", FlowerTile.FlowerType.Tallgrass));
		tiles.set(82, new FlowerTile("Desert grass", FlowerTile.FlowerType.DesertTallgrass));
		tiles.set(83, new SkygrassTile("Skygrass"));
		tiles.set(84, new OtherRockTile("Sky rock", OtherRockTile.OtherRockType.Sky));
		tiles.set(85, new IceTile("Ice"));
		tiles.set(86, new SnowTile("Snow"));
		tiles.set(87, new TreeTile(TreeTile.TreeType.Conifer));
		tiles.set(88, new SaplingTile("Conifer sapling", SaplingTile.SaplingType.Conifer));
		tiles.set(89, new TreeTile(TreeTile.TreeType.SnowyConifer));
		tiles.set(90, new SaplingTile("Snowy Conifer sapling", SaplingTile.SaplingType.SnowyConifer));
        tiles.set(91, new RockTileG("RockG", RockTileG.RockType.Rock));
        tiles.set(92, new GroundRockTile("Ground rock"));
		tiles.set(93, new WoolTile(WoolTile.WoolType.ORANGE));
		tiles.set(94, new WoolTile(WoolTile.WoolType.PURPLE));
		tiles.set(95, new WoolTile(WoolTile.WoolType.MAGENTA));
		tiles.set(96, new WoolTile(WoolTile.WoolType.LBLUE));
		tiles.set(97, new WoolTile(WoolTile.WoolType.LIME));
		tiles.set(98, new WoolTile(WoolTile.WoolType.CYAN));
		tiles.set(99, new WoolTile(WoolTile.WoolType.LGRAY));
		tiles.set(100, new WoolTile(WoolTile.WoolType.PINK));
		tiles.set(101, new WoolTile(WoolTile.WoolType.GRAY));
		tiles.set(102, new SaplingTile("cloud cactus Sapling", SaplingTile.SaplingType.CloudCactus));
        tiles.set(103, new BrambleTile("Bramble"));
		tiles.set(104, new CoarseDirtTile("Coarse dirt"));
		tiles.set(105, new DeadTreeTile("Dead Tree C", DeadTreeTile.DeadTreeType.Coarse));
		tiles.set(106, new RockTileG("DeepslateG",RockTileG.RockType.Deepslate));
		tiles.set(107, new OreBlockTile(OreBlockTile.OreType.Lapis));
		tiles.set(108, new OreBlockTile(OreBlockTile.OreType.Iron));
		tiles.set(109, new OreBlockTile(OreBlockTile.OreType.Gem));
		tiles.set(110, new DecorTile(Tile.Material.Stone));
		tiles.set(111, new DecorTile(Tile.Material.Stone2));
		tiles.set(112, new DecorTile(Tile.Material.Obsidian));
		tiles.set(113, new DecorTile(Tile.Material.ObsidianD));
		tiles.set(114, new FlowerTile("Cloud tallgrass", FlowerTile.FlowerType.CloudTallgrass));
		tiles.set(115, new TreeTile(TreeTile.TreeType.Sky));
		tiles.set(116, new OreBlockTile(OreBlockTile.OreType.Gold));
		tiles.set(117, new TreeTile(TreeTile.TreeType.Mangrove));
		tiles.set(118, new TreeTile(TreeTile.TreeType.MangroveWater));
		tiles.set(119, new LilyPadTile("Lily pad"));
		tiles.set(120, new TreeTile(TreeTile.TreeType.SkyConifer));
		tiles.set(121, new FlowerTile("Fern", FlowerTile.FlowerType.Fern));
		tiles.set(122, new FlowerTile("Cloud flower", FlowerTile.FlowerType.CloudFlower));
		tiles.set(123, new SaplingTile("Fern spores", SaplingTile.SaplingType.Fern));
		tiles.set(124, new DeadTreeTile("snowy Dead Tree", DeadTreeTile.DeadTreeType.Snow));
		tiles.set(125, new OtherRockTile("Glacier", OtherRockTile.OtherRockType.Iced));
		tiles.set(126, new SmallGlacierTile("Small glacier", SmallGlacierTile.SmallGlacierType.Block));
		tiles.set(127, new SmallGlacierTile("Small glacier spikes", SmallGlacierTile.SmallGlacierType.Spikes));
		tiles.set(128, new PatchTile("Empty patch vase"));
		tiles.set(129, new PlantPatchTile("Cloud Cactus", PlantPatchTile.PlantPatchType.CloudCactus));
		tiles.set(130, new SaplingPatchTile("Cloud Cactus Sapling", Tiles.get("Empty patch vase"), Tiles.get("patch cloud cactus"),Tiles.get("Cloud")));
		tiles.set(131, new PlantPatchTile("Cactus", PlantPatchTile.PlantPatchType.Cactus));
		tiles.set(132, new SaplingPatchTile("Cactus Sapling", Tiles.get("Empty patch vase"), Tiles.get("patch cactus"),Tiles.get("sand")));
        tiles.set(133, new SaplingTile("Fungus spores", SaplingTile.SaplingType.Fungus));
        tiles.set(134, new LockedStairsTile("Iced stairs down", LockedStairsTile.LockedStairsType.IcedStairsDown));
		tiles.set(135, new OtherRockTile("Iced rock", OtherRockTile.OtherRockType.IcedRock));
		tiles.set(136, new SmallTreeTile(SmallTreeTile.TreeType.Birch));
		tiles.set(137, new SmallTreeTile(SmallTreeTile.TreeType.Conifer));
		tiles.set(138, new SmallTreeTile(SmallTreeTile.TreeType.SnowyConifer));
		tiles.set(139, new SmallTreeTile(SmallTreeTile.TreeType.Cactus));
		tiles.set(140, new SmallTreeTile(SmallTreeTile.TreeType.CloudCactus));
		tiles.set(141, new SmallTreeTile(SmallTreeTile.TreeType.Fungus));
		tiles.set(142, new PlantPatchTile("Fungus", PlantPatchTile.PlantPatchType.Fungus));
		tiles.set(143, new SaplingPatchTile("Fungus Spores", Tiles.get("Empty patch vase"), Tiles.get("patch Fungus"),Tiles.get("Moss")));
		tiles.set(144, new AerocloudTile("Aerocloud"));
		tiles.set(145, new RockTileG("Obsidian deepslate",RockTileG.RockType.DeepslateObsidian));
		tiles.set(146, new OreTile(OreTile.OreType.Obsidium));
		tiles.set(147, new OreTile(OreTile.OreType.ObsidiumNF));
		tiles.set(148, new OreTile(OreTile.OreType.Coal));
		tiles.set(149, new InfiniteVoidTile("Infinite Void"));
		tiles.set(150,new RockTile("Obsidian rock",RockTile.RockType.Obsidian));
		tiles.set(151,new BridgeSupportTile("Obsidian bridge support"));
		tiles.set(152, new WallTile(Tile.Material.Unbreakable));
		tiles.set(153, new DungeonTallgrassTile("Dungeon tallgrass"));
		tiles.set(154, new DecorTile(Tile.Material.Unbreakable));
		tiles.set(155, new FloorTile(Tile.Material.ObsidianD));
		tiles.set(156, new PortalTile("Obsidian void portal"));
		tiles.set(157, new GatewayTile("Obsidian void portal frame"));
		tiles.set(158, new FlowerTile("Fungus",FlowerTile.FlowerType.Fungus));
		tiles.set(159, new DoorTile(Tile.Material.ObsidianD));
		tiles.set(160, new SpikesTile("Spikes"));
		tiles.set(161,  new SpikeWallTile("Spike wall"));
		tiles.set(162, new OreBlockTile(OreBlockTile.OreType.Coal));
		tiles.set(163, new OreBlockTile(OreBlockTile.OreType.Obsidium));
		tiles.set(164, new OreTile(OreTile.OreType.LapisNF));

		// WARNING: don't use this tile for anything!
		tiles.set(511, new ConnectTile());
		
		for(int i = 0; i < tiles.size(); i++) {
			if(tiles.get(i) == null) continue;
			tiles.get(i).id = (short)i;
		}
	}
	

	protected static void add(int id, Tile tile) {
		tiles.set(id, tile);
		System.out.println("Adding " + tile.name + " to tile list with id " + id);
		tile.id =(short) id;
	}

	
	private static int overflowCheck = 0;
	public static Tile get(String name) {
		//System.out.println("Getting from tile list: " + name);
		
		name = name.toUpperCase();
		
		overflowCheck++;
		
		if(overflowCheck > 50) {
			System.out.println("STACKOVERFLOW prevented in Tiles.get(), on: " + name);
			System.exit(1);
		}
		
		//System.out.println("Fetching tile " + name);
		
		Tile getting = null;
		
		boolean isTorch = false;
		if(name.startsWith("TORCH")) {
			isTorch = true;
			name = name.substring(6); // Cuts off torch prefix.
		}

		if(name.contains("_")) {
			name = name.substring(0, name.indexOf("_"));
		}
		
		for(Tile t: tiles) {
			if(t == null) continue;
			if(t.name.equals(name)) {
				getting = t;
				break;
			}
		}
		
		if(getting == null) {
			System.out.println("TILES.GET: Invalid tile requested: " + name);
			getting = tiles.get(0);
		}
		
		if(isTorch) {
			getting = TorchTile.getTorchTile(getting);
		}
		
		overflowCheck = 0;
		return getting;
	}
	
	public static Tile get(int id) {
		//System.out.println("Requesting tile by id: " + id);
		if(id < 0) id += 256;
		
		if(tiles.get(id) != null) {
			return tiles.get(id);
		}
		else if(id >= 128) {
			return TorchTile.getTorchTile(get(id-128));
		}
		else {
			System.out.println("TILES.GET: Unknown tile id requested: " + id);
			return tiles.get(0);
		}
	}
	
	public static boolean containsTile(int id) {
		return tiles.get(id) != null;
	}
	
	public static String getName(String descriptName) {
		if(!descriptName.contains("_")) return descriptName;
		int data;
		String[] parts = descriptName.split("_");
		descriptName = parts[0];
		data = Integer.parseInt(parts[1]);
		return get(descriptName).getName(data);
	}
}
