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
		if (mob.equals("Air Wizard") && level.depth>=0) {
			Sound.fuseChests.play(); // Play boss-rise/awakening sound.
			AirWizard aw = new AirWizard(false);
			level.add(aw, player.x+8, player.y+8, false);
			System.out.println("Summoned new Air Wizard");
			Game.notifications.add("Don't mess with the Air.");
			success = true;
		}else{
			if(level.depth<0) Game.notifications.add("Cannot summon in caves.");
		}
		if (mob.equals("Night Wizard")) {
			if(Updater.getTime() == Updater.Time.Night && level.depth==0) {
				if(NightWizard.revenge==0) {
					NightWizard nw = new NightWizard(1);
					level.add(nw, player.x + 8, player.y + 8, false);
					Sound.nightactivate.play(); // Play boss-rise/awakening sound.
					System.out.println("Summoned the night wizard");
					Game.notifications.add("Face the pitch black night");
					success = true;
				}else{
					Game.notifications.add("Already summoned.");
				}
			}else{
				if(level.depth!=0 && Updater.getTime() == Updater.Time.Night)
				Game.notifications.add("Only surface night lures him.");
				else if(level.depth==0 && Updater.getTime() != Updater.Time.Night)
					Game.notifications.add("Wait for the night.");
				success = false;
			}
		}
		return super.interactOn(success);
	}
	
	@Override
	public boolean interactsWithWorld() { return false; }
	
	public SummonItem clone() {
		return new SummonItem(getName(), sprite, count, mob);
	}
}
