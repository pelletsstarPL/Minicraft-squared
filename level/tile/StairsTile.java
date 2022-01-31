package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.PowerGloveItem;
import minicraft.level.Level;

public class StairsTile extends Tile {
	private static Sprite down = new Sprite(21, 0, 2, 2, 1, 0);
	private static Sprite up = new Sprite(19, 0, 2, 2, 1, 0);
	
	protected StairsTile(String name, boolean leadsUp) {
		super(name, leadsUp ? up : down);
		maySpawn = false;
		connectsToGrass = true;
	}
	
	@Override
	public void render(Screen screen, Level level, int x, int y) {

		if(level.getTile(x-1, y) == Tiles.get("Grass") || level.getTile(x+1, y) == Tiles.get("Grass") || level.getTile(x, y+1) == Tiles.get("Grass") || level.getTile(x, y-1) == Tiles.get("Grass")){
			Tiles.get("Grass").render(screen, level, x, y);
			sprite.render(screen, x << 4, y << 4, 0,0x80A560);
		}
		else if(level.depth<1) sprite.render(screen, x * 16, y * 16, 0, DirtTile.dCol(level.depth));
		else sprite.render(screen, x * 16, y * 16, 0, 0xC2C2C2);
	}

	@Override
	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		super.interact(level, xt, yt, player, item, attackDir);

		// Makes it so you can remove the stairs if you are in creative and debug mode.
		if (item instanceof PowerGloveItem && Game.isMode("Creative") && Game.debug) {
			level.setTile(xt, yt, Tiles.get("Grass"));
			Sound.monsterHurt.play();
			return true;
		} else {
			return false;
		}
	}
}
