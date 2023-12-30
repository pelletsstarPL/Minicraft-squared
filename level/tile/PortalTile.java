package minicraft.level.tile;

import minicraft.core.Updater;
import minicraft.entity.Entity;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;

public class PortalTile extends Tile {
	private ConnectorSprite sprite = new ConnectorSprite(PortalTile.class, new Sprite(56, 14, 3, 3, 1, 3), Sprite.dots(/*Color.get(005, 105, 115, 115)*/ 0))
	{
		public boolean connectsTo(Tile tile, boolean isSide) {
			return tile.connectsToFluid;
		}
	};

	protected PortalTile(String name) {
		super(name, (ConnectorSprite)null);
		csprite = sprite;
		connectsToFluid = true;
	}
	
	public void render(Screen screen, Level level, int x, int y) {
		long seed = (tickCount + (x / 2 - y) * 4311) / 10 * 54687121l + x * 3271612l + y * 3412987161l;

		sprite.full = Sprite.randomDots(seed, 4);
		Tiles.get("Obsidian bridge support").render(screen, level, x, y);
		sprite.render(screen, level, x, y);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canSwim()||e.canFly() ;
	}
	public int getLightRadius(Level level, int x, int y) {
		return 6;
	}


}
