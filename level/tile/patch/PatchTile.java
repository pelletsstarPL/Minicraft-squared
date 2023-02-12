package minicraft.level.tile.patch;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class PatchTile extends Tile {
    private static Sprite sprite = new Sprite(44, 2, 2, 2, 1);
    private static Sprite vaseBottom = new Sprite(7,14,2,2,1);

    public PatchTile(String name) {
        super(name, sprite);
        maySpawn = true;
    }

    public void render(Screen screen, Level level, int x, int y) {
        int xa = x;
        int ya = y;
        x = x << 4;
        y = y << 4;

       if(level.getData(xa,ya)==0) { //if vase is empty (has no soil or fluid inside)
            vaseBottom.render(screen, x, y);
        }else{
            Tiles.get(level.getData(xa,ya)).render(screen, level, xa, ya);
       }
        sprite.render(screen, x, y);

    }
    public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
        if (item instanceof ToolItem) {
            ToolItem tool = (ToolItem) item;
            if (tool.type == ToolType.Shovel) {
                if (level.getData(xt, yt) > 0) {
                        level.dropItem(xt * 16 + 8, yt * 16 + 8, 1+(Tiles.get(level.getData(xt, yt)).name.contains("MOSS") ? 1 : 0),1+(Tiles.get(level.getData(xt, yt)).name.contains("MOSS") ? 1 : 0),Items.get(Tiles.get(level.getData(xt, yt)).name)); //drop the soil
                        level.setData(xt, yt, 0); //clean the vase
                    return true;
                }else {
                    if (player.payStamina(4 - tool.level < 3 ? 3 : 4 - tool.level) && tool.payDurability()) {
                        level.setTile(xt, yt, Tiles.get(level.depth>0 ? "Cloud" : "Hole"));
                        Sound.monsterHurt.play();
                        level.dropItem(xt * 16 + 8, yt * 16 + 8, Items.get("Patch vase"));
                    }
                    return true;
                }

            }
        }
        return false;
    }
}
