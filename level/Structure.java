package minicraft.level;

import java.util.HashMap;
import java.util.HashSet;
import minicraft.level.Level;
import minicraft.entity.furniture.*;
import minicraft.entity.mob.*;
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
	
	public void draw(Level level, int xt, int yt) {
		for (TilePoint p: tiles)
			 level.setTile(xt+p.x, yt+p.y, p.t);

		for (Point p: furniture.keySet())
			 level.add(furniture.get(p).clone(), xt+p.x, yt+p.y, true);
	}

	public void draw(byte[] map, int xt, int yt, int mapWidth) {
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
	
	static final Structure dungeonGate;
	static final Structure dungeonLock;
	static final Structure lavaPool;
	static final Structure lavaFountain;
	static final Structure lavaSpawner;

	// All the "mobDungeon" structures are for the spawner structures
	static final Structure mobDungeonCenter;
	static final Structure mobDungeonNorth;
	static final Structure mobDungeonSouth;
	static final Structure mobDungeonEast;
	static final Structure mobDungeonWest;
	// All the "mobDungeon" structures are for the spawner structures (depth -2 edition)
	static final Structure mobDungeonCenter2;
	static final Structure mobDungeonCenter2Pillars;
	static final Structure mobDungeonNorth2;
	static final Structure mobDungeonSouth2;
	static final Structure mobDungeonEast2;
	static final Structure mobDungeonWest2;
	// All the "mobDungeon" structures are for the spawner structures (depth -3 edition)
	static final Structure mobDungeonCenter3;
	static final Structure mobDungeonNorth3;
	static final Structure mobDungeonSouth3;
	static final Structure mobDungeonEast3;
	static final Structure mobDungeonWest3;
	static final Structure dungeonOre;

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

	static final Structure stoneRuinRuinedOverlay1;
	static final Structure stoneRuinRuinedOverlay2;
	static final Structure RuinedCastleOverlay1;
	static final Structure RuinedCastleOverlay2;
	static final Structure RuinedCastleOverlay3;
	static final Structure RuinedCastleOverlay4;

	// Ok, because of the way the system works, these structures are rotated 90 degrees clockwise when placed
	// Then it's flipped on the vertical
	static {
		dungeonGate = new Structure();
		dungeonGate.setData("O:Obsidian,D:Obsidian Door,W:Obsidian Wall",
					"WWDWW\n" +
					"WOOOW\n" +
					"DOOOD\n" +
					"WOOOW\n" +
					"WWDWW"
		);
		dungeonGate.addFurniture(-1, -1, new Lantern(Lantern.Type.IRON));

		dungeonLock = new Structure();
		dungeonLock.setData("O:Obsidian,W:ObsidianD Wall",
					"WWWWW\n" +
					"WOOOW\n" +
					"WOOOW\n" +
					"WOOOW\n" +
					"WWWWW"
		);

		lavaPool = new Structure();
		lavaPool.setData("L:Lava",
					"LL\n" +
					"LL"
		);
		lavaFountain = new Structure();
		lavaFountain.setData("L:Lava,O:Obsidian,R:Raw Obsidian,W:Obsidian Wall",
				"*OORRO*\n"+
				"OLLLLLO\n"+
				"OLLLLLO\n"+
				"OLLWLLO\n"+
				"OLLLLLO\n"+
				"OLLLLLO\n"+
						"*OORRO*"
		);
		lavaSpawner = new Structure();
		lavaSpawner.setData("L:Lava,O:Obsidian,R:Raw Obsidian,W:Obsidian Wall,D:Obsidian Door",
				"WDWWWWWDW\n"+
						"OOOROOOOO\n"+
						"WLOOORRLW\n"+
						"WLRROOOLW\n"+
						"WLOOOOOLW\n"+
						"WLOOOOOLW\n"+
						"WLROOORLW\n"+
						"OOOROOOOO\n"+
						"WDWWWWWDW"
		);
		lavaSpawner.addFurniture(-3, +2, new Crafter(Crafter.Type.Workbench));
		lavaSpawner.addFurniture(1, 0, new Spawner(new KnightTop()));

		dungeonOre=new Structure();
		dungeonOre.setData("D:Dirt,O:Obsidian ore",
				"***D**D\n"+
						"*ODDDDD\n"+
						"DDDOOOD\n"+
						"O***D**\n"+
						"DDDDDO*\n"
				);
		mobDungeonCenter = new Structure();
		mobDungeonCenter.setData("B:Stone Bricks,W:Stone Wall",
					"WWBWW\n" +
					"WBBBW\n" +
					"BBBBB\n" +
					"WBBBW\n" +
					"WWBWW"
		);
		mobDungeonNorth = new Structure();
		mobDungeonNorth.setData("B:Stone Bricks,W:Stone Wall",
					"WWWWW\n" +
					"WBBBB\n" +
					"BBBBB\n" +
					"WBBBB\n" +
					"WWWWW"
		);
		mobDungeonSouth = new Structure();
		mobDungeonSouth.setData("B:Stone Bricks,W:Stone Wall",
					"WWWWW\n" +
					"BBBBW\n" +
					"BBBBB\n" +
					"BBBBW\n" +
					"WWWWW"
		);
		mobDungeonEast = new Structure();
		mobDungeonEast.setData("B:Stone Bricks,W:Stone Wall",
					"WBBBW\n" +
					"WBBBW\n" +
					"WBBBW\n" +
					"WBBBW\n" +
					"WWBWW"
		);
		mobDungeonWest = new Structure();
		mobDungeonWest.setData("B:Stone Bricks,W:Stone Wall",
					"WWBWW\n" +
					"WBBBW\n" +
					"WBBBW\n" +
					"WBBBW\n" +
					"WBBBW"
		);
		//Floor 2 dungeons will have unique look
		mobDungeonCenter2 = new Structure();
		mobDungeonCenter2.setData("B:Stone Bricks,W:Stone Wall,C:Dungeon Bricks,X:Dungeon Wall,D:Dirt",
				"XXCWW\n" +
						"XCCBW\n" +
						"BBCCC\n" +
						"WDDDW\n" +
						"WWCXX"
		);
		mobDungeonCenter2Pillars = new Structure();
		mobDungeonCenter2Pillars.setData("W:Stone Wall",
				"WW\nWW"
		);
		mobDungeonNorth2 = new Structure();
		mobDungeonNorth2.setData("B:Stone Bricks,W:Stone Wall,C:Dungeon Bricks,X:Dungeon Wall,D:Dirt",
				"WXXWC\n" +
						"XCBDB\n" +
						"CDCCC\n" +
						"WBDBW\n" +
						"XXXCW"
		);
		mobDungeonSouth2 = new Structure();
		mobDungeonSouth2.setData("B:Stone Bricks,W:Stone Wall,C:Dungeon Bricks,X:Dungeon Wall,D:Dirt",
				"DXXXW\n" +
						"CBBCW\n" +
						"CBBBD\n" +
						"BBCCW\n" +
						"WXWW*"
		);
		mobDungeonEast2 = new Structure();
		mobDungeonEast2.setData("B:Stone Bricks,W:Stone Wall,C:Dungeon Bricks,X:Dungeon Wall,D:Dirt",
				"XBDDW\n" +
						"XBCBX\n" +
						"XCDBX\n" +
						"XCCCW\n" +
						"XXBXW"
		);
		mobDungeonWest2 = new Structure();
		mobDungeonWest2.setData("B:Stone Bricks,W:Stone Wall,C:Dungeon Bricks,X:Dungeon Wall,D:Dirt",
				"WWBWW\n" +
						"WBBDX\n" +
						"WDDBX\n" +
						"XCCCX\n" +
						"WCCCW"
		);

		//Floor 3 dungeons
		mobDungeonCenter3 = new Structure();
		mobDungeonCenter3.setData("B:Stone Bricks,W:Obsidian Wall,L:Lava",
				"WWWBWWW\n" +
						"WLBBBLW\n" +
						"WBBBBBW\n" +
						"BBBBBBB\n" +
						"WBBBBBW\n" +
						"WLBBBLW\n" +
						"WWWBWWW"
		);
		mobDungeonNorth3 = new Structure();
		mobDungeonNorth3.setData("B:Stone Bricks,W:Obsidian Wall",
				"WWWWW\n" +
						"WBBBB\n" +
						"BBBBB\n" +
						"WBBBB\n" +
						"WWWWW"
		);
		mobDungeonSouth3 = new Structure();
		mobDungeonSouth3.setData("B:Stone Bricks,W:Obsidian Wall",
				"WWWWW\n" +
						"BBBBW\n" +
						"BBBBB\n" +
						"BBBBW\n" +
						"WWWWW"
		);
		mobDungeonEast3 = new Structure();
		mobDungeonEast3.setData("B:Stone Bricks,W:Obsidian Wall",
				"WBBBW\n" +
						"WBBBW\n" +
						"WBBBW\n" +
						"WBBBW\n" +
						"WWBWW"
		);
		mobDungeonWest3 = new Structure();
		mobDungeonWest3.setData("B:Stone Bricks,W:Obsidian Wall",
				"WWBWW\n" +
						"WBBBW\n" +
						"WBBBW\n" +
						"WBBBW\n" +
						"WBBBW"
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
		villageHouseNormal.setData("F:Wood Planks,W:Wood Wall,D:Wood Door,G:Grass",
					"WWWWW\n" +
					"WFFFW\n" +
					"WFFFD\n" +
					"WFFFG\n" +
					"WWWWW"
		);

		villageHouseTwoDoor = new Structure();
		villageHouseTwoDoor.setData("F:Wood Planks,W:Wood Wall,D:Wood Door,G:Grass",
					"WWWWW\n" +
					"WFFFW\n" +
					"DFFFW\n" +
					"WFFFW\n" +
					"WWDWW"
		);
		villageFarmhouseNormal = new Structure();
		villageFarmhouseNormal.setData("F:Wood Planks,W:Wood Wall,D:Wood Door,G:Grass,H:Farmland",
				"WWWWWWWW\n" +
						"WFFFDHHW\n" +
						"WFFFWHHW\n" +
						"WFFFWWWW\n" +
						"WWDWW***"
		);
		villageFarmhouseTwoDoor = new Structure();
		villageFarmhouseTwoDoor.setData("F:Wood Planks,W:Wood Wall,D:Wood Door,G:Grass,H:Farmland,I:Wheat",
				"WWWWWWWW\n" +
						"WFFFDHHW\n" +
						"DFFFWIIW\n" +
						"WFFFWWWW\n" +
						"WWDWW***"
		);
		villageStonehouseNormal = new Structure();
		villageStonehouseNormal.setData("W:Wood Wall,D:Wood Door,G:Grass,R:Rock,H:Hole,E:Dirt",
				"*R***\n" +
					"****R\n" +
						"WWWGW\n" +
						"WEEHW\n" +
						"DEEEW\n" +
						"WRRRW\n" +
						"WWWWG"
		);
		villageStonehouseTwoDoor = new Structure();
		villageStonehouseTwoDoor.setData("W:Wood Wall,D:Wood Door,G:Grass,R:Rock,H:Hole,E:Dirt",
				"****R\n" +
						"RR**R\n" +
						"WWWGW\n" +
						"WRRRW\n" +
						"DEERD\n" +
						"WHRHW\n" +
						"WWWWG"
		);

		villageRuinedOverlay1 = new Structure();
		villageRuinedOverlay1.setData("G:Grass,F:Wood Planks",
					"**FG*\n" +
					"F*GG*\n" +
					"*G**F\n" +
					"G*G**\n" +
					"***G*"
		);

		villageRuinedOverlay2 = new Structure();
		villageRuinedOverlay2.setData("G:Grass,F:Wood Planks",
					"F**G*\n" +
					"*****\n" +
					"*GG**\n" +
					"F**G*\n" +
					"*F**G"
		);
		stoneRuinHouseNormal = new Structure();
		stoneRuinHouseNormal.setData("F:Stone Bricks,W:Stone Wall,D:Stone Door,G:Grass,R:Rose,H:Small Flower,E:Dungeon Bricks,X:Dungeon Wall",
				"WWXXX\n" +
						"WHEEW\n" +
						"WHFRD\n" +
						"WFEFG\n" +
						"XXXWW"
		);

		stoneRuinHouseTwoDoor = new Structure();
		stoneRuinHouseTwoDoor.setData("F:Stone Bricks,W:Stone Wall,D:Stone Door,G:Grass,R:Sunflower,H:Small Flower,E:Dungeon Bricks,X:Dungeon Wall,Y:Dungeon Door",
				"WWDWW\n" +
						"EFFEW\n" +
						"XFRRW\n" +
						"WEEFW\n" +
						"XXYXW"
		);

		stoneRuinRuinedOverlay1 = new Structure();
		stoneRuinRuinedOverlay1.setData("G:Grass,F:Stone Bricks,H:Flower,E:Dungeon Bricks",
				"**EGE\n" +
						"F*GG*\n" +
						"*GHHF\n" +
						"G*G**\n" +
						"***G*"
		);

		stoneRuinRuinedOverlay2 = new Structure();
		stoneRuinRuinedOverlay2.setData("G:Grass,F:Stone Bricks,E:Dungeon Bricks",
				"F**G*\n" +
						"**EE*\n" +
						"*GG**\n" +
						"F**G*\n" +
						"*E**G"
		);
		stoneFountain= new Structure();
		stoneFountain.setData("W:Water,D:Dungeon Bricks,S:Stone Bricks,U:Stone Wall,T:Torch Stone Bricks,E:Dirt",
				"**DDSE*\n" +
						"DWWWWWT\n" +
						"SWWSWWD\n" +
						"SWDUDWS\n" +
						"SWWDWWD\n"+
						"EWWWWWS\n"+
						"*TSSSD*"
		);stoneFountain2= new Structure();
		stoneFountain2.setData("W:Water,D:Dungeon Bricks,S:Stone Bricks,U:Dungeon Wall,T:Torch Dungeon Bricks,E:Dirt",
				"**SSSS*\n" +
						"SWWWWWE\n" +
						"EWWSWWE\n" +
						"EWSUWWS\n" +
						"EWWWWWS\n"+
						"EWWWWWT\n"+
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
		stoneFountain4.setData("W:Water,D:Dungeon Bricks,S:Stone Bricks,U:Stone Wall,T:Torch Stone Bricks,E:Dirt",
				"**SSSE*\n" +
						"SWWWDDS\n" +
						"DWWTWWS\n" +
						"SWUUUWS\n" +
						"DWWUWWS\n"+
						"EWWWWWS\n"+
						"*TSSSS*"
		);

		RuinedCastleOverlay1 = new Structure();
		RuinedCastleOverlay1.setData("S:Stone Bricks,W:Stone Wall,P:Wood Planks,D:Wood Door,X:Wood Wall,E:Stone Door,F:Farmland,B:Birch,R:Path,T:Reed,A:Flower,C:Rose,G:Grass,I:Wheat,J:Carrot,K:Dungeon Bricks,H:Dungeon Wall",
				"****W******WGGKK\n"+
				"****WGGGGGGWPPPK\n"+
				"****GGGGGGGDPBPK\n"+
				"KPPPSSWSESSSGGGG\n"+
				"WSDHHHHHHHHHHDHH\n"+
				"TWSWGGJXRFFGWSH*\n"+
				"**KKCIFXRGGGSSH*\n"+
				"*SSSXXDXGGGRESH*\n"+
				"*KKERRGGAAGGWKH*\n"+
				"*SSKGGGRGGGGWSH*\n"+
				"*KSWGGGRGCCCWSH*\n"+
				"WWPSSGGRGGGSSDSS\n"+
				"HPPPWWWEWWWWPPPW\n"+
				"HPBPDSSKKKKDPBPW\n"+
				"W******SSKKKGGGG"
				);
		RuinedCastleOverlay1.addFurniture(2, -3, new Crafter(Crafter.Type.Workbench));
		RuinedCastleOverlay1.addFurniture(-2, +2, new Crafter(Crafter.Type.Workbench));
		RuinedCastleOverlay1.addFurniture(-1, 0, new Spawner(new KnightTop()));
		RuinedCastleOverlay1.addFurniture(1, 0, new Spawner(new KnightTopT()));
		RuinedCastleOverlay2 = new Structure();
		RuinedCastleOverlay2.setData("S:Stone Bricks,W:Stone Wall,P:Wood Planks,D:Wood Door,X:Wood Wall,E:Stone Door,F:Farmland,B:Birch,R:Path,T:Reed,A:Flower,C:Sunflower,G:Grass,H:Rock,I:Wheat,K:Dungeon Bricks,H:Dungeon Wall",
				"***********WWWWW\n"+
				"A******WW*******\n"+
				"AABADSSS***DPPPK\n"+
				"GGGBWWWWXWWWPPPH\n"+
				"GGDWWFFXRGGWWDHH\n"+
				"TWPWFITXRGGHWGH*\n"+
				"*WPWIIFXRGGGWGG*\n"+
				"*WPWXXDXGGGGXGG*\n"+
				"*WSHHGGRRABBWKW*\n"+
				"*WKWHGGRGGGGWGG*\n"+
				"*WGWGGGRGCCCWSW*\n"+
				"XXDWWGGXGGGGGD**\n"+
				"XRRPWWWXWWWWPRSK\n"+
				"XPGGDSSBKKKDPBPX\n"+
				"XRRR*****HHHRRRX\n"+
				"******TT**TXXXXX"
		);
		RuinedCastleOverlay2.addFurniture(3, -3, new Crafter(Crafter.Type.Workbench));
		RuinedCastleOverlay2.addFurniture(-3, +2, new Crafter(Crafter.Type.Workbench));
		RuinedCastleOverlay2.addFurniture(1, 0, new Spawner(new KnightTop()));
		RuinedCastleOverlay2.addFurniture(-1, 0, new Spawner(new KnightTopT()));
		RuinedCastleOverlay3 = new Structure();
		RuinedCastleOverlay3.setData("S:Stone Bricks,W:Stone Wall,P:Wood Planks,D:Wood Door,X:Wood Wall,E:Stone Door,F:Farmland,B:Birch,R:Path,T:Reed,A:Rose,C:Sunflower,G:Grass,I:Wheat,K:Dungeon Bricks,H:Dungeon Wall",
				"***********WWWW*\n"+
				"WPPPSSCCCCCSPPP*\n"+
				"WPGSDRRRRRRDPBP*\n"+
				"WSKKWWWSEWWWPPP*\n"+
				"WWDSSRRXRGGSWDS*\n"+
				"TSSSRATPRAGGWWW*\n"+
				"*SSSFIRPGAAASBB*\n"+
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
		RuinedCastleOverlay3.addFurniture(1, 0, new Spawner(new KnightTop()));
		RuinedCastleOverlay3.addFurniture(-1, 0, new Spawner(new KnightTopT()));
		RuinedCastleOverlay4 = new Structure();
		RuinedCastleOverlay4.setData("S:Stone Bricks,W:Stone Wall,P:Wood Planks,D:Wood Door,X:Wood Wall,E:Stone Door,F:Farmland,B:Birch,R:Path,T:Reed,A:Flower,C:Sunflower,G:Grass,D:Dirt,I:Wheat,J:Carrot,K:Dungeon Bricks,H:Dungeon Wall",
				"HHHHHCCCCCCHHHHH\n"+
				"WDDDWWWWWWWWGGGH\n"+
				"WDDDDPPPPPPGGGGH\n"+
				"HDDDSSSSSSSSGGGH\n"+
				"HWDSSTT*RGGWHGHH\n"+
				"TWPSTTT*RGGGWPW*\n"+
						"*HCSTJT*RGGGHPH*\n"+
						"*HAS**G*RRRREPW*\n"+
				"*HAERRRRRAGGWPW*\n"+
				"*HASGGGRGGGGWPH*\n"+
				"*WAWGGGRGCCCWPH*\n"+
				"WWGWWGGRGGISSGWW\n"+
				"HHGGSSSSWWHHRRBH\n"+
				"WGGGGPPPPPPRRRRW\n"+
				"HGGGWWWWWGWWBBBW\n"+
				"HWHWH*TT**TWWHHW"
		);
		RuinedCastleOverlay4.addFurniture(2, -3, new Crafter(Crafter.Type.Workbench));
		RuinedCastleOverlay4.addFurniture(-2, +2, new Crafter(Crafter.Type.Workbench));
		RuinedCastleOverlay4.addFurniture(1, 0, new Spawner(new KnightTop()));
		RuinedCastleOverlay4.addFurniture(-1, 0, new Spawner(new KnightTopT()));
	}
}
