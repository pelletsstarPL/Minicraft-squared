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
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class TreeTileFungus extends Tile {
	
	protected TreeTileFungus(String name) {
		super(name, (ConnectorSprite)null);
		connectsToMoss = true;
	}
	
	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("Moss").render(screen, level, x, y);
		
		boolean u = level.getTile(x, y - 1) == this;
		boolean l = level.getTile(x - 1, y) == this;
		boolean r = level.getTile(x + 1, y) == this;
		boolean d = level.getTile(x, y + 1) == this;
		boolean ul = level.getTile(x - 1, y - 1) == this;
		boolean ur = level.getTile(x + 1, y - 1) == this;
		boolean dl = level.getTile(x - 1, y + 1) == this;
		boolean dr = level.getTile(x + 1, y + 1) == this;

		if (u && ul && l) {
			screen.render(x * 16 + 0, y * 16 + 0, 6 + 19 * 32, 0, 1);
		} else {
			screen.render(x * 16 + 0, y * 16 + 0, 5 + 18 * 32, 0, 1);
		}
		if (u && ur && r) {
			screen.render(x * 16 + 8, y * 16 + 0, 6 + 20 * 32, 0, 1);
		} else {
			screen.render(x * 16 + 8, y * 16 + 0, 6 + 18 * 32, 0, 1);
		}
		if (d && dl && l) {
			screen.render(x * 16 + 0, y * 16 + 8, 6 + 20 * 32, 0, 1);
		} else {
			screen.render(x * 16 + 0, y * 16 + 8, 5 + 19 * 32, 0, 1);
		}
		if (d && dr && r) {
			screen.render(x * 16 + 8, y * 16 + 8, 6 + 19 * 32, 0, 1);
		} else {
			screen.render(x * 16 + 8, y * 16 + 8, 6 + 21 * 32, 0, 1);
		}
	}

	public boolean tick(Level level, int xt, int yt) {
		int damage = level.getData(xt, yt);
		if (damage > 0) {
			level.setData(xt, yt, damage - 1);
			return true;
		}
		return false;
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}
	
	@Override
	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		hurt(level, x, y, dmg);
		return true;
	}
	
	@Override
	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if(Game.isMode("Creative"))
			return false; // Go directly to hurt method
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Axe) {
				int staminaPay=(4-tool.level < 2 ? 2 : 4-tool.level);
				if(4-tool.level<2)staminaPay=2;
				int dmg=random.nextInt(10) + tool.damage;
				if (player.payStamina(staminaPay) && tool.payDurability() && tool.level!=6) {
					hurt(level, xt, yt, dmg);
					return true;
				}else if(player.payStamina(staminaPay) && tool.payDurability() && tool.level==6){
					hurt(level, xt, yt, random.nextInt(3)+2);
				}
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg) {
		if (random.nextInt(100) == 0)
			level.dropItem(x * 16 + 8, y * 16 + 8);
		
		int damage = level.getData(x, y) + dmg;
		int treeHealth = 16;
		if (Game.isMode("Creative")) dmg = damage = treeHealth;
		
		level.add(new SmashParticle(x*16, y*16));
		Sound.monsterHurt.play();

		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= treeHealth) {
			level.dropItem(x * 16 + 8, y * 16 + 8, 0, 5, Items.get("Fungus"));
			level.setTile(x, y, Tiles.get("Moss"));
		} else {
			level.setData(x, y, damage);
		}
	}
}
