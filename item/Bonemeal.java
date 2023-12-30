package minicraft.item;

import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Point;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

import java.util.ArrayList;

import static minicraft.core.Game.player;
import static minicraft.entity.mob.Player.INTERACT_DIST;

public class Bonemeal extends StackableItem {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new Bonemeal("Bottle", new Sprite(9, 7, 0)));

		return items;
	}
	private Point getInteractionTile() {
		int x = player.x, y = player.y - 2;

		x += player.dir.getX()*INTERACT_DIST;
		y += player.dir.getY()*INTERACT_DIST;

		return new Point(x >> 4, y >> 4);
	}
	public boolean displayBox() {
		return true;
	}

	private Bonemeal(String name, Sprite sprite) { this(name, sprite, 1); }
	private Bonemeal(String name, Sprite sprite, int count) {
		super(name, sprite, count);
	}

	/** What happens when the player uses the item on a tile */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		boolean success = false;
		//interaction code
		Point t = getInteractionTile();
		if(level.getTile(t.x,t.y).id==6) {
			Game.player.getInventory().add(Items.get("Dirty water"));
			success = true;
		}


		return super.interactOn(success,true);
	}

	@Override
	public boolean interactsWithWorld() { return true; }

	public Bonemeal clone() {
		return new Bonemeal(getName(), sprite, count);
	}
}
