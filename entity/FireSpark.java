package minicraft.entity;

import minicraft.core.Game;
import minicraft.entity.mob.*;
import minicraft.gfx.Color;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.item.PotionType;
import minicraft.level.tile.Tiles;

import java.util.List;

public class FireSpark extends Entity {

	@Override
	public boolean canFly() {
		return true;
	}
	private int lifeTime; // How much time until the spark disappears
	private double xa, ya; // The x and y acceleration
	private double xx, yy; // The x and y positions
	private int time; // The amount of time that has passed
	private final EnemyMob owner; // The mob that created the spark

	/**
	 * Creates a new spark. Owner is the Obsidian Knight which is spawning this spark.
	 * @param owner The Obsidian Knight spawning the spark.
	 * @param xa X velocity.
	 * @param ya Y velocity.
	 */
	public FireSpark(EnemyMob owner, double xa, double ya) {
		super(0, 0);

		this.owner = owner;
		xx = owner.x;
		yy = owner.y;
		this.xa = xa;
		this.ya = ya;

		// Max time = 199 ticks. Min time = 180 ticks.
		lifeTime = 60 * 3 + random.nextInt(20);
	}

	@Override
	public void tick() {
		time++;
		if (time >= lifeTime) {
			remove(); // Remove this from the world
			return;
		}
		// Move the spark:


			xx += level.getTile((int)(x + xa) / 16 , y / 16).mayPass(level,x,y,this) ? xa : 0;
			yy += level.getTile(x / 16 , (int)(y + ya) / 16).mayPass(level,x,y,this) ? ya : 0;

			x = (int) xx;
			y = (int) yy;


		Player player = getClosestPlayer();
		if (player != null) { // Failsafe if player dies in a fire spark.
			if (player.isWithin(0, this)) {
				if(owner.lvl>2 && !player.potionEffects.containsKey(PotionType.FireMark) )player.potionEffects.put(PotionType.FireMark,700 * (owner.lvl-2)); //Mark of the Fire
				player.burningDuration=100 * owner.lvl; // Burn the player for 5 seconds
			}
			// If the entity is a mob, but not a Night Wizard or Wraith, then hurt the mob with 1 damage.
			List<Entity> toHit = level.getEntitiesInRect(entity -> owner instanceof FireSage ?  !(entity instanceof ObsidianKnight) && !(entity instanceof FireSage) && entity instanceof Mob && !(entity instanceof AirWizard) && !(entity instanceof Wraith)  && !(entity instanceof NightWizard) && !(entity instanceof Knight) && !(entity instanceof Snake) && entity.canBurn() : entity instanceof Mob , new Rectangle(x, y, 0, 0, Rectangle.CENTER_DIMS)); // Gets the entities in the current position to hit.
			toHit.forEach(entity -> ((Mob) entity).burningDuration=(entity.canBurn() ? 0 :100) * owner.lvl);
			if(owner instanceof FireSage) {
				List<Entity> toHeal = level.getEntitiesInRect(entity -> entity instanceof Knight || entity instanceof KnightTop, new Rectangle(x, y, 0, 0, Rectangle.CENTER_DIMS)); // Gets the entities in the current position to hit.
				toHeal.forEach(entity -> {
					if(entity instanceof Knight && ((Knight) entity).extradata<=0)((Mob) entity).heal(2 * owner.lvl);
					if(entity instanceof Knight)((Knight) entity).extradata=250;
				});
			}
		}
	}

	/** Can this entity block you? Nope. */
	public boolean isSolid() {
		return false;
	}

	@Override
	public void render(Screen screen) {
		int randmirror = 0;

		// If we are in a menu, or we are on a server.
		if (Game.getMenu() == null || Game.ISONLINE) {
			// The blinking effect.
			if (time >= lifeTime - 6 * 20) {
				if (time / 6 % 2 == 0) return; // If time is divisible by 12, then skip the rest of the code.
			}

			randmirror = random.nextInt(4);
		}

		screen.render(x - 4, y - 4 - 2, 9 + 29 * 32, randmirror, 2); // Renders the spark
	}

	/**
	 * Returns the owners id as a string.
	 * @return the owners id as a string.
	 */
	public String getData() {
		return owner.eid + "";
	}
}
