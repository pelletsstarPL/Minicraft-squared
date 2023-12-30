package minicraft.level.tile.farming;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class FarmTile extends Tile {

   private  int pick; //picks a farmland texture

    public FarmTile(String name) {
        super(name, new Sprite(0,32,1,1,1));
    }
    protected FarmTile(String name, Sprite sprite) {
        super(name, sprite);
    }

    @Override
    public void render(Screen screen, Level level, int x, int y) {
        if(level.realm.contains("dungeon") || level.depth<=-6)pick=2;
        else if(level.depth < 0)pick=1;
        else pick = 0;
        new Sprite(pick,(32 + (Plant.IfWater(level,x,y) ? 1 : 0)),1,1,1).render(screen,x * 16, y * 16);
          new Sprite(pick,(32 + (Plant.IfWater(level,x,y) ? 1 : 0)),1,1,1).render(screen,x * 16 + 8, y * 16 + 0);
          new Sprite(pick,(32 + (Plant.IfWater(level,x,y) ? 1 : 0)),1,1,1).render(screen,x * 16 + 0, y * 16 + 8);
          new Sprite(pick,(32 + (Plant.IfWater(level,x,y) ? 1 : 0)),1,1,1).render(screen,x * 16 + 8, y * 16 + 8 );

    }
    @Override
    public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
        if (item instanceof ToolItem) {
            ToolItem tool = (ToolItem) item;
            if (tool.type == ToolType.Shovel) {
                if (player.payStamina(4 - tool.level) && tool.payDurability()) {
                    if(name.contains("REED"))level.setTile(xt, yt, Tiles.get("Water"));
                    else level.setTile(xt, yt, Tiles.get("Dirt"));
                    Sound.monsterHurt.play();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean tick(Level level, int xt, int yt) {
        int age = level.getData(xt, yt);
        if (age < 5) level.setData(xt, yt, age + 1);
        return true;
    }

    @Override
    public void steppedOn(Level level, int xt, int yt, Entity entity) {
        if (entity instanceof ItemEntity) return;
        if (random.nextInt(60) != 0) return;
        if (level.getData(xt, yt) < 5) return;
        if(level.getTile(xt, yt).name=="Reed")level.setTile(xt, yt, Tiles.get("Water"));
            level.setTile(xt,yt,Tiles.get("Dirt"));
    }
}
