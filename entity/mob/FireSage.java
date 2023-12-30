package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.entity.*;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.item.Items;
import minicraft.item.PotionType;

import java.util.List;

public class FireSage extends EnemyMob {
	private static MobSprite[][][] sprites;
	private static final MobSprite[][] ponytail = MobSprite.compileMobSpriteAnimations(16, 38);

	static {
		sprites = new MobSprite[5][4][2];
		for (int i = 0; i < 5; i++) {
			MobSprite[][] list = MobSprite.compileMobSpriteAnimations(16, 10 + (i * 2));
			sprites[i] = list;
		}
	}

	private int attackDelay = 0;
	private int attackTime = 0;
	private int attackType = 0;

	/**
	 * Creates a knight of a given level.
	 *
	 * @param lvl The knights level.
	 */
	public FireSage(int lvl) {
		super(lvl, sprites, 8, 100);
		this.extracolor = random.nextInt(0xFFFFFF);
		this.usesCustomColor = true;
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);
		if ((this.extracolor + this.lvl) % 4 == 0) {
			MobSprite ptail = ponytail[dir.getDir()][(walkDist >> 3) % ponytail[dir.getDir()].length];
			ptail.render(screen, x - 8, y - 11, -1, this.extracolor);
		}
	}

	@Override
	protected void touchedBy(Entity entity) {
		if (entity instanceof Player) {
			int damage = lvl + Settings.getIdx("diff");
			((Player) entity).hurt(this, damage);
		}
	}

	public void die() {
		if (Settings.get("diff").equals("Easy"))
			dropItem(1, 3, Items.get("shard"));
		else
			dropItem(0, 2, Items.get("shard")
			);

		if (random.nextInt(24 / lvl / (Settings.getIdx("diff") + 1)) == 0)
			dropItem(1, 1, Items.get("key"));
		if (getClosestPlayer().curArmor != null)
			if (Renderer.player.curArmor.getName() == "Night Armor" && Updater.getTime() == Updater.Time.Night && lvl > 4 && getClosestPlayer().getRealmId()==0)
				getClosestPlayer().heal(3);
		super.die();
	}

	public int getMaxLevel() {
		return 5;
	}

	@Override
	public void tick() {
		super.tick();
		if (Game.isMode("Creative")) return; // Should not attack if NEAREST player is in creative

			//if(Updater.tickCount%50==0)


		if (attackDelay > 0) {
			xmov = ymov = 0;
			int dir = (attackDelay - 45) / 4 % 4; // The direction of attack.
			dir = (dir * 2 % 4) + (dir / 2); // Direction attack changes
			if (attackDelay < 45)
				dir = 0; // Direction is reset, if attackDelay is less than 45; prepping for attack.

			this.dir = Direction.getDirection(dir);

			attackDelay--;
			if (attackDelay == 0) {
				//attackType = 0; // Attack type is set to 0, as the default.
				if (health < maxHealth / 2) attackType = 2; // If at 1000 health (50%) or lower, attackType = 1
				else attackType = 1; // If at 200 health (10%) or lower, attackType = 2
				attackTime = 120; // attackTime set to 120 (2 seconds, at default 60 ticks/sec)
			}
			return; // Skips the rest of the code (attackDelay must have been > 0)
		}

		// Send out sparks
		if (attackTime > 0) {
			xmov = ymov = 0;
			attackTime *= 0.92; // attackTime will decrease by 7% every time.
			double dir = attackTime * 0.25 * (attackTime % 2 * 2 - 1); // Assigns a local direction variable from the attack time.
			double speed =( 0.7 + (attackType/3) ) + (lvl/10); // speed is dependent on the attackType. (higher attackType, faster speeds)
			level.add(new FireSpark(this, Math.cos(dir) * speed, Math.sin(dir) * speed)); // Adds a spark entity with the cosine and sine of dir times speed.
			return; // Skips the rest of the code (attackTime was > 0; ie we're attacking.)
		}

		Player player = getClosestPlayer();

		if ((player != null)  && randomWalkTime == 0) { // If there is a player around, and the walking is not random
			int xd = player.x - x; // The horizontal distance between the player and the air wizard.
			int yd = player.y - y; // The vertical distance between the player and the air wizard.
			if (xd * xd + yd * yd < 16*16 * 2*2) {
				/// Move away from the player if less than 2 blocks away

				this.xmov = 0; // Accelerations
				this.ymov = 0;

				// These four statements basically just find which direction is away from the player:
				if (xd < 0) this.xmov = +1;
				if (xd > 0) this.xmov = -1;
				if (yd < 0) this.ymov = +1;
				if (yd > 0) this.ymov = -1;
			}else if(xd * xd + yd * yd < 16*16 * 10 * 10 && Updater.tickCount%30==0)
				level.add(new FireSpark(this, 0 * speed, 0 * speed),x,y); // Adds a spark entity with the cosine and sine of dir times speed.


			xd = player.x - x; // Recalculate these two
			yd = player.y - y;
			if (random.nextInt(4) == 0 && (xd * xd + yd * yd < 50 * 50) && attackDelay == 0 && attackTime == 0) { // If a random number, 0-3, equals 0, and the player is less than 50 blocks away or there are some knights in very close area, and attackDelay and attackTime equal 0...
				attackDelay = 60 * 2; // ...then set attackDelay to 120 (2 seconds at default 60 ticks/sec)
			}
		}
	}
}