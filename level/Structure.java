package minicraft.level;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import minicraft.core.World;
import minicraft.level.Level;
import minicraft.entity.furniture.*;
import minicraft.entity.mob.*;
import minicraft.entity.Entity;
import minicraft.gfx.Point;
import minicraft.item.FurnitureItem;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

// this stores structures that can be drawn at any location.
public class Structure {

	private HashSet<TilePoint> tiles;
	private HashMap<Point, Furniture> furniture;
	
	public Structure() {
		tiles = new HashSet<>();
		furniture = new HashMap<>();
	}
	public Structure(Structure struct) {
		this.tiles = struct.tiles;
		this.furniture = struct.furniture;
	}
	
	public void setTile(int x, int y, Tile tile) {
		tiles.add(new TilePoint(x, y, tile));
	}
	public void addFurniture(int x, int y, Furniture furniture) {
		this.furniture.put(new Point(x, y), furniture);
	}
	public String[] blacklist={"hard rock","obsidiand wall","obsidian stairs down","obsidian bridge support","obsidian void portal frame","obsidian stairs down","stairs down","stairs up","decorated unbreakable"};
	public void draw(Level level, int xt, int yt) {
		for (TilePoint p: tiles){
			if(Arrays.asList(blacklist).indexOf(level.getTile(xt+p.x,yt+p.y).name.toLowerCase())==-1) //if tile is not restricted
			level.setTile(xt+p.x, yt+p.y, p.t);
		}


		for (Point p: furniture.keySet())
			 level.add(furniture.get(p).clone(), xt+p.x, yt+p.y, true,Arrays.binarySearch(World.realms,level.realm));
	}

	public void draw(short[] map, int xt, int yt, int mapWidth) {
		for (TilePoint p: tiles)
			map[(xt + p.x) + (yt + p.y) * mapWidth] = p.t.id;
	}

	public void setData(String keys, String data) {
		// So, the keys are single letters, each letter represents a tile
		HashMap<String, String> keyPairs = new HashMap<>();
		String[] stringKeyPairs = keys.split(",");

		// Puts all the keys in the keyPairs HashMap
		for (int i = 0; i < stringKeyPairs.length; i++) {
			String[] thisKey = stringKeyPairs[i].split(":");
			keyPairs.put(thisKey[0], thisKey[1]);
		}

		String[] dataLines = data.split("\n");
		int width = dataLines[0].length();
		int height = dataLines.length;

		for (int i = 0; i < dataLines.length; i++) {
			for (int c = 0; c < dataLines[i].length(); c++) {
				if (dataLines[i].charAt(c) != '*') {
					Tile tile = Tiles.get(keyPairs.get(String.valueOf(dataLines[i].charAt(c))));
					this.setTile(-width / 2 + i, - height / 2 + c, tile);
				}
			}
		}
	}
	
	static class TilePoint {
		int x, y;
		Tile t;
		
		public TilePoint(int x, int y, Tile tile) {
			this.x = x;
			this.y = y;
			this.t = tile;
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof TilePoint)) return false;
			TilePoint p = (TilePoint) o;
			return x == p.x && y == p.y && t.id == p.t.id;
		}
		
		@Override
		public int hashCode() {
			return x + y * 51 + t.id * 131;
		}
	}
	static final Structure portalOBV;
	static final Structure portalOBVR;
	static final Structure dungeonGate;
	static final Structure dungeonOBVGate;
	static final Structure dungeonLock;
	static final Structure dungeonLockInside;

	static final Structure dungeonLockOBV;
	static final Structure lavaPool;
	static final Structure obsidianFountainClassic;


	// All the "mobDungeon" structures are for the spawner structures
	static final Structure mobDungeonCenter;
	static final Structure mobDungeonNorth;
	static final Structure mobDungeonSouth;
	static final Structure mobDungeonEast;
	static final Structure mobDungeonWest;
	//Ruined
	static final Structure mobDungeonCenterRuined;
	static final Structure mobDungeonNorthRuined;
	static final Structure mobDungeonSouthRuined;
	static final Structure mobDungeonEastRuined;
	static final Structure mobDungeonWestRuined;
	//Ruined 2
	static final Structure mobDungeonCenterRuined2;
	static final Structure mobDungeonNorthRuined2;
	static final Structure mobDungeonSouthRuined2;
	static final Structure mobDungeonEastRuined2;
	static final Structure mobDungeonWestRuined2;


	// All the "mobDungeon" structures are for the spawner structures (depth -3 edition)
	static final Structure mobDungeonCenter2Pillars;

	static final Structure mobDungeonCenter2;
	static final Structure mobDungeonNorth2;
	static final Structure mobDungeonSouth2;
	static final Structure mobDungeonEast2;
	static final Structure mobDungeonWest2;
	//Ruined
	static final Structure mobDungeonCenter2Ruined;
	static final Structure mobDungeonNorth2Ruined;
	static final Structure mobDungeonSouth2Ruined;
	static final Structure mobDungeonEast2Ruined;
	static final Structure mobDungeonWest2Ruined;
	//Ruined 2
	static final Structure mobDungeonCenter2Ruined2;
	static final Structure mobDungeonNorth2Ruined2;
	static final Structure mobDungeonSouth2Ruined2;
	static final Structure mobDungeonEast2Ruined2;
	static final Structure mobDungeonWest2Ruined2

			;
	// All the "mobDungeon" structures are for the spawner structures (depth -4 edition)
	static final Structure mobDungeonCenter3;
	static final Structure mobDungeonNorth3;
	static final Structure mobDungeonSouth3;
	static final Structure mobDungeonEast3;
	static final Structure mobDungeonWest3;
	//Ruined
	static final Structure mobDungeonCenter3Ruined;
	static final Structure mobDungeonNorth3Ruined;
	static final Structure mobDungeonSouth3Ruined;
	static final Structure mobDungeonEast3Ruined;
	static final Structure mobDungeonWest3Ruined;
	//Ruined 2
	static final Structure mobDungeonCenter3Ruined2;
	static final Structure mobDungeonNorth3Ruined2;
	static final Structure mobDungeonSouth3Ruined2;
	static final Structure mobDungeonEast3Ruined2;
	static final Structure mobDungeonWest3Ruined2;

	static final Structure airWizardHouse;

	// Used for random villages
	static final Structure villageHouseNormal;
	static final Structure villageHouseTwoDoor;
	static final Structure villageFarmhouseNormal;
	static final Structure villageFarmhouseTwoDoor;
	static final Structure villageStonehouseNormal;
	static final Structure villageStonehouseTwoDoor;

	static final Structure villageRuinedOverlay1;
	static final Structure villageRuinedOverlay2;

	static final Structure stoneRuinHouseNormal;
	static final Structure stoneRuinHouseTwoDoor;
	static final Structure stoneFountain;
	static final Structure stoneFountain2;
	static final Structure stoneFountain3;
	static final Structure stoneFountain4;
	static final Structure stoneFountainDry;
	static final Structure stoneFountainDry2;


	static final Structure stoneRuinRuinedOverlay1;
	static final Structure stoneRuinRuinedOverlay2;
	static final Structure RuinedCastleOverlay1;
	static final Structure RuinedCastleOverlay2;
	static final Structure RuinedCastleOverlay3;
	static final Structure RuinedCastleOverlay4;

	static final Structure mine;
	static final Structure mine2;
	static final Structure mine3;
	static final Structure mine4;

	//staircase structures
	static final Structure stairsRuinsDown;
	static final Structure stairsRuinsUp;

	static final Structure dungeonTowerEntrance;
	static final Structure dungeonTowerTop;
	static final Structure dungeonBigTowerRoom;

	static final Structure snakeLairDungeon;

	static final Structure obsidianKnightRoom;
	static final Structure villageRuinedOverlayObs1;
	static final Structure villageRuinedOverlayObs2;
	static final Structure obsidianFountain;
	static final Structure obsidianFountainRuined;
	static final Structure villageRuinedOverlayObs3;

	// Ok, because of the way the system works, these structures are rotated 90 degrees clockwise when placed
	// Then it's flipped on the vertical
	static {

		portalOBV = new Structure();
		portalOBV.setData("P:Obsidian void portal frame,I:Obsidian Bridge Support",
				"*PP*\n"+
				"PIIP\n"+
				"PIIP\n"+
				"*PP*"
		);
		portalOBVR = new Structure();
		portalOBVR.setData("P:Obsidian void portal frame,I:Obsidian void portal,O:Obsidian wall",
				"OPPO\n"+
						"PIIP\n"+
						"PIIP\n"+
						"OPPO"
		);

		dungeonGate = new Structure();
		dungeonGate.setData("O:Obsidian,D:Obsidian Door,W:Obsidian Wall",
					"WWDWW\n" +
					"WOOOW\n" +
					"DOOOD\n" +
					"WOOOW\n" +
					"WWDWW"
		);
		dungeonGate.addFurniture(-1, -1, new Lantern(Lantern.Type.IRON));

		dungeonOBVGate = new Structure();
		dungeonOBVGate.setData("O:Ornate Obsidian,D:Obsidian Door,W:Obsidian Wall,P:Decorated Obsidian,Q:Purple Wool",
				"WWWDWWW\n" +
						"WOOQOOW\n" +
						"WOPQPOW\n" +
						"DQQPQQD\n" +
						"WOPQPOW\n" +
						"WOOQOOW\n" +
						"WWWDWWW"
		);

		dungeonLock = new Structure();
		dungeonLock.setData("O:Obsidian,W:ObsidianD Wall,G:Ground rock,L:Lava,w:Obsidian wall",
					"ww**O**wO\n"+
						"ww******w\n"+
						"**WWWWW**\n" +
						"*LWOOOWL*\n" +
						"OOWOOOWGO\n" +
						"G*WOOOW*L\n" +
						"G*WWWWW**\n"+
							"OO**O**ww\n"+
							"wO**LL*ww"
		);

		dungeonLockInside = new Structure();
		dungeonLockInside.setData("O:Obsidian,W:ObsidianD Wall,B:Bramble,P:Purple Wool",
						"WWWWW\n" +
						"WBPBW\n" +
						"WPPPW\n" +
						"WBPBW\n" +
						"WWWWW"
		);
		dungeonLockOBV = new Structure();
		dungeonLockOBV.setData("O:Obsidian,W:ObsidianD Wall,G:Ground rock,L:Lava,w:Obsidian wall,D:Obsidian door",
				"ww**O**wO\n"+
						"ww******w\n"+
						"**WWDWW**\n" +
						"*LWOOOWL*\n" +
						"OODOOODGO\n" +
						"G*WOOOW*L\n" +
						"G*WWDWW**\n"+
						"OO**O**ww\n"+
						"wO**LL*ww"
		);
		dungeonTowerTop = new Structure();
		dungeonTowerTop.setData("O:Obsidian,D:Obsidian Door,W:Obsidian Wall,B:Obsidian bridge support",
				"*****BBB*****\n"+
						"*****BBB*****\n"+
						"*****BBB*****\n"+
				"***WWWDWWW***\n" +
						"***WOOOOOW***\n" +
						"BBBWOOOOOWBBB\n" +
						"BBBDOOOOODBBB\n" +
						"BBBWOOOOOWBBB\n" +
						"***WOOOOOW***\n" +
						"***WWWDWWW***\n"+
						"*****BBB*****\n"+
						"*****BBB*****\n"+
						"*****BBB*****"

		);
		dungeonBigTowerRoom = new Structure();
		dungeonBigTowerRoom.setData("O:Obsidian,D:Obsidian Door,W:Obsidian Wall,R:Ornate Obsidian,S:Decorated Obsidian",
				"WWWWWWWDWWWWWWW\n" +
						"WRRRROOROORRRRW\n" +
						"WRRROORSROORRRW\n" +
						"WRROOROSOROORRW\n" +
						"WROORSOSOOROORW\n" +
						"WOORSSSSOOOROOW\n" +
						"WOROOSRSROOOROW\n" +
						"DRSSSSSSSSSSSRD\n" +
						"WOROOORSRSOOROW\n" +
						"WOOROOOSSSSROOW\n" +
						"WROOROOSOSROORW\n" +
						"WRROOROSOROORRW\n" +
						"WRRROORSROORRRW\n" +
						"WRRRROOROORRRRW\n" +
						"WWWWWWWDWWWWWWW"
		);

		dungeonTowerEntrance = new Structure();
		dungeonTowerEntrance .setData("O:Decorated Obsidian,W:ObsidianD Wall,G:Ground rock,L:Lava,w:Obsidian wall,R:Obsidian Door,C:Red Wool,D:Orange Wool",
				"*****WWRWW*****\n"+
						"*****WOCOW*****\n"+
					"*****WODOW*****\n"+
					"*****WODOW*****\n"+
					"****WWODOWW****\n"+
					"WWWWWOOCOOWWWWW\n"+
					"WOOOOWOCOWOOOOW\n"+
					"RCDDDCCCCCDDDCR\n"+
					"WOOOOWOCOWOOOOW\n"+
					"WWWWWOOCOOWWWWW\n"+
					"****WWODOWW****\n"+
					"*****WODOW*****\n"+
					"*****WODOW*****\n"+
					"*****WOCOW*****\n"+
					"*****WWRWW*****"
		);

		lavaPool = new Structure();
		lavaPool.setData("L:Lava",
					"LL\n" +
					"LL"
		);
		obsidianFountainClassic = new Structure();
		obsidianFountainClassic.setData("L:Lava,O:Obsidian,R:Raw Obsidian,W:Obsidian Wall",
				"*OORRO*\n"+
				"OLLLLLO\n"+
				"OLLLLLO\n"+
				"OLLWLLO\n"+
				"OLLLLLO\n"+
				"OLLLLLO\n"+
						"*OORRO*"
		);
		snakeLairDungeon = new Structure();
		snakeLairDungeon.setData("D:Dirt,O:Obsidian,T:Dungeon Tallgrass",
				"**D*O\n"+
						"TTDDD\n"+
						"TTTTT\n"+
						"D*DTT\n"+
						"*TDOT"
		);
		obsidianKnightRoom = new Structure();
		obsidianKnightRoom.setData("U:Spike wall,O:Obsidian,D:Decorated Unbreakable,R:ObsidianD Door,S:Spikes",
				"****UURUU****\n"+
							"**UUUUSUUUU**\n"+
							"*UUUUSSSUUUU*\n"+
							"*UUDDDDDDDUU*\n"+
							"UUUDDDSDDDUUU\n"+
							"UUSDDDDDDDSUU\n"+
							"RSSDSDDDSDSSR\n"+
							"UUSDDDDDDDSUU\n"+
							"UUUDDDSDDDUUU\n"+
							"*UUDDDDDDDUU*\n"+
							"*UUUUSSSUUUU*\n"+
							"**UUUUSUUUU**\n"+
							"****UURUU****"
		);


		//Normal dungeon with variations
		mobDungeonCenter = new Structure();
		mobDungeonCenter.setData("B:Stone Bricks,W:Stone Wall,O:Ornate stone",
					"WWOWW\n" +
					"WBOBW\n" +
					"OOOOO\n" +
					"WBOBW\n" +
					"WWOWW"
		);
		mobDungeonCenterRuined = new Structure();
		mobDungeonCenterRuined.setData("B:Stone Bricks,W:Stone Wall,O:Ornate stone,R:Rocky stone,D:Dirt",
				"*BOBB\n" +
						"WOOOBW\n" +
						"OODDO\n" +
						"DDRB*\n" +
						"WRBWB"
		);
		mobDungeonCenterRuined2 = new Structure();
		mobDungeonCenterRuined2.setData("B:Stone Bricks,W:Stone Wall,O:Ornate stone,R:Rocky stone,C:Coarse Dirt",
				"WWRCC\n" +
						"CCBRW\n" +
						"BBOCC\n" +
						"WCCBW\n" +
						"WWRRW"
		);
		mobDungeonNorth = new Structure();
		mobDungeonNorth.setData("B:Stone Bricks,W:Stone Wall,O:Ornate stone",
					"WWWWW\n" +
					"WBBBB\n" +
					"OOOOO\n" +
					"WBBBB\n" +
					"WWWWW"
		);
		mobDungeonNorthRuined = new Structure();
		mobDungeonNorthRuined.setData("B:Stone Bricks,W:Stone Wall,O:Ornate stone,R:Rocky stone,D:Dirt",
				"WWBRW\n" +
						"***BB\n" +
						"OOODD\n" +
						"DRDB*\n" +
						"WWBBB"
		);
		mobDungeonNorthRuined2 = new Structure();
		mobDungeonNorthRuined2.setData("B:Stone Bricks,W:Stone Wall,O:Ornate stone,R:Rocky stone,C:Coarse Dirt",
				"BWRWB\n" +
						"CCBBB\n" +
						"OOOCC\n" +
						"W***R\n" +
						"WWWWW"
		);
		mobDungeonSouth = new Structure();
		mobDungeonSouth.setData("B:Stone Bricks,W:Stone Wall,O:Ornate stone",
					"WWWWW\n" +
					"BBBBW\n" +
					"OOOOO\n" +
					"BBBBW\n" +
					"WWWWW"
		);
			mobDungeonSouthRuined = new Structure();
		mobDungeonSouthRuined.setData("B:Stone Bricks,W:Stone Wall,O:Ornate stone,R:Rocky stone",
				"WWWWW\n" +
						"B**BW\n" +
						"*****\n" +
						"RROBW\n" +
						"WWWWB"
		);
		mobDungeonSouthRuined2 = new Structure();
		mobDungeonSouthRuined2.setData("B:Stone Bricks,W:Stone Wall,O:Ornate stone,R:Rocky stone,C:Coarse Dirt",
				"**CCW\n" +
						"CCCBW\n" +
						"OOCCC\n" +
						"BBBBW\n" +
						"WW***"
		);
		mobDungeonEast = new Structure();
		mobDungeonEast.setData("B:Stone Bricks,W:Stone Wall,O:Ornate stone",
					"WBOBW\n" +
					"WBOBW\n" +
					"WBOBW\n" +
					"WBOBW\n" +
					"WWOWW"
		);
		mobDungeonEastRuined = new Structure();
		mobDungeonEastRuined.setData("B:Stone Bricks,W:Stone Wall,O:Ornate stone,R:Rocky stone,D:Dirt",
				"WBO*W\n" +
						"*BO*W\n" +
						"WB***\n" +
						"**OBW\n" +
						"WWOWW"
		);
		mobDungeonEastRuined2 = new Structure();
		mobDungeonEastRuined2.setData("B:Stone Bricks,W:Stone Wall,O:Ornate stone,R:Rocky stone,C:Coarse Dirt",
				"WBOB*\n" +
						"RRORW\n" +
						"RROBR\n" +
						"WBORW\n" +
						"C*C*C"
		);
		mobDungeonWest = new Structure();
		mobDungeonWest.setData("B:Stone Bricks,W:Stone Wall,O:Ornate stone",
					"WWOWW\n" +
					"WBOBW\n" +
					"WBOBW\n" +
					"WBOBW\n" +
					"WBOBW"
		);
		mobDungeonWestRuined = new Structure();
		mobDungeonWestRuined.setData("B:Stone Bricks,W:Stone Wall,O:Ornate stone,R:Rocky Stone",
				"BBOBB\n" +
						"WBO**\n" +
						"***W\n" +
						"*****\n" +
						"**OBW"
		);
		mobDungeonWestRuined2 = new Structure();
		mobDungeonWestRuined2.setData("B:Stone Bricks,W:Stone Wall,O:Ornate stone,R:Rocky Stone",
				"WWOWW\n" +
						"WBOBW\n" +
						"WBOBW\n" +
						"WBOBW\n" +
						"WBOBW"
		);
		//Floor 2 dungeons will have unique look
		mobDungeonCenter2 = new Structure();
		mobDungeonCenter2.setData("B:Stone Bricks,W:Stone Wall,C:Dungeon Bricks,X:Dungeon Wall",
				"XXBXX\n"+
						"WBBBW\n"+
						"CBCBC\n"+
						"XBBBW\n"+
						"WWBWW\n"
		);
		mobDungeonCenter2Ruined = new Structure();
		mobDungeonCenter2Ruined.setData("B:Stone Bricks,W:Stone Wall,C:Dungeon Bricks,X:Dungeon Wall,D:dirt",
				"XXDXC\n"+
						"WDDDC\n"+
						"*B*BD\n"+
						"XBDDW\n"+
						"CW*WB\n"
		);
		mobDungeonCenter2Ruined2 = new Structure();
		mobDungeonCenter2Ruined2.setData("B:Stone Bricks,W:Stone Wall,C:Dungeon Bricks,X:Dungeon Wall",
				"***XX\n"+
						"WBBBW\n"+
						"CBC**\n"+
						"****W\n"+
						"***WW\n"
		);
		mobDungeonCenter2Pillars = new Structure();
		mobDungeonCenter2Pillars.setData("W:Stone Wall",
				"WW\nWW"
		);
		mobDungeonNorth2 = new Structure();
		mobDungeonNorth2.setData("B:Stone Bricks,W:Stone Wall,C:Dungeon Bricks,X:Dungeon Wall,D:Dirt,R:rocky stone",
				"XXXXX\n" +
						"XBBBB\n"+
						"RRRRR\n"+
						"WBBBB\n"+
						"WWWWW"
		);
		mobDungeonNorth2Ruined = new Structure();
		mobDungeonNorth2Ruined.setData("B:Stone Bricks,W:Stone Wall,C:Dungeon Bricks,X:Dungeon Wall,D:Dirt,R:rocky stone",
				"XWWDW\n" +
						"XDDDR\n"+
						"RR***\n"+
						"W***B\n"+
						"WW***"
		);
		mobDungeonNorth2Ruined2 = new Structure();
		mobDungeonNorth2Ruined2.setData("B:Stone Bricks,W:Stone Wall,C:Dungeon Bricks,X:Dungeon Wall,D:Dirt,R:rocky stone",
				"XXXXX\n" +
						"XBBBB\n"+
						"RRRRR\n"+
						"WBBBB\n"+
						"WWWWW"
		);
		mobDungeonSouth2 = new Structure();
		mobDungeonSouth2.setData("B:Stone Bricks,W:Stone Wall,C:Dungeon Bricks,X:Dungeon Wall,D:Dirt,R:Rocky stone",
				"XXWWX\n" +
						"BBBBW\n" +
						"RRRRR\n" +
						"BBBBX\n" +
						"WWXXX"
		);
		mobDungeonSouth2Ruined = new Structure();
		mobDungeonSouth2Ruined.setData("B:Stone Bricks,W:Stone Wall,C:Dungeon Bricks,X:Dungeon Wall,D:Dirt,R:Rocky stone",
				"*****\n" +
						"BDDB*\n" +
						"D*DDR\n" +
						"B**B*\n" +
						"**WWW"
		);
		mobDungeonSouth2Ruined2 = new Structure();
		mobDungeonSouth2Ruined2.setData("B:Stone Bricks,W:Stone Wall,C:Dungeon Bricks,X:Dungeon Wall,D:Dirt,R:Rocky stone",
				"X***X\n" +
						"*CCDW\n" +
						"*RRCD\n" +
						"CCB*X\n" +
						"WWX**"
		);
		mobDungeonEast2 = new Structure();
		mobDungeonEast2.setData("B:Stone Bricks,W:Stone Wall,R:rocky stone",
				"WBRBW\n" +
						"WBRBW\n" +
						"WBRBW\n" +
						"WBRBW\n" +
						"WWRWW"
		);
		mobDungeonEast2Ruined = new Structure();
		mobDungeonEast2Ruined.setData("B:Stone Bricks,W:Stone Wall,R:rocky stone,D:dirt",
				"*BRBD\n" +
						"*DRBD\n" +
						"**RBW\n" +
						"*DDBW\n" +
						"DDDWW"
		);
		mobDungeonEast2Ruined2 = new Structure();
		mobDungeonEast2Ruined2.setData("B:Stone Bricks,W:Stone Wall,R:rocky stone,D:dirt",
				"WBRDD\n" +
						"**RBB\n" +
						"DDDBB\n" +
						"WBRBD\n" +
						"***BB"
		);
		mobDungeonWest2 = new Structure();
		mobDungeonWest2.setData("B:Stone Bricks,W:Stone Wall,C:Dungeon Bricks,R:Rocky stone,D:Dirt,X:Dungeon wall",
				"WWRWW\n" +
						"WBRBW\n" +
						"XBRBX\n" +
						"XBRBX\n" +
						"WBRBW"
		);
		mobDungeonWest2Ruined = new Structure();
		mobDungeonWest2Ruined.setData("B:Stone Bricks,W:Stone Wall,C:Dungeon Bricks,X:Dungeon Wall,M:Moss,R:rocky stone",
				"WWMMM\n" +
						"MMRBW\n" +
						"WBR**\n" +
						"***BW\n" +
						"WWMWW"
		);
		mobDungeonWest2Ruined2 = new Structure();
		mobDungeonWest2Ruined2.setData("B:Stone Bricks,W:Stone Wall,C:Dungeon Bricks,X:Dungeon Wall,D:Dirt,O:Ornate stone",
				"WBO**\n" +
						"**O**\n" +
						"**O**\n" +
						"*B**\n" +
						"**O**"
		);

		//Floor 3 dungeons
		mobDungeonCenter3 = new Structure();
		mobDungeonCenter3.setData("B:Stone Bricks,W:Obsidian Wall,L:Lava,O:Ornate stone",
				"WWWOWWW\n" +
						"WLBOBLW\n" +
						"WBBOBBW\n" +
						"OOOOOOO\n" +
						"WBBOBBW\n" +
						"WLBOBLW\n" +
						"WWWOWWW"
		);
		mobDungeonCenter3Ruined = new Structure();
		mobDungeonCenter3Ruined.setData("B:Stone Bricks,W:Obsidian Wall,L:Lava,D:Dirt,O:Ornate stone,H:Hole",
				"***BBWW\n" +
						"WLBDDDW\n" +
						"WBDDDBW\n" +
						"ODDDDDO\n" +
						"WBDDDDW\n" +
						"WLBODW\n" +
						"WWWOWWW"
		);
		mobDungeonCenter3Ruined2 = new Structure();
		mobDungeonCenter3Ruined2.setData("B:Stone Bricks,W:Obsidian Wall,D:Dirt,O:Ornate stone,H:Hole",
				"WWWOWWW\n" +
						"WHBOBHW\n" +
						"WBDDBBB\n" +
						"DBBBDBD\n" +
						"DBBOBBW\n" +
						"WDBOBDW\n" +
						"WWWOWWW"
		);
		mobDungeonNorth3 = new Structure();
		mobDungeonNorth3.setData("B:Stone Bricks,W:Obsidian Wall,X:Stone wall,O:Ornate stone",
				"XXXXW\n" +
						"XBBBB\n" +
						"OOOOO\n" +
						"XBBBB\n" +
						"XXXXW"
		);
		mobDungeonNorth3Ruined= new Structure();
		mobDungeonNorth3Ruined.setData("B:Stone Bricks,W:Obsidian Wall,D:Dirt,X:Stone wall,O:Ornate stone",
				"X***W\n" +
						"XBBDD\n" +
						"OO***\n" +
						"XBB**\n" +
						"XXXX*"
		);
		mobDungeonNorth3Ruined2 = new Structure();
		mobDungeonNorth3Ruined2.setData("B:Stone Bricks,W:Obsidian Wall,D:Dirt,X:Stone wall,O:Ornate stone",
				"XXXXW\n" +
						"X***B\n" +
						"ODD*D\n" +
						"X****\n" +
						"XXXXW"
		);
		mobDungeonSouth3 = new Structure();
		mobDungeonSouth3.setData("B:Stone Bricks,W:Obsidian Wall,X:Stone wall,O:Ornate stone",
				"WXXXX\n" +
						"BBBBX\n" +
						"OOOOO\n" +
						"BBBBX\n" +
						"WXXXX"
		);
		mobDungeonSouth3Ruined = new Structure();
		mobDungeonSouth3Ruined.setData("B:Stone Bricks,W:Obsidian Wall,X:Stone wall,D:Dirt,O:Ornate stone",
				"WXXBX\n" +
						"B**BX\n" +
						"OOBBO\n" +
						"*****\n" +
						"WXXXX"
		);
		mobDungeonSouth3Ruined2 = new Structure();
		mobDungeonSouth3Ruined2.setData("B:Stone Bricks,W:Obsidian Wall,X:Stone wall,O:Ornate stone",
				"****X\n" +
						"***BB\n" +
						"*****\n" +
						"BBBB*\n" +
						"WXXXX"
		);
		mobDungeonEast3 = new Structure();
		mobDungeonEast3.setData("B:Stone Bricks,W:Obsidian Wall,X:Stone wall,O:Ornate stone",
				"WBOBW\n" +
						"XBOBX\n" +
						"XBOBX\n" +
						"XBOBX\n" +
						"XXOXX"
		);
		mobDungeonEast3Ruined = new Structure();
		mobDungeonEast3Ruined.setData("B:Stone Bricks,W:Obsidian Wall,X:Stone wall,O:Ornate stone",
				"WBOBW\n" +
						"XBOBX\n" +
						"XBOBX\n" +
						"XBOBX\n" +
						"XXOXX"
		);
		mobDungeonEast3Ruined2 = new Structure();
		mobDungeonEast3Ruined2.setData("B:Stone Bricks,W:Obsidian Wall,X:Stone wall,O:Ornate stone,D:dirt",
				"BX***\n" +
						"****B\n" +
						"*****\n" +
						"*****\n" +
						"XXO*X"
		);
		mobDungeonWest3 = new Structure();
		mobDungeonWest3.setData("B:Stone Bricks,W:Obsidian Wall,X:Stone wall,O:Ornate stone",
				"XXOXX\n" +
						"XBOBX\n" +
						"XBOBX\n" +
						"XBOBX\n" +
						"WBOBW"
		);
		mobDungeonWest3Ruined = new Structure();
		mobDungeonWest3Ruined.setData("B:Stone Bricks,W:Obsidian Wall,X:Stone wall,O:Ornate stone,D:Deepslate",
				"XXOXX\n" +
						"*BOBX\n" +
						"*BDDD\n" +
						"***DD\n" +
						"W***W"
		);
		mobDungeonWest3Ruined2 = new Structure();
		mobDungeonWest3Ruined2.setData("B:Stone Bricks,W:Obsidian Wall,X:Stone wall,O:Ornate stone,C:Coarse dirt",
				"XBOXX\n" +
						"XBCCC\n" +
						"***BC\n" +
						"BBOCB\n" +
						"WCC*W"
		);
		airWizardHouse = new Structure();
		airWizardHouse.setData("F:Wood Planks,W:Wood Wall,D:Wood Door",
					"WWWWWWW\n" +
					"WFFFFFW\n" +
					"DFFFFFW\n" +
					"WFFFFFW\n" +
					"WWWWWWW"
		);

		airWizardHouse.addFurniture(-2, 0, new Lantern(Lantern.Type.GOLD));
		airWizardHouse.addFurniture(0, 0, new Crafter(Crafter.Type.Enchanter));

		villageHouseNormal = new Structure();
		villageHouseNormal.setData("F:Wood Planks,W:Wood Wall,D:Wood Door",
					"WWWWW\n" +
					"WFFFW\n" +
					"WFFFD\n" +
					"WFFFW\n" +
					"WWWWW"
		);

		villageHouseTwoDoor = new Structure();
		villageHouseTwoDoor.setData("F:Wood Planks,W:Wood Wall,D:Wood Door",
					"WWWWW\n" +
					"WFFFW\n" +
					"DFFFW\n" +
					"WFFFW\n" +
					"WWDWW"
		);
		villageFarmhouseNormal = new Structure();
		villageFarmhouseNormal.setData("F:Wood Planks,W:Wood Wall,D:Wood Door,H:Farmland",
				"WWWWWWWW\n" +
						"WFFFDHHW\n" +
						"WFFFWHHW\n" +
						"WFFFWWWW\n" +
						"WWDWW***"
		);
		villageFarmhouseTwoDoor = new Structure();
		villageFarmhouseTwoDoor.setData("F:Wood Planks,W:Wood Wall,D:Wood Door,H:Farmland,I:Wheat",
				"WWWWWWWW\n" +
						"WFFFDHHW\n" +
						"DFFFWIIW\n" +
						"WFFFWWWW\n" +
						"WWDWW***"
		);
		villageStonehouseNormal = new Structure();
		villageStonehouseNormal.setData("W:Stone Wall,D:Stone Door,R:Rock,H:Hole,E:Dirt",
				"*R***\n" +
					"****R\n" +
						"WWWWW\n" +
						"WEEHW\n" +
						"DEEEW\n" +
						"WRRRW\n" +
						"WWWW*"
		);
		villageStonehouseTwoDoor = new Structure();
		villageStonehouseTwoDoor.setData("W:Stone Wall,D:Stone Door,R:Rock,H:Hole,E:Dirt",
				"****R\n" +
						"RR**R\n" +
						"WWW*W\n" +
						"WRRRW\n" +
						"DEERD\n" +
						"WHRHW\n" +
						"WWWW*"
		);

		villageRuinedOverlay1 = new Structure();
		villageRuinedOverlay1.setData("F:Wood Planks,B:Bramble,W:Wood Wall",
					"**W**\n" +
					"F*FF*\n" +
					"*F**F\n" +
					"**F*B\n" +
					"B****"
		);

		villageRuinedOverlay2 = new Structure();
		villageRuinedOverlay2.setData("F:Wood Planks,B:Bramble",
					"F****\n" +
					"**FF*\n" +
					"*FF*B\n" +
					"FFFFF\n" +
					"*F***"
		);
		villageRuinedOverlayObs1 = new Structure();
		villageRuinedOverlayObs1.setData("F:Decorated obsidian,B:Bramble,W:Obsidian Wall",
				"**W**\n" +
						"F*FFW\n" +
						"*F**F\n" +
						"**F*B\n" +
						"B***W"
		);
		obsidianFountain= new Structure();
		obsidianFountain.setData("L:Lava,O:Ornate obsidian,U:Obsidian wall",
				"*OOOOO*\n" +
						"OLLLLLO\n" +
						"OLLULLO\n" +
						"OLUUULO\n" +
						"OLLULLO\n"+
						"OLLLLLO\n"+
						"*OOOOO*"
		);
		obsidianFountainRuined= new Structure();
		obsidianFountainRuined.setData("L:Lava,D:Ornate obsidian,S:Obsidian,U:Obsidian wall,T:Fungus,E:Dirt",
				"**D**E*\n" +
						"**LLLLT\n" +
						"*LLDD**\n" +
						"DLUUULD\n" +
						"DLLDL**\n"+
						"EL*****\n"+
						"*TSS***"
		);
		villageRuinedOverlayObs2 = new Structure();
		villageRuinedOverlayObs2.setData("F:Obsidian,B:Bramble,W:Obsidian wall",
				"WW***\n" +
						"**FF*\n" +
						"*FF*B\n" +
						"FBFFF\n" +
						"*FWWW"
		);
		villageRuinedOverlayObs3 = new Structure();
		villageRuinedOverlayObs3.setData("F:Decorated Obsidian,B:Bramble,W:Obsidian wall",
				"BW*FF\n" +
						"W*F**\n" +
						"***FB\n" +
						"FBFF*\n" +
						"*FWW*"
		);
		stoneRuinHouseNormal = new Structure();
		stoneRuinHouseNormal.setData("F:Stone Bricks,W:Stone Wall,D:Stone Door,E:Dungeon Bricks,X:Dungeon Wall,O:Ornate stone",
				"WWXXX\n" +
						"W*EEW\n" +
						"W*O*D\n" +
						"WFEF*\n" +
						"XXXWW"
		);

		stoneRuinHouseTwoDoor = new Structure();
		stoneRuinHouseTwoDoor.setData("F:Stone Bricks,W:Stone Wall,D:Stone Door,G:Grass,E:Dungeon Bricks,X:Dungeon Wall,Y:Dungeon Door,D:Rocky stone",
				"WWDWW\n" +
						"EDDEW\n" +
						"XF**W\n" +
						"WEEFW\n" +
						"XXYXW"
		);

		stoneRuinRuinedOverlay1 = new Structure();
		stoneRuinRuinedOverlay1.setData("F:Stone Bricks,E:Dungeon Bricks,B:Bramble,R:Rocky stone,O:Ornate stone",
				"**E*E\n" +
						"F***R\n" +
						"RROOF\n" +
						"*****\n" +
						"****B"
		);

		stoneRuinRuinedOverlay2 = new Structure();
		stoneRuinRuinedOverlay2.setData("F:Stone Bricks,E:Dungeon Bricks,B:Bramble,O:Ornate stone,R:Rocky stone",
				"F**F*\n" +
						"B*EER\n" +
						"*FF**\n" +
						"OO*F*\n" +
						"*E**F"
		);
		stoneFountain= new Structure();
		stoneFountain.setData("W:Water,D:Dungeon Bricks,S:Stone Bricks,U:Stone Wall,T:Torch Stone Bricks,E:Dirt,O:Ornate stone,R:Rocky stone",
				"**DRRE*\n" +
						"DWWWWWT\n" +
						"SWWSWWD\n" +
						"OWDUDWO\n" +
						"OWWDWWD\n"+
						"EWWWWWS\n"+
						"*TSSSD*"
		);stoneFountain2= new Structure();
		stoneFountain2.setData("W:Water,D:Dungeon Bricks,S:Stone Bricks,U:Dungeon Wall,T:Torch Dungeon Bricks,E:Dirt,O:Ornate stone,R:Rocky stone",
				"**SSSS*\n" +
						"SWWWWWE\n" +
						"EWWSWWE\n" +
						"EWSUWWR\n" +
						"RWWWWWO\n"+
						"ODWWWWT\n"+
						"*DSSST*"
		);stoneFountain3= new Structure();
		stoneFountain3.setData("W:Water,D:Dungeon Bricks,S:Stone Bricks,U:Stone Wall,T:Torch Stone Bricks,E:Dirt",
				"**EETS*\n" +
						"SWWWWWD\n" +
						"EWWSWWD\n" +
						"DWESDWS\n" +
						"SWWDWWD\n"+
						"EWWWWWU\n"+
						"*ESSTD*"
		);stoneFountain4= new Structure();
		stoneFountain4.setData("W:Water,D:Dungeon Bricks,S:Stone Bricks,U:Stone Wall,T:Torch Stone Bricks,E:Dirt,B:Bramble",
				"**SSSE*\n" +
						"SWWWDDS\n" +
						"DWWTWWS\n" +
						"SWUUUWB\n" +
						"DWWUWWB\n"+
						"EWWWWWS\n"+
						"*TSSSS*"
		);
        stoneFountainDry= new Structure();
        stoneFountainDry.setData("D:Dungeon Bricks,S:Stone Bricks,U:Stone Wall,E:Dirt,B:Bramble",
                "**SSSE*\n" +
                        "SEEEDDS\n" +
                        "DEBUEES\n" +
                        "SEUUEES\n" +
                        "DEEUEES\n"+
                        "EEEEEEB\n"+
                        "*SSSSS*"
        );
        stoneFountainDry2= new Structure();
        stoneFountainDry2.setData("D:Dungeon Bricks,S:Stone Bricks,U:Stone Wall,E:Dirt,T:Torch Stone Bricks",
                "**SS*E*\n" +
                        "S***DDS\n" +
                        "D**T**S\n" +
                        "SEUUU*S\n" +
                        "D**U**S\n"+
                        "E****ES\n"+
                        "*TSS***"
        );


		RuinedCastleOverlay1 = new Structure();
		RuinedCastleOverlay1.setData("S:Stone Bricks,W:Stone Wall,P:Wood Planks,D:Wood Door,X:Wood Wall,E:Stone Door,F:Farmland,R:Path,T:Reed,G:Grass,I:Wheat,J:Carrot,K:Dungeon Bricks,H:Dungeon Wall",
				"****W******W**KK\n"+
				"****W******WPPPK\n"+
				"***********DP*PK\n"+
				"KPPPSSWSESSS****\n"+
				"WSD*HHHHHHHHHDHH\n"+
				"TWSWGGJXRFFGWSH*\n"+
				"**KK**FXRGGGSSH*\n"+
				"*SSSXXDXGGGRESH*\n"+
				"*KKERRGG**GGWKH*\n"+
				"*SSKGGGRGGGGWSH*\n"+
				"*KSWGGGR****WSH*\n"+
				"WWPSSGGRGGGSSDSS\n"+
				"H**PWWWEWWWWPPPW\n"+
				"HP*PDSSKKKKDP*PW\n"+
				"W******SSKKK****"
				);
		RuinedCastleOverlay1.addFurniture(2, -3, new Crafter(Crafter.Type.Workbench));
		RuinedCastleOverlay1.addFurniture(-2, +2, new Crafter(Crafter.Type.Workbench));
		RuinedCastleOverlay1.addFurniture(-1, 0, new Spawner(new KnightTop(1)));
		RuinedCastleOverlay1.addFurniture(1, 0, new Spawner(new KnightTop(2)));
		RuinedCastleOverlay2 = new Structure();
		RuinedCastleOverlay2.setData("S:Stone Bricks,W:Stone Wall,P:Wood Planks,D:Wood Door,X:Wood Wall,E:Stone Door,F:Farmland,B:Birch,R:Path,T:Reed,A:Flower,C:Sunflower,G:Grass,H:Rock,I:Wheat,K:Dungeon Bricks,H:Dungeon Wall",
				"***********WWWWW\n"+
				"*******WW*******\n"+
				"**B*DSSS***DPPPK\n"+
				"***BWWWWXWWWPPPH\n"+
				"**DWW**XR**WWDHH\n"+
				"TWPWFITXRGGHW*H*\n"+
				"*WPWIIFXRGGGW***\n"+
				"*WPWXXDXGGGGX***\n"+
				"*WSHHGGRR*BBWKW*\n"+
				"*WKWHGGRGGGGW***\n"+
				"*WGWGGGRGCCCWSW*\n"+
				"XXDWWGGX*****D**\n"+
				"XRRPWWWXWWWWPRSK\n"+
				"XP**DSSBKKKDP**X\n"+
				"XRRR*****HHHRRRX\n"+
				"******TT**TXXXXX"
		);
		RuinedCastleOverlay2.addFurniture(3, -3, new Crafter(Crafter.Type.Workbench));
		RuinedCastleOverlay2.addFurniture(-3, +2, new Crafter(Crafter.Type.Workbench));
		RuinedCastleOverlay2.addFurniture(1, 0, new Spawner(new KnightTop(1)));
		RuinedCastleOverlay2.addFurniture(-1, 0, new Spawner(new KnightTop(2)));
		RuinedCastleOverlay3 = new Structure();
		RuinedCastleOverlay3.setData("S:Stone Bricks,W:Stone Wall,P:Wood Planks,X:Wood Wall,E:Stone Door,F:Farmland,B:Birch,R:Path,T:Reed,A:Rose,C:Sunflower,G:Grass,I:Wheat,K:Dungeon Bricks,H:Dungeon Wall,D:Wood Door",
				"***********WWWW*\n"+
				"WPPPSSCCCCCSPPP*\n"+
				"WPGSDRRRRRRDPBP*\n"+
				"WSKKWWWSEWWWPPP*\n"+
				"WWDSSRRXRGGSWDS*\n"+
				"TSSSR*TPR*GGWWW*\n"+
				"*SSSFIRPG***SBB*\n"+
				"*SSEXXXXGGGGGBB*\n"+
				"*ESSGGGGGGBBSSB*\n"+
				"*KKKGGGRGGGGSSB*\n"+
				"*KKKGGGRGCCCSSH*\n"+
				"SKDSSGGTTTTSSDH*\n"+
				"WPPPSSSESSSSPPP*\n"+
				"WGGGDGGGGGGPBP*\n"+
				"*PPPGGGGGGPPPK\n"+
				"****************\n"
		);
		RuinedCastleOverlay3.addFurniture(1, +2, new Crafter(Crafter.Type.Workbench));
		RuinedCastleOverlay3.addFurniture(-1, -2, new Crafter(Crafter.Type.Workbench));
		RuinedCastleOverlay3.addFurniture(1, 0, new Spawner(new KnightTop(1)));
		RuinedCastleOverlay3.addFurniture(-1, 0, new Spawner(new KnightTop(2)));
		RuinedCastleOverlay4 = new Structure();
		RuinedCastleOverlay4.setData("S:Stone Bricks,W:Stone Wall,P:Wood Planks,X:Wood Wall,E:Stone Door,F:Farmland,B:Birch,R:Path,T:Reed,A:Flower,C:Sunflower,G:Grass,D:Dirt,I:Wheat,J:Carrot,K:Dungeon Bricks,H:Dungeon Wall",
				"HHHHH******HHHHH\n"+
				"W***WWWWWWWW***H\n"+
				"W****PPPP******H\n"+
				"H***SSSSSSSS***H\n"+
				"HWDSSTT*RGGWHGHH\n"+
				"TWPSTTT*RGGGWPW*\n"+
						"*HCSTJT*RGGGHPH*\n"+ "*H*S**G*RRRREPW*\n"+
				"*H*ERRRRR*GGWPW*\n"+
				"*H*SGGGRGGGGWPH*\n"+
				"*W*WGGGRGCCCWPH*\n"+
				"WWGWWGGRGGISSGWW\n"+
				"HHGGSSSSWWHHRRBH\n"+
				"WGGGGPPPPPPRRRRW\n"+
				"HGGGWWWWWGWWBBBW\n"+
				"HWHWH*TT**TWWHHW"
		);
		RuinedCastleOverlay4.addFurniture(2, -3, new Crafter(Crafter.Type.Workbench));
		RuinedCastleOverlay4.addFurniture(-2, +2, new Crafter(Crafter.Type.Workbench));
		RuinedCastleOverlay4.addFurniture(1, 0, new Spawner(new KnightTop(1)));
		RuinedCastleOverlay4.addFurniture(-1, 0, new Spawner(new KnightTop(2)));

		mine= new Structure();
		mine.setData("T:Torch Wood Planks,P:Wood Planks,W:Wood Wall,D:Wood Door",
				"*******\n" +
						"WTPP**W\n"+
						"D*PWP**\n"+
						"W**PPTW\n"+
						"WWPPPPW"
		);
		mine2= new Structure();
		mine2.setData("T:Torch Wood Planks,P:Wood Planks,W:Wood Wall,D:Wood Door",
				"P*PPWWW\n" +
						"W*PPPPW\n"+
						"D*WWW*P\n"+
						"WPPPPTW\n"+
						"**WWWPP"
		);
		mine3= new Structure();
		mine3.setData("T:Torch Wood Planks,P:Wood Planks,W:Wood Wall,D:Wood Door",
				"WWW****\n" +
						"PTPP***\n"+
						"DPPWP**\n"+
						"WPPP**W\n"+
						"WT*WW*P"
		);
		mine4= new Structure();
		mine4.setData("T:Torch Wood Planks,P:Wood Planks,W:Wood Wall,D:Wood Door",
				"WWPP*PP\n" +
						"WTPP*PW\n"+
						"*PP*PP*\n"+
						"PPPPPPW\n"+
						"PPPPWWW"
		);
		stairsRuinsDown = new Structure();
		stairsRuinsDown.setData("B:Stone Bricks,W:Stone Wall,O:Ornate stone,S:Stairs Down",
				"WWOWW\n" +
						"WBOBW\n" +
						"OOSOO\n" +
						"WBOBW\n" +
						"WWOWW"
		);
		stairsRuinsUp = new Structure();
		stairsRuinsUp.setData("B:Stone Bricks,W:Stone Wall,O:Ornate stone,S:Stairs Up",
				"WWBWW\n" +
						"WOBOW\n" +
						"BBSBB\n" +
						"WOBOW\n" +
						"WWBWW"
		);
	}
}
