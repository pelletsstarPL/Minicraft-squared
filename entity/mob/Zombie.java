package minicraft.entity.mob;

import minicraft.core.Renderer;
import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;

public class Zombie extends EnemyMob {
	private static MobSprite[][][] sprites;
	static {
		sprites = new MobSprite[5][4][2];
		for (int i = 0; i < 5; i++) {
			MobSprite[][] list = MobSprite.compileMobSpriteAnimations(8, 0 + (i * 2));
			sprites[i] = list;
		}
	}
	
	/**
	 * Creates a zombie of the given level.
	 * @param lvl Zombie's level.
	 */
	public Zombie(int lvl) {
		super(lvl, sprites, 5, 100);
	}
	
	public void die() {
		if (Settings.get("diff").equals("Easy")) {dropItem(2, 4, Items.get("cloth"));dropItem(1, 2, Items.get("Flesh"));};
		if (Settings.get("diff").equals("Normal")) {dropItem(1, 3, Items.get("cloth"));dropItem(1, 1, Items.get("Flesh"));};
		if (Settings.get("diff").equals("Hard")){ dropItem(1, 2, Items.get("cloth"));dropItem(0, 1, Items.get("Flesh"));};
		
		if (random.nextInt(60) == 2) {
			level.dropItem(x, y, Items.get("iron"));
		}
		
		if (random.nextInt(44) == 19) {
			String[] colors = {"red", "L. Blue", "green", "blue","orange","purple","white","pink","gray"};
			int rand = random.nextInt(9);
			level.dropItem(x, y, Items.get(colors[rand]+" clothes"));
		}

		if (random.nextInt(100) < 4) {
			level.dropItem(x, y, Items.get("Potato"));
		}
		if (random.nextInt(100) < 4) {
			level.dropItem(x, y, Items.get("Carrot"));
		}
		if(level.depth>=0 && Updater.isbloody && random.nextInt(3)==1 && Updater.getTime()==Updater.Time.Night)
		level.dropItem(x, y, random.nextInt(3+lvl), Items.get("blood shard"));
		if(Renderer.player.curArmor!=null)
		if(Renderer.player.curArmor.getName()=="Night Armor" && Updater.getTime()==Updater.Time.Night && lvl>4)getClosestPlayer().heal(3);
		super.die();
	}
	public int getMaxLevel() { return 5; }
}
