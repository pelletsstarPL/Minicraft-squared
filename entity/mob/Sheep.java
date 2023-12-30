package minicraft.entity.mob;

import org.jetbrains.annotations.Nullable;

import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.entity.Direction;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;

public class Sheep extends PassiveMob {
	private static final MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(0, 32);
	private static final MobSprite[][] cutSprites = MobSprite.compileMobSpriteAnimations(0, 28);


	/**
	 * Creates a sheep entity.
	 *  extradata will be cooldown for our sheep to be wooled
	 */
	public Sheep() {
		super(sprites);
	}

	@Override
	public void render(Screen screen) {
		int xo = x - 8;
		int yo = y - 11;

		MobSprite[][] curAnim = this.extradata>0 ? cutSprites : sprites;

		MobSprite curSprite = curAnim[dir.getDir()][(walkDist >> 3) % curAnim[dir.getDir()].length];
		if (hurtTime > 0) {
			curSprite.render(screen, xo, yo, true);
		} else {
			curSprite.render(screen, xo, yo);
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (this.extradata > 0) this.extradata--;
	}

	public boolean interact(Player player, @Nullable Item item, Direction attackDir) {
		if (this.extradata>0) return false;

		if (item instanceof ToolItem) {
			if (((ToolItem) item).type == ToolType.Shears) {
				this.extradata = 18400;

				dropItem(1, 3, Items.get("Wool"));
				((ToolItem) item).payDurability();
				return true;

			}
		}
		return false;
	}

	public void die() {
		int min = 0, max = 0;
		if (Settings.get("diff").equals("Easy")) {min = 1; max = 3;}
		if (Settings.get("diff").equals("Normal")) {min = 1; max = 2;}
		if (Settings.get("diff").equals("Hard")) {min = 0; max = 2;}
		if(this.burningDuration<=150)
		if (this.extradata==0) dropItem(min, max, Items.get("wool"));

		dropItem(min, max, Items.get(this.burningDuration>150 ? "Steak" : "Raw Beef"));

		super.die();
	}
}
