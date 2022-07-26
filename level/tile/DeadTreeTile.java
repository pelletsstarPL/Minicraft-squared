package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class DeadTreeTile extends Tile {
	private static Sprite sprite = new Sprite(2, 4, 2, 2, 1);
	
	protected DeadTreeTile(String name) {
		super(name, sprite);
		connectsToSand = true;
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
				if (player.payStamina(staminaPay) && tool.payDurability() && tool.level!=6) {
					hurt(level, xt, yt, random.nextInt(10) + (tool.level) * 5 + 10);
					return true;
				}else if(player.payStamina(staminaPay) && tool.payDurability() && tool.level==6){
					hurt(level, xt, yt, random.nextInt(2) + 3 * 2 + 1);
				}
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + dmg;
		int treeHealth = 15;
		if (Game.isMode("Creative")) dmg = damage = treeHealth;

		level.add(new SmashParticle(x*16, y*16));
		Sound.monsterHurt.play();

		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= treeHealth) {
			level.dropItem(x * 16 + 8, y * 16 + 8, 0, 1, Items.get("Wood"));
			level.dropItem(x * 16 + 8, y * 16 + 8, 0, 4, Items.get("Stick"));
			level.setTile(x, y, Tiles.get("Sand"));
		} else {
			level.setData(x, y, damage);
		}
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("Sand").render(screen, level, x, y);

		sprite.render(screen, x << 4, y << 4);
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
