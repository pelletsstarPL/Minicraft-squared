package minicraft.level.tile;

import minicraft.core.Game;
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

public class HardRockTwoTile extends Tile {
	// Theoretically the full sprite should never be used, so we can use a placeholder
	private static ConnectorSprite sprite = new ConnectorSprite(HardRockTwoTile.class, new Sprite(47, 9, 3, 3, 1, 3), new Sprite(50, 10, 2, 2, 1, 3),new Sprite(28, 30, 2, 2, 1));
	
	protected HardRockTwoTile(String name) {
		super(name, sprite);
	}
	
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		hurt(level, x, y, 0);
		return true;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if(Game.isMode("Creative"))
			return false; // Go directly to hurt method
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			int dmg = random.nextInt(10) + tool.damage;
			if (tool.type == ToolType.Pickaxe && tool.level == 5) {
				if (player.payStamina(2) && tool.payDurability()) {
					hurt(level, xt, yt, random.nextInt(10) + (tool.level) * 5 + 10);
					return true;
				}
			} else {
			//	Game.notifications.add("Zanite Pickaxe or stronger Required.");
				Game.notifications.add("Planned for 3.0.0 release");
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + dmg;
		int hrHealth = 250;
		if (Game.isMode("Creative")) dmg = damage = hrHealth;
		level.add(new SmashParticle(x * 16, y * 16));
		Sound.monsterHurt.play();

		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= hrHealth) {
			if(level.depth>=1) level.setTile(x, y, Tiles.get("cloud"));
			else level.setTile(x, y, Tiles.get("dirt"));
			level.dropItem(x * 16 + 8, y * 16 + 8, 1, 3, Items.get("Stone"));
			level.dropItem(x * 16 + 8, y * 16 + 8, 1, 3, Items.get("Gem"));
			level.dropItem(x * 16 + 8, y * 16 + 8, 2, 9, Items.get("Lapis"));
			level.dropItem(x * 16 + 8, y * 16 + 8, 2, 4, Items.get("Iron"));
			level.dropItem(x * 16 + 8, y * 16 + 8, 0, 1, Items.get("Gold"));
		} else {
			level.setData(x, y, damage);
		}
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		if (level.depth < 1) {
			sprite.sparse.color = DirtTile.dCol(level.depth);
		}else{
			sprite.sparse.color = 0xC3C3C3;
		}

		super.render(screen, level, x, y);
	}

	public boolean tick(Level level, int xt, int yt) {
		int damage = level.getData(xt, yt);
		if (damage > 0) {
			level.setData(xt, yt, damage - 1);
			return true;
		}
		return false;
	}
}
