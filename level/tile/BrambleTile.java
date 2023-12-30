package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.*;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.*;
import minicraft.level.Level;

import javax.swing.*;

public class BrambleTile extends Tile {
	private static Sprite sprite = new Sprite(4, 11, 2, 2, 1);
	private static Sprite spriteO = new Sprite(47, 18, 2, 2, 1); //obsidian dungeon sprite

	protected BrambleTile(String name) {
		super(name, sprite);
		connectsToMoss=true;
	}
	
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e instanceof AirWizard || e instanceof Wraith || e instanceof Clallay;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		hurt(level, x, y, dmg);
		return true;
	}
	public void render(Screen screen, Level level, int x, int y) {
		if((level.getTile(x-1, y)==Tiles.get("Coarse dirt") || level.getTile(x+1, y)==Tiles.get("Coarse dirt") || level.getTile(x, y-1)==Tiles.get("Coarse dirt") || level.getTile(x, y+1)==Tiles.get("Coarse dirt")))Tiles.get("coarse dirt").render(screen, level, x, y);
		else if(level.depth==-3)Tiles.get("moss").render(screen, level, x, y);
		else Tiles.get("dirt").render(screen, level, x, y);
		if(level.depth==-6 || level.realm.contains("dungeon"))spriteO.render(screen, x * 16, y * 16);
		else sprite.render(screen, x * 16, y * 16);
	}
	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if(Game.isMode("Creative"))
			return false; // Go directly to hurt method
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Axe || tool.type == ToolType.Hammer) {
				int staminaPay=(4-tool.level < 2 ? 2 : 4-tool.level);
				if(4-tool.level<2)staminaPay=2;
				int dmg= (int)(random.nextInt(10) + tool.damage * (tool.type == ToolType.Hammer ? 1.2 : 1));
				if (player.payStamina(staminaPay) && tool.payDurability() && tool.level!=6) {
					hurt(level, xt, yt, dmg);
					return true;
				}else if(player.payStamina(staminaPay) && tool.payDurability() && tool.level==6){
					hurt(level, xt, yt, random.nextInt(2) + 3 * 2 + 1);
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
			level.dropItem(x * 16 + 8, y * 16 + 8, 0, 4, Items.get("Stick"));
			if(level.depth==-3)level.setTile(x, y, Tiles.get("Moss"));
			else level.setTile(x, y, Tiles.get("Dirt"));
		} else {
			level.setData(x, y, damage);
		}
	}

	public void bumpedInto(Level level, int x, int y, Entity entity) {
		if (entity instanceof AirWizard || entity instanceof Wraith || entity instanceof Ghost ||  entity instanceof Clallay    ||  entity instanceof Knight  ||  entity instanceof Snake) return;
		
		if(entity instanceof Mob)
			((Mob)entity).hurt(this, x, y, 1 + Settings.getIdx("diff"));
		/*if(entity instanceof Player) {
			double chance = Math.random();
			if (chance < 0.03 && Game.isMode("Survival")) Game.player.potioneffects.put(PotionType.Poison, 400); //beware!
		}*/
	}


}
