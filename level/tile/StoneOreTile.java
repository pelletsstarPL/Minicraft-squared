package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.AirWizard;
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

public class StoneOreTile extends Tile {
	private StoneOreType type;
	Sprite coalspike1=new Sprite(23,2,1);
	Sprite coalspike2=new Sprite(23,3,1);
	Sprite coalspike3=new Sprite(23,4,1);
	public enum StoneOreType{
		Stone(10,11,"Dirt"),
		WaterStone(10,11,"Water"),
		Deepslate(6,11,"Dirt"),
		LavaDeepslate(6,11,"Lava");
		private int spriteX,spriteY;
		private String baseTile;

		StoneOreType(int spriteX,int spriteY,String baseTile){
			this.spriteX = spriteX;this.spriteY=  spriteY;
			this.baseTile = baseTile;
		}
	}

	protected StoneOreTile(String name,StoneOreType type) {
		super(name, new Sprite(type.spriteX, type.spriteY, 1));
		this.type= type;
		switch(type.baseTile){
			case "Water": case "Lava":connectsToFluid=true;break;
		}
	}
	
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e instanceof AirWizard;
	}

	public boolean hurt(Level level,int x, int y, Mob source, int dmg, Direction attackDir) {
		hurt(level, x, y, 1);
		return true;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			int staminaPay=(4-tool.level < 2 ? 2 : 4-tool.level);
			int dmg = random.nextInt(10) + tool.damage;
			if (tool.level!=6 && tool.type == ToolType.Pickaxe && player.payStamina(staminaPay) && tool.payDurability(dmg)) {
				// Drop coal since we use a pickaxe.
				hurt(level, xt, yt, dmg);
				return true;
			}else if (tool.level==6 && tool.type == ToolType.Pickaxe && player.payStamina(staminaPay) && tool.payDurability(dmg)) {
				dmg=random.nextInt(8)+2;
				hurt(level, xt, yt, dmg);
			}
		}
		return false;
	}
	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get(type.baseTile).render(screen, level, x, y);


		new Sprite(type.spriteX, type.spriteY,2,2,1).render(screen, x << 4, y << 4);
		if((x-y)%4==0)coalspike2.render(screen,x << 4,y << 4);
		if((x+y)%9==0)coalspike3.render(screen,(x << 4) + 8,y << 4);
		if((x-y)%7==0)coalspike1.render(screen,x << 4,(y << 4) + 8);
	}
	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + dmg;
		int health = (type.name().contains("Deepslate") ? 60 : 40);
		if (Game.isMode("creative")) dmg = damage = health;
		level.add(new SmashParticle(x * 16, y * 16));
		Sound.monsterHurt.play();
		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= health) {
			level.setTile(x, y, Tiles.get(type.baseTile));
			level.dropItem(x * 16 + 8, y * 16 + 8,1,2,Items.get("Stone"));
			if((x-y)%4==0&&(x+y)%9==0&&(x-y)%7==0)level.dropItem(x * 16 + 8, y * 16 + 8,1,Items.get("coal"));
		} else
			level.setData(x, y, damage);
	}

	public boolean tick(Level level, int xt, int yt) {
		int xn = xt;
		int yn = yt;

		if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
		else yn += random.nextInt(2) * 2 - 1;

		if (level.getTile(xn, yn) == Tiles.get("Hole") && (type.name()=="WaterStone" || type.name()=="LavaDeepslate")){
			level.setTile(xn, yn, Tiles.get(type.name()=="WaterStone" ? "Water" : "Lava"));
		}

		// These set only the non-diagonally adjacent lava tiles to obsidian
		for (int x = -1; x < 2; x++) {
			if (level.getTile(xt + x, yt) == Tiles.get("Lava")  && type.name()=="WaterStone")
				level.setTile(xt + x, yt, Tiles.get("Raw Obsidian"));
		}
		for (int y = -1; y < 2; y++) {
			if (level.getTile(xt, yt + y) == Tiles.get("lava") && type.name()=="WaterStone" )
				level.setTile(xt, yt + y, Tiles.get("Raw Obsidian"));
		}
		return false;
	}
}
