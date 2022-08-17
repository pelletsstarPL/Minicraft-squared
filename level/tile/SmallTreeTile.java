package minicraft.level.tile;

import minicraft.core.io.Settings;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.item.Items;
import minicraft.item.Item;
import minicraft.core.io.Sound;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.entity.particle.SmashParticle;
import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.mob.Mob;
import minicraft.entity.Entity;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.gfx.Sprite;

public class SmallTreeTile extends Tile
{
    private static Sprite sprite;
    private int damage;
    private int maxHealth=15;
    protected SmallTreeTile(final String name) {
        super(name, SmallTreeTile.sprite);
        this.connectsToGrass = true;
    }
    
    public boolean mayPass(final Level level, final int x, final int y, final Entity e) {
        return false;
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
            if (tool.type == ToolType.Axe) {
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
        int treeHealth = 15;
        if (Game.isMode("Creative")) dmg = damage = treeHealth;

        level.add(new SmashParticle(x*16, y*16));
        Sound.monsterHurt.play();

        level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
        if (damage >= treeHealth) {
            level.dropItem(x * 16 + 8, y * 16 + 8, 1, 2, Items.get("Wood"));
            level.dropItem(x * 16 + 8, y * 16 + 8, 0, 2, Items.get("Stick"));
            level.dropItem(x * 16 +  8, y * 16 + 8, 0, 2, Items.get("Acorn"));
            level.setTile(x, y, Tiles.get("Grass"));
        } else {
            level.setData(x, y, damage);
        }
    }
    public void render(final Screen screen, final Level level, final int x, final int y) {
        Tiles.get("Grass").render(screen, level, x, y);
        SmallTreeTile.sprite.render(screen, x << 4, y << 4);
    }
    
    public boolean tick(final Level level, final int xt, final int yt) {
        final int damage = level.getData(xt, yt);
        if (damage > 0) {
            level.setData(xt, yt, damage - 1);
            return true;
        }
        return false;
    }
    
    static {
        SmallTreeTile.sprite = new Sprite(0, 4, 2, 2, 1);
    }
}