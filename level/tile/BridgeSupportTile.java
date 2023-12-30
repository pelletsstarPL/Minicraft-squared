package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;

public class BridgeSupportTile extends Tile {
	private static ConnectorSprite sprite = new ConnectorSprite(BridgeSupportTile.class, new Sprite(51, 17, 3, 3, 1, 3), new Sprite(54, 16, 2, 2, 1))
	{
		public boolean connectsTo(Tile tile, boolean isSide) {
			return tile.connectsToLiquid();
		}
	};

	protected BridgeSupportTile(String name) {
		super(name, sprite);
		connectsToSand = true;
		connectsToFluid = true;
	}
	
	public void render(Screen screen, Level level, int x, int y) {
		sprite.render(screen, level, x, y);
	}

}
