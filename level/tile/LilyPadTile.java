package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Arrow;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.*;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.level.LevelGen;

public class LilyPadTile extends Tile {
	private static final Sprite  lilySprite = new Sprite(0, 9, 2,2,1);
	private static final Sprite azlaeaSprite = new Sprite(3, 20, 1);
	private static final Sprite flowerSprite = new Sprite(3, 8, 1);

	protected LilyPadTile(String name) {
		super(name, (ConnectorSprite)null);
		connectsToFluid = true;
		maySpawn = true;
	}

	public boolean tick(Level level, int xt, int yt) {
		// TODO revise this method.
		int xn = xt;
		int yn = yt;

		if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
		else yn += random.nextInt(2) * 2 - 1;

		if (level.getTile(xn, yn) == Tiles.get("Hole")) {
			level.setTile(xn, yn, Tiles.get("Water"));
		}
		int type = level.getData(xt, yt);
		if(type==0) level.setData(xt,yt,random.nextInt(3)+1);

		if (random.nextInt(30) != 0) return false; // Skips every 31 tick. Lily may collapse

			//level.setTile(xt, yt, Tiles.get("Water"));
	//			level.dropItem(xt,yt,Items.get("lily pad"));

		return false;
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e instanceof Player || e instanceof Wraith || e instanceof AirWizard  || e instanceof Clallay || e instanceof Arrow || e instanceof Ghost;
	}

	public void render(Screen screen, Level level, int x, int y) {
		int xa = x;
		int ya = y;
		Tiles.get("Water").render(screen, level, x, y);
		x = x << 4;
		y = y << 4;
		lilySprite.render(screen, x, y);
		switch(level.getData(xa,ya)){
			case 1:azlaeaSprite.render(screen, x+4, y+4);break;
			case 2:flowerSprite.render(screen, x+4, y+4);break;
		}
	}
	public void steppedOn(Level level, int x, int y, Entity entity) {
		int chance=random.nextInt(80);
		if (entity instanceof Mob && (!(entity instanceof AirWizard)  && !(entity instanceof Ghost) && !(entity instanceof Wraith) && !(entity instanceof WraithA) && !(entity instanceof Clallay))) {
			//lily is collapsing under you
			if(chance==40) {
				level.setTile(x, y, Tiles.get("Water"));
				level.dropItem(x * 16 + 8, y * 16 + 8, Items.get("lily pad"));
				switch (level.getData(x, y)) {
					case 1:
						level.dropItem(x * 16 + 8, y * 16 + 8, 1, 1, Items.get("Azalea"));
						break;
					case 2:
						level.dropItem(x * 16 + 8, y * 16 + 8, 1, 1, Items.get("Flower"));
						break;
				}
			}
		}
	}
	public boolean interact(Level level, int x, int y, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(2 - tool.level) && tool.payDurability()) {
					level.setTile(x, y, Tiles.get("water"));
					Sound.monsterHurt.play();
					level.dropItem(x * 16 + 8, y * 16 + 8, Items.get("lily pad"));
					switch(level.getData(x,y)){
						case 1:level.dropItem(x *16 + 8, y * 16 + 8, 1, 1, Items.get("Azalea"));break;
						case 2:level.dropItem(x *16 + 8, y * 16 + 8, 1, 1, Items.get("Flower"));break;
					}
					return true;
				}
			}
		}
		return false;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		level.dropItem(x * 16 + 8, y * 16 + 8, Items.get("lily pad"));
		switch(level.getData(x,y)){
			case 1:level.dropItem(x *16 + 8, y * 16 + 8, 1, 1, Items.get("Azalea"));break;
			case 2:level.dropItem(x *16 + 8, y * 16 + 8, 1, 1, Items.get("Flower"));break;
		}
		level.setTile(x, y, Tiles.get("water"));
		return true;
	}
}
