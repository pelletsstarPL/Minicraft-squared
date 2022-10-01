package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class LavaBrickTile extends Tile {
	private static Sprite sprite = new Sprite(27, 16, 2, 2, 1);

	protected LavaBrickTile(String name) {

		super(name, sprite);
		connectsToFluid = true;
	}
	public void render(Screen screen, Level level, int x, int y) {
		if (Tiles.get("lava") != null) {
			Tiles.get("lava").render(screen, level, x, y);
		}
		screen.render(x * 16 + 0, y * 16 + 0, 27 + 16 * 32, 0, 1);
		screen.render(x * 16 + 8, y * 16 + 0, 28 + 16 * 32, 0, 1);
		screen.render(x * 16 + 0, y * 16 + 8, 27 + 17 * 32, 0, 1);
		screen.render(x * 16 + 8, y * 16 + 8, 28 + 17 * 32, 0, 1);
	}
	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Pickaxe && tool.level>1 && tool.level!=6 || Game.isMode("creative")) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					level.setTile(xt, yt, Tiles.get("Lava"));
					Sound.monsterHurt.play();
					return true;
				}
			}else if(tool.type == ToolType.Pickaxe && (tool.level<=1 || tool.level==6)){
				Game.notifications.add("Iron Pickaxe or stronger Required.");
			}
		}
		return false;
	}


	public boolean mayPass(Level level, int x, int y, Entity e) { return true; }
}
