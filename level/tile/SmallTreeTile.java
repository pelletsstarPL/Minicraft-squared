package minicraft.level.tile;

import minicraft.core.Game;
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

public class SmallTreeTile extends Tile {

    private TreeType type;
    public enum TreeType{
        Oak(new Sprite(56, 4,2,2,1), "Oak","Grass",new String[] {"Wood","Acorn","Stick"},15,false,ToolType.Axe),
        Birch(new Sprite(56, 2,2,2,1),"Birch","Grass",new String[] {"Wood","Catkin","Stick"},15,false,ToolType.Axe),
        Conifer(new Sprite(58, 4,2,2,1),"Conifer","Grass",new String[] {"Wood","Cone","Stick"},15,false,ToolType.Axe),
        SnowyConifer(new Sprite(58, 2,2,2,1),"Snowy conifer","Snow",new String[] {"Wood","Snow cone","Stick"},15,false,ToolType.Axe),
        Fungus(new Sprite(54, 4,2,2,1),"Fungus Tree","Moss",new String[] {"Fungus"},8,false,ToolType.Axe),
        Cactus(new Sprite(54, 2,2,2,1),"Cactus","Sand",new String[] {"Cactus"}, 8,true,ToolType.Axe),
        CloudCactus(new Sprite(54, 0,2,2,1),"Cloud cactus","Cloud",new String[] {"Cloud shard","Cloud cactus"}, 8,true,ToolType.Pickaxe);
        private Sprite treeSprite;
        private String baseTile,Tname;
        private String[] loot;
        private int health;
        private boolean shouldHarm;
        private ToolType tool;

        TreeType(Sprite treeSprite, String Tname, String baseTile, String[] loot, int health,boolean shouldHarm,ToolType tool){
            this.treeSprite= treeSprite;
            this.Tname=Tname;
            this.baseTile = baseTile;
            this.loot = loot;
            this.health = health;
            this.shouldHarm = shouldHarm;
            this.tool = tool;
        }
    }
    protected SmallTreeTile(TreeType type){
        super("Small " + type.Tname,(ConnectorSprite) null);
        this.type= type;
        this.extraSmallHbox = 4;
        switch(type.baseTile){
            case "Grass": connectsToGrass = true; break;
            case "Skygrass": connectsToSkygrass = true; break;
            case "Moss": connectsToMoss = true; break;
            case "Water": connectsToFluid = true; break;
            case "Snow": connectsToSnow = true; break;
            case "Sand": connectsToSand = true; break;
        }
    }
    public void render(Screen screen, Level level, int x, int y) {
        int xa = x << 4, ya = y << 4;
        Tiles.get(type.baseTile).render(screen, level, x, y);
        type.treeSprite.render(screen,xa,ya);
    }

    public boolean tick(Level level, int xt, int yt) {
        int damage = level.getData(xt, yt);
        if (damage > 0) {
            level.setData(xt, yt, damage - 1);
            return true;
        }
        return false;
    }

    public boolean mayPass(Level level, int x, int y, Entity e){
        if(e instanceof NightWizard && level.depth>=0)
            return true;
        else return false;
    }

    @Override
    public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
        hurt(level, x, y, dmg);
        return true;
    }

    @Override
    public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
        if(Game.isMode("Creative"))
            return false; // Go directly to hurt method
        if (item instanceof ToolItem) {
            ToolItem tool = (ToolItem) item;
            if (tool.type == type.tool) {
                int staminaPay=(4-tool.level < 2 ? 2 : 4-tool.level);
                if(4-tool.level<2)staminaPay=2;
                int dmg=random.nextInt(10) + tool.damage;
                if (player.payStamina(staminaPay) && tool.payDurability() && tool.level!=6) {
                    hurt(level, xt, yt, dmg);
                    return true;
                }else if(player.payStamina(staminaPay) && tool.payDurability() && tool.level==6){
                    hurt(level, xt, yt, random.nextInt(3)+2);
                }
            }
        }
        return false;
    }

    public void hurt(Level level, int x, int y, int dmg) {

        int damage = level.getData(x, y) + dmg;
        int treeHealth = type.health;
        if (Game.isMode("Creative")) dmg = damage = treeHealth;

        level.add(new SmashParticle(x*16, y*16));
        Sound.monsterHurt.play();
        level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
        if (damage >= treeHealth) {
            for(int i=0;i< type.loot.length;i++){
                switch(type.loot[i]){
                    default:level.dropItem(x * 16 + 8, y * 16 + 8, 0, 1, Items.get(type.loot[i]));break;
                }
            }
            level.setTile(x, y, Tiles.get(type.baseTile));
        } else {
            level.setData(x, y, damage);
        }
    }
    public void bumpedInto(Level level, int x, int y, Entity entity) {
        if(type.shouldHarm) { //only harm when cactus is growing and only while plant is at its second stage
            if (entity instanceof AirWizard || entity instanceof Wraith || entity instanceof Ghost || entity instanceof Clallay)
                return;

            if (entity instanceof Mob)
                ((Mob) entity).hurt(this, x, y, 1);
        }
    }

}
