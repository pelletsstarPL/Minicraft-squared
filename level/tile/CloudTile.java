package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class CloudTile extends Tile {
	private static ConnectorSprite sprite = new ConnectorSprite(CloudTile.class, new Sprite(0, 22, 3, 3, 1, 3), new Sprite(3, 24, 2, 2, 1, 3), new Sprite(3, 22, 2, 2, 1))
	{
		public boolean connectsTo(Tile tile, boolean isSide) {
			return tile != Tiles.get("Infinite Fall") && tile != Tiles.get("aerocloud");
		}
	};

	protected CloudTile(String name) {
		super(name, sprite);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}
	public void render(Screen screen, Level level, int x, int y) {
		Tile aero= Tiles.get("aerocloud");

		aero.render(screen, level, x, y);
		sprite.render(screen, level, x, y);
	}
	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		// We don't want the tile to break when attacked with just anything, even in creative mode
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel && player.payStamina(4 - tool.level < 0 ? 2 : 4 - tool.level) && tool.payDurability()) {
				if(xt >30*(level.w/128) && xt <level.w-(30*(level.w/128)) && yt>30*(level.h/128) && yt<level.h-(30*(level.h/128)))
				level.setTile(xt, yt, Tiles.get("Aerocloud")); //allow to get aerocloud closer to the Cloud middle
				else level.setTile(xt, yt, Tiles.get("infinite fall"));
				Sound.monsterHurt.play();
				level.dropItem(xt * 16 + 8, yt * 16 + 8, 1, 3, Items.get("Cloud"));
				return true;
			}
		}
		return false;
	}
}
