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

public class SmallStoneGrassTile extends Tile {
	private static final Sprite stoneSprite = new Sprite(0, 13, 1);
	private static final Sprite stoneSprite2 = new Sprite(1, 13, 1);

	protected SmallStoneGrassTile(String name) {
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

		if (level.getTile(xn, yn) == Tiles.get("Dirt")) {
			level.setTile(xn, yn, Tiles.get("Grass"));
		}
		return false;
	}
	
	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("Grass").render(screen, level, x, y);
		int data = level.getData(x, y);
		int shape = (data / 16) % 2;

		x = x << 4;
		y = y << 4;

		stoneSprite.render(screen, x + 8 * shape, y, 0,0x80A560);
		stoneSprite2.render(screen, x + 8 * (shape == 0 ? 1 : 0), y + 8, 0,0x80A560);
	}

	public boolean interact(Level level, int x, int y, Player player, Item item, Direction attackDir) {
		double chance=Math.random();
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(2 - tool.level) && tool.payDurability()) {
					level.setTile(x, y, Tiles.get("Grass"));
					Sound.monsterHurt.play();
					if(chance<0.4) level.dropItem(x * 16 + 8, y * 16 + 8, Items.get("Stone"));
					return true;
				}
			}
		}
		return false;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		double chance=Math.random();
		if(chance<0.1)level.dropItem(x *16 + 8, y * 16 + 8, 0, 1, Items.get("Stone"));
		level.setTile(x, y, Tiles.get("Grass"));
		return true;
	}
}
