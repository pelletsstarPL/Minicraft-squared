package minicraft.level.tile;

import minicraft.core.Renderer;
import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.mob.*;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class SpikesTile extends Tile {
	private static Sprite[] states = new Sprite[2];
	static {
		states[0] = new Sprite(58, 17, 2, 2, 1);
		states[1] = new Sprite(60, 17, 2, 2, 1);
	}

	protected SpikesTile(String name) {
		super(name, states[0]);
		maySpawn = true;
	}


	
	public void render(Screen screen, Level level, int x, int y) {
		if(level.getData(x,y)==1) states[1].render(screen, x * 16, y * 16, 0);
		else states[0].render(screen, x * 16, y * 16, 0);
	}
	
	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		return false;
	}

	public void steppedOn(Level level, int x, int y, Entity entity) {
		if(!(entity instanceof ItemEntity) && !(entity instanceof Wraith) && !(entity instanceof Ghost) &&!(entity instanceof Knight) &&!(entity instanceof ObsidianKnight) &&!(entity instanceof Snake) && level.getData(x,y)==1) {
			Mob m = (Mob) entity;
			byte dmg = 2;
			if (Settings.get("diff").equals("Hard") && entity instanceof Player) dmg++;


			m.hurt(this, x, y, dmg);
		}
	}

}
