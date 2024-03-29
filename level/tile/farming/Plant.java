package minicraft.level.tile.farming;

import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.mob.*;
import minicraft.gfx.Sprite;
import minicraft.item.Items;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class Plant extends FarmTile {
    protected static int maxAge = 100;
    private String name;

    protected Plant(String name) {
        super(name, (Sprite)null);
        this.name = name;
    }

    @Override
    public void steppedOn(Level level, int xt, int yt, Entity entity) {
        if(!(entity instanceof Wraith) && !(entity instanceof Ghost)) {
            if (entity instanceof ItemEntity) return;

            super.steppedOn(level, xt, yt, entity);
            if(random.nextInt(level.getTile(xt,yt).name.contains("REED") ? 2 : 30)==0) harvest(level, xt, yt, entity);
        }
    }

    @Override
    public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
        harvest(level, x, y, source);
        return true;
    }

    @Override
    public boolean tick(Level level, int xt, int yt) {
        if (random.nextInt(2) == 0) return false;

        int age = level.getData(xt, yt);
        if (age < maxAge && level.realm.contains("overworld")) {
            if (!IfWater(level, xt, yt)) level.setData(xt, yt, age + 1);
            else if (IfWater(level, xt, yt)) level.setData(xt, yt, age + (random.nextInt(12)==0 ? 3 : 2));
            if(age > maxAge)age = maxAge;
            return true;
        }

        return false;
    }

    public static boolean IfWater(Level level, int xs, int ys) {
        Tile[] areaTiles = level.getAreaTiles(xs, ys, 1);
        for(Tile t: areaTiles) {
            if (t.name.contains("WATER") ||  t.name.contains("REED"))
                return true;
        }

        return false;
    }

    /** Default harvest method, used for everything that doesn't really need any special behavior. */
    protected void harvest(Level level, int x, int y, Entity entity) {
        int age = level.getData(x, y);

        if(name!="Reed")level.dropItem(x * 16 + 8, y * 16 + 8, 1,Math.random()<0.3 ? 2 : 1 , Items.get(name + " Seeds"));

        int count = 0;
        if (age >= maxAge) {
            count = random.nextInt(3) + 2;
        } else if (age >= maxAge - maxAge / 5) {
            count = random.nextInt(2) + 1;
        }

        level.dropItem(x * 16 + 8, y * 16 + 8, count, Items.get(name));

        if (age >= maxAge && entity instanceof Player) {
            ((Player)entity).addScore(random.nextInt(5) + 1);
        }
        if(name.contains("Reed"))level.setTile(x, y, Tiles.get("Water"));
        else {
            if(IfWater(level,x,y) && Math.random()<0.1)

            level.setTile(x, y, Tiles.get("Farmland"));
            else
            level.setTile(x, y, Tiles.get("Dirt"));
        }
    }
}
