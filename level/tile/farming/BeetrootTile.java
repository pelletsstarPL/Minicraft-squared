package minicraft.level.tile.farming;

import minicraft.gfx.Screen;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;

public class BeetrootTile extends Plant {

	public BeetrootTile(String name) {
		super(name);
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		int age = level.getData(x, y);
		int icon = age / (maxAge / 5);

		Tiles.get("Farmland").render(screen, level, x, y);
		if(age<42) {
			screen.render(x * 16 + 0, y * 16 + 0, 13 + 1 * 32 + icon, 0, 1);
			screen.render(x * 16 + 8, y * 16 + 0, 13 + 1 * 32 + icon, 0, 1);
			screen.render(x * 16 + 0, y * 16 + 8, 13 + 1 * 32 + icon, 1, 1);
			screen.render(x * 16 + 8, y * 16 + 8, 13 + 1 * 32 + icon, 1, 1);
		}else{
			screen.render(x * 16 + 0, y * 16 + 0, 13 + 3 * 32 + icon, 0, 1);
			screen.render(x * 16 + 8, y * 16 + 0, 13 + 3 * 32 + icon, 0, 1);
			screen.render(x * 16 + 0, y * 16 + 8, 13 + 3 * 32 + icon, 1, 1);
			screen.render(x * 16 + 8, y * 16 + 8, 13 + 3 * 32 + icon, 1, 1);
		}
	}
}
