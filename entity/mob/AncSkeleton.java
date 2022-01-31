package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.entity.Arrow;
import minicraft.core.io.Settings;
import minicraft.entity.Entity;
import minicraft.entity.furniture.Bed;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;
import minicraft.level.tile.Tiles;

public class AncSkeleton extends EnemyOneTierMob { //ancient skeleton
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(24, 4);
	private int arrowtime;
	private int artime;
	/**
	 * Creates ancient skeleton
	 */
	/**
	 * Constructor for a hostile one tier mob.
	 * healthFactor = 3.
	 */
	public AncSkeleton() {
		super(sprites,73,35,100,4,200);
		arrowtime = 500 / (2 + 5);
		artime = arrowtime;
	}
	double chance=Math.random();
	public void die() {
		if (Settings.get("diff").equals("Easy")) {
			if (chance < 0.3) dropItem(1, 3, Items.get("arrow"));
			dropItem(1,2, Items.get("bone"));
			chance=Math.random();
		}else {
			if (chance < 0.2) dropItem(1, 2, Items.get("arrow"));
			dropItem(0,1, Items.get("bone"));
			chance=Math.random();
		};
		super.die();
	}
	@Override
	protected void touchedBy(Entity entity) { // If an entity (like the player) touches the enemy mob
		super.touchedBy(entity);
		// Hurts the player, damage is based on lvl.
		if(entity instanceof Player) {
			((Player)entity).hurt(this, dmg * (Settings.get("diff").equals("Hard") ? 2 : 1));
		}
	}
	@Override
	public void tick() {
		super.tick();
		if (skipTick()) return;

		Player player = getClosestPlayer();
		if (player != null && randomWalkTime == 0 && !Game.isMode("Creative")) { // Run if there is a player nearby, the skeleton has finished their random walk, and gamemode is not creative.
			artime--;

			int xd = player.x - x;
			int yd = player.y - y;
			if (xd * xd + yd * yd < 100 * 100) {
				if (artime < 1) {
					level.add(new Arrow(this, dir, 4));
					artime = arrowtime;
				}
			}
			if (xd * xd + yd * yd < 100 * 100) {

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
}