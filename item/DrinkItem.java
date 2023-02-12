package minicraft.item;
import java.util.ArrayList;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.core.Game;
import minicraft.level.tile.Tile;

public class DrinkItem extends StackableItem {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new DrinkItem("Milk", new Sprite(0, 7, 0), 3));
		items.add(new DrinkItem("Milk Bucket", new Sprite(3, 6, 0), 12));
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
		if(name=="Milk Bucket") {
		 staminaCost=10;
		}else staminaCost = 2;
	}
	
	/** What happens when the player uses the item on a tile */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		int onTile = level.getTile(player.x >> 4, player.y >> 4).id;
		boolean success = false;
		if(getName()=="Milk Bucket") {
			if(player.stamina>8 && count > 0 && player.thirst < Player.maxThirst && player.payStamina(staminaCost)){
				for (PotionType potionType : player.potionEffects.keySet().toArray(new PotionType[0])) {
					if (!potionType.nat) { //if effect is a superopower like speed or lava for instance
						player.potionEffects.put(potionType, player.potionEffects.get(potionType) - 18000); // Remove 18000
					}
				}
				player.potionEffects.put(PotionType.Fatigue,650); // Remove 18000
				Game.player.getInventory().add(Items.get("Empty Bucket"));
				success=true;
			}else{
				Game.notifications.add("Cannot drink Milk Bucket");
			}
		}else if (count > 0 && player.thirst < Player.maxThirst && player.payStamina(staminaCost)) { // If the player has thirst to fill, and stamina to pay... and bottle isn't empty
			double chance = Math.random();
			switch (getName()) {
				case "Milk":
					for (PotionType potionType : player.potionEffects.keySet().toArray(new PotionType[0])) {
						if (!potionType.nat) { //if effect is a superopower like speed or lava for instance
							player.potionEffects.put(potionType, player.potionEffects.get(potionType) - 4000); // remove 4000
						}
					}
					break;
				case "Dirty water":
					if (chance > 0.2) {
						Game.player.potionEffects.put(PotionType.Thirst, 500);
					}
					;
					break;
				case "Purified water":
					if (chance < 0.04) {
						Game.player.potionEffects.put(PotionType.Thirst, 400);
					}
					;
					break;
			}
			if (getName() == "Herb tea" && Math.random() < 0.6) player.heal(1);
			Game.player.getInventory().add(Items.get("Bottle"));
			if(player.thirst + feed > player.maxThirst){
				int mult=(player.thirst + feed) - player.maxThirst;
				if(Math.random()<0.05*mult)Game.player.potionEffects.put(PotionType.WellHydrated, 300*mult); //the more above the max thirst the longer effect is and has bigger chance
			}
			success = true;
		}
		player.thirst = Math.min(player.thirst + feed, Player.maxThirst); // Restore the thirst
		return super.interactOn(success);
	}
	
	@Override
	public boolean interactsWithWorld() { return false; }
	
	public DrinkItem clone() {
		return new DrinkItem(getName(), sprite, count, feed);
	}
}
