package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.entity.Arrow;
import minicraft.entity.Entity;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;
import minicraft.item.PotionType;

public class AncSkeleton extends EnemyMob {
	private static MobSprite[][][] sprites;
	static {
		sprites = new MobSprite[1][4][2];
		for (int i = 0; i < 1; i++) {
			MobSprite[][] list = MobSprite.compileMobSpriteAnimations(24, 4 + (i * 2));
			sprites[i] = list;
		}
	}

	private int arrowtime;
	private int artime;

	/**
	 * Creates a skeleton of a given level.
	 * @param lvl The skeleton's level.
	 */
	public AncSkeleton(int lvl) {
		super(lvl, sprites, 90, false, 160, 45, 200);

		arrowtime = 500 / (2 + 5);
		artime = arrowtime;
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
		}
	}
	@Override
	protected void touchedBy(Entity entity) {
		if (entity instanceof Player) {
			int damage = 2 + Settings.getIdx("diff");
			((Player)entity).hurt(this, damage);
		}
	}
	@Override
	public void die() {
		int[] diffrands = {20, 20, 30};
		int[] diffvals = {13, 18, 28};
		int diff = Settings.getIdx("diff");

		int count = random.nextInt(3 - diff) + 1;
		int rand = random.nextInt(diffrands[diff]);

		if (rand <= diffvals[diff])
			level.dropItem(x, y, count, Items.get("bone"), Items.get("arrow"));

		super.die();
	}
	public int getMaxLevel() { return 1; }
}
