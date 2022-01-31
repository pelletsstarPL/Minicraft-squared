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
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class CloudCactusTile extends Tile {
	private static Sprite sprite = new Sprite(6, 2, 2, 2, 1);
	
	protected CloudCactusTile(String name) {
		super(name, sprite);
	}
	
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e instanceof AirWizard || e instanceof AirWizardPhase2 || e instanceof WraithA || e instanceof Wraith || e instanceof Clallay;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		hurt(level, x, y, 0);
		return true;
	}
	public void render(Screen screen, Level level, int x, int y) {
		if (Tiles.get("cloud") != null) {
			Tiles.get("cloud").render(screen, level, x, y);
		}
		screen.render(x * 16 + 0, y * 16 + 0, 6 + 2 * 32, 0, 1);
		screen.render(x * 16 + 8, y * 16 + 0, 7 + 2 * 32, 0, 1);
		screen.render(x * 16 + 0, y * 16 + 8, 6 + 3 * 32, 0, 1);
		screen.render(x * 16 + 8, y * 16 + 8, 7 + 3 * 32, 0, 1);
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



	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + dmg;
		int health = 10;
		if (Game.isMode("creative")) dmg = damage = health;
		level.add(new SmashParticle(x * 16, y * 16));
		Sound.monsterHurt.play();
		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= health) {
			level.setTile(x, y, Tiles.get("Cloud"));
		} else
			level.setData(x, y, damage);
	}

	public void bumpedInto(Level level, int x, int y, Entity entity) {
		if (entity instanceof AirWizard || entity instanceof AirWizardPhase2 || entity instanceof AirWizardPhase3 || entity instanceof AirWizardPhase4 || entity instanceof AirWizardPhase5 || entity instanceof Wraith || entity instanceof WraithA || entity instanceof Ghost ||  entity instanceof Clallay) return;
		
		if(entity instanceof Mob)
			((Mob)entity).hurt(this, x, y, 1 + Settings.getIdx("diff"));
	}


}
