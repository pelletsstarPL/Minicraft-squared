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

public class CloudgrassTile extends Tile {
	private static final Sprite flowerSprite = new Sprite(5, 22, 1);

	protected CloudgrassTile(String name) {
		super(name, (ConnectorSprite)null);
		connectsToGrass = true;
		maySpawn = true;
	}

	public boolean tick(Level level, int xt, int yt) {
		// TODO revise this method.
		if (random.nextInt(40) != 0) return false;

		int xn = xt;
		int yn = yt;

		if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
		else yn += random.nextInt(2) * 2 - 1;
		return false;
	}
	
	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("Cloud").render(screen, level, x, y);
		int data = level.getData(x, y);
		int shape = (data / 16) % 2;

		x = x << 4;
		y = y << 4;

		flowerSprite.render(screen, (x+1) + 8 * 0, y, 0,0x80A560);
		flowerSprite.render(screen, x + 8 * 1, y, 0,0x80A560);
		flowerSprite.render(screen, x + 8 * 1, y + 8, 1,0x80A560);
		flowerSprite.render(screen, x + 8 * 0, y + 8, 1,0x80A560);
	}

	public boolean interact(Level level, int x, int y, Player player, Item item, Direction attackDir) {
			if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(2 - tool.level) && tool.payDurability()) {
					level.setTile(x, y, Tiles.get("Cloud"));
					Sound.monsterHurt.play();
					return true;
				}
			}
		}
		return false;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		level.setTile(x, y, Tiles.get("Cloud"));
		return true;
	}
}
