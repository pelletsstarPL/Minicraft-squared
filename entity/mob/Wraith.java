package minicraft.entity.mob;

import minicraft.core.Renderer;
import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.level.Level;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;
import minicraft.entity.mob.Player;
public class Wraith extends EnemyMob {
	private static MobSprite[][][] sprites;
	static {
		sprites = new MobSprite[5][4][2];
		for (int i = 0; i < 5; i++) {
			MobSprite[][] list = MobSprite.compileMobSpriteAnimations(24, 8 + (i * 2));
			sprites[i] = list;
		}
	}
	public boolean canSwim() { return true; }
	public boolean canBurn() { return false; }
	public boolean isSolid() { return false; }
	/**
	 * Creates a wraith of the given level.
	 * @param lvl Wraith's level.
	 */
	public Wraith(int lvl) {
		super(lvl, sprites, lvl==4 ? 5 : 6, true,60,400000,8,150);
	}
	public boolean canBeAffectedByLava() { return false; }
	public void die() {
		int xd = Renderer.player.x - x; // The horizontal distance between the player and the wraith
		int yd = Renderer.player.y - y; // The vertical distance between the player and the wraith
		if (xd * xd + yd * yd < 16 * 16 * 10 * 10) {
			///play if less than 10 blocks away
			Sound.wraithDeath.play(); // Play wraith-death sound
			if(level.depth>=0 && Updater.isbloody && Updater.getTime()==Updater.Time.Night)
				level.dropItem(x, y, random.nextInt(3), Items.get("blood shard"));
		}
		if(Renderer.player.curArmor!=null)
		if(Renderer.player.curArmor.getName()=="Night Armor" && Updater.getTime()==Updater.Time.Night && lvl>4)getClosestPlayer().heal(3);
		super.die();
	}
}
