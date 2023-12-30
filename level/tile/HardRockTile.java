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

public class HardRockTile extends Tile {
	private HRType type;
	public enum HRType {
		One(200,4, new ConnectorSprite(HardRockTile.class, new Sprite(18, 9, 3, 3, 1, 3), new Sprite(21, 10, 2, 2, 1, 3),new Sprite(26, 30, 2, 2, 1))),
		Two(300,5, new ConnectorSprite(HardRockTile.class, new Sprite(47, 9, 3, 3, 1, 3), new Sprite(50, 10, 2, 2, 1, 3),new Sprite(28, 30, 2, 2, 1)));
		private int health,minLevel;
		private ConnectorSprite sprite;
		HRType(int health,int minLevel,ConnectorSprite sprite) {
				this.health = health;
				this.minLevel = minLevel;
				this.sprite = sprite;
		}
	}
	
	protected HardRockTile(String name, HRType type) {
		super(name, type.sprite);
		this.type= type;
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
			int staminaPay=(4-tool.level < 2 ? 2 : 4-tool.level);
			if(tool.type==ToolType.Hammer)staminaPay++;
			int dmg = (int)(random.nextInt(10) + tool.damage * (tool.type == ToolType.Hammer ? 1.2 : 1));
			if (tool.type == ToolType.Pickaxe && (tool.level >= type.minLevel) && tool.level!=6) { //beyond req lvl and except candy tools
				if (player.payStamina(staminaPay) && tool.payDurability()) {
					hurt(level, xt, yt, dmg);
					return true;
				}
			} else {
				Game.notifications.add(ToolItem.LEVEL_NAMES[type.minLevel] + " tool or stronger Required.");
			}
		}
		return false;
	}
	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + dmg;
		int hrHealth = type.health;
		if (Game.isMode("Creative")) dmg = damage = hrHealth;
		level.add(new SmashParticle(x * 16, y * 16));
		Sound.monsterHurt.play();

		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= hrHealth) {
			if(level.depth>=1) level.setTile(x, y, Tiles.get("cloud"));
			else level.setTile(x, y, Tiles.get("dirt"));
			if(type.name()=="Two"){
				level.dropItem(x * 16 + 8, y * 16 + 8, 3, 9, Items.get("Iron"));
				level.dropItem(x * 16 + 8, y * 16 + 8, 3, 7, Items.get("Gold"));
				level.dropItem(x * 16 + 8, y * 16 + 8, 2, 5, Items.get("Lapis"));
				level.dropItem(x * 16 + 8, y * 16 + 8, 2, 6, Items.get("Gem"));
			}else {
				level.dropItem(x * 16 + 8, y * 16 + 8, 1, 3, Items.get("Stone"));
				level.dropItem(x * 16 + 8, y * 16 + 8, 0, 1, Items.get("Coal"));
			}
		} else {
			level.setData(x, y, damage);
		}
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		if (level.depth < 1) {
			Tiles.get("dirt").render(screen, level, x, y);
		}else{
			Tiles.get("cloud").render(screen, level, x, y);
		}
		super.render(screen, level, x, y);
	}
}
