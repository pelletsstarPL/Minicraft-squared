package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
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
		switch (type) {
			case Wood: sprite = new Sprite(5, 14, 2, 2, 1, 0); break;
			case Stone: sprite = new Sprite(15, 14, 2, 2, 1, 0); break;
			case Obsidian: sprite = new Sprite(25, 14, 2, 2, 1, 0); break;
			case ObsidianD: sprite = new Sprite(25, 14, 2, 2, 1, 0); break;
			case Dungeon: sprite = new Sprite(25, 22, 2, 2, 1, 0); break;
		}
		super.sprite = sprite;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == type.getRequiredTool()) {
				if(type==Material.Wood || type==Material.Stone || type==Material.Dungeon) {
					if (player.payStamina(4 - tool.level) && tool.payDurability()) {
						if (level.depth == 1) {
							level.setTile(xt, yt, Tiles.get("Cloud"));
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
							if (level.depth == 1) {
								level.setTile(xt, yt, Tiles.get("Cloud"));
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

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}
}
