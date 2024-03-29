package minicraft.item;

import java.util.ArrayList;

import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import sun.print.BackgroundLookupListener;

public class FoodItem extends StackableItem {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new FoodItem("Baked potato", new Sprite(19, 0, 0), 2));
		items.add(new FoodItem("Cooked carrot", new Sprite(21, 0, 0), 1));
		items.add(new FoodItem("Apple", new Sprite(16, 0, 0), 1));
		items.add(new FoodItem("Raw pork", new Sprite(10, 0, 0), 1));
		items.add(new FoodItem("Raw fish", new Sprite(14, 0, 0), 1));
		items.add(new FoodItem("Raw beef", new Sprite(12, 0, 0), 1));
		items.add(new FoodItem("Bread", new Sprite(7, 0, 0), 4));
		items.add(new FoodItem("Cooked fish", new Sprite(15, 0, 0), 4));
		items.add(new FoodItem("Cooked pork", new Sprite(11, 0, 0), 5));
		items.add(new FoodItem("Steak", new Sprite(13, 0, 0), 5));
		items.add(new FoodItem("Gold apple", new Sprite(17, 0, 0), 12));
		items.add(new FoodItem("Flesh", new Sprite(23, 0, 0), 1));

		return items;
	}
	
	private int feed; // The amount of hunger the food "satisfies" you by.
	private int staminaCost; // The amount of stamina it costs to consume the food.
	
	private FoodItem(String name, Sprite sprite, int feed) { this(name, sprite, 1, feed); }
	private FoodItem(String name, Sprite sprite, int count, int feed) {
		super(name, sprite, count);
		this.feed = feed;
		staminaCost = 4;
	}
	
	/** What happens when the player uses the item on a tile */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		boolean success = false;

		if (count > 0 && player.hunger < Player.maxHunger && player.payStamina(staminaCost)) { // If the player has hunger to fill, and stamina to pay...
			double chance=Math.random();
			switch(getName()){
				case "Raw Fish": if(chance<0.1){
					Game.player.potionEffects.put(PotionType.Hunger, 600);
				};break;
				case "Raw Beef": if(chance<0.06){
					Game.player.potionEffects.put(PotionType.Hunger, 500);
				};break;
				case "Raw Pork": if(chance<0.06){
					Game.player.potionEffects.put(PotionType.Hunger, 500);
				};break;
			//	case "Gold Apple": if(chance<0.5){
				//	Game.player.potioneffects.put(PotionType.Regen, 200);
				//};
				case "Flesh": if(chance<0.76){
					Game.player.potionEffects.put(PotionType.Hunger, 900);
					if(chance<0.004)Game.player.potionEffects.put(PotionType.Poison, 200);
				};break;
			}
			if(player.hunger + feed > player.maxHunger){
				int mult=(player.hunger + feed) - player.maxHunger;
				if(Math.random()<0.05*mult)Game.player.potionEffects.put(PotionType.WellFed, 300*mult); //the more above the max hunger the longer effect is and has bigger chance
			}
			player.hunger = Math.min(player.hunger + feed, player.maxHunger); // Restore the hunger
			success = true;
		}

		return super.interactOn(success);
	}
	
	@Override
	public boolean interactsWithWorld() { return false; }
	
	public FoodItem clone() {
		return new FoodItem(getName(), sprite, count, feed);
	}
}
