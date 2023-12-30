package minicraft.core;

import minicraft.level.tile.Tiles;
import org.jetbrains.annotations.Nullable;

import minicraft.core.io.Settings;
import minicraft.entity.furniture.Bed;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.RemotePlayer;
import minicraft.level.Level;
import minicraft.network.Analytics;
import minicraft.saveload.Load;
import minicraft.saveload.Save;
import minicraft.screen.LoadingDisplay;
import minicraft.screen.PlayerDeathDisplay;
import minicraft.screen.WorldGenDisplay;
import minicraft.screen.WorldSelectDisplay;

import java.util.Random;

public class World extends Game {
	private World() {}

	public static final String[] realms = {"overworld","dungeon realm"};
	static Random random = new Random();
	public static Level lvlList[][] = {levels,obvLevels};
	public static final int[] idxToDepth = {-6,-5,-4,-3,-2, -1, 0, 1}; /// This is to map the level depths to each level's index in Game's levels array. This must ALWAYS be the same length as the levels array, of course.
	public static final int[] idxToDepthObv = {-2,-1,0,1}; //maps level depths of the Obsidian Void

	public static final int lvlIdxList[][] = {idxToDepth,idxToDepthObv};
	public static  int minLevelDepth, maxLevelDepth;
	static  {
		minMax(idxToDepth);
	}
	
	static int worldSize = 128; // The size of the world
	public static int lvlw = worldSize; // The width of the world
	public static int lvlh = worldSize; // The height of the world
	
	static int playerDeadTime; // The time after you die before the dead menu shows up.
	static int pendingLevelChange; // Used to determine if the player should change levels or not.
	static int pendingRealmChange; // Used to determine if the player should change realms or not.


	public static void minMax(int[] idx){
		int min, max;
		min = max = idx[0];
		for (int depth: idx) {
			if (depth < min)
				min = depth;
			if (depth > max)
				max = depth;
		}
		minLevelDepth = min;
		maxLevelDepth = max;
	}
	@Nullable
	public static Action onChangeAction; // Allows action to be stored during a change schedule that should only occur once the screen is blacked out.
	
	/// SCORE MODE
	public static int lvlIdx(int depth) {
		return  lvlIdx(depth,idxToDepth);
	}
	/** This is for a contained way to find the index in the levels array of a level, based on it's depth. This is also helpful because add a new level in the future could change this. */
	public static int lvlIdx(int depth,int[] idx) {
		if (depth > maxLevelDepth) return lvlIdx(minLevelDepth,idx);
		if (depth < minLevelDepth) return lvlIdx(maxLevelDepth,idx);

	//	if (depth == -6) return 0;

		return depth + idx.length - 2;
	}
	
	
	/** This method is used when respawning, and by initWorld to reset the vars. It does not generate any new terrain. */
	public static void resetGame() { resetGame(true); }
	public static void resetGame(boolean keepPlayer) {
		if (debug) System.out.println("Resetting...");
		playerDeadTime = 0;
		currentLevel = levels.length-2;
		Updater.asTick = 0;
		Updater.notifications.clear();
		
		// Adds a new player
		if (isValidServer()) {
			player = null;
			return;
		}
		if (keepPlayer) {
			if (player instanceof RemotePlayer)
				player = new RemotePlayer(true, (RemotePlayer) player);
			else
				player = new Player(player, input);
		} else
			player = new Player(null, input);
		
		if (levels[currentLevel] == null) return;
		
		// "shouldRespawn" is false on hardcore, or when making a new world.
		if (PlayerDeathDisplay.shouldRespawn) { // respawn, don't regenerate level.
			// if (debug) System.out.println("Current Level = " + currentLevel);
			if (!isValidClient()) {
				Level level = levels[currentLevel];
				player.respawn(level);
				// if (debug) System.out.println("respawned player in current world");
				level.add(player); // Adds the player to the current level (always surface here)
			} else {
				client.requestRespawn();
			}
		}
	}
	public static byte getTotalLen(){
		byte len=0;
		Level lvlList[][] = {levels,obvLevels};
		for(byte i=0;i<lvlList.length;i++) {
			len += (byte) lvlList[i].length;
		}
		return len;
	}
	/** This method is used to create a brand new world, or to load an existing one from a file.
	 * For the loading screen updates to work, it it assumed that *this* is called by a thread *other* than the one rendering the current *menu*.
	 **/
	public static void initWorld() { // This is a full reset; everything.
		if(debug) System.out.println("Resetting world...");
		
		/*if(isValidServer()) {
			System.err.println("Cannot initialize world while acting as a server runtime; not running initWorld().");
			return;
		}*/
		
		PlayerDeathDisplay.shouldRespawn = false;
		resetGame();
		player = new Player(null, input);
		Bed.removePlayers();
		Updater.gameTime = 0;
		Updater.gamespeed = 1;
		
		Updater.changeTimeOfDay(Updater.Time.Morning); // Resets tickCount; game starts in the day, so that it's nice and bright.
		gameOver = false;
		
		levels = new Level[idxToDepth.length];
		obvLevels = new Level[idxToDepthObv.length];
		
		Updater.scoreTime = (Integer) Settings.get("scoretime") * 60 * Updater.normSpeed;
		
		LoadingDisplay.setPercentage(0); // This actually isn't necessary, I think; it's just in case.
		
		if (!isValidClient()) {
			if (debug) System.out.println("Initializing world non-client...");
			
			if (isValidServer())
				Analytics.MultiplayerGame.ping();
			else
				Analytics.SinglePlayerGame.ping();
			
			if (WorldSelectDisplay.loadedWorld()) {
				Load loader = new Load(WorldSelectDisplay.getWorldName());
				if  (isValidServer() && loader.getWorldVersion().compareTo(Game.VERSION) < 0) {
					Analytics.SaveFileUpdate.ping();
					new Save(player, true); // Overwrite the old player save, to update it.
					new Save(WorldSelectDisplay.getWorldName()); // Save the main world
				}
			} else {
				Analytics.WorldCreation.ping();
				
				worldSize = (Integer) Settings.get("size");
				
				float loadingInc = 0;
				Level lvlList[][] = {levels,obvLevels};
				int lvlIdxList[][] = {idxToDepth,idxToDepthObv};
				for (int j = 0;j < lvlList.length;j++){
				minMax(lvlIdxList[j]);
					loadingInc = 100/(getTotalLen() - 1); // The .002 is for floating point errors, in case they occur.
				for (int i = maxLevelDepth; i >= minLevelDepth; i--) {
					// i = level depth; the array starts from the top because the parent level is used as a reference, so it should be constructed first. It is expected that the highest level will have a null parent.
					if (debug) System.out.println("Loading level " + i + "...");

					LoadingDisplay.setMessage(Level.getDepthString(i,realms[j]));
					lvlList[j][lvlIdx(i,lvlIdxList[j])] = new Level(worldSize, worldSize, WorldGenDisplay.getSeed(), i,lvlList[j][lvlIdx(i+1,lvlIdxList[j])], !WorldSelectDisplay.loadedWorld(),realms[j]);

					LoadingDisplay.progress(loadingInc);
				}
				}
				
				if(debug) System.out.println("Level loading complete.");
				
				Level level = levels[currentLevel]; // Sets level to the current level (3; surface)
				Updater.pastDay1 = false;
				player.findStartPos(level, WorldGenDisplay.getSeed()); // Finds the start level for the player
				level.add(player);
			}
			
			Renderer.readyToRenderGameplay = true;
		} else {
			levels = new Level[idxToDepth.length];
			currentLevel = 4;
		}

		
		PlayerDeathDisplay.shouldRespawn = true;
		
		if (debug) System.out.println("World initialized.");
	}


	/** This method is called when you interact with  portals, this will give you the transition effect. While changeLevel(int) just changes the level. */
	public static void scheduleRealmChange(int dir) { scheduleRealmChange(dir,0, null); }
	public static void scheduleRealmChange(int dirR,int dirL) { scheduleRealmChange(dirR,dirL, null); }
	public static void scheduleRealmChange(int dirR,int dirL, @Nullable Action changeAction) {
		if (!isValidServer()) {
			onChangeAction = changeAction;
			pendingRealmChange = dirR;
			pendingLevelChange = dirL;
		}
	}
	
	/** This method is called when you interact with stairs, this will give you the transition effect. While changeLevel(int) just changes the level. */
	public static void scheduleLevelChange(int dir) { scheduleLevelChange(dir, null); }
	public static void scheduleLevelChange(int dir, @Nullable Action changeAction) {
		if (!isValidServer()) {
			onChangeAction = changeAction;
			pendingLevelChange = dir;
		}
	}
	
	/** This method changes the level that the player is currently on.
	 * It takes 1 integer variable, which is used to tell the game which direction to go.
	 * For example, 'changeLevel(1)' will make you go up a level,
	 while 'changeLevel(-1)' will make you go down a level. */
	public static void changeLevel(int dir) {
		Level lvlList[][] = {levels,obvLevels};
		int lvlIdxList[][] = {idxToDepth,idxToDepthObv};
		if (isValidServer()) {
			System.out.println("Server tried to change level.");
			return;
		}
		
		if (onChangeAction != null && !Game.isConnectedClient()) {
			onChangeAction.act();
			onChangeAction = null;
		}
		
		if (isConnectedClient())
			lvlList[player.getRealmId()][currentLevel].clearEntities(); // Clear all the entities from the last level, so that no artifacts remain. They're loaded dynamically, anyway.
		else
			lvlList[player.getRealmId()][currentLevel].remove(player); // Removes the player from the current level.
		
		int nextLevel = currentLevel + dir;
		if (nextLevel <= -1) nextLevel =lvlList[player.getRealmId()].length-1; // Fix accidental level underflow
		if (nextLevel >= lvlList[player.getRealmId()].length) nextLevel = 0; // Fix accidental level overflow
		//level = levels[currentLevel]; // Sets the level to the current level
		if (Game.debug) System.out.println(Network.onlinePrefix()+"setting level from "+currentLevel+" to "+nextLevel);
		currentLevel = nextLevel;
		
		player.x = (player.x >> 4) * 16 + 8; // Sets the player's x coord (to center yourself on the stairs)
		player.y = (player.y >> 4) * 16 + 8; // Sets the player's y coord (to center yourself on the stairs)
		
		if (isConnectedClient()/* && levels[currentLevel] == null*/) {
			Renderer.readyToRenderGameplay = false;
			client.requestLevel(currentLevel);
		} else
			lvlList[player.getRealmId()][currentLevel].add(player,player.getRealmId()); // Adds the player to the level.
	}

	public static void changeRealm(int dirR) { changeRealm(dirR,0); } //we won't change level with this call
	public static void changeRealm(int dirR,int dirL) { //here we will change BOTH realmId and depth
		Level lvlList[][] = {levels,obvLevels};
		int lvlIdxList[][] = {idxToDepth,idxToDepthObv};
		if (isValidServer()) {
			System.out.println("Server tried to change level.");
			return;
		}

		if (onChangeAction != null && !Game.isConnectedClient()) {
			onChangeAction.act();
			onChangeAction = null;
		}
		int nextLevel = currentLevel + dirL;
		if (isConnectedClient())
			lvlList[player.getRealmId()][currentLevel].clearEntities(); // Clear all the entities from the last level, so that no artifacts remain. They're loaded dynamically, anyway.
		else
			lvlList[player.getRealmId()][currentLevel].remove(player); // Removes the player from the current level.

		int destination = player.getRealmId() + dirR;
	//	player.setLevel(lvlList[dirR][currentLevel],player.x,player.y); DZIAŁA BEZ TEGO O CO KAMAN
		player.setRealmId(destination);
	//	if (nextLevel <= -1) nextLevel =lvlList[player.getRealmId()].length-1; // Fix accidental level underflow
	//	if (nextLevel >= lvlList[player.getRealmId()].length) nextLevel = 0; // Fix accidental level overflow
		//level = levels[currentLevel]; // Sets the level to the current level
		//if (Game.debug) System.out.println(Network.onlinePrefix()+"setting level from "+currentLevel+" to "+nextLevel);
		//currentLevel = nextLevel;
		currentLevel = nextLevel	< 0 ? 0 : nextLevel;
		player.x = (player.x >> 4) * 16 + 8; // Sets the player's x coord (to center yourself on the stairs)
		player.y = (player.y >> 4) * 16 + 8; // Sets the player's y coord (to center yourself on the stairs)
		int w=lvlList[player.getRealmId()][currentLevel].w;
		int h=lvlList[player.getRealmId()][currentLevel].h;;
		while(!lvlList[player.getRealmId()][currentLevel].getTile(player.x >> 4,player.y >> 4).mayPass(lvlList[player.getRealmId()][currentLevel],player.x,player.y,player) || !lvlList[player.getRealmId()][currentLevel].getTile(player.x >> 4,player.y >> 4).mayPass(lvlList[player.getRealmId()][currentLevel],player.x >> 4,player.y >> 4,player)&& !(lvlList[player.getRealmId()][currentLevel].getTile(player.x >> 4,player.y >> 4).isSurface)){
			player.x =  random.nextInt((w - 60)*(w/128)) + (30*(w/128)) << 4;player.x+=8;
			player.y =  random.nextInt((h - 60)*(h/128)) + (30*(h/128)) << 4;player.y+=8;
		}
		if (isConnectedClient()/* && levels[currentLevel] == null*/) {
			Renderer.readyToRenderGameplay = false;
			client.requestLevel(currentLevel);
		} else
			lvlList[player.getRealmId()][currentLevel].add(player,player.getRealmId()); // Adds the player to the level.
		if(lvlList[player.getRealmId()][currentLevel].getTile(player.x >>4,player.y >> 4)==Tiles.get("lava"))
		lvlList[player.getRealmId()][currentLevel].setAreaTiles(player.x >> 4,player.y >> 4,2, Tiles.get("obsidian"),1);
				//we add 8 to center players ontile spawn


	}


	
}
