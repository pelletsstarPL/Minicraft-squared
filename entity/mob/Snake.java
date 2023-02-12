package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.entity.Entity;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;
import minicraft.item.PotionType;

public class Snake extends EnemyMob {
	private static MobSprite[][][] sprites;
	static {
		sprites = new MobSprite[5][4][2];
		for (int i = 0; i < 5; i++) {
			MobSprite[][] list  = MobSprite.compileMobSpriteAnimations(8, 10 + (i * 2));
			sprites[i] = list;
		}
	}
	
	public Snake(int lvl) {
		super(lvl, sprites, lvl > 1 ? 8 : 7, 100);
	}
	
	@Override
	protected void touchedBy(Entity entity) {
		if (entity instanceof Player) {
			int damage = 0 + Settings.getIdx("diff");
			double chance=Math.random();
			if(chance<(0.04+(0.03*lvl))){
				int effect=random.nextInt(5);
				if(!Game.isMode("Creative"))
				switch(effect){
					case 0: Game.player.potionEffects.put(PotionType.Poison, 300+(100*lvl));break;
					case 1: Game.player.potionEffects.put(PotionType.Weak, 300+(100*lvl));break;
					case 2: Game.player.potionEffects.put(PotionType.Fatigue, 300+(100*lvl));break;
					case 3: Game.player.potionEffects.put(PotionType.Hunger, 300+(100*lvl));break;
					case 4: Game.player.potionEffects.put(PotionType.Thirst, 300+(100*lvl));break;
				}
			}
			((Player)entity).hurt(this, damage);
		}
	}
	
	public void die() {
		int num = Settings.get("diff").equals("Hard") ? 1 : 0;
		dropItem(num, num + 1, Items.get("scale"));
		
		if (random.nextInt(24 / lvl / (Settings.getIdx("diff") + 1)) == 0)
			dropItem(1, 1, Items.get("key"));
		if(getClosestPlayer().curArmor!=null)
		if(Renderer.player.curArmor.getName()=="Night Armor" && Updater.getTime()==Updater.Time.Night && lvl>4)getClosestPlayer().heal(3);
		
		super.die();
	}
}
