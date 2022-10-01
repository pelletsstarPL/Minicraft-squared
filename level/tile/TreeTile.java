package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.NightWizard;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class TreeTile extends Tile {

	private TreeType type;
	public enum TreeType{
		Oak(0,1,0,1,2,3,"Grass",new String[] {"Wood","Apple","Acorn","Stick"},20),
		Birch(17,18,18,19,20,21,"Grass",new String[] {"Wood","Catkin","Stick"},20),
		Conifer(7,8,18,19,20,21,"Grass",new String[] {"Wood","Cone","Stick"},20),
		SkyConifer(19,20,18,19,20,21,"Skygrass",new String[] {"Wood","Cone","Stick"},20),
		SnowyConifer(9,10,18,19,20,21,"Snow",new String[] {"Wood","Snow cone","Stick"},20),
		BigFungus(5,6,18,19,20,21,"Moss",new String[] {"Fungus"},15),
		Sky(11,12,18,19,20,21,"Skygrass",new String[] {"Wood","Stick"},20),
		MangroveWater(15,16,18,19,20,21,"Water",new String[] {"Wood","Stick"},20),
		Mangrove(13,14,18,19,20,21,"Moss",new String[] {"Wood","Stick"},20);
		private int sprite_x1,sprite_x2;
		private int sprite_y1,sprite_y2;
		private int sprite_y3,sprite_y4;
		private String baseTile;
		private String[] loot;
		private int health;

		TreeType(int sprite_x1,int sprite_x2,int sprite_y1,int sprite_y2,int sprite_y3,int sprite_y4,String baseTile,String[] loot,int health){
			this.sprite_x1= sprite_x1;this.sprite_x2= sprite_x2;
			this.sprite_y1= sprite_y1;this.sprite_y2= sprite_y2;
			this.sprite_y3= sprite_y3;this.sprite_y4= sprite_y4;
			this.baseTile = baseTile;
			this.loot = loot;
			this.health = health;
		}
	}
	protected TreeTile(TreeType type){
		super((type == TreeTile.TreeType.BigFungus ? "Big fungus" : type==TreeTile.TreeType.Sky ? "Sky tree" : type==TreeTile.TreeType.SkyConifer ? "Sky conifer" : type.name()),(ConnectorSprite) null);
		this.type= type;
		switch(type.baseTile){
			case "Grass": connectsToGrass = true; break;
			case "Skygrass": connectsToSkygrass = true; break;
			case "Moss": connectsToMoss = true; break;
			case "Water": connectsToFluid = true; break;
			case "Snow": connectsToSnow = true; break;
		}
	}
	protected String[] lootTable() {return type.loot;}
	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get(type.baseTile).render(screen, level, x, y);
		
		boolean u = level.getTile(x, y - 1) == this;
		boolean l = level.getTile(x - 1, y) == this;
		boolean r = level.getTile(x + 1, y) == this;
		boolean d = level.getTile(x, y + 1) == this;
		boolean ul = level.getTile(x - 1, y - 1) == this;
		boolean ur = level.getTile(x + 1, y - 1) == this;
		boolean dl = level.getTile(x - 1, y + 1) == this;
		boolean dr = level.getTile(x + 1, y + 1) == this;
		if(type.name()=="Mangrove" || type.name()=="MangroveWater"){ //mangrove connectio so mangroves on water and mangroves on land can connect with themselves
			u = level.getTile(x, y - 1) == this || level.getTile(x, y - 1) == Tiles.get((type.name()=="Mangrove" ? "MangroveWater" : "Mangrove"));
			d = level.getTile(x, y + 1) == this || level.getTile(x, y + 1) == Tiles.get((type.name()=="Mangrove" ? "MangroveWater" : "Mangrove"));
			l = level.getTile(x - 1, y) == this || level.getTile(x - 1, y) == Tiles.get((type.name()=="Mangrove" ? "MangroveWater" : "Mangrove"));
			r = level.getTile(x + 1, y) == this || level.getTile(x + 1, y) == Tiles.get((type.name()=="Mangrove" ? "MangroveWater" : "Mangrove"));
			ur = level.getTile(x + 1, y - 1) == this || level.getTile(x + 1, y - 1) == Tiles.get((type.name()=="Mangrove" ? "MangroveWater" : "Mangrove"));
			ul = level.getTile(x - 1, y - 1) == this || level.getTile(x - 1, y - 1) == Tiles.get((type.name()=="Mangrove" ? "MangroveWater" : "Mangrove"));
			dl = level.getTile(x - 1, y + 1) == this || level.getTile(x - 1, y + 1) == Tiles.get((type.name()=="Mangrove" ? "MangroveWater" : "Mangrove"));
			dr = level.getTile(x + 1, y + 1) == this || level.getTile(x + 1, y + 1) == Tiles.get((type.name()=="Mangrove" ? "MangroveWater" : "Mangrove"));
		}

		if (u && ul && l) {
			screen.render(x * 16 + 0, y * 16 + 0, this.type.sprite_x2 + this.type.sprite_y2 * 32, 0, 1);
		} else {
			screen.render(x * 16 + 0, y * 16 + 0, this.type.sprite_x1 + this.type.sprite_y1 * 32, 0, 1);
		}
		if (u && ur && r) {
			screen.render(x * 16 + 8, y * 16 + 0, this.type.sprite_x2 + this.type.sprite_y3 * 32, 0, 1);
		} else {
			screen.render(x * 16 + 8, y * 16 + 0, this.type.sprite_x2 + this.type.sprite_y1 * 32, 0, 1);
		}
		if (d && dl && l) {
			screen.render(x * 16 + 0, y * 16 + 8, this.type.sprite_x2 + this.type.sprite_y3 * 32, 0, 1);
		} else {
			screen.render(x * 16 + 0, y * 16 + 8, this.type.sprite_x1 + this.type.sprite_y2 * 32, 0, 1);
		}
		if (d && dr && r) {
			screen.render(x * 16 + 8, y * 16 + 8, this.type.sprite_x2 + this.type.sprite_y2 * 32, 0, 1);
		} else {
			screen.render(x * 16 + 8, y * 16 + 8, this.type.sprite_x2 + this.type.sprite_y4 * 32, 0, 1);
		}
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
		int treeHealth = type.health;
		if (Game.isMode("Creative")) dmg = damage = treeHealth;
		
		level.add(new SmashParticle(x*16, y*16));
		Sound.monsterHurt.play();
		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= treeHealth) {
			for(int i=0;i< lootTable().length;i++){
				switch(lootTable()[i]){
					case "Apple": if(random.nextInt(100)==0)level.dropItem(x * 16 + 8, y * 16 + 8, 1, 1, Items.get("Apple"));break;
					case "Fungus":level.dropItem(x * 16 + 8, y * 16 + 8, 2, 5, Items.get("Fungus"));break;
					default:level.dropItem(x * 16 + 8, y * 16 + 8, 1, 2, Items.get(lootTable()[i]));break;
				}
			}
			/*level.dropItem(x * 16 + 8, y * 16 + 8, 1, 2, Items.get("Wood"));
			level.dropItem(x * 16 + 8, y * 16 + 8, 0, 2, Items.get("Stick"));
			level.dropItem(x * 16 +  8, y * 16 + 8, 0, 2, Items.get("Acorn"));*/
			level.setTile(x, y, Tiles.get(type.baseTile));
		} else {
			level.setData(x, y, damage);
		}
	}
}
