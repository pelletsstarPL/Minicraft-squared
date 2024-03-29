package minicraft.level.tile;

import minicraft.core.Updater;
import minicraft.entity.Entity;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;

public class WaterTile extends Tile {
	private ConnectorSprite sprite = new ConnectorSprite(WaterTile.class, new Sprite(12, 6, 3, 3, 1, 3), Sprite.dots(/*Color.get(005, 105, 115, 115)*/ 0))
	{
		public boolean connectsTo(Tile tile, boolean isSide) {
			return tile.connectsToFluid;
		}
	};
	
	protected WaterTile(String name) {
		super(name, (ConnectorSprite)null);
		csprite = sprite;
		isSurface =false;
		connectsToFluid = true;
	}
	
	public void render(Screen screen, Level level, int x, int y) {
		long seed = (tickCount + (x / 2 - y) * 4311) / 10 * 54687121l + x * 3271612l + y * 3412987161l;

		sprite.full = Sprite.randomDots(seed, 0);
		Tiles.get("Dirt").render(screen, level, x, y);
		sprite.render(screen, level, x, y);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canSwim()||e.canFly() ;
	}

	public boolean tick(Level level, int xt, int yt) {
		int xn = xt;
		int yn = yt;
		if(level.realm .contains( "dungeon realm")) level.setTile(xt, yt, Tiles.get("Hole"));
		if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
		else yn += random.nextInt(2) * 2 - 1;

		if (level.getTile(xn, yn) == Tiles.get("Hole")) {
			level.setTile(xn, yn, this);
		}
		if(level.depth==-6 && Updater.tickCount%5==0)level.setTile(xt, yt, Tiles.get("Hole"));
		// These set only the non-diagonally adjacent lava tiles to obsidian
		for (int x = -1; x < 2; x++) {
			if (level.getTile(xt + x, yt) == Tiles.get("Lava"))
				level.setTile(xt + x, yt, Tiles.get("Raw Obsidian"));
			if (level.getTile(xt + x, yt) == Tiles.get("Lava Brick"))
				level.setTile(xt + x, yt, Tiles.get("Obsidian"));
		}
		for (int y = -1; y < 2; y++) {
			if (level.getTile(xt, yt + y) == Tiles.get("lava"))
				level.setTile(xt, yt + y, Tiles.get("Raw Obsidian"));
			if (level.getTile(xt, yt + y) == Tiles.get("Lava Brick"))
				level.setTile(xt, yt + y, Tiles.get("Obsidian"));
		}
		return false;
	}
}
