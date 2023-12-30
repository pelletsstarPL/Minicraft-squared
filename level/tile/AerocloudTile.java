package minicraft.level.tile;

import minicraft.core.Updater;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Zombie;
import minicraft.entity.mob.Player;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class AerocloudTile extends Tile {
	private static ConnectorSprite sprite = new ConnectorSprite(AerocloudTile.class, new Sprite(17, 22, 3, 3, 1, 3), new Sprite(20, 22, 3, 3, 1, 3), new Sprite(20, 22, 2, 2, 1))
	{
		public boolean connectsTo(Tile tile, boolean isSide) {
			return tile != Tiles.get("Infinite Fall") ;
		}
	};
	
	protected AerocloudTile(String name) {
		super(name, sprite);isSurface =false;
	}


	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		// We don't want the tile to break when attacked with just anything, even in creative mode
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel && player.payStamina(4 - tool.level < 0 ? 2 : 4 - tool.level) && tool.payDurability()) {
				level.setTile(xt, yt, Tiles.get("Infinite Fall")); // Would allow you to shovel cloud, I think.
				Sound.monsterHurt.play();
				//level.dropItem(xt * 16 + 8, yt * 16 + 8, 1, 3, Items.get("Dark Cloud"));
				return true;
			}
		}
		return false;
	}
	public void render(Screen screen, Level level, int x, int y) {
		long seed = (tickCount + (x / 2 - y) * 4311) / 10 * 54687121l + x * 3271612l + y * 3412987161l;
		sprite.full = Sprite.randomDots(seed, 2);
		sprite.render(screen, level, x, y);
	}
	public void steppedOn(Level level, int x, int y, Entity entity) {
		if (entity instanceof Player && Updater.tickCount%13==0) {
			((Player) entity).stamina--;
		}
	}
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canSwim()||e.canFly() || e instanceof Zombie;
	}
}
