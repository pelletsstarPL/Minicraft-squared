package minicraft.item;

import minicraft.core.Updater;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

public abstract class Item {
	
	/* Note: Most of the stuff in the class is expanded upon in StackableItem/PowerGloveItem/FurnitureItem/etc */
	
	private final String name;
	private final String shortenedName;
	public Sprite sprite;

	public int durAdjusted;
	public int arrAdjusted;
	
	public boolean used_pending = false; // This is for multiplayer, when an item has been used, and is pending server response as to the outcome, this is set to true so it cannot be used again unless the server responds that the item wasn't used. Which should basically replace the item anyway, soo... yeah. this never gets set back.
	
	protected Item(String name) {
		sprite = Sprite.missingTexture(1, 1);
		this.name = name;
		this.shortenedName = name.substring(0,name.length()<12 ? name.length() : 11);
	}
	protected Item(String name, Sprite sprite) {
		this.name = name;
		this.shortenedName = name.substring(0,name.length()<12 ? name.length() : 11);
		this.sprite = sprite;
	}

	/** Renders an item on the HUD */
	public void renderHUD(Screen screen, int x, int y, int fontColor) {
		String dispName = getDisplayName();

		switch (dispName.length()) {
			case 6:
				durAdjusted = 0;
				break;

			case 7:
				durAdjusted = 4;
				break;

			case 8:
				durAdjusted = 8;
				arrAdjusted = 0;
				break;

			case 9:
				durAdjusted = 12;
				arrAdjusted = 4;
				break;

			case 10:
				durAdjusted = 16;
				arrAdjusted = 8;
				break;

			case 11:
				durAdjusted = 20;
				arrAdjusted = 12;
				break;

			case 12:
				durAdjusted = 24;
				arrAdjusted = 16;
				break;

			case 13:
				durAdjusted = 28;
				arrAdjusted = 20;
				break;

			case 14:
				durAdjusted = 32;
				arrAdjusted = 24;
				break;

			case 15:
				durAdjusted = 36;
				arrAdjusted = 28;
				break;

			case 16:
				durAdjusted = 40;
				arrAdjusted = 32;
				break;

			default: // Nothing
				break;
		}

		int w = dispName.length(); // Length of message in characters.
		int wCond =w>9 ? 9 : w;

		int xx = ((Screen.w - Font.textWidth("..........")) / 2); // The width of the box
		int yy = (Screen.h - 8) - 1; // The height of the box


		int h = 1;

		// Renders the four corners of the box
		screen.render(xx - 8, yy - 8, 0 + (Settings.get("coloredgui").equals(true) ? 21 : 20) * 32, 0, 3);
		screen.render(xx + (wCond+2) * 8, yy - 8, 0 + (Settings.get("coloredgui").equals(true) ? 21 : 20) * 32, 1, 3);
		screen.render(xx - 8, yy + 8, 0 + (Settings.get("coloredgui").equals(true) ? 21 : 20) * 32, 2, 3);
		screen.render(xx + (wCond+2) * 8, yy + 8, 0 + (Settings.get("coloredgui").equals(true) ? 21 : 20) * 32, 3, 3);

		// Renders each part of the box...
		for (x = 0; x < (wCond+2); x++) {
			screen.render(xx + x * 8, yy - 8, 1 + (Settings.get("coloredgui").equals(true) ? 21 : 20) * 32, 0, 3); // ...top part
			screen.render(xx + x * 8, yy + 8, 1 + (Settings.get("coloredgui").equals(true) ? 21 : 20) * 32, 2, 3); // ...bottom part
		}
		for (y = 0; y < h; y++) {
			screen.render(xx - 8, yy + y * 8, 2 + (Settings.get("coloredgui").equals(true) ? 21 : 20) * 32, 0, 3); // ...left part
			screen.render(xx + (wCond+2) * 8, yy + y * 8, 2 + (Settings.get("coloredgui").equals(true) ? 21 : 20) * 32, 1, 3); // ...right part
		}
		//text
		char[] ch = new char[10];

		// Copying character by character into array
		// using for each loop
		if(dispName.length()>10) {
			for (int i = 0; i < 10; i++) {

				//System.out.println(j > dispName.length() - 1);
				//if (j + Updater.textDisplayMove>= dispName.length() ){ j = 0;Updater.textDisplayMove=j;};
				ch[i] = dispName.charAt((i + Updater.textDisplayMove)%dispName.length());
			}
		}
		// The middle
		for (x = 0; x < (wCond+2); x++) {
			screen.render(xx + x * 8, yy, 3 + (Settings.get("coloredgui").equals(true) ? 21 : 20) * 32, 0, 3);
		}

		// Item sprite
		sprite.render(screen, xx, yy);

		String str="";
		if(dispName.length()>10){
			if(Updater.paused)str=dispName.substring(0,9);
			else str=String.valueOf(ch);
		}
		else str=dispName;
		// Item name
		Font.drawTransparentBackground(str, screen, xx + 8, yy, fontColor);
	}
	
	/** Determines what happens when the player interacts with a tile */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		return false;
	}



	/** Returning true causes this item to be removed from the player's active item slot */
	public boolean isDepleted() {
		return false;
	}
	
	/** Returns if the item can attack mobs or not */
	public boolean canAttack() {
		return false;
	}

	/** Sees if an item equals another item */
	public boolean equals(Item item) {
		return item != null && item.getClass().equals(getClass()) && item.name.equals(name);
	}
	public boolean displayBox(){return false;}
	//normally don't display

	@Override
	public int hashCode() { return name.hashCode(); }
	
	/** This returns a copy of this item, in all necessary detail. */
	public abstract Item clone();
	
	@Override
	public String toString() {
		return name + "-Item";
	}
	
	/** Gets the necessary data to send over a connection. This data should always be directly input-able into Items.get() to create a valid item with the given properties. */
	public String getData() {
		return name;
	}
	
	public final String getName() { return name; }
	
	// Returns the String that should be used to display this item in a menu or list. 
	public String getDisplayName() {
		return " " + Localization.getLocalized(getName());
	}
	
	public boolean interactsWithWorld() { return true; }

}
