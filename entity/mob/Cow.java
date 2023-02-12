package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.entity.Direction;
import minicraft.gfx.MobSprite;
import minicraft.item.*;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import org.jetbrains.annotations.Nullable;

import java.util.Stack;

public class Cow extends PassiveMob {
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(0, 30);
	public int milkCooldown=0;
	/**
	 * Creates the cow with the right sprites and color.
	 */
	public Cow() {
		super(sprites, 5);
	}

	public boolean interact(Player player, @Nullable Item item, Direction attackDir) {
		if (milkCooldown>0) return false;
		String name = item.getName();
		if(milkCooldown<=0) {
			if (name.contains("Bottle")) {
				((StackableItem) item).count--;
				if (((StackableItem) item).count <= 0) Game.player.activeItem = null;
				Game.player.getInventory().add(Items.get("Milk"));
				milkCooldown = 12000;
				return true;
			}
			if (name.contains("Empty Bucket")) {
				((StackableItem) item).count--;
				if (((StackableItem) item).count <= 0) Game.player.activeItem = null;
				Game.player.getInventory().add(Items.get("Milk Bucket"));
				milkCooldown = 24000;
				return true;
			}
		}
		return false;
	}
	public void die() {
		int min = 0, max = 0;
		if (Settings.get("diff").equals("Easy")) {min = 1; max = 3;}
		if (Settings.get("diff").equals("Normal")) {min = 1; max = 2;}
		if (Settings.get("diff").equals("Hard")) {min = 0; max = 1;}
		
		dropItem(min, max, Items.get("leather"), Items.get((this.burningDuration>150 ? "Cooked beef" : "Raw Beef")));
		
		super.die();
	}

	@Override
	public void tick() {
		super.tick();
		if (milkCooldown > 0) milkCooldown--;
	}
}
