package minicraft.item;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

import java.util.ArrayList;

public class ShardItem extends StackableItem {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new ShardItem("Shard", (new Sprite(14, 4,  0))));

		return items;
	}


	protected ShardItem(String name, Sprite sprite) {
		super(name, sprite);
		count = 1;
	}
	protected ShardItem(String name, Sprite sprite, int count) {
		this(name, sprite);
		this.count = count;
	}


	/**
	 *
	 * @param tile tile we are interacting with
	 * @param level current player's world level
	 * @param xt x axis
	 * @param yt y axis
	 * @param player us
	 * @param attackDir direction. Does not matter tbh
	 * @return Could we insert a shard and if so can we activate a portal then?
	 */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		boolean success = false;
if(level.getData(xt,yt)%3!=2 && level.getTile(xt, yt) == Tiles.get("Obsidian void portal frame")){
	level.setData(xt,yt,level.getData(xt,yt)+1);
	success = true;
	int fullCount=0;
	//check if we are now able to activate a portal to OBV
	for(int x = xt - 3;x <= xt + 3;x++)
		for(int y = yt - 3;y <= yt + 3;y++) {
			//0 nothing has been inserted, 1 - one shard, 2 - two shards
			if (level.getData(x, y) > 0 && level.getData(x, y) % 3 == 2)
				fullCount++;
			Sound.place.play();
		}
	if(fullCount==8){ //yes we can
		for(int x = xt - 3;x <= xt + 3;x++)
			for(int y = yt - 3;y <= yt + 3;y++)if(level.getTile(x,y)==Tiles.get("obsidian bridge support"))level.setTile(x,y,Tiles.get("Obsidian void portal"));
		}

}else{
	if(level.getTile(xt, yt) == Tiles.get("Obsidian void portal frame"))Game.notifications.add("All shards are inserted there");
	success = false;
}

		return super.interactOn(success,true);
	}
	@Override
	public boolean interactsWithWorld() { return true; }
	public boolean displayBox(){return true;}
	public ShardItem clone() {
		return new ShardItem(getName(), sprite, count);
	}
}
