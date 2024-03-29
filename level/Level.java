package minicraft.level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import minicraft.core.*;
import minicraft.core.io.Settings;
import minicraft.entity.ClientTickable;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.Spark;
import minicraft.entity.furniture.*;
import minicraft.entity.mob.*;
import minicraft.entity.particle.Particle;
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.level.tile.StairsTile;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.level.tile.TorchTile;
import org.jetbrains.annotations.Nullable;
import sun.tools.jconsole.JConsole;


public class Level {
	private Random random = new Random();
	//private static final String[] levelNames = {"Sky", "Surface", "Iron","Transit-Iron/Gold" "Gold","Transit-Gold/Lava", "Lava", "Dungeon"}; upcoming
	private static final String[] levelNames = {"Sky", "Surface", "Iron","Iron/Gold","Gold", "Gold/Lava ","Lava", "Dungeon"};
	private static final String[] obvLevelNames = {"Dungeon Bridges","Surface","Dungeon F1","Dungeon F2",};

	private static String nameMatrix[][] = {levelNames, obvLevelNames};

	public static String getLevelName(int depth) { return getLevelName(depth, 0); }
	public static String getLevelName(int depth,int realm) { return nameMatrix[realm][-1 * depth + 1]; }

	
	public static String getDepthString(int depth,String realm) { return "Level("+realm+") " + (depth < 0 ? "B" + (-depth) : depth); }

	public static String getDepthString(int depth) { return  getDepthString(depth,"Overworld"); } //OVERWORLD by default
	
	private static final int MOB_SPAWN_FACTOR = 100; // The chance of a mob actually trying to spawn when trySpawn is called equals: mobCount / maxMobCount * MOB_SPAWN_FACTOR. so, it basically equals the chance, 1/number, of a mob spawning when the mob cap is reached. I hope that makes sense...

	public int w, h; // Width and height of the level
	private long seed; // The used seed that was used to generate the world
	
	public short[] tiles; // An array of all the tiles in the world.
	public short[] data; // An array of the data of the tiles in the world.
	
	public final int depth; // Depth level of the level
	public int monsterDensity = 16; // Affects the number of monsters that are on the level, bigger the number the less monsters spawn.
	public int maxMobCount;

	public String realm; //realm determines to which floor list we assign a level
	public int chestCount;
	public int mimicCount;
	public int mobCount = 0;
	int stairStructCoordx =0;
	int stairStructCoordy =0;

	/**
	 * I will be using this lock to avoid concurrency exceptions in entities and sparks set
	 */
	private final Object entityLock = new Object();
	private Set<Entity> entities = java.util.Collections.synchronizedSet(new HashSet<>()); // A list of all the entities in the world
	private Set<Spark> sparks = java.util.Collections.synchronizedSet(new HashSet<>()); // A list of all the sparks in the world
	private Set<Player> players = java.util.Collections.synchronizedSet(new HashSet<>()); // A list of all the players in the world
	private List<Entity> entitiesToAdd = new ArrayList<>(); /// entities that will be added to the level on next tick are stored here. This is for the sake of multithreading optimization. (hopefully)
	private List<Entity> entitiesToRemove = new ArrayList<>(); /// entities that will be removed from the level on next tick are stored here. This is for the sake of multithreading optimization. (hopefully)
	
	// Creates a sorter for all the entities to be rendered.
	private static Comparator<Entity> spriteSorter = Comparator.comparingInt(new ToIntFunction<Entity>() {
		@Override
		public int applyAsInt(Entity e) { return e.y; }
	});
	
	public Entity[] getEntitiesToSave() {
		Entity[] allEntities = new Entity[entities.size() + sparks.size() + entitiesToAdd.size()];
		Entity[] toAdd = entitiesToAdd.toArray(new Entity[entitiesToAdd.size()]);
		Entity[] current = getEntityArray();
		System.arraycopy(current, 0, allEntities, 0, current.length);
		System.arraycopy(toAdd, 0, allEntities, current.length, toAdd.length);
		
		return allEntities;
	}
	
	/// This is a solely debug method I made, to make printing repetitive stuff easier.
	// Should be changed to accept prepend and entity, or a tile (as an Object). It will get the coordinates and class name from the object, and will divide coords by 16 if passed an entity.
	public void printLevelLoc(String prefix, int x, int y) { printLevelLoc(prefix, x, y, ""); }
	public void printLevelLoc(String prefix, int x, int y, String suffix) {
		String levelName = getLevelName(depth);
		
		System.out.println(prefix + " on " + levelName + " level (" + x + "," + y + ")" + suffix);
	}
	
	public void printTileLocs(Tile t) {
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++)
				if (getTile(x, y).id == t.id)
					printLevelLoc(t.name, x, y);
	}
	public void printEntityLocs(Class<? extends Entity> c) {
		int numfound = 0;
		for (Entity entity: getEntityArray()) {
			if (c.isAssignableFrom(entity.getClass())) {
				printLevelLoc(entity.toString(), entity.x >> 4, entity.y >> 4);
				numfound++;
			}
		}
		
		System.out.println("Found " + numfound + " entities in level of depth " + depth);
	}
	
	public void updateMobCap() {
		if(realm=="overworld") {
			maxMobCount = 150 + 150 * Settings.getIdx("diff");
			if (depth == 1) {
				maxMobCount /= 2;
				maxMobCount += (Updater.isbloody ? 100 : 0);
			}
			if (depth == 0) maxMobCount = (maxMobCount + (Updater.isbloody ? 100 : 0)) * 2 / 3;
		}else maxMobCount = 240;
	}

	public Level(int w, int h, long seed, int level, Level parentLevel, boolean makeWorld,String realm) {
		this.realm=realm;
		depth = level;
		this.w = w;
		this.h = h;
		this.seed = seed;
		short[][] maps; // Multidimensional array (an array within a array), used for the map

		if ( realm=="overworld" &&  level != -6 && level != 0)
			monsterDensity =  8;


		if (realm=="dungeon realm")
			monsterDensity = 10;
	
		updateMobCap();
		
		if(!makeWorld) {
			int arrsize = w * h;
			tiles = new short[arrsize];
			data = new short[arrsize];
			return;
		}
		
		if (Game.debug) System.out.println("Making level " + level + "... for realm:" + realm);
		switch(realm) {
			default:maps = LevelGen.createAndValidateMap(w, h, level);break;
			case "dungeon realm":maps = LevelGenObV.createAndValidateMap(w,h ,level);break;
		}
		if (maps == null) {
			System.err.println("Level Gen ERROR: Returned maps array is null");
			return;
		}
		
		tiles = maps[0]; // Assigns the tiles in the map
		data = maps[1]; // Assigns the data of the tiles

		switch(realm) {
			case "overworld":	if (level <= -1 && level >= -3) generateMines();
			if (level < 0)
				generateSpawnerStructures();

			if (level == -3 || level == -4)
				//actually fountains
				generateVillages(null,new String[]{"azalea","bramble","deepslate","deepslateg","fungus spores","fungus tree","fungus","ground rock","lily pad","moss","reed","small fungus tree","stone ore","water"},new Structure[]{Structure.stoneFountain,Structure.stoneRuinHouseTwoDoor,Structure.stoneFountain,Structure.stoneFountain2,Structure.stoneFountain3,Structure.stoneFountain4,Structure.stoneRuinHouseNormal},5,5,1.75f); //we can even generate old fountain ruins and do even more
			if (level >= -4 && level <= -1)
				generateStairStructures();
			if (level == 0) {
				generateVillages(new String[]{"water","lava"},new String[]{"coarse dirt","dirt","fern","flower","grass","reed","small flower","small rose","small stones","sunflower","tallgrass","tree"}, new Structure[]{Structure.stoneRuinHouseNormal,Structure.stoneRuinHouseTwoDoor,Structure.stoneFountainDry,Structure.villageFarmhouseTwoDoor,Structure.villageFarmhouseNormal,Structure.villageHouseNormal,Structure.villageHouseTwoDoor},5,"villagehouse");
				generateCastle();
			}break;
			case "dungeon realm":break;
		}

		switch(realm) {
			case "overworld":overworld(level, parentLevel);break;
			case "dungeon realm":obv(level, parentLevel);break;
		}
	}

	public Level(int w, int h, int level, Level parentLevel, boolean makeWorld) {
		this(w, h, 0, level, parentLevel, makeWorld,"overworld"); //Overworld by default
	}

	public Level(int w, int h, int level, Level parentLevel, boolean makeWorld,String realm) {
		this(w, h, 0, level, parentLevel, makeWorld,realm); //Support other realms
	}

	/** Level which the world is contained in */
	public Level(int w, int h, int level, Level parentLevel) {
		this(w, h, level, parentLevel, true);
	}

	public long getSeed() {
		return seed;
	}

	public void checkAirWizard() {
		checkAirWizard(true);
	}
	private void checkAirWizard(boolean check) {
		if (depth == 1 && (!AirWizard.beaten )){ // Add the airwizard to the surface
			
			boolean found = false;
			if (check) {
				for (Entity e: entitiesToAdd)
					if (e instanceof AirWizard )
						found = true;
				for (Entity e: entities)
					if (e instanceof AirWizard )
						found = true;
			}
			if(AirWizard.beaten==true)found=true;
			if (!found) {
				AirWizard aw = new AirWizard(false);
				add(aw, w/2, h/2, true);
			}
		}
	}

	private void checkMimics(int min,int max) {
		if(this.mimicCount<10 * (w/128)) {
			if ((depth <= -1 && this.realm == "dungeon realm") || (depth == -6 && this.realm == "overworld")) { // Basically if this is dungeon floor
				int numChests = 0;
				for (int i = numChests; i < 10 * (w / 128) / 2; i++) {
					MimicChest d = new MimicChest(random.nextInt(max - min + 1) + min);
					boolean addedchest = false;
					while (!addedchest) { // Keep running until we successfully add a Mimic

						// Pick a random tile:
						int x2 = random.nextInt(w - ((w / 128) * 12)) + ((w / 128) * 12);
						int y2 = random.nextInt(h - ((h / 128) * 12)) + ((h / 128) * 12);
						if (getTile(x2, y2) == Tiles.get("Obsidian") || getTile(x2, y2) == Tiles.get("dirt") || getTile(x2, y2) == Tiles.get("raw Obsidian") || getTile(x2, y2) == Tiles.get("Dungeon tallgrass")) {
							add(d, (x2 * 16) + 8, (y2 * 16) + 8, Arrays.asList(World.realms).indexOf(this.realm));
							this.mimicCount++;
							addedchest = true;
						}
					}
				}
			}
		}
	}
	
	public void checkChestCount() {
		checkChestCount(true);
	}

	private void checkChestCount(boolean check) {
		// If the level is the dungeon, and we're not just loading the world...
		if (depth != -6 && realm == "overworld") return;
		if (depth<0 && realm == "dungeon realm") return;

		int numChests = 0;

		if (check) {
			for (Entity e : entitiesToAdd)
				if (e instanceof DungeonChest)
					numChests++;
			for (Entity e : entities)
				if (e instanceof DungeonChest)
					numChests++;
		}
		String[] allowed = {"DIRT","DUNGEON TALLGRASS","FUNGUS","OBSIDIAN","ORNATE OBSIDIAN","RAW OBSIDIAN"};
		/// Make DungeonChests!
		for (int i = numChests; i < 10 * (w / 128); i++) {
			DungeonChest d = new DungeonChest(true);
			if(i%10==0)d = new DungeonChest(true,false,true);
			d.setRealmId(Arrays.binarySearch(World.realms,this.realm));
			boolean addedchest = false;

			while (!addedchest) { // Keep running until we successfully add a DungeonChest

				// Pick a random tile:
				int x2 = random.nextInt(16 * w) / 16;
				int y2 = random.nextInt(16 * h) / 16;
				if(Arrays.binarySearch(allowed,getTile(x2,y2).name)>-1) {
					d.x = x2 * 16 + 8;
					d.y = y2 * 16 + 8;
					d.setRealmId(Arrays.asList(World.realms).indexOf(this.realm));
					add(d, Arrays.asList(World.realms).indexOf(this.realm));
					this.chestCount++;
					addedchest = true;
				}
				}
		}
	}

	private void tickEntity(Entity entity) {
		if (entity == null) return;

		if (Game.hasConnectedClients() && entity instanceof Player && !(entity instanceof RemotePlayer)) {
			if (Game.debug)
				System.out.println("SERVER is removing regular player " + entity + " from level " + this);
			entity.remove();
		}
		
		if (Game.isValidServer() && entity instanceof Particle) {
			// There is no need to track this.
			if (Game.debug)
				System.out.println("SERVER warning: Found particle in entity list: " + entity + ". Removing from level " + this);
			entity.remove();
		}

		if (entity.isRemoved()) {
			remove(entity);
			return;
		}

		if (entity != Game.player) { // Player is ticked separately, others are ticked on server
			if (!Game.isValidClient())
				entity.tick(); /// The main entity tick call.
			else if (entity instanceof ClientTickable)
				((ClientTickable) entity).clientTick();
		}

		if (entity.isRemoved() || entity.getLevel() != this) {
			remove(entity);
			return;
		}

		if (Game.hasConnectedClients()) // This means it's a server
			Game.server.broadcastEntityUpdate(entity);
	}

	public void tick(boolean fullTick) {
		int count = 0;
		if(depth==-6 && realm=="overworld"){checkChestCount(true);if(random.nextInt(300)==0)checkMimics(1,2);}
		if(depth<0 && realm=="dungeon realm"){if(random.nextInt(300)==0)checkMimics(0 + (-depth),2 + (-depth));checkChestCount(true);}
		if(Updater.tickCount%100==0)updateMobCap();
		while (entitiesToAdd.size() > 0) {
			Entity entity = entitiesToAdd.get(0);
			boolean inLevel = entities.contains(entity);
			
			if (!inLevel) {
				if (Game.isValidServer())
					Game.server.broadcastEntityAddition(entity, true);
				
				if (!Game.isValidServer() || !(entity instanceof Particle)) {
					if (Game.debug) printEntityStatus("Adding ", entity, "furniture.DungeonChest", "mob.AirWizard", "mob.Player");
					
					synchronized (entityLock) {
						if (entity instanceof Spark) {
							sparks.add((Spark) entity);
						} else {
							entities.add(entity);
							if (entity instanceof Player) {
								players.add((Player) entity);
							}
						}
					}
				}
			}

			entitiesToAdd.remove(entity);
		}
		
		if(fullTick && (!Game.isValidServer() || getPlayers().length > 0)) {
			// This prevents any entity (or tile) tick action from happening on a server level with no players.
			
			if (!Game.isValidClient()) {
				for (int i = 0; i < w * h / 50; i++) {
					int xt = random.nextInt(w);
					int yt = random.nextInt(w);
					boolean notableTick = getTile(xt, yt).tick(this, xt, yt);
					if (Game.isValidServer() && notableTick)
						Game.server.broadcastTileUpdate(this, xt, yt);
				}
			}
			
			// Entity loop
			for (Entity e : entities) {
				tickEntity(e);
				if (e instanceof Mob) count++;
			}

			// Spark loop
			sparks.forEach(this::tickEntity);
		}
		
		while (count > maxMobCount) {
			Entity removeThis = (Entity)entities.toArray()[(random.nextInt(entities.size()))];
			if (removeThis instanceof MobAi) {
				// Make sure there aren't any close players
				boolean playerClose = false;
				for (Player player : players) {
					if (Math.abs(player.x - removeThis.x) < 128 && Math.abs(player.y - removeThis.x) < 76) {
						playerClose = true;
						break;
					}
				}

				if (!playerClose) {
					remove(removeThis);
					count--;
				}
			}
		}

		while (entitiesToRemove.size() > 0) {
			Entity entity = entitiesToRemove.get(0);
			
			if (Game.isValidServer() && !(entity instanceof Particle) && entity.getLevel() == this)
				Game.server.broadcastEntityRemoval(entity, this, true);
			
			if (Game.debug) printEntityStatus("Removing ", entity, "mob.Player");
			
			entity.remove(this); // This will safely fail if the entity's level doesn't match this one.
			synchronized (entityLock) {
				if (entity instanceof Spark) {
					sparks.remove(entity);
				} else {
					entities.remove(entity);
				}
			}
			
			if (entity instanceof Player)
				players.remove(entity);
			entitiesToRemove.remove(entity);
		}
		

		mobCount = count;
		
		if (Game.isValidServer() && players.size() == 0)
			return; // Don't try to spawn any mobs when there's no player on the level, on a server.
		
		if (fullTick && count < maxMobCount && !Game.isValidClient())
			trySpawn();
	}
	
	public void printEntityStatus(String entityMessage, Entity entity, String... searching) {
		// "searching" can contain any number of class names I want to print when found.
		String clazz = entity.getClass().getCanonicalName();
		clazz = clazz.substring(clazz.lastIndexOf(".")+1);
		for (String search: searching) {
			try {
				if (Class.forName("minicraft.entity." + search).isAssignableFrom(entity.getClass())) {
					if (clazz.equals("AirWizard")) clazz += ((AirWizard)entity).secondform ? " II" : "";
					printLevelLoc(Network.onlinePrefix() + entityMessage + clazz, entity.x >> 4, entity.y >> 4, ": " + entity);
					break;
				}
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void dropItem(int x, int y, int mincount, int maxcount, Item... items) {
		dropItem(x, y, mincount+random.nextInt(maxcount - mincount + 1), items);
	}
	public void dropItem(int x, int y, int count, Item... items) {
		for (int i = 0; i < count; i++)
			 dropItem(x, y, items);
	}
	public void dropItem(int x, int y, Item... items) {
		for (Item i: items)
			 dropItem(x, y, i, Renderer.player.getRealmId());
	}
	public ItemEntity dropItem(int x, int y, Item i,int realm) {

		if (Game.isValidClient())
			System.out.println("Dropping item on client: " + i);
		
		int ranx, rany;
		
		do {
			ranx = x + random.nextInt(11) - 5;
			rany = y + random.nextInt(11) - 5;
		} while (ranx >> 4 != x >> 4 || rany >> 4 != y >> 4);
		ItemEntity ie = new ItemEntity(i, ranx, rany);
		ie.setRealmId(realm);
		add(ie);
		return ie;
	}

	public void renderBackground(Screen screen, int xScroll, int yScroll) {
		int xo = xScroll >> 4; // Latches to the nearest tile coordinate
		int yo = yScroll >> 4;
		int w = (Screen.w) >> 4; // There used to be a "+15" as in below method
		int h = (Screen.h) >> 4;
		screen.setOffset(xScroll, yScroll);
		for (int y = yo; y <= h + yo; y++) {
			for (int x = xo; x <= w + xo; x++) {
				getTile(x, y).render(screen, this, x, y);
			}
		}
		screen.setOffset(0, 0);
	}
	
	public void renderSprites(Screen screen, int xScroll, int yScroll,boolean justVisual) {
		int xo = xScroll >> 4; // Latches to the nearest tile coordinate
		int yo = yScroll >> 4;
		int w = (Screen.w + 15) >> 4;
		int h = (Screen.h + 15) >> 4;
		
		screen.setOffset(xScroll, yScroll);
		sortAndRender(screen, getEntitiesInTiles(xo, yo, xo + w, yo + h),justVisual);
		
		screen.setOffset(0, 0);
	}

	public void renderLight(Screen screen, int xScroll, int yScroll, int brightness) {
		int xo = xScroll >> 4;
		int yo = yScroll >> 4;
		int w = (Screen.w + 15) >> 4;
		int h = (Screen.h + 15) >> 4;

		screen.setOffset(xScroll, yScroll);
		int r = 4;
		
		List<Entity> entities = getEntitiesInTiles(xo - r, yo - r, w + xo + r, h + yo + r);
		for (Entity e: entities) {
			int lr = e.getLightRadius();
			if (lr > 0) screen.renderLight(e.x - 1, e.y - 4, lr * brightness);
		}
		
		for (int y = yo - r; y <= h + yo + r; y++) {
			for (int x = xo - r; x <= w + xo + r; x++) {
				if (x < 0 || y < 0 || x >= this.w || y >= this.h) continue;
				
				int lr = getTile(x, y).getLightRadius(this, x, y);
				if (lr > 0) screen.renderLight(x * 16 + 8, y * 16 + 8, lr * brightness);
			}
		}
		screen.setOffset(0, 0);
	}
	
	private void sortAndRender(Screen screen, List<Entity> list,boolean visualOnly) {
		list.sort(spriteSorter);
		for (Entity e : list) {
            if(visualOnly) {
                if (e instanceof Furniture)
                    e.render(screen); //we can freely render furnitures without any problems
            }else{
                if (e.getLevel() == this && !e.isRemoved())
                    e.render(screen);
                else
                    remove(e);
            }
		}
	}
	
	public Tile getTile(int x, int y) {
		if (x < 0 || y < 0 || x >= w || y >= h /* || (x + y * w) >= tiles.length*/ ) return Tiles.get("connector tile");
		int id = tiles[x + y * w];
		if(id < 0) id += 256;
		return Tiles.get(id);
	}
	
	public void setTile(int x, int y, String tilewithdata) {
		if (!tilewithdata.contains("_")) {
			setTile(x, y, Tiles.get(tilewithdata));
			return;
		}
		String name = tilewithdata.substring(0, tilewithdata.indexOf("_"));
		int data = Tiles.get(name).getData(tilewithdata.substring(name.length()+1));
		setTile(x, y, Tiles.get(name), data);
	}
	
	public void setTile(int x, int y, Tile t) {
		setTile(x, y, t, t.getDefaultData());
	}
	
	public void setTile(int x, int y, Tile t, int dataVal) {
		if (x < 0 || y < 0 || x >= w || y >= h) return;
		
		if (Game.isValidClient() && !Game.isValidServer()) {
			System.out.println("Client requested a tile update for the " + t.name + " tile at " + x + "," + y);
		} else {
			tiles[x + y * w] = t.id;
			data[x + y * w] = (short) dataVal;
		}
		
		if(Game.isValidServer())
			Game.server.broadcastTileUpdate(this, x, y);
	}
	
	public int getData(int x, int y) {
		if (x < 0 || y < 0 || x >= w || y >= h) return 0;
		//return data[x + y * w] & 0xff //why so? We only limit ourself with that;
		return data[x + y * w]; //allows for negative data
	}
	/*public int[] getDataA(int x,int y){
		if (x < 0 || y < 0 || x >= w || y >= h) return new int[]{0};
		//return data[x + y * w] & 0xff //why so? We only limit ourself with that;
		return new int[] {data[x + y * w]};
	}*/

	public void setData(int x, int y, int val) {
		if (x < 0 || y < 0 || x >= w || y >= h) return;
		data[x + y * w] = (short) val;
	}
	
	public void add(Entity e) { if(e==null) return; add(e, e.x, e.y); }
	public void add(Entity e,int realm) { if(e==null) return; add(e, e.x, e.y,realm); }
	public void add(Entity entity, int x, int y) { add(entity, x, y, false); }
	public void add(Entity entity, int x, int y, boolean tileCoords) {
		add(entity, x, y, tileCoords,0); //Overworld by default
	}
	public void add(Entity entity, int x, int y,int realm) {
		add(entity, x, y, false,realm); //Nontile coords by default
	}
	public void add(Entity entity, int x, int y, boolean tileCoords,int realm) {
		if(entity == null) return;
		if(tileCoords) {
			x = x * 16 + 8;
			y = y * 16 + 8;
		}
		entity.setLevel(this, x, y);
		entity.setRealmId(realm);
		entitiesToRemove.remove(entity); // To make sure the most recent request is satisfied.
		if (!entitiesToAdd.contains(entity))
			entitiesToAdd.add(entity);
	}
	
	public void remove(Entity e) {
		entitiesToAdd.remove(e);
		if (!entitiesToRemove.contains(e))
			entitiesToRemove.add(e);
	}
	
	private void trySpawn() {

		int spawnSkipChance = (int) (Updater.isbloody ? 1 : ((MOB_SPAWN_FACTOR * Math.pow(mobCount, 2)) / Math.pow(maxMobCount,2)));
		if (spawnSkipChance > 0 && random.nextInt(spawnSkipChance) != 0)
			return; // Hopefully will make mobs spawn a lot slower.
		
		boolean spawned = false;
		for (int i = 0; i < (Updater.isbloody && depth==0 ? 82 : (depth==-1 || depth == 0 ? 40 : 80)) && !spawned; i++) { //more spawn attempts during bloodmoon. Bloodmoon affects surface and above
			int minLevel = 1, maxLevel = 1;
			switch(depth){
				case 1:minLevel=3+(Updater.isbloody ? 1 : 0);maxLevel=4;break;
				case 0:minLevel=1+NightWizard.revenge+(Updater.isbloody && Updater.tickCount>=40800 ? 1 : 0);maxLevel=1+NightWizard.revenge+(Updater.isbloody && Updater.tickCount>=40800 ? 1 : 0);break;
				case -1:minLevel=1;maxLevel=2;break;
				case -2:minLevel=2;maxLevel=2;break;
				case -3:minLevel=1;maxLevel=3;break;
				case -4:minLevel=2;maxLevel=3;break;
				case -5:minLevel=2;maxLevel=4;break;
				case -6:minLevel=1;maxLevel=4;break;
			}
			
			
			int lvl = random.nextInt(maxLevel - minLevel + 1) + minLevel;
			int rnd = random.nextInt(100);
			int nx = random.nextInt(w) * 16 + 8, ny = random.nextInt(h) * 16 + 8;

			//System.out.println("trySpawn on level " + depth + " of lvl " + lvl + " mob w/ rand " + rnd + " at tile " + nx + "," + ny);

			// Spawns the enemy mobs; first part prevents enemy mob spawn on surface on first day, more or less.
			if (realm.contains("overworld")) {
			if ((Updater.tickCount >= (Updater.isbloody ? 39600 : 42800) && !Updater.pastDay1 || depth != 0) && EnemyMob.checkStartPos(this, nx, ny)) { // if night or underground, with a valid tile, spawn an enemy mob.
				double chance = Math.random();

				if (depth != -6) { // Normal mobs

					if (rnd < 2)
						add((new Wraith(Math.random() < 0.05 * (Game.isMode("hardcore") ? 2 : 1) && Updater.isbloody && depth >= 0 && Updater.tickCount >= 40800 ? 5 : lvl, 0)), nx, ny);
					else if (rnd >= 2 && rnd <= 40)
						add((new Slime(Math.random() < 0.05 * (Game.isMode("hardcore") ? 2 : 1) && Updater.isbloody && depth >= 0 && Updater.tickCount >= 40800 ? 5 : lvl)), nx, ny);
					else if (rnd <= 75)
						add((new Zombie(Math.random() < 0.05 * (Game.isMode("hardcore") ? 2 : 1) && Updater.isbloody && depth >= 0 && Updater.tickCount >= 40800 ? 5 : lvl)), nx, ny);
					else if (rnd >= 85 && chance > 0.012)
						add((new Skeleton(Math.random() < 0.05 * (Game.isMode("hardcore") ? 2 : 1) && Updater.isbloody && depth >= 0 && Updater.tickCount >= 40800 ? 5 : lvl)), nx, ny);
					else if (rnd >= 85 && chance <= 0.012 && depth == -3) add((new AncSkeleton(lvl)), nx, ny);
					else if (depth < 1)
						add((new Creeper(Math.random() < 0.05 * (Game.isMode("hardcore") ? 2 : 1) && Updater.isbloody && depth >= 0 ? 5 : lvl)), nx, ny);
					if (depth < 0 && chance < 0.04) add((new Ghost()), nx, ny);
					else if (depth >= 0 && chance < 0.09 && Updater.getTime() == Updater.Time.Night)
						add((new Ghost()), nx, ny);
				} else { // Special dungeon mobs
					if (Updater.tickCount % 16000 == 0 && this.mimicCount < 5 * (w / 128)) {
						this.mimicCount++;
						add((new MimicChest(-(depth + 1) + 1 + random.nextInt(2),Math.random()<0.05)), nx, ny, 0);
					}
					if (rnd <= 30) add((new FireSage(lvl)), nx, ny);
					else if (rnd >= 30 && rnd <= 40) add((new Snake(lvl, false)), nx, ny);
					else if (rnd <= 75) add((new Knight(lvl)), nx, ny);
					else if (rnd <= 80) add((new Wraith(4, 0)), nx, ny);
					else if (rnd >= 85) add((new Snake(lvl, false)), nx, ny);
					else add((new Knight(lvl)), nx, ny);
					if (chance < 0.04) add((new Ghost()), nx, ny);

				}
				//if(depth==1)if (rnd <= 75) add((new Zombie(lvl)), nx, ny);
				spawned = true;
			}

				if (depth == 0 && PassiveMob.checkStartPos(this, nx, ny) && i < 31) {
					// Spawns the friendly mobs.
					if (rnd <= (Updater.getTime() == Updater.Time.Night ? 22 : 33)) add((new Cow()), nx, ny);
					else if (rnd >= 68) add((new Pig()), nx, ny);
					else add((new Sheep()), nx, ny);

					spawned = true;
				}
				if (depth == 1) {
					int ran = random.nextInt(200);
					if (mobCount < 75 && ran == 14) add((new Clallay()), nx, ny);
					spawned = true;

				}
			}else  if (realm.contains( "dungeon")){
				switch(depth){
					case 1:minLevel=2;maxLevel=5;break;
					case 0:minLevel=1;maxLevel=4;break;
					case -1:minLevel=2;maxLevel=4;break;
					case -2:minLevel=3;maxLevel=5;break;
				}
				lvl = random.nextInt(maxLevel - minLevel + 1) + minLevel;
				 rnd = random.nextInt(310);
				 nx = random.nextInt(w) * 16 + 8;ny = random.nextInt(h) * 16 + 8;
				 if( EnemyMob.checkStartPos(this, nx, ny)){
					 if(rnd <=20)add((new AncSkeleton(2)),nx,ny,1);
			else if (rnd < 50)add((new FireSage(lvl)),nx,ny,1);
				else if (rnd >= 50 && rnd <= 80) add((new Snake(lvl,false)), nx, ny,1);
				else if(rnd > 80 & rnd<= 210) add((new Knight(  lvl)), nx, ny,1);
				//here for obv
				if(Updater.tickCount%16000==0 && this.mimicCount < 5 * (w/128)  && depth < 0) {
					this.mimicCount++;
					add((new MimicChest(-(depth + 1) + 1 + random.nextInt(2))), nx, ny, 1);
				}
				}
			}
		}
	}

	public void removeAllEnemies() {
		for (Entity e: getEntityArray()) {
			if (e instanceof EnemyMob)
				if (!(e instanceof AirWizard) && !(e instanceof NightWizard) || Game.isMode("creative")) // Don't remove the airwizard bosses! Unless in creative, since you can spawn more.
					e.remove();
		}
	}
	
	public void clearEntities() {
		if (!Game.ISONLINE)
			entities.clear();
		else
			for (Entity e: getEntityArray())
				e.remove();
	}
	
	public Entity[] getEntityArray() {
		Entity[] entityArray;
		int index = 0;

		synchronized (entityLock) {
			entityArray = new Entity[entities.size() + sparks.size()];

			for (Entity entity : entities) {
				entityArray[index++] = entity;
			}
			for (Spark spark : sparks) {
				entityArray[index++] = spark;
			}
		}

		return entityArray;
	}
	
	public List<Entity> getEntitiesInTiles(int xt, int yt, int radius) { return getEntitiesInTiles(xt, yt, radius, false); }
	
	@SafeVarargs
	public final List<Entity> getEntitiesInTiles(int xt, int yt, int radius, boolean includeGiven, Class<? extends Entity>... entityClasses) { return getEntitiesInTiles(xt-radius, yt-radius, xt+radius, yt+radius, includeGiven, entityClasses); }

	/**
	 * Get entities in a certain area on the level.
	 * @param xt0 Left
	 * @param yt0 Top
	 * @param xt1 Right
	 * @param yt1 Bottom
	 */
	public List<Entity> getEntitiesInTiles(int xt0, int yt0, int xt1, int yt1) { return getEntitiesInTiles(xt0, yt0, xt1, yt1, false); }

	/**
	 * Get entities in a certain area on the level, and filter them by class.
	 * @param xt0 Left
	 * @param yt0 Top
	 * @param xt1 Right
	 * @param yt1 Bottom
	 * @param includeGiven If we should accept entities that match the provided entityClasses. If false, we ignore the provided entityClasses.
	 * @param entityClasses Entities to accept.
	 * @return A list of entities in the area.
	 */
	@SafeVarargs
	public final List<Entity> getEntitiesInTiles(int xt0, int yt0, int xt1, int yt1, boolean includeGiven, Class<? extends Entity>... entityClasses) {
		List<Entity> contained = new ArrayList<>();
		for (Entity e: getEntityArray()) {
			int xt = e.x >> 4;
			int yt = e.y >> 4;

			// Check if entity is in area.
			if (xt >= xt0 && xt <= xt1 && yt >= yt0 && yt <= yt1) {
				boolean matches = false;

				// Look through all entity classes to see if they match the current entity we are at.
				for (int i = 0; !matches && i < entityClasses.length; i++)
					// If the current entity and an entity class match.
					matches = entityClasses[i].isAssignableFrom(e.getClass());

				// Add if the current entity matches an entity class and includeGiven is true.
				// If includeGiven is false, add if it doesn't match.
				if (matches == includeGiven)
					contained.add(e);
			}
		}
		
		return contained;
	}
	
	public List<Entity> getEntitiesInRect(Rectangle area) {
		List<Entity> result = new ArrayList<>();
		for (Entity e: getEntityArray()) {
			if (e.isTouching(area))
				result.add(e);
		}
		return result;
	}

	public List<Entity> getEntitiesInRect(Predicate<Entity> filter, Rectangle area) {
		List<Entity> result = new LinkedList<>();
		for (Entity entity : entities) {
			if (filter.test(entity) && entity.isTouching(area)) {
				result.add(entity);
			}
		}
		return result;
	}
	
	/// Finds all entities that are an instance of the given entity.
	public Entity[] getEntitiesOfClass(Class<? extends Entity> targetClass) {
		ArrayList<Entity> matches = new ArrayList<>();
		for (Entity e: getEntityArray()) {
			if (targetClass.isAssignableFrom(e.getClass()))
				matches.add(e);
		}
		
		return matches.toArray(new Entity[0]);
	}
	
	public Player[] getPlayers() {
		return players.toArray(new Player[players.size()]);
	}
	
	public Player getClosestPlayer(int x, int y) {
		Player[] players = getPlayers();
		if (players.length == 0)
			return null;
		
		Player closest = players[0];
		int xd = closest.x - x;
		int yd = closest.y - y;
		for (int i = 1; i < players.length; i++) {
			 int curxd = players[i].x - x;
			 int curyd = players[i].y - y;
			 if(xd*xd + yd*yd > curxd*curxd + curyd*curyd) {
				closest = players[i];
				xd = curxd;
				yd = curyd;
			}
		}
		
		return closest;
	}
	
	public Point[] getAreaTilePositions(int x, int y, int r) { return getAreaTilePositions(x, y, r, r); }
	public Point[] getAreaTilePositions(int x, int y, int rx, int ry) {
		ArrayList<Point> local = new ArrayList<>();
		for (int yp = y-ry; yp <= y+ry; yp++)
			for (int xp = x-rx; xp <= x+rx; xp++)
				if (xp >= 0 && xp < w && yp >= 0 && yp < h)
					local.add(new Point(xp, yp));
		return local.toArray(new Point[local.size()]);
	}
	
	public Tile[] getAreaTiles(int x, int y, int r) { return getAreaTiles(x, y, r, r); }
	public Tile[] getAreaTiles(int x, int y, int rx, int ry) {
		ArrayList<Tile> local = new ArrayList<>();
		
		for (Point p: getAreaTilePositions(x, y, rx, ry))
			 local.add(getTile(p.x, p.y));
		
		return local.toArray(new Tile[local.size()]);
	}
	
	public void setAreaTiles(int xt, int yt, int r, Tile tile, int data) { setAreaTiles(xt, yt, r, tile, data, false); }
	public void setAreaTiles(int xt, int yt, int r, Tile tile, int data, boolean overwriteStairs) {
		for(int y = yt - r; y <= yt + r; y++) {
			for (int x = xt - r; x <= xt + r; x++) {
				if(overwriteStairs || (!getTile(x, y).name.toLowerCase().contains("stairs")))
					setTile(x, y, tile, data);
			}
		}
	}

	public void setAreaTiles(int xt, int yt, int r, Tile tile, int data, String[] blacklist) {
		for (int y = yt - r; y <= yt + r; y++) {
			for (int x = xt - r; x <= xt + r; x++) {
				if (!Arrays.asList(blacklist).contains(getTile(x, y).name.toLowerCase()))
					setTile(x, y, tile, data);
			}
		}
	}
	String[] bl={ "hard rock","hard rock II", "obsidian wall","obsidiand wall","obsidian stairs up","stairs up","obsidian stairs down","stairs down"};
	public void blow(int xt, int yt, int r, Tile tile, int data) {
		for (int y = yt - r; y <= yt + r; y++) {
			for (int x = xt - r; x <= xt + r; x++) {
				if (!Arrays.asList(bl).contains(getTile(x, y).name.toLowerCase()))
					setTile(x, y, tile, data);
			}
		}
	}
	@FunctionalInterface
	public interface TileCheck {
		boolean check(Tile t, int x, int y);
	}
	
	public List<Point> getMatchingTiles(Tile search) { return getMatchingTiles((t, x, y) -> t.equals(search)); }
	public List<Point> getMatchingTiles(Tile... search) {
		return getMatchingTiles((t, x, y) -> {
			for (Tile poss: search)
				if (t.equals(poss))
					return true;
			return false;
		});
	}
	public List<Point> getMatchingTiles(TileCheck condition) {
		List<Point> matches = new ArrayList<>();
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++)
				if (condition.check(getTile(x, y), x, y))
					matches.add(new Point(x, y));
		
		return matches;
	}
	
	public boolean isLight(int x, int y) {
		for (Tile t: getAreaTiles(x, y, 3))
			if (t instanceof TorchTile)
				return true;
		
		return false;
	}
	
	private boolean noStairs(int x, int y) {
		return getTile(x, y) != Tiles.get("Stairs Down");
	}

	private void generateStairStructures() {
		int xx = random.nextInt(w - 20) + 10;
		int yy = random.nextInt(h - 20) + 10;
		for (int i = 0; i < w / 128;i++) {
			while ((getTile(xx, yy).name.contains("ROCK") || getTile(xx, yy).name.contains("ORE"))) {
				xx = random.nextInt(w - 20) + 10;
				yy = random.nextInt(h - 20) + 10;
				stairStructCoordx = xx;stairStructCoordy = yy;
			}

			Structure.stairsRuinsDown.draw(this, xx, yy);

		}
	}

	private void generateSpawnerStructures() {
		for (int i = 0; i < 18 / -depth * (w / 128); i++) {
			if(depth>-5 && depth<1) {
				/// For generating spawner dungeons
				MobAi m;
				int r = random.nextInt(5);

				if (r == 1) {
					if (depth <= -4)
						m = new Skeleton(3);
					else if (depth != -3) m = new Skeleton(-depth);
					else m = new AncSkeleton(1);
				} else if (r == 2 || r == 0) {
					if (depth <= -4) m = new Slime(3);
					else if (depth == -3) m = new Slime(2);
					else m = new Slime(-depth);
				} else {
					if (depth <= -4) m = new Zombie(3);
					else if (depth == -3) m = new Zombie(2);
					else m = new Zombie(-depth);
				}
				Spawner sp = new Spawner(m);
				int x3 = random.nextInt(16 * w) / 16;
				int y3 = random.nextInt(16 * h) / 16;
				if (getTile(x3, y3) != Tiles.get("Rock") && getTile(x3, y3) != Tiles.get("RockG") && getTile(x3, y3) != Tiles.get("Lava") && getTile(x3, y3) != Tiles.get("Water") && !getTile(x3,y3).name.toLowerCase().contains("ore")) {
					boolean xaxis2 = random.nextBoolean();

					if (xaxis2) {
						for (int s2 = x3; s2 < w - s2; s2++) {
							if (getTile(s2, y3) == Tiles.get("rock") || getTile(s2, x3) == Tiles.get("rockG")) {
								sp.x = s2 * 16 - 24;
								sp.y = y3 * 16 - 24;
							}
						}
					} else {
						for (int s2 = y3; s2 < y3 - s2; s2++) {
							if (getTile(x3, s2) == Tiles.get("rock") || getTile(x3, s2) == Tiles.get("rockG")) {
								sp.x = x3 * 16 - 24;
								sp.y = s2 * 16 - 24;
							}
						}
					}

					if (sp.x == 0 && sp.y == 0) {
						sp.x = x3 * 16 - 8;
						sp.y = y3 * 16 - 8;
					}

					if (getTile(sp.x / 16, sp.y / 16) == Tiles.get("rock") || getTile(sp.x / 16, sp.y / 16) == Tiles.get("rockG")) {
						setTile(sp.x / 16, sp.y / 16, Tiles.get("dirt"));
					}
					if (depth == -3) {
						int type = random.nextInt(7);
						if (r != 1) {

							switch (type) {
								case 0:
									Structure.mobDungeonCenter.draw(this, sp.x / 16, sp.y / 16);
									break;
								case 1:
									Structure.mobDungeonCenterRuined.draw(this, sp.x / 16, sp.y / 16);
									break;
								case 2:
									Structure.mobDungeonCenter2.draw(this, sp.x / 16, sp.y / 16);
									break;
								case 3:
									Structure.mobDungeonCenter2Ruined.draw(this, sp.x / 16, sp.y / 16);
									break;
								case 4:
									Structure.mobDungeonCenter2Ruined2.draw(this, sp.x / 16, sp.y / 16);
									break;
								case 5:
									Structure.mobDungeonCenterRuined2.draw(this, sp.x / 16, sp.y / 16);
									break;
								case 6:
									Structure.mobDungeonCenterRuined2.draw(this, sp.x / 16, sp.y / 16);
									break;
							}
						} else {
							Structure.mobDungeonCenter2Pillars.draw(this, (sp.x / 16) - 5, (sp.y / 16) - 5);
							Structure.mobDungeonCenter2Pillars.draw(this, (sp.x / 16) + 5, (sp.y / 16) - 5);
							Structure.mobDungeonCenter2Pillars.draw(this, (sp.x / 16) - 5, (sp.y / 16) + 5);
							Structure.mobDungeonCenter2Pillars.draw(this, (sp.x / 16) + 5, (sp.y / 16) + 5);
							//for ancient skeletons dungeons will fully generate with all corridors
							Structure.mobDungeonCenter2.draw(this, sp.x / 16, sp.y / 16);
							Structure.mobDungeonNorth2.draw(this, sp.x / 16, sp.y / 16 - 5);
							Structure.mobDungeonSouth2.draw(this, sp.x / 16, sp.y / 16 + 5);
							Structure.mobDungeonEast2.draw(this, sp.x / 16 + 5, sp.y / 16);
							Structure.mobDungeonWest2.draw(this, sp.x / 16 - 5, sp.y / 16);
						}
						if (getTile(sp.x / 16, sp.y / 16 - 4) != Tiles.get("Rock") && getTile(sp.x / 16, sp.y / 16 - 4) != Tiles.get("RockG")) {
							type = random.nextInt(3);
							switch (type) {
								case 0:
									Structure.mobDungeonNorth2.draw(this, sp.x / 16, sp.y / 16 - 5);
									break;
								case 1:
									Structure.mobDungeonNorth2Ruined.draw(this, sp.x / 16, sp.y / 16 - 5);
									break;
								case 2:
									Structure.mobDungeonNorth2Ruined2.draw(this, sp.x / 16, sp.y / 16 - 5);
									break;
							}
						}
						if (getTile(sp.x / 16, sp.y / 16 + 4) != Tiles.get("Rock") && getTile(sp.x / 16, sp.y / 16 + 4) != Tiles.get("RockG")) {
							type = random.nextInt(3);
							switch (type) {
								case 0:
									Structure.mobDungeonSouth2.draw(this, sp.x / 16, sp.y / 16 + 5);
									break;
								case 1:
									Structure.mobDungeonSouth2Ruined.draw(this, sp.x / 16, sp.y / 16 + 5);
									break;
								case 2:
									Structure.mobDungeonSouth2Ruined2.draw(this, sp.x / 16, sp.y / 16 + 5);
									break;
							}
						}
						if (getTile(sp.x / 16 + 4, sp.y / 16) != Tiles.get("Rock") && getTile(sp.x / 16 + 4, sp.y / 16) != Tiles.get("RockG")) {
							type = random.nextInt(3);
							switch (type) {
								case 0:
									Structure.mobDungeonEast2.draw(this, sp.x / 16 + 5, sp.y / 16);
									break;
								case 1:
									Structure.mobDungeonEast2Ruined.draw(this, sp.x / 16 + 5, sp.y / 16);
									break;
								case 2:
									Structure.mobDungeonEast2Ruined2.draw(this, sp.x / 16 + 5, sp.y / 16);
									break;
							}
						}
						if (getTile(sp.x / 16 - 4, sp.y / 16) != Tiles.get("Rock") && getTile(sp.x / 16 - 4, sp.y / 16) != Tiles.get("RockG")) {
							type = random.nextInt(3);
							switch (type) {
								case 0:
									Structure.mobDungeonWest2.draw(this, sp.x / 16 - 5, sp.y / 16);
									break;
								case 1:
									Structure.mobDungeonWest2Ruined.draw(this, sp.x / 16 - 5, sp.y / 16);
									break;
								case 2:
									Structure.mobDungeonWest2Ruined2.draw(this, sp.x / 16 - 5, sp.y / 16);
									break;
							}
						}
					} else if (depth <= -4 && depth>= -5) {
						int type = random.nextInt(3);

						switch (type) {
							case 0:
								Structure.mobDungeonCenter3.draw(this, sp.x / 16, sp.y / 16);
								break;
							case 1:
								Structure.mobDungeonCenter3Ruined.draw(this, sp.x / 16, sp.y / 16);
								break;
							case 2:
								Structure.mobDungeonCenter3Ruined2.draw(this, sp.x / 16, sp.y / 16);
								break;
						}
						if (getTile(sp.x / 16, sp.y / 16 - 4) == Tiles.get("dirt") || getTile(sp.x / 16, sp.y / 16 - 4) == Tiles.get("coarse dirt")) {
							type = random.nextInt(3);
							switch (type) {
								case 0:
									Structure.mobDungeonNorth3.draw(this, sp.x / 16, sp.y / 16 - 6);
									break;
								case 1:
									Structure.mobDungeonNorth3Ruined.draw(this, sp.x / 16, sp.y / 16 - 6);
									break;
								case 2:
									Structure.mobDungeonNorth3Ruined2.draw(this, sp.x / 16, sp.y / 16 - 6);
									break;
							}
						}
						if (getTile(sp.x / 16, sp.y / 16 + 4) == Tiles.get("dirt") || getTile(sp.x / 16, sp.y / 16 + 4) == Tiles.get("coarse dirt")) {
							type = random.nextInt(3);
							switch (type) {
								case 0:
									Structure.mobDungeonSouth3.draw(this, sp.x / 16, sp.y / 16 + 6);
									break;
								case 1:
									Structure.mobDungeonSouth3Ruined.draw(this, sp.x / 16, sp.y / 16 + 6);
									break;
								case 2:
									Structure.mobDungeonSouth3Ruined2.draw(this, sp.x / 16, sp.y / 16 + 6);
									break;
							}
						}
						if (getTile(sp.x / 16 + 4, sp.y / 16) == Tiles.get("dirt") || getTile(sp.x / 16 + 4, sp.y / 16) == Tiles.get("coarse dirt")) {
							type = random.nextInt(3);
							switch (type) {
								case 0:
									Structure.mobDungeonEast3.draw(this, sp.x / 16 + 6, sp.y / 16);
									break;
								case 1:
									Structure.mobDungeonEast3Ruined.draw(this, sp.x / 16 + 6, sp.y / 16);
									break;
								case 2:
									Structure.mobDungeonEast3Ruined2.draw(this, sp.x / 16 + 6, sp.y / 16);
									break;
							}
						}
						if (getTile(sp.x / 16 - 4, sp.y / 16) == Tiles.get("dirt") || getTile(sp.x / 16 - 4, sp.y / 16) == Tiles.get("coarse dirt")) {
							type = random.nextInt(3);
							switch (type) {
								case 0:
									Structure.mobDungeonWest3.draw(this, sp.x / 16 - 6, sp.y / 16);
									break;
								case 1:
									Structure.mobDungeonWest3Ruined.draw(this, sp.x / 16 - 6, sp.y / 16);
									break;
								case 2:
									Structure.mobDungeonWest3Ruined2.draw(this, sp.x / 16 - 6, sp.y / 16);
									break;
							}
						}
					} else if (depth > -5 && depth < 0) {
						int type = random.nextInt(7);
						switch (type) {
							case 0:
								Structure.mobDungeonCenter.draw(this, sp.x / 16, sp.y / 16);
								break;
							case 1:
								Structure.mobDungeonCenterRuined.draw(this, sp.x / 16, sp.y / 16);
								break;
							case 2:
								Structure.mobDungeonCenter2.draw(this, sp.x / 16, sp.y / 16);
								break;
							case 3:
								Structure.mobDungeonCenter2Ruined.draw(this, sp.x / 16, sp.y / 16);
								break;
							case 4:
								Structure.mobDungeonCenter2Ruined2.draw(this, sp.x / 16, sp.y / 16);
								break;
							case 5:
								Structure.mobDungeonCenterRuined2.draw(this, sp.x / 16, sp.y / 16);
								break;
							case 6:
								Structure.mobDungeonCenterRuined2.draw(this, sp.x / 16, sp.y / 16);
								break;
						}
						if (getTile(sp.x / 16, sp.y / 16 - 4) == Tiles.get("dirt")) {
							type = random.nextInt(3);
							switch (type) {
								case 0:
									Structure.mobDungeonNorth.draw(this, sp.x / 16, sp.y / 16 - 5);
									break;
								case 1:
									Structure.mobDungeonNorthRuined.draw(this, sp.x / 16, sp.y / 16 - 5);
									break;
								case 2:
									Structure.mobDungeonNorthRuined2.draw(this, sp.x / 16, sp.y / 16 - 5);
									break;
							}
						}
						if (getTile(sp.x / 16, sp.y / 16 + 4) == Tiles.get("dirt")) {
							type = random.nextInt(3);
							switch (type) {
								case 0:
									Structure.mobDungeonSouth.draw(this, sp.x / 16, sp.y / 16 + 5);
									break;
								case 1:
									Structure.mobDungeonSouthRuined.draw(this, sp.x / 16, sp.y / 16 + 5);
									break;
								case 2:
									Structure.mobDungeonSouthRuined2.draw(this, sp.x / 16, sp.y / 16 + 5);
									break;
							}
						}
						if (getTile(sp.x / 16 + 4, sp.y / 16) == Tiles.get("dirt")) {
							type = random.nextInt(3);
							switch (type) {
								case 0:
									Structure.mobDungeonEast.draw(this, sp.x / 16 + 5, sp.y / 16);
									break;
								case 1:
									Structure.mobDungeonEastRuined.draw(this, sp.x / 16 + 5, sp.y / 16);
									break;
								case 2:
									Structure.mobDungeonEastRuined2.draw(this, sp.x / 16 + 5, sp.y / 16);
									break;
							}
						}
						if (getTile(sp.x / 16 - 4, sp.y / 16) == Tiles.get("dirt")) {
							type = random.nextInt(3);
							switch (type) {
								case 0:
									Structure.mobDungeonWest.draw(this, sp.x / 16 - 5, sp.y / 16);
									break;
								case 1:
									Structure.mobDungeonWestRuined.draw(this, sp.x / 16 - 5, sp.y / 16);
									break;
								case 2:
									Structure.mobDungeonWestRuined2.draw(this, sp.x / 16 - 5, sp.y / 16);
									break;
							}
						}
					}

					add(sp);
					for (int rpt = 0; rpt < 2; rpt++) {
						if (random.nextInt(2) != 0) continue;
						Chest c = new Chest();
						int chance = -depth;
						switch (depth) {
							case -3:
								c.populateInvRandom("minidungeonFloor2", chance);
								break;
							default:
								c.populateInvRandom("minidungeon", chance);
								break;
						}
						;
						add(c, sp.x - 16 + rpt * 32, sp.y - 16 - (0 - random.nextInt(3) * 16));
					}
				}
			}
		}
	}
	private void generateMines() {
	int j=w/128;
	for(int i=0;i<(depth==2 ? j/2 : j);i++){
		int xCoord= random.nextInt(75*(w/128))+15;
		int yCoord= random.nextInt(75*(h/128))+15;
		while(getTile(xCoord,yCoord) == Tiles.get("water") || getTile(xCoord,yCoord) == Tiles.get("rock") || getTile(xCoord,yCoord) == Tiles.get("rockG") || getTile(xCoord,yCoord) == Tiles.get("reed") || getTile(xCoord,yCoord) == Tiles.get("Spiky stone") || getTile(xCoord,yCoord) == Tiles.get("iron ore") || getTile(xCoord,yCoord) == Tiles.get("ironNF ore") || getTile(xCoord,yCoord) == Tiles.get("gold ore") || getTile(xCoord,yCoord) == Tiles.get("goldNF ore")){ //reroll when water
			xCoord= random.nextInt(75*(w/128))+15;
			yCoord= random.nextInt(75*(h/128))+15;
		}
		int type=random.nextInt(4);
		switch(type) {
			case 0:Structure.mine.draw(this, xCoord, yCoord);break;
			case 1:Structure.mine2.draw(this, xCoord, yCoord);break;
			case 2:Structure.mine3.draw(this, xCoord, yCoord);break;
			case 3:Structure.mine4.draw(this, xCoord, yCoord);break;
		}
	}
	}


	/**
	 * VOID generateVillages
	 * @param allowed is for tiles we can start settling village on
	 * @param banned is for tiles we can't settle buildings on
	 * @param structures is for list of structures we will use for worldgen. We will be rolling structure every time
	 * @param proportion is for proportion of villages count and tries for each village
	 *   @param lootTable defines table of loot that will be used to generate random items inside chests found in the village
	 */
	private void generateVillages(@Nullable String banned[],String[] allowed,Structure[] structures,int horizontalOffset,int verticalOffset,@Nullable String lootTable,float proportion) {
		int lastVillageX = 0;
		int lastVillageY = 0;

		for (int i = 0; i <(int)(( w /128) * (proportion)); i++) {
			// Makes 2-8 villages based on world size

		for (int t = 0; t < 10 * proportion; t++) {
				// Tries 10 times for each one

				int x = random.nextInt(w);
				int y = random.nextInt(h);

				// Makes sure the village isn't to close to the previous village and checks if tile can be used for setting up a village
				if (Arrays.binarySearch(allowed, getTile(x, y).name.toLowerCase())>-1&&  (Math.abs(x - lastVillageX) > 16 && Math.abs(y - lastVillageY) > 16)) {
					lastVillageX = x;
					lastVillageY = y;

					// A number between 2 and 5
					int numHouses = random.nextInt(4) + 2;
					// Loops for each house in the village
					for (int hs = 0; hs < numHouses; hs++) {
						boolean hasChest = random.nextBoolean();

						int overlay = random.nextInt(2) + 1;
						// Basically just gets what offset this house should have from the center of the village
						int xo = (hs == 0 || hs == horizontalOffset - 1) ? -horizontalOffset : horizontalOffset;
						int yo = (hs < verticalOffset / 2) ? -verticalOffset : verticalOffset;
						for(int k=0;k<10;k++){ //try 10 times for each house
							if(Arrays.binarySearch(allowed, getTile(x + xo, y + yo).name.toLowerCase())>-1)break;
							xo += random.nextInt(horizontalOffset + 1) - horizontalOffset / 2;
							yo += random.nextInt(verticalOffset + 1) - verticalOffset / 2;
						}

						boolean canBuild = banned == null || Arrays.binarySearch(banned, getTile(x + xo, y + yo).name.toLowerCase())<=-1;
						int houseType = random.nextInt(structures.length);
							if(canBuild  ){
						//	System.out.println(x+ " " + y + " " + Arrays.binarySearch(allowed, getTile(x + xo, y + yo).name.toLowerCase()) + getTile(x + xo, y + yo).name + " GLEBOKOSC: " + this.depth + " " + this.realm);
							structures[houseType].draw(this, x + xo, y + yo);
							hs++;
							// Add a chest to some of the houses/ruins or whatever
							if (hasChest && lootTable != null) { //if there is no loot table why should we generate chests in first place then?
								Chest c = new Chest();
								c.populateInvRandom(lootTable, 1);
								add(c, (x + random.nextInt(2) + xo) << 4, (y + random.nextInt(2) + yo) << 4, lootTable == "obvruins" ? 1 : 0);
							}
						}
					}


				}

			}
		}
	}

	/**
	 *Alternative versions of generateVillages void
	 */
	private void generateVillages(String[] allowed,Structure[] structures,int offset){
		generateVillages(null,allowed, structures, offset, offset, null,2);
	};

	private void generateVillages(String banned[],String[] allowed,Structure[] structures,int offset){
		generateVillages(banned,allowed, structures, offset, offset, null,2);
	};
	private void generateVillages(String banned[],String[] allowed,Structure[] structures,int offset,float proportion){
		generateVillages(banned,allowed, structures, offset, offset, null,proportion);
	};
	private void generateVillages(String banned[],String[] allowed,Structure[] structures,int horizontalOffset,int verticalOffset){
		generateVillages(banned,allowed, structures, horizontalOffset, verticalOffset, null,2);
	};
	private void generateVillages(String banned[],String[] allowed,Structure[] structures,int horizontalOffset,int verticalOffset, float proportion){
		generateVillages(banned,allowed, structures, horizontalOffset, verticalOffset, null,proportion);
	};

	private void generateVillages(String banned[],String[] allowed,Structure[] structures,int offset,@Nullable String lootTable){
		generateVillages(banned,allowed, structures, offset, offset, lootTable,2);
	};
	private void generateCastle() {
		int castleType = random.nextInt(4);

		int xCoord= (random.nextInt(w-20*(w/128))+(20*(w/128)));
		int yCoord= (random.nextInt(h-20*(h/128))+(20*(h/128)));
		while(!getTile(xCoord,yCoord).name.contains("GRASS")){
			xCoord= (random.nextInt(w-20*(w/128))+(20*(w/128)));
			yCoord= (random.nextInt(h-20*(h/128))+(20*(h/128)));
		}
		Chest c = new Chest();
		c.populateInvRandom("castlechest", 1);
			switch (castleType) {
				case 0:
					Structure.RuinedCastleOverlay1.draw(this, xCoord, yCoord);
					add(c, xCoord*16, yCoord*16);
					break;
				case 1:
					Structure.RuinedCastleOverlay2.draw(this, xCoord, yCoord);
					add(c, xCoord*16, yCoord*16);
					break;
				case 2:
					Structure.RuinedCastleOverlay3.draw(this, xCoord, yCoord);
					add(c, xCoord*16, yCoord*16);
					break;
				case 3:
					Structure.RuinedCastleOverlay4.draw(this, xCoord, yCoord);
					add(c, xCoord*16, yCoord*16);
					break;
			}

		};

	//Simple void converting toString
	public String toString() {
		return "Level(depth=" + depth + ")";
	}

	//Simply a function for generating stuff  in overworld
	void overworld(int level,Level parentLevel){
		if (level == -6) {
		int lairs=0; //to count how many snake lairs are there
		int xl=0;int yl=0;
		while(lairs < (w/128) * 3) {

			while(getTile(xl,yl)!=Tiles.get("Dirt")) {
				xl = random.nextInt(w);
				yl = random.nextInt(h);
			}

					Structure.snakeLairDungeon.draw(this, xl,yl);
							lairs++;
							Spawner sp = new Spawner(new Snake(lairs%3 + 1,false));
							add(sp,xl * 16 +8,yl * 16 + 8);

			}
			boolean spawned=false;
			while(!spawned){
				int xx = random.nextInt(w -(20 * (w/128)) * 2) + (20 * (w/128));
				int yy = random.nextInt(h -(20 * (h/128)) * 2)+ (20 * (h/128));
				String[] allowedTiles = {"dirt","coarse dirt","lava brick","dungeon tallgrass","fungus"}; //just to make finding it easier
				if(Arrays.binarySearch(allowedTiles,getTile(xx,yy).name.toLowerCase())>-1){
					Structure.portalOBV.draw(this,xx,yy);
					setData(xx -1,yy -2 ,3);
					setData(xx ,yy -2 ,3);
					setData(xx - 1,yy + 1,3);
					setData(xx ,yy + 1,3);
					spawned = true;
				}
			}
		}
		if (parentLevel != null) { // If the level above this one is not null (aka, if this isn't the sky level)
			for (int y = 0; y < h; y++) { // Loop through height
				for (int x = 0; x < w; x++) { // Loop through width
					if (parentLevel.getTile(x, y) == Tiles.get("Stairs Down")) { // If the tile in the level above the current one is a stairs down then...
						if (level == -6) { /// Make the obsidian wall formation around the stair in the dungeon level
							Structure.dungeonGate.draw(this, x, y);

						}

						else if (level == 0) { // Surface
							if (Game.debug) System.out.println("Setting tiles around " + x + "," + y + " to hard rock");
							setAreaTiles(x, y, 1, Tiles.get("Hard Rock"), 0); // surround the sky stairs with hard rock
						}
						else // Any other level, the up-stairs should have dirt on all sides.
							if(level==-1) setAreaTiles(x, y, (Math.random()<0.04 ? 2 : 1), Tiles.get("Small stones"), 0);
							else if(level==-3)
								if(getTile(x,y)==Tiles.get("moss"))setAreaTiles(x, y, (Math.random()<0.04 ? 2 : 1), Tiles.get("Moss"), 0);
								else setAreaTiles(x, y, (Math.random()<0.04 ? 2 : 1), Tiles.get("Dirt"), 0);
							else setAreaTiles(x, y, (Math.random()<0.034 ? 2 : 1), Tiles.get("dirt"), 0);
						if(parentLevel.getTile(x, y).name.contains("STAIRS DOWN")){
							if((x%9>6 || y%9<2) && depth<0){
								Structure.stairsRuinsUp.draw(this,x,y);
							}
							else
								setTile(x, y, Tiles.get("Stairs Up"));
							if ((x % 12 == 0 || y % 12 == 0) && depth < -1)
								setData(x, y, 30); //blocked upstairs will start appearing at -2 to let player enter the caverns for iron at last

						}
					}else if(parentLevel.getTile(x, y) == Tiles.get("Obsidian Stairs Down")){
						Structure.dungeonGate.draw(this, x, y);
						setTile(x, y, Tiles.get("Obsidian Stairs Up"));
					}
				}
			}
		} else { // This is the sky level
			boolean placedHouse = false;
			while (!placedHouse) {

				int x = random.nextInt(this.w - 7);
				int y = random.nextInt(this.h - 5);

				if ((this.getTile(x - 3, y - 2) == Tiles.get("Cloud") || this.getTile(x - 3, y - 2) == Tiles.get("Skygrass")) && (this.getTile(x + 3, y - 2) == Tiles.get("Cloud") || this.getTile(x + 3, y - 2) == Tiles.get("Skygrass") || this.getTile(x + 3, y + 2) == Tiles.get("cloud tallgrass")) ) {
					if ((this.getTile(x - 3, y + 2) == Tiles.get("Cloud") || this.getTile(x - 3, y + 2) == Tiles.get("Skygrass")) && (this.getTile(x + 3, y + 2) == Tiles.get("Cloud") || this.getTile(x + 3, y + 2) == Tiles.get("Skygrass") || this.getTile(x + 3, y + 2) == Tiles.get("cloud tallgrass")) ) {
						Structure.airWizardHouse.draw(this, x, y);

						placedHouse = true;
					}
				}
			}

		}
			checkChestCount(false);
			checkMimics(1, 2);

		checkAirWizard();

		if (Game.debug) printTileLocs(Tiles.get("Stairs Down"));
	}

	void obv(int level,Level parentLevel) {
		if (parentLevel != null) { // If the level above this one is not null (aka, if this isn't the sky level)
			for (int y = 0; y < h; y++) { // Loop through height
				for (int x = 0; x < w; x++) { // Loop through width
					if (parentLevel.getTile(x, y) == Tiles.get("Obsidian Stairs Down"))
						if (depth == 0) {
							Structure.dungeonTowerEntrance.draw(this, x, y);
							setTile(x, y, Tiles.get("Obsidian Stairs Up"));
						} else if (depth < 0) {
							Structure.dungeonOBVGate.draw(this, x, y);
							setTile(x, y, Tiles.get("Obsidian Stairs Up"));
							checkMimics(0 + (-depth), 2 + (-depth));
							checkChestCount(false);
						}

				}
			}
			if (depth == 0) {
				boolean spawned = false;
				generateVillages(new String[]{"infinite void"},new String[]{"coarse dirt", "dirt", "dungeon tallgrass", "fungus", "lava brick", "lava", "obsidian"},new Structure[]{Structure.obsidianFountainClassic,Structure.villageRuinedOverlayObs1,Structure.villageRuinedOverlayObs2,Structure.villageRuinedOverlayObs3,Structure.obsidianFountain,Structure.obsidianFountainRuined},6,4,"obvruins",1);
				while (!spawned) {
					int xx = random.nextInt(w - (20 * (w / 128)) * 2) + (20 * (w / 128));
					int yy = random.nextInt(h - (20 * (h / 128)) * 2) + (20 * (h / 128));
					String[] allowedTiles = { "coarse dirt", "dungeon tallgrass", "fungus", "lava brick","dirt"}; //just to make finding it easier
					if (Arrays.binarySearch(allowedTiles, getTile(xx, yy).name.toLowerCase()) != -1) {
						Structure.portalOBVR.draw(this, xx, yy);
						setData(xx - 1, yy - 2, 5);
						setData(xx, yy - 2, 5);
						setData(xx - 1, yy + 1, 5);
						setData(xx, yy + 1, 5);
						setData(xx + 1, yy - 1, 2);
						setData(xx + 1, yy, 2);
						setData(xx - 2, yy, 2);
						setData(xx - 2, yy - 1, 2);
						spawned = true;
					}
				}


				}
			if(depth == -2) {
				int x = w/2 + ((random.nextInt(40) - 20) * w/128);
				int y = h/2 + ((random.nextInt(40) - 20) * h/128);
				Structure.obsidianKnightRoom.draw(this,x,y);
				add(new KnightStatue(3300),  x << 4 ,y << 4,1);
			}
			}
		}
}
