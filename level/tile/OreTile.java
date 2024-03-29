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

/// this is all the spikey stuff (except "cloud cactus")
public class OreTile extends Tile {
	private Sprite sprite;
	private OreType type;
	
	public enum OreType { //8,10,12 are reserved for wool
        Iron (Items.get("Iron Ore"), 0,0,20,2),

		Lapis (Items.get("Lapis"), 2,0,15,1,1.1),

		Obsidian (Items.get("Obsidian"), 2,4,90,4,0.8),
		Obsidium (Items.get("Obsidium ore"), 15,18,100,5,0.6),
		Gold (Items.get("Gold Ore"), 4,0,40,3),
		Gem (Items.get("Gem"), 6,0,70,3,0.95),
		GemG (Items.get("Gem"), 6,2,70,3,0.95),
		GemB (Items.get("Gem"), 6,4,70,3,0.95),
		Coal (Items.get("Coal"), 0,2,15,1,1.1),

		//"not full ores". Those will drop less
		IronNF (Items.get("Iron Ore"), 0,4,10),
		LapisNF (Items.get("Lapis"), 2,2,8,1,1.2),
		GoldNF (Items.get("Gold Ore"), 4,2,20,3),
		GemNF (Items.get("Gem"), 6,6,35,3),
		GemGNF (Items.get("Gem"), 6,8,35,3),
		GemBNF (Items.get("Gem"), 6,10,35,3),
		ObsidiumNF (Items.get("Obsidium ore"), 15,18,105,5,0.75);

		private Item drop;
		public final int color;
		public final int yaxis;
		public final int hpModifier;
		public final int reqTier; //you can mine obviously with low-level tools high-level ores but you will not do much damage to them
		public final double dmgMult;

		OreType(Item drop, int color,int y,int hpMod,int rqTier,double dmMult) {
			this.drop = drop;
			this.color = color;
			this.yaxis = y;
			this.hpModifier = hpMod;
			this.reqTier = rqTier;
			this.dmgMult = dmMult;
		}
		OreType(Item drop, int color,int y,int hpMod,int rqTier) {
			this.drop = drop;
			this.color = color;
			this.yaxis = y;
			this.hpModifier = hpMod;
			this.reqTier = rqTier;
			this.dmgMult = 1;
		}
		OreType(Item drop, int color,int y,int hpMod) {
			this.drop = drop;
			this.color = color;
			this.yaxis = y;
			this.hpModifier = hpMod;
			this.reqTier = 1;
			this.dmgMult = 1;
		}
		
		protected Item getOre() {
			return drop.clone();
		}
    }
	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		hurt(level, x, y, 0,1);
		return true;
	}
	protected OreTile(OreType o) {
		super((o == OreTile.OreType.Lapis ? "Lapis"  : (o==OreTile.OreType.IronNF || o==OreTile.OreType.GoldNF || o==OreTile.OreType.GemNF ? o.name() + " Ore" : o.name() + " Ore")), new Sprite(24 + o.color, 0+o.yaxis, 2, 2, 1));
        this.type = o;
		this.sprite = super.sprite;
	}

	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("Dirt").render(screen,level,x , y );
		sprite.render(screen, x * 16, y * 16);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}


	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if(Game.isMode("Creative"))
			return false; // Go directly to hurt method
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Pickaxe) {
				if (player.payStamina(6 - tool.level < 2 ? 2 : 6 - tool.level) && tool.payDurability()) {
					hurt(level, xt, yt, tool.getDamage()/(type.reqTier - tool.level < 1 ? 1 : type.reqTier - tool.level),1/(type.reqTier - tool.level < 1 ? 1 : type.reqTier - tool.level));
					return true;
				}
			}
		}
		return false;
	}
	
    public Item getOre() {
        return type.getOre();
    }
    
	public void hurt(Level level, int x, int y, int dmg,int dropProb) {
		int damage = level.getData(x, y) + dmg;
		int oreH = random.nextInt(10) * 4 + type.hpModifier;
		if(type==OreType.IronNF || type==OreType.GoldNF || type==OreType.GemNF || type==OreType.GemGNF || type==OreType.GemBNF)oreH/=1.6;
		if (Game.isMode("Creative")) dmg = damage = oreH;

		level.add(new SmashParticle(x * 16, y * 16));
		Sound.monsterHurt.play();

		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (dmg > 0) {
			int count = random.nextInt(2);
			if (damage >= oreH) {
				level.setTile(x, y, Tiles.get("Dirt"));
				count += 2;
			} else {
				level.setData(x, y, damage);
			}
		if (Game.isMode("Creative")) dmg = damage = oreH;

		level.add(new SmashParticle(x * 16, y * 16));
		Sound.monsterHurt.play();

			if(type.name().contains("NF")) {
				if (Math.random() < dropProb/2)
					level.dropItem(x * 16 + 8, y * 16 + 8, count / 2, type.getOre()); //drop less
			}else{ if(Math.random()<dropProb)level.dropItem(x * 16 + 8, y * 16 + 8, count, type.getOre());}
		}
	}

	public void bumpedInto(Level level, int x, int y, Entity entity) {
		/// this was used at one point to hurt the player if they touched the ore; that's probably why the sprite is so spikey-looking.
	}
}
