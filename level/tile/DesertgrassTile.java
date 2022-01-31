package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class DesertgrassTile extends Tile {
	private static final Sprite flowerSprite = new Sprite(11, 6, 1);
	
	protected DesertgrassTile(String name) {
		super(name, (ConnectorSprite)null);
		connectsToSand = true;
		maySpawn = true;
	}

	public boolean tick(Level level, int xt, int yt) {
		// TODO revise this method.
		if (random.nextInt(30) != 0) return false; // Skips every 31 tick.

		int xn = xt;
		int yn = yt;

		if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
		else yn += random.nextInt(2) * 2 - 1;
		return false;
	}
	
	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("Sand").render(screen, level, x, y);
		int data = level.getData(x, y);
		int shape = (data / 16) % 2;

		x = x << 4;
		y = y << 4;

		flowerSprite.render(screen, (x+1) + 8 * 0, y, 0,0xE2E26F);
		flowerSprite.render(screen, x + 8 * 1, y, 0,0xE2E26F);
		flowerSprite.render(screen, x + 8 * 1, y + 8, 1,0xE2E26F);
		flowerSprite.render(screen, x + 8 * 0, y + 8, 1,0xE2E26F);
	}

	public boolean interact(Level level, int x, int y, Player player, Item item, Direction attackDir) {
			if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(2 - tool.level) && tool.payDurability()) {
					level.setTile(x, y, Tiles.get("Sand"));
					Sound.monsterHurt.play();
					double chance=Math.random();
						if(chance<0.004){
							level.dropItem(x * 16 + 8, y * 16 + 8, Items.get("Wheat Seeds"));
						}
					return true;
				}
			}
		}
		return false;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		double chance=Math.random();
		level.setTile(x, y, Tiles.get("Sand"));
		return true;
	}
}
