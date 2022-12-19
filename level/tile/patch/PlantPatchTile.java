package minicraft.level.tile.patch;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.*;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.level.tile.RockTile;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class PlantPatchTile extends Tile {
    private static Sprite spriteVase = new Sprite(44, 2, 2, 2, 1);
    private PlantPatchType type;
    public enum PlantPatchType {
        CloudCactus(10,"Cloud",new Sprite(6, 2, 2, 2, 1),new String[] {"Cloud shard","Cloud cactus"},new Integer[] {0,0}, new Integer[]{4,3}),
        Cactus(10,"Sand",new Sprite(6, 0, 2, 2, 1),new String[] {"Cactus"},new Integer[]{2},new Integer[]{4}),
        SkyTree(20,"Skygrass",new Sprite(6, 0, 2, 2, 1),new String[] {"Wood","Stick"},new Integer[] {1,1}, new Integer[]{2,2}),
        SkyConifer(20,"Skygrass",new Sprite(6, 0, 2, 2, 1),new String[] {"Wood","Stick"},new Integer[] {1,1}, new Integer[]{2,2});

        int health;
        String soil;
        Sprite sprite;
        private String[] loot;
        private Integer[] lootAmMin;
        private Integer[] lootAmMax;
        PlantPatchType(int health,String soil, Sprite sprite,String[] loot,Integer[] lootAmMin,Integer[] lootAmMax){
            this.health = health;
            this.soil = soil;
            this.sprite = sprite;
            this.loot = loot;
            this.lootAmMin = lootAmMin;
            this.lootAmMax = lootAmMax;
        }
    }

    public PlantPatchTile(String name, PlantPatchType type) {
        super("Patch " + name, type.sprite);
        this.type = type;
    }
    public void render(Screen screen, Level level, int x, int y) {
        int xa=x;
        int ya=y;
        y=y <<4;
        x=x <<4;
       Tiles.get(type.soil).render(screen,level,xa,ya);
        spriteVase.render(screen, x, y);
        type.sprite.render(screen,x,y);

    }

    public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
        if(Game.isMode("creative"))
            return false; // Go directly to hurt method
        if (item instanceof ToolItem) {
            ToolItem tool = (ToolItem) item;
            if (tool.type == ToolType.Pickaxe) {
                if (player.payStamina(6 - tool.level) && tool.payDurability()) {
                    hurt(level, xt, yt, 1);
                    return true;
                }
            }
        }
        return false;
    }



    public void hurt(Level level, int x, int y, int dmg) {
        int damage = level.getData(x, y) + dmg;
        int health = 10;
        if (Game.isMode("creative")) dmg = damage = health;
        level.add(new SmashParticle(x * 16, y * 16));
        Sound.monsterHurt.play();
        level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
        if (damage >= health) {
            level.setTile(x, y, Tiles.get("Empty patch vase"));
            level.setData(x,y,Tiles.get(type.soil).id);
            for(int i=0;i< type.loot.length;i++){
                level.dropItem(x * 16 + 8, y * 16 + 8, type.lootAmMin[i], type.lootAmMax[i], Items.get(type.loot[i]));
            }
        } else
            level.setData(x, y, damage);
    }

    public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
        hurt(level, x, y, type.name()=="CloudCactus" ? 0 : dmg);
        return true;
    }
    public boolean mayPass(Level level, int x, int y, Entity e) {
        return e instanceof AirWizard || e instanceof WraithA || e instanceof Wraith || e instanceof Clallay;
    }

    //FOR CLOUD CACTUS
    public void bumpedInto(Level level, int x, int y, Entity entity) {
        if (name.contains("PATCH CLOUD CACTUS") || name.contains("PATCH CACTUS")) {
            if (entity instanceof AirWizard || entity instanceof Wraith || entity instanceof WraithA || entity instanceof Ghost || entity instanceof Clallay)
                return;

            if (entity instanceof Mob)
                ((Mob) entity).hurt(this, x, y, 1 + Settings.getIdx("diff"));
        }
    }
}
