package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.entity.Entity;
import minicraft.entity.furniture.Bed;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;

public class KnightTop extends EnemyMob {
	private static MobSprite[][][] sprites;
	static {
		sprites = new MobSprite[2][4][2];
		for (int i = 0; i < 2; i++) {
			MobSprite[][] list  = MobSprite.compileMobSpriteAnimations(24, 0 + (i * 2));
			sprites[i] = list;
		}
	}


	/**
	 * Creates a knight of a given level.
	 * @param lvl The knights level.
	 */
	public KnightTop(int lvl) {
		super(lvl, sprites, 20, 100);
	}
	@Override
	protected void touchedBy(Entity entity) {
		if (entity instanceof Player) {
			int damage = lvl + Settings.getIdx("diff");
			((Player)entity).hurt(this, damage);
		}
	}
	public void die() {
		double chance=Math.random();
		if (Settings.get("diff").equals("Easy")) {
			if (chance < 0.3) dropItem(1, 3, Items.get("iron ore"));
			chance=Math.random();
		}else {
			if (chance < 0.2) dropItem(1, 2, Items.get("apple"));
			chance=Math.random();
		};
		if(random.nextInt(24/1/(Settings.getIdx("diff")+1)) == 0)
			if(lvl==1)
			dropItem(1, 1, Items.get("Rock Sword"));
			else
				dropItem(1, 1, Items.get("Iron Sword"));

		super.die();
	}
	public int getMaxLevel() { return 2; }

}