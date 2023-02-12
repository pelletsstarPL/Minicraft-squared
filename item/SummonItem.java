package minicraft.item;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.NightWizard;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

import java.util.ArrayList;

public class SummonItem extends StackableItem {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new SummonItem("Air totem", new Sprite(0, 20, 0), "Air Wizard"));
		items.add(new SummonItem("Moonlight totem", new Sprite(1, 20, 0), "Night Wizard"));
		items.add(new SummonItem("Time totem", new Sprite(2, 20, 0), "Time"));
		items.add(new SummonItem("Antitime totem", new Sprite(3, 20, 0), "Antitime"));

		return items;
	}

	private final String mob;

	private SummonItem(String name, Sprite sprite, String mob) { this(name, sprite, 1, mob); }
	private SummonItem(String name, Sprite sprite, int count, String mob) {
		super(name, sprite, count);
		this.mob = mob;
	}
	
	/** What happens when the player uses the item on a tile */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		boolean success = false;
		switch(mob) {
			case "Air Wizard":
				if (level.depth >= 0) {
					Sound.fuseChests.play(); // Play boss-rise/awakening sound.
					AirWizard aw = new AirWizard(false);
					level.add(aw, player.x + 8, player.y + 8, false);
					System.out.println("Summoned new Air Wizard");
					Game.notifications.add("Don't mess with the Air.");
					success = true;
				} else {
					if (level.depth < 0) Game.notifications.add("Cannot summon in caves.");
				}
				break;
			case "Night Wizard":
				if ((Updater.getTime() == Updater.Time.Night || Updater.getTime() == Updater.Time.Evening) && level.depth == 0) {
					if (NightWizard.revenge == 0) {
						NightWizard nw = new NightWizard(Updater.isbloody ? 2 : 1);
						level.add(nw, player.x + 8, player.y + 8, false);
						Sound.nightactivate.play(); // Play boss-rise/awakening sound.
						System.out.println("Summoned the night wizard");
						Game.notifications.add(Updater.getTime() == Updater.Time.Evening ? "He will wake up soon" : "Face the pitch black night");
						success = true;
					} else {
						Game.notifications.add("Already summoned.");
					}
				} else {
					if (level.depth != 0 && Updater.getTime() == Updater.Time.Night)
						Game.notifications.add("Only surface night lures him.");
					else if (level.depth == 0 && Updater.getTime() != Updater.Time.Night)
						Game.notifications.add("Wait for the night.");
					success = false;
				}
				break;
			case "Time":

				if(player.potionEffects.containsKey(PotionType.AntiTime)){
					Game.notifications.add("Cannot activate the totem");
					success=false;
				}else {
					player.addPotionEffect(PotionType.Time, 3000); // Add it
					Game.notifications.add("Totem activated");
					Sound.woosh.play();
					success=true;
				}
				break;
			case "Antitime":
				Sound.woosh.play();
				player.addPotionEffect(PotionType.AntiTime,3000); // Add it
				Game.notifications.add(player.potionEffects.containsKey(PotionType.Time) ? "Time effect negated" : "Totem activated");
				success=true;
				break;
		}
		return super.interactOn(success);
	}
	
	@Override
	public boolean interactsWithWorld() { return false; }
	
	public SummonItem clone() {
		return new SummonItem(getName(), sprite, count, mob);
	}
}
