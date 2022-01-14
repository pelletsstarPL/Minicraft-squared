package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.entity.Arrow;
import minicraft.entity.Entity;
import minicraft.entity.mob.*;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;

public class InfiniteFallTile extends Tile {
	
	protected InfiniteFallTile(String name) {
		super(name, (Sprite)null);
	}

	public void render(Screen screen, Level level, int x, int y) {}

	public boolean tick(Level level, int xt, int yt) { return false; }

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e instanceof WraithA || e instanceof Wraith || e instanceof AirWizard || e instanceof AirWizardPhase2 || e instanceof AirWizardPhase5 || e instanceof AirWizard || e instanceof AirWizardPhase3 || e instanceof AirWizardPhase4 || e instanceof Arrow || e instanceof Ghost|| e instanceof Player && ( ((Player) e).suitOn || Game.isMode("creative") );
	}
}
