package minicraft.item;
import java.util.ArrayList;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.core.Game;
import minicraft.entity.Entity;
import minicraft.item.Inventory;
import minicraft.level.tile.Tile;

public class DrinkItem extends StackableItem {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new DrinkItem("Milk", new Sprite(0, 7, 0), 3));
		items.add(new DrinkItem("Bottle", new Sprite(9, 7, 0), 0)); //empty bottle so why should it feed ya
		items.add(new DrinkItem("Beetroot juice", new Sprite(10, 7, 0), 4));
		items.add(new DrinkItem("Apple juice", new Sprite(1, 7, 0), 4));
		items.add(new DrinkItem("Cactus juice", new Sprite(2, 7, 0), 5));
		items.add(new DrinkItem("Gold apple juice", new Sprite(3, 7, 0), 18));
		items.add(new DrinkItem("Herb tea", new Sprite(4, 7, 0), 4));
		items.add(new DrinkItem("Dirty water", new Sprite(5, 7, 0), 1));
		items.add(new DrinkItem("Purified water", new Sprite(6, 7, 0), 2));
		items.add(new DrinkItem("Water bottle", new Sprite(7, 7, 0), 3));
		items.add(new DrinkItem("Carrot juice", new Sprite(8, 7, 0), 4));
		
		return items;
	}
	
	private int feed; // The amount of hunger the food "satisfies" you by.
	private int staminaCost; // The amount of stamina it costs to consume the food.
	
	private DrinkItem(String name, Sprite sprite, int feed) { this(name, sprite, 1, feed); }
	private DrinkItem(String name, Sprite sprite, int count, int feed) {
		super(name, sprite, count);
		this.feed = feed;
		if(feed==0) {
		 staminaCost=1;
		}else staminaCost = 3;
	}
	
	/** What happens when the player uses the item on a tile */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		boolean success = false;
		if (count > 0 && player.thirst < Player.maxThirst && player.payStamina(staminaCost)) { // If the player has thirst to fill, and stamina to pay...
			double chance=Math.random();
			switch(getName()) {
				case "Dirty water": if(chance>0.2){
					Game.player.potioneffects.put(PotionType.Thirst, 500);
				};break;
				case "Purified water": if(chance<0.12){
					Game.player.potioneffects.put(PotionType.Thirst, 400);
				};break;
			}
			int onTile = level.getTile(player.x >> 4, player.y >> 4).id;
			if(feed==0 && onTile==6){ //0 bc its Bottle
				staminaCost=1;
				Game.player.getInventory().add(Items.get("Dirty water"));
			}else Game.player.getInventory().add(Items.get("Bottle")); //refunds bottle on drinking
			player.thirst = Math.min(player.thirst + feed, Player.maxThirst); // Restore the thirst
			success = true;
		}
		
		return super.interactOn(success);
	}
	
	@Override
	public boolean interactsWithWorld() { return false; }
	
	public DrinkItem clone() {
		return new DrinkItem(getName(), sprite, count, feed);
	}
}
