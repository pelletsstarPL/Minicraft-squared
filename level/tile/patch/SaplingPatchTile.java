package minicraft.level.tile.patch;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Mob;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class SaplingPatchTile extends Tile {
    private static Sprite spriteVase = new Sprite(44, 2, 2, 2, 1);
    private static Sprite sprite = new Sprite(12, 1, 1);
    private static Sprite spriteCactus = new Sprite(11, 7, 1);
    private static Sprite spriteFern = new Sprite(8, 1, 0);
    private static Sprite spriteBirch = new Sprite(11, 8, 1);
    private static Sprite spriteCloud = new Sprite(10, 8, 1);

    private Tile onType;
    private Tile growsTo;
    private Tile soil;

    public SaplingPatchTile(String name, Tile onType, Tile growsTo,Tile soil) {
        super("Patch "+ name, sprite);
        this.onType = onType;
        this.growsTo = growsTo;
        this.soil = soil;
        maySpawn = true;
    }

    public void render(Screen screen, Level level, int x, int y) {
        int xa=x;
        int ya=y;
        y=y <<4;
        x=x <<4;
       if(growsTo.name.contains("CLOUD CACTUS")) {
            Tiles.get("Cloud").render(screen,level,xa,ya);spriteCloud.render(screen, (xa * 16)+4, (ya * 16)+4);
        }else if(growsTo.name.contains("CACTUS")){
           Tiles.get("Sand").render(screen,level,xa,ya);spriteCactus.render(screen, (xa * 16)+4, (ya * 16)+4);
         }
        spriteVase.render(screen, x, y);
        //onType.render(screen, level, x, y);
    }

    public boolean tick(Level level, int x, int y) {
        int age = level.getData(x, y) + 1;
        if (age > 100 && growsTo!=Tiles.get("Cloud cactus")) {
            level.setTile(x, y, growsTo);
        } else if(age > 250 && growsTo==Tiles.get("Cloud cactus")){
            level.setTile(x, y, growsTo);
            level.setData(x,y, 10);
        }else{
            level.setData(x, y, age);
        }
        return true;
    }

    public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
        level.setTile(x, y, Tiles.get("Empty patch vase"));
        level.setData(x, y, soil.id);
        Sound.monsterHurt.play();
        return true;
    }
}
