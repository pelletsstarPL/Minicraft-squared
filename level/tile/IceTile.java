package minicraft.level.tile;

import minicraft.core.Renderer;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.*;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class IceTile extends Tile {
	private static ConnectorSprite sprite = new ConnectorSprite(IceTile.class, new Sprite(37, 9, 3, 3, 1, 3), Sprite.dots(/*Color.get(005, 105, 115, 115)*/ 3))
	{
		public boolean connectsTo(Tile tile, boolean isSide) {
			if(!isSide) return true;
			return tile.connectsToIce;
		}
	};
	protected IceTile(String name) {
		super(name, sprite);
		csprite.sides = csprite.sparse;
		connectsToIce = true;
		connectsToFluid = true;
		maySpawn = true;
	}


	public void render(Screen screen, Level level, int x, int y) {
		long seed = (tickCount + (x / 2 - y) * 4311) / 10 * 54687121l + x * 3271612l + y * 3412987161l;
		sprite.full = Sprite.randomDots(seed, 3);
		sprite.render(screen, level, x, y);
	}
	
	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Pickaxe || tool.type == ToolType.Hammer) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					level.setTile(xt, yt, Tiles.get("Water"));
					Sound.monsterHurt.play();
					level.dropItem(xt *16 + 8, yt * 16 + 8, 0, 3, Items.get("Ice"));
					return true;
				}
			}
		}
		return false;
	}

	public void steppedOn(Level level, int x, int y, Entity entity) {
		int chance=random.nextInt(769);
		if (entity instanceof Mob && (!(entity instanceof AirWizard)  && !(entity instanceof Ghost) && !(entity instanceof Wraith)  && !(entity instanceof Clallay))) {
			//ice is cracking under you
			if(chance==233)level.setTile(x, y, Tiles.get("Water"));
		}
	}
	public boolean tick(Level level, int xt, int yt) { //because it has water underneath it
		int xn = xt;
		int yn = yt;

		if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
		else yn += random.nextInt(2) * 2 - 1;
		if(level.depth==-6)level.setTile(xn, yn, Tiles.get("Hole")); //melt
		if (level.getTile(xn, yn) == Tiles.get("Hole")) {
			level.setTile(xn, yn, "Water");
		}

		// These set only the non-diagonally adjacent lava tiles to obsidian
		for (int x = -1; x < 2; x++) {
			if (level.getTile(xt + x, yt) == Tiles.get("Lava"))
				level.setTile(xt + x, yt, Tiles.get("Raw Obsidian"));
		}
		for (int y = -1; y < 2; y++) {
			if (level.getTile(xt, yt + y) == Tiles.get("lava"))
				level.setTile(xt, yt + y, Tiles.get("Raw Obsidian"));
		}
		return false;
	}
	public boolean mayPass(Level level, int x, int y, Entity e){
		if(e instanceof NightWizard  || e instanceof AirWizard || e instanceof Player || (NightWizard.revenge>0 && (e instanceof Zombie || e instanceof Slime || e instanceof Skeleton)))
			return true;
		else return false;
	}
	public boolean maySpawn() { return false; }
}
