package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
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
import minicraft.entity.mob.NightWizard;

// This is the normal stone you see underground and on the surface, that drops coal and stone.

public class RockTile extends Tile {
	private RockType type;
	public enum RockType{
		Rock(50,new ConnectorSprite(RockTile.class, new Sprite(18, 6, 3, 3, 1, 3), new Sprite(21, 8, 2, 2, 1, 3), new Sprite(21, 6, 2, 2, 1, 3))),
		Deepslate(70,new ConnectorSprite(RockTile.class, new Sprite(24, 9, 3, 3, 1, 3), new Sprite(27, 11, 2, 2, 1, 3), new Sprite(27, 9, 2, 2, 1, 3))),
		Obsidian(110,new ConnectorSprite(RockTile.class, new Sprite(57, 6, 3, 3, 1, 3), new Sprite(60, 8, 2, 2, 1, 3), new Sprite(60, 6, 2, 2, 1, 3)));;
		private int health;
		private ConnectorSprite sprite;
		RockType(int health,ConnectorSprite sprite){
			this.health = health;
			this.sprite = sprite;
		}
	}
	private boolean dropCoal = false;

	private int damage;

	protected RockTile(String name, RockType type) {
		super(name, type.sprite);
		this.type = type;
	}

	public void render(Screen screen, Level level, int x, int y) {

		if(level.depth==-3){
			RockType.Rock.sprite=new ConnectorSprite(RockTile.class, new Sprite(15, 6, 3, 3, 1, 3), new Sprite(21, 8, 2, 2, 1, 3), new Sprite(22, 6, 2, 2, 1, 3));
		}
		else{
			RockType.Rock.sprite = new ConnectorSprite(RockTile.class, new Sprite(18, 6, 3, 3, 1, 3), new Sprite(21, 8, 2, 2, 1, 3), new Sprite(21, 6, 2, 2, 1, 3));
		}
		Tiles.get("Dirt").render(screen, level, x, y);
		if((level.getTile(x-1, y)==Tiles.get("Coarse dirt") || level.getTile(x+1, y)==Tiles.get("Coarse dirt") || level.getTile(x, y-1)==Tiles.get("Coarse dirt") || level.getTile(x, y+1)==Tiles.get("Coarse dirt")))Tiles.get("coarse dirt").render(screen, level, x, y);
		type.sprite.render(screen, level, x, y);
	}

	public boolean mayPass(Level level, int x, int y, Entity e){
		if(e instanceof NightWizard && level.depth>=0)
			return true;
		else return false;
	}
	
	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		hurt(level, x, y, type == RockType.Obsidian ? 0 : dmg);
		return true;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			int staminaPay=(4-tool.level < 2 ? 2 : 4-tool.level);
			if(tool.type==ToolType.Hammer)staminaPay++;
			int dmg = (int)(random.nextInt(10) + tool.damage * (tool.type == ToolType.Hammer ? 1.2 : 1));
			if(type == RockType.Obsidian){
				if (tool.level>=2 && tool.level!=6 && (tool.type == ToolType.Pickaxe || tool.type == ToolType.Hammer) && player.payStamina(staminaPay) && tool.payDurability(dmg)) {
					// Drop coal since we use a pickaxe or hammer
					dropCoal = true;
					if(type == RockType.Obsidian)dmg *= 0.85;
					hurt(level, xt, yt, dmg);
					return true;
				}else if ((tool.level==6 || tool.level<2) && tool.type == ToolType.Pickaxe && player.payStamina(staminaPay) && tool.payDurability(dmg)) {
					dmg=random.nextInt(8)+2;
					Game.notifications.add(ToolItem.LEVEL_NAMES[2] + " pickaxe or stronger Required.");
					hurt(level, xt, yt, 0);
					return false;
				}
			}else
			if (tool.level!=6 && (tool.type == ToolType.Pickaxe || tool.type == ToolType.Hammer) && player.payStamina(staminaPay) && tool.payDurability(dmg)) {
				// Drop coal since we use a pickaxe or hammer
				dropCoal = true;
				if(type == RockType.Obsidian)dmg *= 0.85;
				hurt(level, xt, yt, dmg);
				return true;
			}else if (tool.level==6 && tool.type == ToolType.Pickaxe && player.payStamina(staminaPay) && tool.payDurability(dmg)) {
				dmg=random.nextInt(8)+2;
				hurt(level, xt, yt, dmg);
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg) {
		int maxHealth = type.health;
		damage = level.getData(x, y) + dmg;

		if (Game.isMode("Creative")) {
			dmg = damage = maxHealth;
			dropCoal = true;
		}

		level.add(new SmashParticle(x * 16, y * 16));
		Sound.monsterHurt.play();

		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= maxHealth) {
				if (dropCoal) {
					if(type==RockType.Obsidian)level.dropItem(x * 16 + 8, y * 16 + 8, 1, 2, Items.get("Obsidian"));
					else level.dropItem(x * 16 + 8, y * 16 + 8, 1, 3, Items.get("Stone"));
					int coal = 0;
					if (!Settings.get("diff").equals("Hard")) {
						coal++;
					}
					if(Math.random()<0.8)
					level.dropItem(x * 16 + 8, y * 16 + 8, coal, coal + 1, Items.get("Coal"));
				} else {
					if(type==RockType.Obsidian)level.dropItem(x * 16 + 8, y * 16 + 8, 2, 4, Items.get("Obsidian"));
					else level.dropItem(x * 16 + 8, y * 16 + 8, 2, 4, Items.get("Stone"));
				}
				level.setTile(x, y, Tiles.get("dirt"));
		} else {
			level.setData(x, y, damage);
		}
	}


}
