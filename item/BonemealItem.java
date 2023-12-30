package minicraft.item;

import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.GreenStarParticle;
import minicraft.gfx.Point;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.SaplingTile;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.level.tile.farming.Plant;

import java.util.ArrayList;

import static minicraft.core.Game.player;
import static minicraft.entity.mob.Player.INTERACT_DIST;

public class BonemealItem extends StackableItem {

	protected static ArrayList<Item> getAllInstances() {

		ArrayList<Item> items = new ArrayList<>();

		items.add(new BonemealItem("Bonemeal", (new Sprite(18, 4, 0))));

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

	private BonemealItem(String name, Sprite sprite) { this(name, sprite, 1); }
	private BonemealItem(String name, Sprite sprite, int count) {
		super(name, sprite, count);
	}

	/** What happens when the player uses the item on a tile */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		boolean success = false;

		//bonemeal only works with worlds like overworld
		if(level.realm.contains("overworld")) {
			int age=level.getData(xt,yt);
			int maxA=level.getTile(xt,yt)==Tiles.get("Wheat") || level.getTile(xt,yt)==Tiles.get("Carrot") || level.getTile(xt,yt)==Tiles.get("Reed") || level.getTile(xt,yt)==Tiles.get("Potato") || level.getTile(xt,yt)==Tiles.get("Beetroot") ? 80 : level.getTile(xt,yt).name.contains("Sapling") ? 100 : 1000;
			//interaction code
			if (level.getTile(xt, yt) == Tiles.get("Grass")) {
				for (int y = yt - 1; y <= yt + 1; y++) {
					for (int x = xt - 1; x <= xt + 1; x++) {
						if (level.getTile(x, y).name.contains("GRASS") && !level.getTile(x, y).name.contains("SMALL STONES") && Math.random() < 0.4 && !level.getTile(x, y).name.contains("TORCH")) //basically if tile name begins with grass
							if (Math.random() < 0.1) level.setTile(x, y, Tiles.get("flower"));
							else level.setTile(x, y, Tiles.get("tall grass"));
						success = true;
					}
				}
			} else if(level.getTile(xt,yt) instanceof Plant || level.getTile(xt,yt) instanceof SaplingTile){
//speeding up plants/trees growth
				if (age < maxA) {
					for (int i = 0; i < 3; i++) {
						int randX = (int) Math.ceil(Math.random() * 15) - 6;
						int randY = (int) Math.ceil(Math.random() * 15) - 6;
						level.add(new GreenStarParticle(xt * 16 + randX, yt * 16 + randY));
					}
					level.setData(xt, yt, age + 40 >= maxA ? maxA : age + 40);
					success = true;
				}
			}
		}else {
			Game.notifications.add("It is too hot here.");
			return super.interactOn(false, false);
		}
		return super.interactOn(success, true);
	}

	@Override
	public boolean interactsWithWorld() { return true; }

	public BonemealItem clone() {
		return new BonemealItem(getName(), sprite, count);
	}
}
