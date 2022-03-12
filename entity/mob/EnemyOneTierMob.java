package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.entity.Entity;
import minicraft.entity.furniture.Bed;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class EnemyOneTierMob extends MobAi {
	protected int color;
	public int detectDist;
	protected int dmg;
	/**
	 * Constructor for a non-hostile (passive) mob.
	 * healthFactor = 3.
	 * @param sprites The mob's sprites.
	 */
	/*public EnemyOneTierMob(MobSprite[][] sprites) {
		this(sprites, 3);
	}*/

	/**
	 * Constructor for a non-hostile (passive) mob.
	 * @param sprites The mob's sprites.
	 * @param detectDist Range where mob detects ya
	 * @param healthFactor Determines the mobs health. Will be multiplied by the difficulty
	 * @param detectDist The distance where the mob will detect the player and start moving towards him/her.

	 * and then added with 5.
	 */
	public EnemyOneTierMob(MobSprite[][] sprites, int healthFactor,int rwTime,int rwChance,int dmg,int detectDist) {
		super(sprites, 5 + healthFactor * Settings.getIdx("diff"), 5*60*Updater.normSpeed, rwTime, rwChance);
	}
	public boolean isSwimmer() {
		return false;
	}
	@Override
	public void render(Screen screen) {
		super.render(screen);
	}

	@Override
	public int getMaxLevel() {
		return 0;
	}

	@Override
	public void tick() {
		super.tick();

		Player player = getClosestPlayer();
		if (player != null && !Bed.sleeping() && randomWalkTime <= 0 && !Game.isMode("Creative")) { // Checks if player is on zombie's level, if there is no time left on randonimity timer, and if the player is not in creative.
			int xd = player.x - x;
			int yd = player.y - y;
			if (xd * xd + yd * yd < detectDist * detectDist) {

				/// If player is less than 6.25 tiles away, then set move dir towards player
				int sig0 = 1; // This prevents too precise estimates, preventing mobs from bobbing up and down.
				this.xmov = this.ymov = 0;
				if (xd < sig0) this.xmov = -1;
				if (xd > sig0) this.xmov = +1;
				if (yd < sig0) this.ymov = -1;
				if (yd > sig0) this.ymov = +1;
			} else {
				// If the enemy was following the player, but has now lost it, it stops moving.
				// *That would be nice, but I'll just make it move randomly instead.
				randomizeWalkDir(false);
			}
		}
	}
	public void die() {
		super.die(50, 1);
	}
	/**
	 * Determines if the mob can spawn at the giving position in the given map.
	 * @param level The level which the mob wants to spawn in.
	 * @param x X map spawn coordinate.
	 * @param y Y map spawn coordinate.
	 * @return true if the mob can spawn here, false if not.
	 */

	public static boolean checkStartPos(Level level, int x, int y) { // Find a place to spawn the mob
		int r = (level.depth == -4 ? (Game.isMode("score") ? 22 : 15) : 13);

		if(!MobAi.checkStartPos(level, x, y, 60, r))
			return false;

		x = x >> 4;
		y = y >> 4;

		Tile t = level.getTile(x, y);if(level.depth == -4) {
			if (t != Tiles.get("Obsidian") || t!=Tiles.get("Raw Obsidian")) return false;
		} else if (t != Tiles.get("Stone Door") && t != Tiles.get("Wood Door") && t != Tiles.get("Obsidian Door") && t != Tiles.get("wheat") && t != Tiles.get("farmland")) {
			// Prevents mobs from spawning on lit tiles, farms, or doors (unless in the dungeons)
			return !level.isLight(x, y);
		} else return false;

		return true;
	}
}
