package minicraft.level.tile;

import java.util.ArrayList;

import minicraft.core.Game;
import minicraft.level.tile.farming.*;

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
		tiles.set(2, new FlowerTile("Flower"));
		tiles.set(3, new HoleTile("Hole"));
		tiles.set(4, new StairsTile("Stairs Up", true));
		tiles.set(5, new StairsTile("Stairs Down", false));
		tiles.set(6, new WaterTile("Water"));
		// This is out of order because of lava buckets
		tiles.set(17, new LavaTile("Lava"));

		tiles.set(7, new RockTile("Rock"));
		tiles.set(8, new TreeTile("Tree"));
		tiles.set(9, new SaplingTile("Tree Sapling", Tiles.get("Grass"), Tiles.get("Tree")));
		tiles.set(10, new SandTile("Sand"));
		tiles.set(11, new CactusTile("Cactus"));
		tiles.set(12, new SaplingTile("Cactus Sapling", Tiles.get("Sand"), Tiles.get("Cactus")));
		tiles.set(13, new OreTile(OreTile.OreType.Iron));
		tiles.set(14, new OreTile(OreTile.OreType.Gold));
		tiles.set(15, new OreTile(OreTile.OreType.Gem));
		tiles.set(16, new OreTile(OreTile.OreType.Lapis));
		tiles.set(18, new LavaBrickTile("Lava Brick"));
		tiles.set(19, new ExplodedTile("Explode"));
		tiles.set(20, new FarmTile("Farmland"));
		tiles.set(21, new WheatTile("Wheat"));
		tiles.set(22, new HardRockTile("Hard Rock"));
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
		tiles.set(44, new FloorTile(Tile.Material.Dungeon));
		tiles.set(45, new WallTile(Tile.Material.Dungeon));
		tiles.set(46, new DarkCloudTile("Dark Cloud"));
		tiles.set(47, new RosesTile("Rose"));
		tiles.set(48, new SmallFlowerTile("Small flower"));
		tiles.set(49, new SmallRosesTile("Small rose"));
		tiles.set(50, new SunflowerTile("Sunflower"));
		tiles.set(51, new ObsidianStairsTile("Obsidian Stairs Up", true));
		tiles.set(52, new ObsidianStairsTile("Obsidian Stairs Down", false));
		tiles.set(53, new TreeTileBirch("Birch"));
		tiles.set(54, new SaplingTile("Birch Sapling", Tiles.get("Grass"), Tiles.get("Birch")));
		tiles.set(55, new DeepslateTile("Deepslate"));
		tiles.set(56, new DeadTreeTile("Dead Tree"));
		tiles.set(57, new SmallTreeTile("Small Tree"));
		tiles.set(58, new HardRockTwoTile("Hard Rock II"));
		tiles.set(59, new ReedTile("Reed"));
		tiles.set(60, new ObsidianTile("Raw Obsidian"));
		tiles.set(61, new CarrotTile("Carrot"));
		tiles.set(62, new MossTile("Moss"));
		tiles.set(63, new AzaleaTile("Azalea"));
		tiles.set(64, new DeepslateOreTile("Deepslate spiky stone"));
		tiles.set(65, new DeepslateOreTileLand("Deepslate spiky stone-L"));
		tiles.set(66, new StoneOreTile("Spiky stone"));
		tiles.set(67, new StoneOreTileLand("Spiky stone-L"));
		tiles.set(68, new TreeTileFungus("Big Fungus"));
		tiles.set(69, new OreTile(OreTile.OreType.Obsidian));
		tiles.set(70, new OreTile(OreTile.OreType.IronNF));
		tiles.set(71, new OreTile(OreTile.OreType.GoldNF));
		tiles.set(72, new OreTile(OreTile.OreType.GemNF));
		tiles.set(73, new OreTile(OreTile.OreType.GemGNF));
		tiles.set(74, new OreTile(OreTile.OreType.GemBNF));
		tiles.set(75, new OreTile(OreTile.OreType.GemG));
		tiles.set(76, new OreTile(OreTile.OreType.GemB));
		tiles.set(77, new WallTile(Tile.Material.ObsidianD)); //this is for a wall
		tiles.set(78, new SmallStoneDirtTile("Small stones"));
		tiles.set(79, new SmallStoneGrassTile("Grass small stones"));
		tiles.set(80, new BeetrootTile("Beetroot"));
		tiles.set(81, new TallgrassTile("Tall grass"));
		tiles.set(82, new DesertgrassTile("Desert grass"));
		tiles.set(83, new SkygrassTile("Skygrass"));
		tiles.set(84, new SkyRockTile("Sky rock"));
		tiles.set(85, new IceTile("Ice"));
		tiles.set(86, new SnowTile("Snow"));
		tiles.set(87, new ConiferTile("Conifer"));
		tiles.set(88, new SaplingTile("Conifer sapling", Tiles.get("Grass"), Tiles.get("Conifer")));
		tiles.set(89, new ConiferSnowyTile("Snowy Conifer"));
		tiles.set(90, new SaplingTile("Snowy Conifer sapling", Tiles.get("Snow"), Tiles.get("Snowy Conifer")));
        tiles.set(91, new RockTileG("RockG"));
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
		tiles.set(102, new SaplingTile("cloud cactus Sapling", Tiles.get("Cloud"), Tiles.get("Cloud Cactus")));
        tiles.set(103, new BrambleTile("Bramble"));
		tiles.set(104, new CoarseDirtTile("Coarse dirt"));
		tiles.set(105, new CDeadTreeTile("Dead Tree C"));
		tiles.set(106, new DeepslateTileG("DeepslateG"));
		tiles.set(107, new OreBlockTile(OreBlockTile.OreType.Lapis));
		tiles.set(108, new OreBlockTile(OreBlockTile.OreType.Iron));
		tiles.set(109, new DecorTile(Tile.Material.Stone));
		tiles.set(110, new DecorTile(Tile.Material.Stone2));
		tiles.set(111, new DecorTile(Tile.Material.Obsidian));
		tiles.set(112, new DecorTile(Tile.Material.ObsidianD));
		tiles.set(113, new CloudgrassTile("Cloud tallgrass"));
		tiles.set(114, new SkyTreeTile("Sky tree"));

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
