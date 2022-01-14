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
import minicraft.gfx.Sprite;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class StoneOreTile extends Tile {
	private static Sprite sprite = new Sprite(4, 4, 2, 2, 1);
	
	protected StoneOreTile(String name) {
		super(name, sprite);
		connectsToFluid=true;
	}
	
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e instanceof AirWizard;
	}

	public boolean hurt(Level level,int x, int y, Mob source, int dmg, Direction attackDir) {
		hurt(level, x, y, 1);
		return true;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if(Game.isMode("creative"))
			return false; // Go directly to hurt method
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Pickaxe) {
				if (player.payStamina(6 - tool.level) && tool.payDurability()) {
					hurt(level, xt, yt, 1);
					return true;
				}
			}
		}
		return false;
	}
	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("Water").render(screen, level, x, y);

		sprite.render(screen, x << 4, y << 4);
	}
	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + dmg;
		int health = 12;
		if (Game.isMode("creative")) dmg = damage = health;
		level.add(new SmashParticle(x * 16, y * 16));
		Sound.monsterHurt.play();
		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= health) {
			level.setTile(x, y, Tiles.get("Water"));
			level.dropItem(x * 16 + 8, y * 16 + 8,1,2,Items.get("Stone"));
		} else
			level.setData(x, y, damage);
	}

	public void bumpedInto(Level level, int x, int y, Entity entity) {
		if (entity instanceof AirWizard) return;
		
		if(entity instanceof Player)
			((Mob)entity).hurt(this, x, y, 1 + Settings.getIdx("diff"));
	}
	public boolean tick(Level level, int xt, int yt) {
		int xn = xt;
		int yn = yt;

		if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
		else yn += random.nextInt(2) * 2 - 1;

		if (level.getTile(xn, yn) == Tiles.get("Hole")) {
			level.setTile(xn, yn, Tiles.get("Water"));
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
}
