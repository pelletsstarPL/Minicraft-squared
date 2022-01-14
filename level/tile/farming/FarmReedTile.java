package minicraft.level.tile;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.Wraith;
import minicraft.gfx.Sprite;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class FarmReedTile extends Tile {
    private static Sprite sprite = new Sprite(2, 0, 2, 2, 1, false, new int[][] {{1, 0}, {0, 1}});

    public FarmReedTile(String name) {
        super(name, sprite);
        connectsToFluid = true;
    }
    public boolean mayPass(Level level, int x, int y, Entity e) {
        return e.canSwim();
    }
    protected FarmReedTile(String name, Sprite sprite) {
        super(name, sprite);
    }

    @Override
    public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
        if (item instanceof ToolItem) {
            ToolItem tool = (ToolItem) item;
                if (player.payStamina(2) && tool.payDurability()) {
                    level.setTile(xt, yt, Tiles.get("Water"));
                    Sound.monsterHurt.play();
                    return true;
                }
        }
        return false;
    }

    @Override
    public boolean tick(Level level, int xt, int yt) {
        int age = level.getData(xt, yt);
        if (age < 5) level.setData(xt, yt, age + 2);
        return true;
    }

    @Override
    public void steppedOn(Level level, int xt, int yt, Entity entity) {
            if (entity instanceof ItemEntity) return;
            if (random.nextInt(60) != 0) return;
            if (level.getData(xt, yt) < 5) return;
    }
}
