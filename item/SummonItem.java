package minicraft.item;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.furniture.KnightStatue;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.NightWizard;
import minicraft.entity.mob.ObsidianKnight;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

import java.util.ArrayList;
import java.util.Arrays;

public class SummonItem extends StackableItem {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new SummonItem("Air totem", new Sprite(0, 20, 0), "Air Wizard"));
		items.add(new SummonItem("Moonlight totem", new Sprite(1, 20, 0), "Night Wizard"));
		items.add(new SummonItem("Obsidian Poppet", new Sprite(4, 20, 0), "Obsidian Knight"));
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
				if (level.depth >= 0 && level.realm.contains("overworld") && !AirWizard.active) {
					Sound.fuseChests.play(); // Play boss-rise/awakening sound.
					AirWizard aw = new AirWizard(false);
					level.add(aw, player.x + 8, player.y + 8, false);
					System.out.println("Summoned new Air Wizard");
					Game.notifications.add("Don't mess with the Air.");
					success = true;
				}else if(AirWizard.active){
					Game.notifications.add("There is one already!");
				} else {
					if (level.depth < 0) Game.notifications.add("Cannot summon there.");
				}
				break;
			case "Night Wizard":
				if ((Updater.getTime() == Updater.Time.Night || Updater.getTime() == Updater.Time.Evening) && level.depth == 0 && level.realm.contains("overworld")) {
					if (!NightWizard.active) {
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
					if(level.realm!="overworld") Game.notifications.add("Cannot summon here");
						else
					if (level.depth != 0 && Updater.getTime() == Updater.Time.Night )
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
			case "Obsidian Knight":
				// Check if we are on the right level and tile

				if (level.depth == -2 && level.realm.contains("dungeon realm")) { //OBV and floor where knight room spawns indeed
					// If the player nears the center.
					//if (new Rectangle(level.w/2-3, level.h/2-3, 7, 7).contains(player.x >> 4, player.y >> 4)) {
					if(level.getTile(player.x >> 4,player.y >> 4)== Tiles.get("Decorated Unbreakable")){ //There is special cross on which you can place the poppet
						if (!ObsidianKnight.active) {

							// Pay stamina
							if (player.payStamina(3)) {
								level.add(new KnightStatue(3300), player.x >> 4, player.y >> 4, true,player.getRealmId());
								//Logger.tag("SummonItem").debug("Summoned new Knight Statue");
								success = true;
							}
						} else {
							Game.notifications.add(Localization.getLocalized("Obsidian knight is already here"));
						}
					} else {
						Game.notifications.add(Localization.getLocalized("Cannot place on this tile"));
					}
				} else {
					Game.notifications.add(Localization.getLocalized("Cannot place  in this area"));
				}
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
