package minicraft.level.tile;

import minicraft.core.Renderer;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class CoarseDirtTile extends Tile {
	private static Sprite[] levelSprite = new Sprite[4];
	static {
		levelSprite[0] = new Sprite(38, 0, 2, 2, 1);
		levelSprite[1] = new Sprite(38, 2, 2, 2, 1);
		levelSprite[2] = new Sprite(38, 4, 2, 2, 1);
	}

	protected CoarseDirtTile(String name) {
		super(name, levelSprite[0]);
		maySpawn = true;
	}

	protected static int CdCol(int depth) {
		if(Renderer.player.getRealmId()==1)return Color.get(1, 76, 30, 100); // Dungeons.
		switch (Level.getLevelName(depth)) {
			case "Surface": return Color.get(1, 129, 105, 83); // Surface.
			case "Dungeon": return Color.get(1, 76, 30, 100); // Dungeons.
			default: return Color.get(1, 102); // Caves.
		}
	}

	protected static int dIdx(int depth) {
		if(Renderer.player.getRealmId()==1)return 2; // Dungeons.
		switch (Level.getLevelName(depth)) {
			case "Surface": return 0; // Surface
			case "Dungeon": return 2; // Dungeons
			default: return 1; // Caves
		}
	}
	
	public void render(Screen screen, Level level, int x, int y) {
		levelSprite[dIdx(level.depth)].render(screen, x * 16, y * 16, 0);
	}
	
	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					level.setTile(xt, yt, Tiles.get("Hole"));
					Sound.monsterHurt.play();
					level.dropItem(xt * 16 + 8, yt * 16 + 8, Items.get("Coarse Dirt"));
					double chance=Math.random();
					if(chance<0.003)level.dropItem(xt * 16 + 8, yt * 16 + 8, Items.get("Stone"));
					return true;
				}
			}
			if (tool.type == ToolType.Hoe) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					level.setTile(xt, yt, Tiles.get("Dirt"));
					Sound.monsterHurt.play();
					return true;
				}
			}
		}
		return false;
	}
}
