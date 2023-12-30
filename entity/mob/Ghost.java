package minicraft.entity.mob;

import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.item.Items;
import minicraft.item.PotionType;

public class Ghost extends PassiveMob {
	private static MobSprite[][][] sprites;
	static {
		sprites = new MobSprite[5][4][2];
		for (int i = 0; i < 5; i++) {

			MobSprite[][] list = MobSprite.compileMobSpriteAnimations(24, i == 0 ? 8 :( (i*2) + 18)); //Ghost and its new variations
			sprites[i] = list;
		}
	}
	public boolean canSwim() { return true; }
	public boolean canBurn() { return false; }
	public boolean isSolid() { return false; }
	@Override
	public boolean canFly() {
		return true;
	}
	public boolean canBeAffectedByLava() { return false; }
	/**
	 * Creates a Ghost
	 */
	public Ghost() {

		super(sprites[0]);
		this.extradata = random.nextInt(5);
	}

	@Override
	public void render(Screen screen) {
		int xo = x - 8;
		int yo = y - 11;
		MobSprite[][] curAnim = sprites[this.extradata%5];
		MobSprite curSprite = curAnim[dir.getDir()][(walkDist >> 3) % curAnim[dir.getDir()].length];
		if((xo + yo)%(150 + (Updater.tickCount/100)%60) < 140 + ((Updater.tickCount/100)%20))
		if (hurtTime > 0) {
			curSprite.render(screen, xo, yo, true);
		} else {
			curSprite.render(screen, xo, yo);
		}
	}
	public void die() {
		
		super.die();
	}


}
