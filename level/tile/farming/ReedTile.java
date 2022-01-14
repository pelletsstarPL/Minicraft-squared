package minicraft.level.tile.farming;

import minicraft.level.tile.Tiles;
import minicraft.gfx.Screen;
import minicraft.entity.Entity;
import minicraft.level.Level;
import minicraft.gfx.ConnectorSprite;

public class ReedTile extends Plant
{
    protected ConnectorSprite csprite;

    public ReedTile(final String name) {
        super(name);
        this.connectsToFluid = true;
    }

    public boolean mayPass(final Level level, final int x, final int y, final Entity e) {
        return e.canSwim();
    }

    public void render(final Screen screen, final Level level, final int x, final int y) {
        int xn = x;
        int yn = y;

        if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
        else yn += random.nextInt(2) * 2 - 1;

        if (level.getTile(xn, yn) == Tiles.get("Hole")) {
            level.setTile(xn, yn, Tiles.get("Water"));
        }
        final int age = level.getData(x, y);
        final int icon = age / (ReedTile.maxAge / 4);
        Tiles.get("Water").render(screen, level, x, y);
        screen.render(x * 16 + 0, y * 16 + 0, 290 + (icon*2), 0, 1);
        screen.render(x * 16 + 8, y * 16 + 0, 291 + (icon*2), 0, 1);
        screen.render(x * 16 + 0, y * 16 + 8, 322 + (icon*2), 0, 1);
        screen.render(x * 16 + 8, y * 16 + 8, 323 + (icon*2), 0, 1);

    }
   /* public boolean tick(Level level, int xt, int yt) {
        int xn = xt;
        int yn = yt;

        if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
        else yn += random.nextInt(2) * 2 - 1;

        if (level.getTile(xn, yn) == Tiles.get("Hole")) {
            level.setTile(xn, yn, Tiles.get("Water"));
        }

        // These set only the non-diagonally adjacent lava tiles to obsidian
        for (int x = -1; x < 2; x++) {
            if (level.getTile(xt + x, yt) == Tiles.get("Lava"))
                level.setTile(xt + x, yt, Tiles.get("Raw Obsidian"));
        }
        for (int y = -1; y < 2; y++) {
            if (level.getTile(xt, yt + y) == Tiles.get("lava"))
                level.setTile(xt, yt + y, Tiles.get("Raw Obsidian"));
        }
        return false;
    }*/
}
