package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.AirWizardPhase4;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.level.Level;

public class WallTile extends Tile {

	private static final String obrickMsg = "The airwizard must be defeated first.";
	protected Material type;
	private ConnectorSprite sprite;

	protected WallTile(Material type) {
		super(type.name() + " Wall", (ConnectorSprite) null);
		this.type = type;
		switch (type) {
			case Wood: sprite = new ConnectorSprite(WallTile.class, new Sprite(0, 14, 3, 3, 1, 3), new Sprite(3, 14, 2, 2, 1, 3), new Sprite(1, 15, 2, 2, 1, 0, true)); break;
			case Stone: sprite = new ConnectorSprite(WallTile.class, new Sprite(10, 14, 3, 3, 1, 3), new Sprite(13, 14, 2, 2, 1, 3), new Sprite(11, 15, 2, 2, 1, 0, true)); break;
			case Obsidian: sprite = new ConnectorSprite(WallTile.class, new Sprite(20, 14, 3, 3, 1, 3), new Sprite(23, 14, 2, 2, 1, 3), new Sprite(21, 15, 2, 2, 1, 0, true)); break;
			case ObsidianD: sprite = new ConnectorSprite(WallTile.class, new Sprite(20, 14, 3, 3, 1, 3), new Sprite(23, 14, 2, 2, 1, 3), new Sprite(21, 15, 2, 2, 1, 0, true)); break;
			case Dungeon: sprite = new ConnectorSprite(WallTile.class, new Sprite(20, 22, 3, 3, 1, 3), new Sprite(23, 22, 2, 2, 1, 3), new Sprite(21, 23, 2, 2, 1, 0, true)); break;
		}
		csprite = sprite;
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}

	@Override
	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		if (Game.isMode("Creative") || level.depth != -3 || type != Material.ObsidianD || AirWizardPhase4.beaten) {
			hurt(level, x, y, random.nextInt(6) / 6 * dmg / 2);
			return true;
		} else {
			Game.notifications.add(obrickMsg);
			return false;
		}
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (Game.isMode("Creative"))
			return false; // Go directly to hurt method
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (type == Material.Obsidian) {
				if (tool.level > 1 && tool.level != 6 && tool.type == type.getRequiredTool()) {
					if (player.payStamina(4 - tool.level) && tool.payDurability()) {
						hurt(level, xt, yt, random.nextInt(10) + (tool.level) * 5 + 10);
						return true;
					}
				} else {
					Game.notifications.add("Iron pickaxe or stronger Required.");
					return false;
				}
			} else if (type == Material.ObsidianD) {
				if (AirWizardPhase4.beaten) {
					if ((tool.level > 1 && tool.level != 6) && level.depth == -4 && tool.type == type.getRequiredTool()) {
						if (player.payStamina(4 - tool.level) && tool.payDurability()) {
							hurt(level, xt, yt, random.nextInt(10) + (tool.level) * 5 + 10);
							return true;
						}
					}
				} else {
					Game.notifications.add(obrickMsg);
					return false;
				}
			}
			if (tool.type == type.getRequiredTool()) {

						if (player.payStamina(4 - tool.level) && tool.payDurability()) {
							hurt(level, xt, yt, random.nextInt(10) + (tool.level) * 5 + 10);
							return true;
						}

			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + dmg;
		int sbwHealth = 0;
		switch(type) {
			case Wood: sbwHealth=75;break;
			case Stone: sbwHealth=90;break;
			case Dungeon: sbwHealth=90;break;
			case Obsidian: sbwHealth=120;break;
			case ObsidianD: sbwHealth=120;break;
		};
		if (Game.isMode("Creative")) dmg = damage = sbwHealth;

		level.add(new SmashParticle(x * 16, y * 16));
		Sound.monsterHurt.play();

		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= sbwHealth) {
			String itemName = "", tilename = "";
			switch (type) { // Get what tile to set and what item to drop
				case Wood: {
					itemName = "Plank";
					tilename = "Wood Planks";
					break;
				}
				case Stone: {
					itemName = "Stone Brick";
					tilename = "Stone Bricks";
					break;
				}
				case Obsidian: {
					itemName = "Obsidian Brick";
					tilename = "Obsidian";
					break;
				}
				case ObsidianD: {
					itemName = "Obsidian Brick";
					tilename = "Obsidian";
					break;
				}case Dungeon: {
					itemName = "Stone Brick";
					tilename = "Dungeon Bricks";
					break;
				}
			}
			if(type!=Material.Dungeon && type!=Material.ObsidianD)level.dropItem(x * 16 + 8, y * 16 + 8, 1, 2, Items.get(itemName));
			 if(type==Material.Dungeon)level.dropItem(x * 16 + 8, y * 16 + 8, 1, 2, Items.get("Stone Brick"));
			else if(type==Material.ObsidianD)level.dropItem(x * 16 + 8, y * 16 + 8, 1, 2, Items.get("Obsidian Brick"));
			level.setTile(x, y, Tiles.get(tilename));
		} else {
			level.setData(x, y, damage);
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

	public String getName(int data) {
		if(Material.values[data].name()!="Dungeon" && Material.values[data].name()!="ObsidianD")return Material.values[data].name() + " Wall";
		else return "Stone Wall";
	}
}
