package minicraft.level.tile;

import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.*;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.level.Level;

public class SpikeWallTile extends Tile {
	private static ConnectorSprite[] states = new ConnectorSprite[2];
	static {
		states[0] = new ConnectorSprite(SpikeWallTile.class, new Sprite(51, 14, 3, 3, 1, 3), new Sprite(54, 14, 2, 2, 1, 3), new Sprite(62, 14, 2, 2, 1, 3));
		states[1] = new ConnectorSprite(SpikeWallTile.class, new Sprite(59, 14, 3, 3, 1, 3), new Sprite(54, 14, 2, 2, 1, 3), new Sprite(62, 14, 2, 2, 1, 3));
	}

	protected SpikeWallTile(String name) {
		super(name, states[0]);
		maySpawn = false;
	}


	
	public void render(Screen screen, Level level, int x, int y) {
		if(level.getData(x,y)==1) states[1].render(screen, level,x, y );
		else states[0].render(screen, level, x , y );
	}
	
	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		return false;
	}

	public void bumpedInto(Level level, int x, int y, Entity entity) {
		if( !(entity instanceof Wraith) && !(entity instanceof Ghost) &&!(entity instanceof Knight) &&!(entity instanceof ObsidianKnight) &&!(entity instanceof Snake) && level.getData(x,y)==1) {

			Mob m = (Mob) entity;
			byte dmg = 3;
			if (Settings.get("diff").equals("Hard") && entity instanceof Player) dmg++;

			m.hurt(this, x, y, dmg);
		}
	}

	@Override
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}
}
