package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class OreBlockTile extends Tile {
	private Sprite sprite;
	private OreType type;

	public enum OreType { //8,10,12 are reserved for snow
        Iron (Items.get("Iron Block"), 0,0,80),
		Lapis (Items.get("Lapis Block"), 2,0,60),
		Gold (Items.get("Gold Block"), 0,2,90),
		Gem (Items.get("Gem Block"), 0,4,105);

		private Item drop;
		public final int color;
		public final int yaxis;
		public final int health;


		OreType(Item drop, int color,int y,int hp) {
			this.drop = drop;
			this.color = color;
			this.yaxis = y;
			this.health = hp;
		}

		protected Item getOre() {
			return drop.clone();
		}
    }

	protected OreBlockTile(OreType o) {
		super((o == OreBlockTile.OreType.Lapis ? "Lapis Block" : o.name() + " Block"), new Sprite(40+o.color, 0+ o.yaxis, 2, 2, 1));
        this.type = o;
		this.sprite = super.sprite;
	}

	public void render(Screen screen, Level level, int x, int y) {
		sprite.color = DirtTile.dCol(level.depth);
		sprite.render(screen, x * 16, y * 16);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		hurt(level, x, y, 0);
		return true;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if(Game.isMode("Creative"))
			return false; // Go directly to hurt method
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Pickaxe) {
				if (player.payStamina(6 - tool.level) && tool.payDurability()) {
					hurt(level, xt, yt, random.nextInt(10)+5+tool.level);
					return true;
				}
			}
		}
		return false;
	}
	
    public Item getOre() {
        return type.getOre();
    }
    
	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + dmg;
		int oreH = type.health;
		if (Game.isMode("Creative")) dmg = damage = oreH;
		
		level.add(new SmashParticle(x * 16, y * 16));
		Sound.monsterHurt.play();
		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (dmg > 0) {
			if (damage >= oreH) {
				if(level.depth<1)
				level.setTile(x, y, Tiles.get("Dirt"));
				else level.setTile(x, y, Tiles.get("Cloud"));
				level.dropItem(x * 16 + 8, y * 16 + 8, 1, type.drop);
			} else {
				level.setData(x, y, damage);
			}

		}
	}

	public void bumpedInto(Level level, int x, int y, Entity entity) {
		/// this was used at one point to hurt the player if they touched the ore; that's probably why the sprite is so spikey-looking.
	}
}
