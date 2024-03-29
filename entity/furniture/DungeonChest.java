package minicraft.entity.furniture;

import java.util.Random;

import minicraft.core.io.Sound;
import minicraft.gfx.Screen;
import org.jetbrains.annotations.Nullable;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.StackableItem;

public class DungeonChest extends Chest {
	private static final Sprite openSprite = new Sprite(14, 28, 2, 2, 2);
	private static final Sprite lockSprite = new Sprite(12, 28, 2, 2, 2);

	private static final Sprite openTwoLocksSprite = new Sprite(18, 28, 2, 2, 2);
	private static final Sprite lockTwoLocksSprite = new Sprite(16, 28, 2, 2, 2);
	
	public Random random = new Random();
	private boolean isLocked;
	private boolean  twoLocks;
	
	/**
	 * Creates a custom chest with the name Dungeon Chest.
	 * @param populateInv
	 */
	public DungeonChest(boolean populateInv) {
		this(populateInv, false,false);
	}
	public DungeonChest(boolean populateInv,boolean unlocked) {
		this(populateInv, unlocked,false);
	}

	public DungeonChest(boolean populateInv, boolean unlocked,boolean hasTwoLocks) {
		super("Dungeon Chest");
		if (populateInv) {
			populateInv();
		}
		this.twoLocks = hasTwoLocks;
		setLocked(!unlocked);
	//setTw
	}
	public boolean hasTwoLocks(){
		return this.twoLocks;
	}
	@Override
	public Furniture clone() {
		return new DungeonChest(false, !this.isLocked);
	}

	public boolean use(Player player) {
		if (isLocked) {
			StackableItem key = (StackableItem) player.activeItem;
			boolean activeKey = player.activeItem != null && player.activeItem.equals(Items.get("Key"))  && key.count > (twoLocks ? 1 : 0);
			boolean invKey = player.getInventory().count(Items.get("key")) > (twoLocks ? 1 : 0);

			if (activeKey || invKey || Game.isMode("creative")) { // If the player has a key... or two if double locked
				if (!Game.isMode("creative")) { // Remove the key unless on creative mode.
					if (activeKey) { // Remove activeItem

						key.count-=(twoLocks ? 2 : 1);
					} else { // Remove from inv
						player.getInventory().removeItem(Items.get("key"));
						if(twoLocks)player.getInventory().removeItem(Items.get("key")); //remove one more
					}
				}

				isLocked = false;
				this.sprite = openSprite; // Set to the unlocked color

				level.add(new SmashParticle(x * 16, y * 16));
				level.add(new TextParticle(twoLocks ? "-2 keys" : "-1 key", x, y, Color.RED));
				/*int numChests = 0;
				for (Entity e : level.getEntityArray())
					if (e instanceof DungeonChest && ((DungeonChest) e).isLocked)
						numChests++;*/
				level.chestCount--;
				if (level.chestCount == 0 && level.depth==-6) { // If this was the last chest...
					level.dropItem(x, y, 5, Items.get("Gold Apple"));
					Sound.fuseChests.play();
					Updater.notifyAll("You hear a noise from the surface!", -100); // Notify the player of the developments
					// Add a level 2 airwizard to the middle surface level.
					AirWizard wizard = new AirWizard(true);
					wizard.x = World.levels[World.lvlIdx(0)].w / 2;
					wizard.y = World.levels[World.lvlIdx(0)].h / 2;
					World.levels[World.lvlIdx(0)].add(wizard); //JUST FOR OVERWORLD
				}

				return super.use(player); // the player unlocked the chest.
			}

			return false; // the chest is locked, and the player has no key.
		}
		else{
			return super.use(player); // the chest was already unlocked.
		}
	}
	
	/**
	 * Populate the inventory of the DungeonChest using the loot table system
	 */
	private void populateInv() {
		Inventory inv = getInventory(); // Yes, I'm that lazy. ;P
		inv.clearInv(); // clear the inventory.

		populateInvRandom("dungeonchest", 0);
	}

	public boolean isLocked() {
		return isLocked;
	}

	public void setLocked(boolean locked) {
		this.isLocked = locked;

		// auto update sprite
		sprite = locked ? DungeonChest.lockSprite : DungeonChest.openSprite;
	}
	public void setLocks(boolean twoLocks) {

	}

	public void render(Screen screen){
		super.render(screen);
		if(twoLocks)
		if(isLocked)lockTwoLocksSprite.render(screen,x - 8,y  - 8);
		else openTwoLocksSprite.render(screen,x - 8,y - 8);
	}
	public void setDoubleLock(boolean twoLocks){
		this.twoLocks = twoLocks;
	}
	
	/** what happens if the player tries to push a Dungeon Chest. */
	@Override
	protected void touchedBy(Entity entity) {
		if(!isLocked) // can only be pushed if unlocked.
			super.touchedBy(entity);
	}

	@Override
	public boolean interact(Player player, @Nullable Item item, Direction attackDir) {
		if(!isLocked)
			return super.interact(player, item, attackDir);
		return false;
	}
	
	@Override
	protected String getUpdateString() {
		String updates = super.getUpdateString() + ";";
		updates += "isLocked,"+isLocked;
		
		return updates;
	}
	
	@Override
	protected boolean updateField(String field, String val) {
		if(super.updateField(field, val)) return true;
		switch(field) {
			case "isLocked":
				isLocked = Boolean.parseBoolean(val);
				return true;
		}
		
		return false;
	}
}
