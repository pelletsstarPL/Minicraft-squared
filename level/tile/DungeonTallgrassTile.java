package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class DungeonTallgrassTile extends Tile {

	private static Sprite sprite = new Sprite(21, 18, 2, 2, 1);


	//private int tileId = Tiles.get("dirt").id;
	protected  DungeonTallgrassTile(String name) {
		super(name, (ConnectorSprite)null);
	}

	public void render(Screen screen, Level level, int x, int y) {
			Tiles.get("dirt").render(screen, level, x, y);
			sprite.render(screen, x<<4, y<<4);
	}


	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}

	@Override
	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		level.setTile(x, y, Tiles.get("Dirt"));
		return true;
	}

	@Override
	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {

		return true;
	}

	/** Update method */
	//public boolean tick(Level level, int xt, int yt) {if(Updater.tickCount%200==0)tileId = level.getData(xt ,yt);return false; }
	
}
