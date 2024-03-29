package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.entity.Direction;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.item.Items;
import minicraft.level.tile.Tiles;

public class Slime extends EnemyMob {
	private static MobSprite[][][] sprites;
	private static MobSprite[][][] spritesTransparent;
	static {
		sprites = new MobSprite[5][1][2];
		for (int i = 0; i < 5; i++) {
			MobSprite[] list = MobSprite.compileSpriteList(0, 0 + (i * 2), 2, 2, 0, 2);
			sprites[i][0] = list;
		}
		spritesTransparent = new MobSprite[5][1][2];
		for (int i = 0; i < 5; i++) {
			MobSprite[] list = MobSprite.compileSpriteList(32, 0 + (i * 2), 2, 2, 0, 2);
			spritesTransparent[i][0] = list;
		}
	}

	private int jumpTime = 0; // jumpTimer, also acts as a rest timer before the next jump
	
	/**
	 * Creates a slime of the given level.
	 * @param lvl Slime's level.
	 */
	public Slime(int lvl) {
		super(lvl,sprites, 1, true, 50+(NightWizard.revenge==0 ? 0 : NightWizard.revenge*15), 60, 40);
		this.usesCustomColor=true;
		this.extradata = Math.random() < Math.random() ? 1 : 0; //slime transparency
	}
	public boolean canSwim() { if(NightWizard.revenge>0 && level.getTile(x/16,y/16) == Tiles.get("water")) return true; else return false;}
	@Override
	public void tick() {
		if(level.getTile(x/16, y/16) == Tiles.get("water") && NightWizard.revenge==0)this.die();//just drown
		super.tick();
		
		/// jumpTime from 0 to -10 (or less) is the slime deciding where to jump.
		/// 10 to 0 is it jumping.

		if (jumpTime <= -10 && (xmov != 0 || ymov != 0))
			jumpTime = 10;
		
		jumpTime--;
		if (jumpTime == 0) {
			xmov = ymov = 0;
		}
	}

	@Override
	public void randomizeWalkDir(boolean byChance) {
		if (jumpTime > 0) return; // Direction cannot be changed if slime is already jumping.
		super.randomizeWalkDir(byChance);
	}
	
	@Override
	public boolean move(int xd, int yd) {
		boolean result = super.move(xd, yd);
		dir = Direction.DOWN;
		return result;
	}
	/*@Override
	public boolean canBurn() {
		return true;
	}*/
	
	@Override
	public void render(Screen screen) {
		int oldy = y;
		this.lvlSprites = this.extradata==1 ? spritesTransparent : sprites;
		if (jumpTime > 0) {
			walkDist = 8; // Set to jumping sprite.
			y -= 4; // Raise up a bit.
		}
		else walkDist = 0; // Set to ground sprite.
		
		dir = Direction.DOWN;
		if(Updater.tickCount%10>6 && level.getTile(x/16,y/16) == Tiles.get("water")) {
			if (!Updater.paused) Renderer.screen.render(x-8, y, 5 + 5 * 32, 0, 3); // Render the moonlight platform;
			if (!Updater.paused) Renderer.screen.render(x, y, 5 + 5 * 32, 1, 3); // Render the mirrored
		}
		super.render(screen);
		
		y = oldy;
	}
	
	public void die() {
		if( NightWizard.revenge>0 || level.getTile(x/16, y/16) != Tiles.get("water")) { //if mob dies on land or if night wizard is alive
			dropItem(1, Game.isMode("score") ? 2 : 4 - Settings.getIdx("diff"), Items.get("slime"));
			if (level.depth >= 0 && Updater.isbloody && Updater.getTime() == Updater.Time.Night)
				level.dropItem(x, y, random.nextInt(3 + lvl), Items.get("blood shard"));
		};
		if(Renderer.player.curArmor!=null)
		if(Renderer.player.curArmor.getName()=="Night Armor" && Updater.getTime()==Updater.Time.Night && lvl>4)getClosestPlayer().heal(2);
		super.die(); // Parent death call
	}
	
	@Override
	protected String getUpdateString() {
		String updates = super.getUpdateString() + ";";
		updates += "jumpTime," + jumpTime;
		
		return updates;
	}
	
	@Override
	protected boolean updateField(String field, String val) {
		if (super.updateField(field, val)) return true;
		switch (field) {
			case "jumpTime":
				jumpTime = Integer.parseInt(val);
				return true;
		}
		
		return false;
	}

	@Override
	public boolean canBurn() {
		return true;
	}
}
