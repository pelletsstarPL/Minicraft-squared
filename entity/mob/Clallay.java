package minicraft.entity.mob;

import minicraft.core.io.Settings;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;

public class Clallay extends PassiveMob {
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(0, 36);
	
	/**
	 * Creates a clallay.
	 */
	public Clallay() {
		super(sprites);
	}
	
	public void die() {
		super.die();
	}
}
