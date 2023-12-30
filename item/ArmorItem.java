package minicraft.item;

import java.util.ArrayList;

import minicraft.core.Updater;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

public class ArmorItem extends StackableItem {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
		
		items.add(new ArmorItem("Leather armor", new Sprite(0, 9, 0), .3f, 1));
		items.add(new ArmorItem("Snake armor", new Sprite(1, 9, 0), .4f, 2));
		items.add(new ArmorItem("Iron armor", new Sprite(2, 9, 0), .5f, 3));
		items.add(new ArmorItem("Gold armor", new Sprite(3, 9, 0), .7f, 4));
		items.add(new ArmorItem("Gem armor", new Sprite(4, 9, 0), 1f, 5));
		items.add(new ArmorItem("Obsidium armor", new Sprite(5, 9, 0), 1.6f, 6));
		items.add(new ArmorItem("Night armor", new Sprite(7, 9, 0), 1.8f, 6));

		return items;
	}

	private final float armor;
	private final int staminaCost;
	public final int level;

	private ArmorItem(String name, Sprite sprite, float health, int level) { this(name, sprite, 1, health, level); }
	private ArmorItem(String name, Sprite sprite, int count, float health, int level) {
		super(name, sprite, count);
		this.armor = health;
		this.level = level;
		staminaCost = 9;
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		boolean success = false;
		if (player.curArmor == null && player.payStamina(staminaCost)) {
			player.curArmor = this; // Set the current armor being worn to this.
			player.armor = (int) (armor * Player.maxArmor); // Armor is how many hits are left
			success = true;
		}

		return super.interactOn(success);
	}

	@Override
	public boolean interactsWithWorld() { return false; }

	public ArmorItem clone() {
		return new ArmorItem(getName(), sprite, count, armor, level);
	}
}

