package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;

public class InfiniteVoidTile extends Tile {

	protected InfiniteVoidTile(String name) {

		super(name, (Sprite)null);
		isSurface =false;
	}



	public void render(Screen screen, Level level, int x, int y) {}

	public boolean tick(Level level, int xt, int yt) { return false; }

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canFly()  || (e instanceof Player && (Game.isMode("Creative") ||  (((Player) e).suitOn)));
	}
}
