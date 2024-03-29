package minicraft.item;

import java.util.ArrayList;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.entity.Entity;
import minicraft.item.Inventory;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

import static minicraft.core.Game.currentLevel;
import static minicraft.core.Game.player;

// Some items are direct instances of this class; those instances are the true "items", like stone, wood, wheat, or coal; you can't do anything with them besides use them to make something else.

public class StackableItem extends Item {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
	
		items.add(new StackableItem("Wood", new Sprite(1, 0, 0)));
		items.add(new StackableItem("Stone", new Sprite(2, 0, 0)));
		items.add(new StackableItem("Leather", new Sprite(8, 0, 0)));
		items.add(new StackableItem("Wheat", new Sprite(6, 0, 0)));
		items.add(new StackableItem("Key", new Sprite(0, 4, 0)));
		items.add(new StackableItem("Arrow", new Sprite(0, 2, 0)));
		items.add(new StackableItem("String", new Sprite(1, 4, 0)));
		items.add(new StackableItem("Coal", new Sprite(2, 4, 0)));
		items.add(new StackableItem("Charcoal", new Sprite(21, 4, 0)));
		items.add(new StackableItem("Iron ore", new Sprite(3, 4, 0)));
		items.add(new StackableItem("Lapis", new Sprite(4, 4, 0)));
		items.add(new StackableItem("Gold ore", new Sprite(5, 4, 0)));
		items.add(new StackableItem("Iron", new Sprite(6, 4, 0)));
		items.add(new StackableItem("Gold", new Sprite(7, 4, 0)));
		items.add(new StackableItem("Rose", new Sprite(5, 0, 0)));
		items.add(new StackableItem("Gunpowder", new Sprite(8, 4, 0)));
		items.add(new StackableItem("Slime", new Sprite(9, 4, 0)));
		items.add(new StackableItem("Glass", new Sprite(10, 4, 0)));
		items.add(new StackableItem("Cloth", new Sprite(11, 4, 0)));
		items.add(new StackableItem("Bone", new Sprite(9, 3, 0)));
		items.add(new StackableItem("Gem", new Sprite(12, 4, 0)));
		items.add(new StackableItem("Scale", new Sprite(13, 4, 0)));
		items.add(new StackableItem("Blood shard", new Sprite(22, 4, 0)));
		items.add(new StackableItem("Cloud shard", new Sprite(20, 4, 0)));
		items.add(new StackableItem("Stick", new Sprite(14, 3, 0)));
		items.add(new StackableItem("Sugar", new Sprite(15, 4, 0)));
		items.add(new StackableItem("Obsidian", new Sprite(16, 4, 0)));
		items.add(new StackableItem("Paper", new Sprite(15, 3, 0)));
		items.add(new StackableItem("Coal filter", new Sprite(16, 3, 0)));
		items.add(new StackableItem("Moss", new Sprite(17, 3, 0)));
		items.add(new StackableItem("Ice", new Sprite(19, 3, 0)));
		items.add(new StackableItem("Snow", new Sprite(18, 3, 0)));
		items.add(new StackableItem("Fungus", new Sprite(17, 4, 0)));
		items.add(new StackableItem("Beetroot", new Sprite(22, 0, 0)));
		items.add(new StackableItem("Plant fiber", new Sprite(10, 1, 0)));
		items.add(new StackableItem("Obsidium ore", new Sprite(24, 4, 0)));
		items.add(new StackableItem("Obsidium", new Sprite(25, 4, 0)));
		return items;
	}
	
	public int count;
	//public int maxCount = 100; // TODO I want to implement this later.
	
	protected StackableItem(String name, Sprite sprite) {
		super(name, sprite);
		count = 1;
	}
	protected StackableItem(String name, Sprite sprite, int count) {
		this(name, sprite);
		this.count = count;
	}
	
	public boolean stacksWith(Item other) { return other instanceof StackableItem && other.getName().equals(getName()); }



	// This is used by (most) subclasses, to standardize the count decrement behavior. This is not the normal interactOn method.
	protected boolean interactOn(boolean subClassSuccess,boolean withBox) {
			if(withBox)Player.boxAnim[0]=39;
			if (subClassSuccess) {
				if (!Game.isMode("creative"))
					count--;
			}else {
				if (withBox) {
					Player.boxAnim[1] = 40;
					if((boolean)Settings.get("soundno"))Sound.no.play();
				}
			}
			return subClassSuccess;
		}
	// This is used by (most) subclasses, to standardize the count decrement behavior. This is not the normal interactOn method.
	protected boolean interactOn(boolean subClassSuccess) {return interactOn(subClassSuccess,false);}

	
	/** Called to determine if this item should be removed from an inventory. */
	@Override
	public boolean isDepleted() {
		return count <= 0;
	}
	
	@Override
	public StackableItem clone() {
		return new StackableItem(getName(), sprite, count);
	}
	
	@Override
	public String toString() {
		return super.toString() + "-Stack_Size:"+count;
	}
	
	public String getData() {
		return getName() + "_" + count;
	}
	
	@Override
	public String getDisplayName() {
		String amt = (Math.min(count, 999)) + " ";
		return " " + amt + Localization.getLocalized(getName());
	}
}
