package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.entity.Entity;
import minicraft.entity.furniture.Bed;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;

public class KnightTopT extends EnemyOneTierMob {
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(24, 2);

	/**
	 * Creates a knight
	 */
	/**
	 * Constructor for a non-hostile (passive) mob.
	 * healthFactor = 3.
	 */
	public KnightTopT() {
		super(sprites,113,3,100,3,200);
	}
		double chance=Math.random();
	public void die() {
		if (Settings.get("diff").equals("Easy")) {
			if (chance < 0.3) dropItem(1, 3, Items.get("iron ore"));
			chance=Math.random();
		}else {
			if (chance < 0.2) dropItem(1, 2, Items.get("apple"));
					chance=Math.random();
		};
		if(random.nextInt(24/1/(Settings.getIdx("diff")+1)) == 0)
			dropItem(1, 1, Items.get("Iron Sword"));

		super.die();
	}
	@Override
	protected void touchedBy(Entity entity) { // If an entity (like the player) touches the enemy mob
		super.touchedBy(entity);
		// Hurts the player, damage is based on lvl.
		if(entity instanceof Player) {
			((Player)entity).hurt(this, 3 * (Settings.get("diff").equals("Hard") ? 2 : 1));
		}
	}
	@Override
	public void tick() {
		super.tick();

		Player player = getClosestPlayer();
		if (player != null && !Bed.sleeping() && randomWalkTime <= 0 && !Game.isMode("Creative")) { // Checks if player is on zombie's level, if there is no time left on randonimity timer, and if the player is not in creative.
			int xd = player.x - x;
			int yd = player.y - y;
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