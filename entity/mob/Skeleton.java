package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.core.io.Settings;
import minicraft.entity.Arrow;
import minicraft.entity.particle.BurnParticle;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.item.Items;
import minicraft.level.tile.Tiles;

public class Skeleton extends EnemyMob {
	private static MobSprite[][][] sprites;
	static {
		sprites = new MobSprite[5][4][2];
		for (int i = 0; i < 5; i++) {
			MobSprite[][] list = MobSprite.compileMobSpriteAnimations(16, 0 + (i * 2));
			sprites[i] = list;
		}
	}
	
	private int arrowtime;
	private int artime;
	
	/**
	 * Creates a skeleton of a given level.
	 * @param lvl The skeleton's level.
	 */
	public Skeleton(int lvl) {
		super(lvl, sprites, 6, true, 100+(NightWizard.revenge==0 ? 0 : NightWizard.revenge*15), 45, 200);
		
		arrowtime = 500 / (lvl + 5);
		artime = arrowtime;
	}
	@Override
	public void render(Screen screen) {
		sprites[lvl-1] = lvlSprites[lvl - 1];
		if(Updater.tickCount%10>6 && level.getTile(x/16,y/16) == Tiles.get("water")) {
			if (!Updater.paused) Renderer.screen.render(x-8, y, 5 + 5 * 32, 0, 3); // Render the moonlight platform;
			if (!Updater.paused) Renderer.screen.render(x, y, 5 + 5 * 32, 1, 3); // Render the mirrored
		}
		super.render(screen);
	}
	@Override
	public void tick() {
		super.tick();

		if (skipTick()) return;

		if(level.getTile(x/16, y/16) == Tiles.get("water") && NightWizard.revenge==0)this.die();//just drown
		Player player = getClosestPlayer();
		if (player != null && randomWalkTime == 0 && !Game.isMode("Creative")) { // Run if there is a player nearby, the skeleton has finished their random walk, and gamemode is not creative.
			artime--;

			int xd = player.x - x;
			int yd = player.y - y;
			if (xd * xd + yd * yd < 100 * 100) {
				if (artime < 1) {
					level.add(new Arrow(this, dir, lvl));
					artime = arrowtime;
				}
			}
		}
	}
	public void die() {
		int[] diffrands = {20, 20, 30};
		int[] diffvals = {13, 18, 28};
		int diff = Settings.getIdx("diff");
		
		int count = random.nextInt(3 - diff) + 1;
		int bookcount = random.nextInt(1) + 1;
		int rand = random.nextInt(diffrands[diff]);
		if( NightWizard.revenge>0 || level.getTile(x/16, y/16) != Tiles.get("water")){ //if mob dies on land or if night wizard is alive
			if (rand <= diffvals[diff])
				level.dropItem(x, y, count, Items.get("bone"), Items.get("arrow"));
			else if (diff == 0 && rand >= 19) // Rare chance of 10 arrows on easy mode
				level.dropItem(x, y, 10, Items.get("arrow"));
			else
				level.dropItem(x, y, bookcount, Items.get("Antidious"), Items.get("arrow"));
			if (level.depth >= 0 && Updater.isbloody && Updater.getTime() == Updater.Time.Night)
				level.dropItem(x, y, random.nextInt(3 + lvl), Items.get("blood shard"));
			if (Renderer.player.curArmor != null)
				if (Renderer.player.curArmor.getName() == "Night Armor" && Updater.getTime() == Updater.Time.Night && lvl > 4)
					getClosestPlayer().heal(3);
		}
		super.die();
	}
	public int getMaxLevel() { return 5; }
	public boolean canSwim() { if(NightWizard.revenge>0 && level.getTile(x/16,y/16) == Tiles.get("water")) return true; else return false;}
}
