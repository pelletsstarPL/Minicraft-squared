package minicraft.entity.furniture;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.ItemHolder;
import minicraft.entity.mob.Mimic;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.StackableItem;
import minicraft.saveload.Load;
import minicraft.screen.ContainerDisplay;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;


public class MimicChest extends Furniture  {
	//private Inventory inventory; // Inventory of the chest
	private  int mimicLvl = 1;

	private static final Sprite lockTwoLocksSprite = new Sprite(16, 28, 2, 2, 2);
	private boolean hasTwoLocks;
	public MimicChest() { this(1); }

	/**
	 * Creates a chest with a custom name.
	 * @param  lvl Name of chest.
	 * Extradata is being used to determine if chest has two or one lock
	 */
	public MimicChest(int lvl) {
		super("Mimic", new Sprite(12, 28, 2, 2, 2), 3, 3); // Name of the chest
		this.mimicLvl =lvl;
	}
	public MimicChest(int lvl,boolean hasTwoLocks) {
		super("Mimic", new Sprite(12, 28, 2, 2, 2), 3, 3); // Name of the chest
		this.mimicLvl =lvl;
		this.hasTwoLocks = true;
	}

	public  int getMimicLvl(){
		return this.mimicLvl;
	}
	public boolean hasTwoLocks(){
		return this.hasTwoLocks;
	}

	public void setDoubleLock(boolean lock){this.hasTwoLocks = lock;}
	/** This is what occurs when the player uses the "Menu" command near this */
	public boolean use(Player player) {
		StackableItem key = (StackableItem) player.activeItem;
		boolean activeKey = player.activeItem != null && player.activeItem.equals(Items.get("Key")) && key.count > (hasTwoLocks ? 1 : 0);
		boolean mimicAteKey = Math.random() < Settings.getIdx("diff") * 0.025;
		boolean invKey = player.getInventory().count(Items.get("key")) > (hasTwoLocks ? 1: 0);
		double doubleEat =Math.random();
		if(invKey) {
			Mimic mimic = new Mimic(this.mimicLvl, this.hasTwoLocks);
			if (!Game.isMode("creative")) { // Remove the key if mimic has eaten it unless on creative mode.
				if (activeKey) { // Remove activeItem

					key.count -= (hasTwoLocks && doubleEat < 0.2 ? 2 : 1); //Mostly despite having two locks they will consume one key
				} else { // Remove from inv
					if(hasTwoLocks)player.getInventory().removeItem(Items.get("key"));
					player.getInventory().removeItem(Items.get("key"));
				}
			}
			level.add(new SmashParticle(x * 16, y * 16));
			if (!mimicAteKey) {
				level.add(new TextParticle("TRAP!", x, y, Color.RED));
				level.dropItem(this.x, this.y, hasTwoLocks  ? 2 : 1, hasTwoLocks  ? 2 : 1,Items.get("key"));
			} else {
				if(doubleEat>0.2)level.dropItem(this.x, this.y, Items.get("key"));
				level.add(new TextParticle((hasTwoLocks && doubleEat < 0.2 ? "-2 keys" : "-1 key"), x, y, Color.RED));
			}

			level.add(mimic, this.x, this.y, player.getRealmId()); //activate the trap!
			level.mimicCount--;
			//	Game.setMenu(new ContainerDisplay(player, this));
			this.die();
			return true;
		}else return false;
	}

	public void render(Screen screen){
		super.render(screen);
		if(hasTwoLocks)lockTwoLocksSprite.render(screen,x - 8,y  - 8);
	}

	@Override
	protected void touchedBy(Entity entity) {
		return;
	}

	@Override
	public boolean interact(Player player, @Nullable Item item, Direction attackDir) {
		return false;
	}

	@Override
	public void die() {

		super.die();
	}
}
