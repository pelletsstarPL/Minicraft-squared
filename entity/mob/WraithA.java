package minicraft.entity.mob;

import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.level.Level;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;
public class WraithA extends EnemyMob {
	private static MobSprite[][][] sprites;
	static {
		sprites = new MobSprite[4][4][2];
		for (int i = 0; i < 4; i++) {
			MobSprite[][] list = MobSprite.compileMobSpriteAnimations(24, 8 + (i * 2));
			sprites[i] = list;
		}
	}
	public boolean canSwim() { return true; }
	public WraithA(int lvl) {
		super(lvl, sprites, 6, true,500,-1,8,150);
	}
	
	public void die() {
		AirWizard.invulnerability--; //you need to kill all his wraiths to disable his shield
		Sound.wraithDeath.play(); // Play wraith-death sound
		super.die();
	}
}
