package minicraft.entity.mob;

import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;

public class Ghost extends PassiveMob {
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(24, 6);
	public boolean canSwim() { return true; }
	/**
	 * Creates a Ghost
	 */
	public Ghost() {
		super(sprites);
	}
	
	public void die() {
		
		super.die();
	}
}
