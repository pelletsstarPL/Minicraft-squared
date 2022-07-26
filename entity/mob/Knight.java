package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.entity.Entity;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;
import minicraft.item.PotionType;

public class Knight extends EnemyMob {
	private static MobSprite[][][] sprites;
	static {
		sprites = new MobSprite[5][4][2];
		for (int i = 0; i < 5; i++) {
			MobSprite[][] list  = MobSprite.compileMobSpriteAnimations(0, 10 + (i * 2));
			sprites[i] = list;
		}
	}

	
	/**
	 * Creates a knight of a given level.
	 * @param lvl The knights level.
	 */
	public Knight(int lvl) {
		super(lvl, sprites, 9, 100);
	}
	@Override
	protected void touchedBy(Entity entity) {
		if (entity instanceof Player) {
			int damage = lvl + Settings.getIdx("diff");
			((Player)entity).hurt(this, damage);
		}
	}
	public void die() {
		if (Settings.get("diff").equals("Easy"))
			dropItem(1, 3, Items.get("shard"));
		else
			dropItem(0, 2, Items.get("shard")
			);
		
		if(random.nextInt(24/lvl/(Settings.getIdx("diff")+1)) == 0)
			dropItem(1, 1, Items.get("key"));
		if(getClosestPlayer().curArmor!=null)
		if(Renderer.player.curArmor.getName()=="Night Armor" && Updater.getTime()==Updater.Time.Night && lvl>4)getClosestPlayer().heal(3);
		super.die();
	}
	public int getMaxLevel() { return 5; }
}
