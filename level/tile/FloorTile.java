package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.ObsidianKnight;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.level.Level;

public class FloorTile extends Tile {
	protected Material type;
	private Sprite sprite;

	protected FloorTile(Material type) {
		super((type == Material.Wood ? "Wood Planks" : type == Material.Obsidian ? "Obsidian" : type.name() + " Bricks"), (Sprite) null);
		this.type = type;
		maySpawn = true;
		if(type==Material.Obsidian)connectsToObsidianBridgeSupport = true;
		switch (type) {
			case Wood: sprite = new Sprite(5, 14, 2, 2, 1, 0); break;
			case Stone: sprite = new Sprite(15, 14, 2, 2, 1, 0); break;
			case Obsidian: sprite = new Sprite(35, 14, 2, 2, 1, 0); break;
			case ObsidianD: sprite = new Sprite(35, 14, 2, 2, 1, 0); break;
			case Dungeon: sprite = new Sprite(26,14, 2, 2, 1, 0); break;
		}
		super.sprite = sprite;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == type.getRequiredTool() && !ObsidianKnight.active) {
				if(type==Material.Wood || type==Material.Stone || type==Material.Dungeon) {
					if (player.payStamina(4 - tool.level) && tool.payDurability()) {
						if (level.depth == 1 &&  level.realm.contains("overworld")) {
							level.setTile(xt, yt, Tiles.get("Cloud"));
						} else if((level.depth == 1) &&  level.realm.contains("dungeon realm")) {
							level.setTile(xt, yt, Tiles.get("Obsidian Bridge Support"));
						} else {
							level.setTile(xt, yt, Tiles.get("Hole"));
						}
						Item drop;
						switch (type) {
							case Wood:
								drop = Items.get("Plank");
								break;
							case Dungeon:
								drop = Items.get("Stone Brick");break;
							default:
								drop = Items.get(type.name() + " Brick");
								break;
						}
						Sound.monsterHurt.play();
						if(type!=Material.ObsidianD) level.dropItem(xt * 16 + 8, yt * 16 + 8, drop);
						return true;
					}
				}else{
					if(tool.level<=1 || tool.level==6) {
						Game.notifications.add("Iron pickaxe or stronger required.");
					}else{
						if (player.payStamina(3) && tool.payDurability()) {
							if (level.depth == 1 &&  level.realm.contains("overworld")) {
								level.setTile(xt, yt, Tiles.get("Cloud"));
							} else if((level.depth == 1) &&  level.realm.contains("dungeon realm")) {
								level.setTile(xt, yt, Tiles.get("Obsidian Bridge Support"));
							} else {
								level.setTile(xt, yt, Tiles.get("Hole"));
							}
							Sound.monsterHurt.play();
							if(type!=Material.ObsidianD) level.dropItem(xt * 16 + 8, yt * 16 + 8, Items.get("Obsidian Brick"));
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public void render(Screen screen, Level level, int x, int y) {
		if (sprite != null)
			sprite.render(screen, x << 4, y << 4);
		if (csprite != null) {
			if((type==Material.Obsidian || type==Material.ObsidianD) && level.depth == 1 && level.realm.contains("dungeon"))Tiles.get("Obsidian bridge support").render(screen,level,x,y);
			csprite.render(screen, level, x, y);
		}
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}
}
