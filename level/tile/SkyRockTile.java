package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.entity.mob.*;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
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

public class SkyRockTile extends Tile {
	// Theoretically the full sprite should never be used, so we can use a placeholder
	private static ConnectorSprite sprite = new ConnectorSprite(SkyRockTile.class, new Sprite(32, 9, 3, 3, 1, 3), new Sprite(35, 9, 2, 2, 1, 3),new Sprite(24, 30, 2, 2, 1));

	protected SkyRockTile(String name) {
		super(name, sprite);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {

		if(e instanceof WraithA || e instanceof Clallay ) return true;
		else return false;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		hurt(level, x, y, 0);
		return true;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.level!=6 && tool.type == ToolType.Pickaxe && player.payStamina(4 - tool.level) && tool.payDurability()) {
				hurt(level, xt, yt, random.nextInt(10) + (tool.level) * 5 + 10);
				return true;
			}else if (tool.level==6 && tool.type == ToolType.Pickaxe && player.payStamina(2) && tool.payDurability()) {
				hurt(level, xt, yt, random.nextInt(10) + (2) * 5 + 2);
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + dmg;
		int hrHealth = 49;
		if (Game.isMode("Creative")) dmg = damage = hrHealth;
		level.add(new SmashParticle(x * 16, y * 16));
		Sound.monsterHurt.play();

		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= hrHealth) {
			if(level.depth>=1) level.setTile(x, y, Tiles.get("cloud"));
			else level.setTile(x, y, Tiles.get("dirt"));
			level.dropItem(x * 16 + 8, y * 16 + 8, 1, 3, Items.get("Stone"));
			level.dropItem(x * 16 + 8, y * 16 + 8, 0, 1, Items.get("Coal"));
		} else {
			level.setData(x, y, damage);
		}
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		if (level.depth < 1) {
			sprite.sparse.color = DirtTile.dCol(level.depth);
		}else{
			if (Tiles.get("cloud") != null) {
				Tiles.get("cloud").render(screen, level, x, y);
			}
			//sprite.sparse.color = 0xC3C3C3;
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
