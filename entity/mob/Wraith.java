package minicraft.entity.mob;

import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.level.Level;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;
public class Wraith extends EnemyMob {
	private static MobSprite[][][] sprites;
	static {
		sprites = new MobSprite[4][4][2];
		for (int i = 0; i < 4; i++) {
			MobSprite[][] list = MobSprite.compileMobSpriteAnimations(24, 8 + (i * 2));
			sprites[i] = list;
		}
	}
	public boolean canSwim() { return true; }
	/**
	 * Creates a wraith of the given level.
	 * @param lvl Wraith's level.
	 */
	public Wraith(int lvl) {
		super(lvl, sprites, lvl==4 ? 5 : 6, true,100,400000,8,150);
	}
	
	public void die() {
		
		Sound.wraithDeath.play(); // Play wraith-death sound
		super.die();
	}
}
