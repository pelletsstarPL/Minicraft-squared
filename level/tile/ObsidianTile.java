package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.gfx.Color;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class ObsidianTile extends Tile {
    private static Sprite sprite = new Sprite(27, 14, 2, 2, 1);

    public ObsidianTile(String name) {
        super(name, sprite);
    }
	protected static int dCol(int depth) {
        return Color.get(1, 86, 30, 100); // Surface.
    };
    public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
        if (item instanceof ToolItem) {
            ToolItem tool = (ToolItem) item;
            if ((tool.type == ToolType.Pickaxe && tool.level>1 && tool.level!=6)) {
                if (player.payStamina(4) && tool.payDurability()) {
                    level.setTile(xt, yt, Tiles.get("Hole"));
                    Sound.monsterHurt.play();
                    level.dropItem(xt * 16 + 8, yt * 16 + 8,1,2, Items.get("Obsidian"));
                    return true;
                }
            }else{
				Game.notifications.add("Iron Pickaxe or stronger Required.");
			}
        }
        return false;
    }
}
