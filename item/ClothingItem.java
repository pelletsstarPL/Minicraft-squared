package minicraft.item;

import java.util.ArrayList;

import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

public class ClothingItem extends StackableItem {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
		
		items.add(new ClothingItem("Red clothes", new Sprite(0, 10, 0), Color.get(1, 204, 0, 0)));
		items.add(new ClothingItem("Blue clothes", new Sprite(1, 10, 0), Color.get(1, 0, 0, 204)));
		items.add(new ClothingItem("Lime clothes",  new Sprite(2, 10, 0), Color.get(1, 0, 204, 0)));
		items.add(new ClothingItem("Green clothes",  new Sprite(10, 10, 0), Color.get(1, 0, 100, 0)));
		items.add(new ClothingItem("L. blue clothes",  new Sprite(11, 10, 0), Color.get(1, 144, 144, 249)));
		items.add(new ClothingItem("White clothes",  new Sprite(12, 10, 0), Color.get(1, 255, 255, 255)));
		items.add(new ClothingItem("Yellow clothes",  new Sprite(3, 10, 0), Color.get(1, 204, 204, 0)));
		items.add(new ClothingItem("Black clothes",  new Sprite(4, 10, 0), Color.get(1, 0,0,0)));
		items.add(new ClothingItem("Gray clothes",  new Sprite(14, 10, 0), Color.get(1, 90,90,90)));
		items.add(new ClothingItem("L. gray clothes",  new Sprite(15, 10, 0), Color.get(1, 200,200,200)));
		items.add(new ClothingItem("Magenta clothes",  new Sprite(13, 10, 0), Color.get(1, 255,76,228)));
		items.add(new ClothingItem("Orange clothes",  new Sprite(5, 10, 0), Color.get(1, 255, 102, 0)));
		items.add(new ClothingItem("Purple clothes",  new Sprite(6, 10, 0), Color.get(1, 102, 0, 153)));
		items.add(new ClothingItem("Cyan clothes",  new Sprite(7, 10, 0), Color.get(1, 0, 102, 153)));
		items.add(new ClothingItem("Pink clothes",  new Sprite(9, 10, 0), Color.get(1, 255, 182, 193)));
		items.add(new ClothingItem("Reg clothes",  new Sprite(8, 10, 0), Color.get(1, 51, 51, 0)));
		//white
		return items;
	}
	
	private int playerCol;
	private Sprite sprite;
	
	private ClothingItem(String name, Sprite sprite, int pcol) { this(name, 1, sprite, pcol); }
	private ClothingItem(String name, int count, Sprite sprite, int pcol) {
		super(name, sprite, count);
		playerCol = pcol;
		this.sprite = sprite;
	}
	
	// Put on clothes
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		if (player.shirtColor == playerCol) {
			return false;
		} else {
			player.shirtColor = playerCol;
			if (Game.isValidClient())
				Game.client.sendShirtColor();
			return super.interactOn(true);
		}
	}
	
	@Override
	public boolean interactsWithWorld() { return false; }
	
	public ClothingItem clone() {
		return new ClothingItem(getName(), count, sprite, playerCol);
	}
}
