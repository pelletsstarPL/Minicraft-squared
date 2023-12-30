package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import org.jetbrains.annotations.Nullable;

public class FlowerTile extends Tile {
    private FlowerType type;
    public enum FlowerType{
        Flower(3,8,"Grass",new String[] {"Flower"}),
        SmallFlower(4,8,"Grass",new String[] {"Small Flower"}),
        Rose(5,6,"Grass",new String[] {"Rose"}),
        SmallRose(5,8,"Grass",new String[] {"Small rose"}),
        Sunflower(5,7,"Grass",new String[] {"Sunflower"}),
        Azalea(3,20,"Moss",new String[] {"Azalea"}),
        Fern(4,13,"Grass",new String[] {"Fern spores"}),
        CloudFlower(5,23,"Cloud",new String[] {"Cloud Flower"}),
        GrassStones(0,13,"Grass",new String[] {"Stone"}),
        DirtStones(0,13,"dirt",new String[] {"Stone"}),
        DesertTallgrass(16,4,"Sand",new String[] {"Plant fiber"}),
        CloudTallgrass(16,5,"Cloud",new String[] {"Plant fiber"}),
       Fungus(9,13,"Dirt",null),
        Tallgrass(16,4,"Grass",new String[] {"Wheat seeds","Plant fiber"});

        private int spriteX,spriteY;

        private String baseTile;
        private String[] loot;

        FlowerType(int spriteX,int spriteY,String baseTile,String[] loot){
            this.spriteX= spriteX;this.spriteY= spriteY;
            this.baseTile = baseTile;
            this.loot = loot;
        }
    }
    protected FlowerTile(String name,FlowerType type){
        super(name,new Sprite(type.spriteX, type.spriteY, 1));
        this.type= type;

        switch(type.baseTile){
            case "Grass": connectsToGrass = true; break;
            case "Moss": connectsToMoss = true; break;
            case "Water": connectsToFluid = true; break;
            case "Snow": connectsToSnow = true; break;
            case "Sand": connectsToSand = true; break;
        }
    }
    private Sprite spr(Level level, int x, int y){
        if(type.name().contains("Tallgrass"))
        return new Sprite(type.spriteX+((x+y)%3), type.spriteY, 1,1,1,(level.getData(x,y)/3)%2);
        else return new Sprite(type.spriteX, type.spriteY, 1);

    };

@Nullable
protected String[] lootTable() {return type.loot;}

    public void render(Screen screen, Level level, int x, int y) {
        if(type.name()=="Fungus" && (level.depth == -6 || level.realm.contains( "dungeon realm")))type.Fungus.spriteX = 8;
            else  type.Fungus.spriteX = 9;
        Tiles.get(type.baseTile).render(screen, level, x, y);
        int data = level.getData(x, y);
        int shape = (data / 16) % 2;

        x = x << 4;
        y = y << 4;
        if(type.name().contains("Stones")) {
            spr(level,x,y).render(screen, x + 8 * shape, y);
            new Sprite(1+(level.depth<-3 ? 1 : 0),13,1).render(screen,x+8,y+8);
        } else if(type.name().contains("Tallgrass")){ //if we have to deal with tallgrass generate grass across the entire tile
            if(data%11<=6 && data%11>2 || data%14==0) spr(level,x+x%64,y-y%32).render(screen, x + 8 * 0, y);
           if(data%9<4) spr(level,x+y%48,y+x%48).render(screen, x + 8, y + 8);
            if(data%8<3 || data%9==5) spr(level,x-x%64,y+y%64).render(screen, x + 8 * 0, y + 8);
            if(data%8>5 || data%14==0) spr(level,x+x%64,y-y%64).render(screen, x + 8, y);
        }else{
            spr(level,x,y).render(screen, x + 8 * shape, y);
            spr(level,x,y).render(screen, x + 8 , y + 8);
        }
    }
    public boolean interact(Level level, int x, int y, Player player, Item item, Direction attackDir) {
        if (item instanceof ToolItem) {
            ToolItem tool = (ToolItem) item;
            if (tool.type == ToolType.Shovel || tool.type == ToolType.Pickaxe) {
                if (player.payStamina(2 - tool.level) && tool.payDurability()) {
                    level.setTile(x, y, Tiles.get(type.baseTile));
                    Sound.monsterHurt.play();
                    if(lootTable()!=null)
                    for(int i=0;i<lootTable().length;i++){
                        switch(lootTable()[i]){
                            case "Wheat seeds":if(random.nextInt(12)==0)level.dropItem(x * 16 + 8, y * 16 + 8, 1,Items.get(type.loot[i]));break;
                            default: level.dropItem(x * 16 + 8, y * 16 + 8, 1,2,Items.get(type.loot[i])); break;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
        if(type!=FlowerType.Fungus) //fungus must be rooted
        level.setTile(x, y, type.baseTile);
        if(lootTable()!=null)
        for(int i=0;i<lootTable().length;i++){
            switch(lootTable()[i]){
                case "Stone":level.dropItem(x * 16 + 8, y * 16 + 8, 1,Items.get(type.loot[i])); break;
                case "Wheat seeds": case "Fern spores":if(random.nextInt(12)==0)level.dropItem(x * 16 + 8, y * 16 + 8, 1,Items.get(type.loot[i]));break;
                default: level.dropItem(x * 16 + 8, y * 16 + 8, 1,2,Items.get(type.loot[i])); break;
            }
        }
        return true;
    }
}
