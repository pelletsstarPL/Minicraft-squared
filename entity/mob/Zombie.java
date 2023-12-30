package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.core.io.Settings;
import minicraft.entity.furniture.Bed;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.item.Items;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class Zombie extends EnemyMob {
	private static MobSprite[][][] sprites;
	private static final MobSprite[][] longhair = MobSprite.compileMobSpriteAnimations(16, 42);
	static {
		sprites = new MobSprite[5][4][2];
		for (int i = 0; i < 5; i++) {
			MobSprite[][] list = MobSprite.compileMobSpriteAnimations(8, 0 + (i * 2));
			sprites[i] = list;
		}
	}
	/**
	 * Creates a zombie of the given level.
	// * @param lvl Zombie's level.
	 */
	public boolean canSwim() { if(NightWizard.revenge>0 && level.getTile(x/16,y/16) == Tiles.get("water")) return true; else return false;}
	public Zombie(int lvl) {
		super(lvl, sprites, 5, 100+(NightWizard.revenge==0 ? 0 : NightWizard.revenge*15));
        this.extracolor=random.nextInt(0xFFFFFF);
        this.usesCustomColor=true;
	}
	@Override
	public void render(Screen screen) {

		if(Updater.tickCount%10>6 && level.getTile(x/16,y/16) == Tiles.get("water")) {
			if (!Updater.paused) Renderer.screen.render(x-8, y, 5 + 5 * 32, 0, 3); // Render the moonlight platform;
			if (!Updater.paused) Renderer.screen.render(x, y, 5 + 5 * 32, 1, 3); // Render the mirrored
		}
		if (level.getTile(x/16,y/16) == Tiles.get("aerocloud")) { // Don't render the bottom half if swimming.
			if (!Updater.paused) Renderer.screen.render(x - 8, y - 4, 5 + (Updater.tickCount%11>6 ? 7 : 8) * 32, 0, 3); // Render the moonlight platform;
			if (!Updater.paused) Renderer.screen.render(x, y - 4, 5 + (Updater.tickCount%11>6 ? 7 : 8) * 32, 1, 3); // Render the mirrored
		}
		super.render(screen);
		if((this.extracolor+this.lvl)%4==0) {
			MobSprite hair = longhair[dir.getDir()][(walkDist >> 3) % longhair[dir.getDir()].length];
			hair.render(screen, x - 8, y - 11, -1, this.extracolor);
		}
	}
	@Override
	public void tick() {
		super.tick();
		Tile onTile = level.getTile(x >> 4, y >> 4);
		if(level.depth>=0 && Updater.tickCount>2500 && Updater.tickCount<37800 && Updater.tickCount%100==0 && lvl<4)this.burningDuration=200;
		//if(onTile == Tiles.get("water") && NightWizard.revenge==0)this.die();//just drown
		Player player = getClosestPlayer();
		if (player != null && !Bed.sleeping() && randomWalkTime <= 0 && !Game.isMode("Creative")) { // Checks if player is on zombie's level, if there is no time left on randonimity timer, and if the player is not in creative.
			int xd = player.x - x;
			int yd = player.y - y;

			if (xd * xd + yd * yd < detectDist * detectDist) {

				/// If player is less than 6.25 tiles away, then set move dir towards player
				int sig0 = 1; // This prevents too precise estimates, preventing mobs from bobbing up and down.
				this.xmov = this.ymov = 0;
				if (xd < sig0) this.xmov = onTile==Tiles.get("Aerocloud") && Updater.tickCount%2==0 ? 0 : -1;
				if (xd > sig0) this.xmov = onTile==Tiles.get("Aerocloud") && Updater.tickCount%2==0 ? 0 : 1;
				if (yd < sig0) this.ymov = onTile==Tiles.get("Aerocloud") && Updater.tickCount%2==0 ? 0 : -1;
				if (yd > sig0) this.ymov = onTile==Tiles.get("Aerocloud") && Updater.tickCount%2==0 ? 0 : 1;
			} else {
				// If the enemy was following the player, but has now lost it, it stops moving.
				// *That would be nice, but I'll just make it move randomly instead.
				randomizeWalkDir(false);
			}
		}
	}
	public void die() {
		if( NightWizard.revenge>0 || level.getTile(x/16, y/16) != Tiles.get("water")) { //if mob dies on land or if night wizard is alive
			if (Settings.get("diff").equals("Easy")) {
				dropItem(2, 4, Items.get("cloth"));
				dropItem(1, 2, Items.get("Flesh"));
			}
			;
			if (Settings.get("diff").equals("Normal")) {
				dropItem(1, 3, Items.get("cloth"));
				dropItem(1, 1, Items.get("Flesh"));
			}
			;
			if (Settings.get("diff").equals("Hard")) {
				dropItem(1, 2, Items.get("cloth"));
				dropItem(0, 1, Items.get("Flesh"));
			}
			;

			if (random.nextInt(60) == 2) {
				level.dropItem(x, y, Items.get("iron"));
			}

			if (random.nextInt(44) == 19) {
				String[] colors = {"red", "L. Blue", "green", "blue", "orange", "purple", "white", "pink", "gray"};
				int rand = random.nextInt(9);
				level.dropItem(x, y, Items.get(colors[rand] + " clothes"));
			}

			if (random.nextInt(100) < 4) {
				level.dropItem(x, y, Items.get("Potato"));
			}
			if (random.nextInt(100) < 4) {
				level.dropItem(x, y, Items.get("Carrot"));
			}
			if (level.realm=="overworld" &&level.depth >= 0 && Updater.isbloody && random.nextInt(3) == 1 && Updater.getTime() == Updater.Time.Night)
				level.dropItem(x, y, random.nextInt(3 + lvl), Items.get("blood shard"));
		}
		if(Renderer.player.curArmor!=null)
		if(Renderer.player.curArmor.getName()=="Night Armor" && Updater.getTime()==Updater.Time.Night && lvl>4)getClosestPlayer().heal(3);
		super.die();
	}
	public int getMaxLevel() { return 5; }

	@Override
	public boolean isSwimming() {
		if (level == null) return false;
		Tile tile = level.getTile(x >> 4, y >> 4); // Get the tile the mob is standing on (at x/16, y/16)
		// Check if the tile is liquid, and return true if so
		return (tile == Tiles.get("water") && NightWizard.revenge<1) || tile == Tiles.get("lava") || tile==Tiles.get("Reed") || tile==Tiles.get("aerocloud");
	}
}
