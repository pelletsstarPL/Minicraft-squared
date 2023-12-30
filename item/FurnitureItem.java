package minicraft.item;

import java.util.ArrayList;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.furniture.*;
import minicraft.entity.mob.*;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

public class FurnitureItem extends Item {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
		
		/// There should be a spawner for each level of mob, or at least make the level able to be changed.
		items.add(new FurnitureItem(new Spawner(new Cow()),new Sprite(1,32,0)));
		items.add(new FurnitureItem(new Spawner(new Pig()),new Sprite(2,32,0)));
		items.add(new FurnitureItem(new Spawner(new Sheep()),new Sprite(3,32,0)));
		items.add(new FurnitureItem(new Spawner(new Slime(1)),new Sprite(4,32,0)));
		items.add(new FurnitureItem(new Spawner(new Zombie(1)),new Sprite(5,32,0)));
		items.add(new FurnitureItem(new Spawner(new Wraith(1,0)),new Sprite(6,32,0)));
		items.add(new FurnitureItem(new Spawner(new Creeper(1)),new Sprite(7,32,0)));
		items.add(new FurnitureItem(new Spawner(new Skeleton(1)),new Sprite(8,32,0)));
		items.add(new FurnitureItem(new Spawner(new Snake(1,false)),new Sprite(9,32,0)));
		items.add(new FurnitureItem(new Spawner(new Knight(1)),new Sprite(10,32,0)));
		items.add(new FurnitureItem(new Spawner(new FireSage(1)),new Sprite(11,32,0)));
		items.add(new FurnitureItem(new Spawner(new KnightTop(1)),new Sprite(12,32,0)));
		items.add(new FurnitureItem(new Spawner(new AncSkeleton(1)),new Sprite(13,32,0)));
		items.add(new FurnitureItem(new Spawner(new Clallay()),new Sprite(3,32,0)));
		items.add(new FurnitureItem(new Spawner(new Ghost()),new Sprite(6,32,0)));
		items.add(new FurnitureItem(new Spawner(new Mimic(1)),new Sprite(14,32,0)));
		items.add(new FurnitureItem(new Spawner(new AirWizard(false)),new Sprite(15,32,0)));

		items.add(new FurnitureItem(new Chest(),new Sprite(1,30,0)));
		items.add(new FurnitureItem(new DungeonChest(false, true),new Sprite(1,31,0)));
		items.add(new FurnitureItem(new MimicChest(),new Sprite(1,29,0)));

		// Add the various types of crafting furniture
		int i=0;
		for (Crafter.Type type: Crafter.Type.values()) {
			 items.add(new FurnitureItem(new Crafter(type),new Sprite(2 + i,30,0)));
			 i++;
		}
		items.add(new FurnitureItem(new Bed(),new Sprite(2 + i,30,0)));
		i++;
		// Add the various lanterns
		for (Lantern.Type type: Lantern.Type.values()) {
			 items.add(new FurnitureItem(new Lantern(type),new Sprite(2 + i,30,0)));
			i++;
		}
		
		items.add(new FurnitureItem(new Tnt(),new Sprite(0,30,0)));


		return items;
	}
	
	public Furniture furniture; // The furniture of this item
	public int sx,sy; // The furniture of this item
	public boolean placed; // Value if the furniture has been placed or not.
	private  Sprite getSprite() {
		return this.sprite ;
	}
	
	public FurnitureItem(Furniture furniture,Sprite sprite) {
		super(furniture.name, sprite);
		this.furniture = furniture; // Assigns the furniture to the item
		placed = false;
	}
	public FurnitureItem(Furniture furniture) {
		super(furniture.name, Items.get(furniture.name).sprite);
		this.furniture = furniture; // Assigns the furniture to the item
		placed = false;
	}
	
	/** Determines if you can attack enemies with furniture (you can't) */
	public boolean canAttack() {
		return false;
	}
	
	/** What happens when you press the "Attack" key with the furniture in your hands */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		if (tile.mayPass(level, xt, yt, furniture)) { // If the furniture can go on the tile
			Sound.place.play();

			// Placed furniture's X and Y positions
			furniture.x = xt * 16 + 8;
			furniture.y = yt * 16 + 8;
			furniture.setRealmId(player.getRealmId());
			level.add(furniture,player.getRealmId()); // Adds the furniture to the world
			if (Game.isMode("creative"))
				furniture = furniture.clone();
			else {

				placed = true; // The value becomes true, which removes it from the player's active item

			}
			
			return true;
		}
		return false;
	}
	
	public boolean isDepleted() {
		return placed;
	}
	
	public FurnitureItem clone() {
		return new FurnitureItem(furniture.clone(),sprite);
	}
}
