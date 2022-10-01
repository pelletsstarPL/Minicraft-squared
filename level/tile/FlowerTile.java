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
        DesertTallgrass(11,6,"Sand",new String[] {}),
        CloudTallgrass(5,22,"Cloud",new String[] {}),
        Tallgrass(11,6,"Grass",new String[] {"Wheat seeds"});

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
    private Sprite spr(){
        return new Sprite(type.spriteX, type.spriteY, 1);
    };
    protected String[] lootTable() {return type.loot;}
    public void render(Screen screen, Level level, int x, int y) {
        Tiles.get(type.baseTile).render(screen, level, x, y);
        int data = level.getData(x, y);
        int shape = (data / 16) % 2;

        x = x << 4;
        y = y << 4;
        if(type.name().contains("Stones")) {
            spr().render(screen, x + 8 * shape, y);
            new Sprite(1+(level.depth<-3 ? 1 : 0),13,1).render(screen,x+8,y+8);
        } else if(type.name().contains("Tallgrass")){ //if we have to deal with tallgrass generate grass across the entire tile
            spr().render(screen, x + 8 * 0, y);
            spr().render(screen, x + 8, y + 8);
            spr().render(screen, x + 8 * 0, y + 8);
            spr().render(screen, x + 8, y);
        }else {
            spr().render(screen, x + 8 * shape, y);
            spr().render(screen, x + 8 * (shape == 0 ? 1 : 0), y + 8);
        }
    }
    public boolean interact(Level level, int x, int y, Player player, Item item, Direction attackDir) {
        if (item instanceof ToolItem) {
            ToolItem tool = (ToolItem) item;
            if (tool.type == ToolType.Shovel) {
                if (player.payStamina(2 - tool.level) && tool.payDurability()) {
                    level.setTile(x, y, Tiles.get(type.baseTile));
                    Sound.monsterHurt.play();
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
        level.setTile(x, y, type.baseTile);
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
