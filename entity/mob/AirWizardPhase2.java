package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Arrow;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.Spark2;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.network.Analytics;
import minicraft.saveload.Save;

public class AirWizardPhase2 extends EnemyMob {
	private int artime;
	private int spritetime;
	int direction;
	private static MobSprite[][][] sprites;
	private static MobSprite[][] invulnerableSprite;

	static {
		sprites = new MobSprite[2][4][2];
		// normal sprite
		for (int i = 0; i < 2; i++) {
			MobSprite[][] list = MobSprite.compileMobSpriteAnimations(8, 20 + (i * 2));
			sprites[i] = list;
			invulnerableSprite = MobSprite.compileMobSpriteAnimations(0, 20+ (i*2));
		}


	}
	public boolean canBeAffectedByLava() { return false; }
	public static boolean beaten = false;
	private boolean secondform;
	private int renderTime;
	private int attackDelay = 0;
	private int attackTime = 0;
	private int attackType = 0;

	/**
	 * Constructor for the AirWizard. Will spawn as secondary form if lvl>1.
	 * @param lvl The AirWizard level.
	 */
	public AirWizardPhase2(int lvl) {
		this(lvl > 1);
	}

	/**
	 * Constructor for the AirWizard.
	 * @param secondform determines if the wizard should be level 2 or 1.
	 */
	public AirWizardPhase2(boolean secondform) {
		super(secondform ? 2 : 1, sprites, secondform ? 1000 : 500, false, 16 * 8, -1, 10, 50);

		this.secondform = secondform;
		if (secondform) speed = 3;
		else speed = 2;
		walkTime = 2;
	}
	public boolean canSwim() { return true; }

	public boolean canWool() { return false; }

	@Override
	public void tick() {
		super.tick();

		if (Game.isMode("Creative")) return; // Should not attack if player is in creative

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
				if (health < maxHealth / 2) attackType = 1; // If at 1000 health (50%) or lower, attackType = 1
				if (health < maxHealth / 10) attackType = 2; // If at 200 health (10%) or lower, attackType = 2
				attackTime = 60 * (secondform ? 3 : 2); // attackTime set to 120 or 180 (2 or 3 seconds, at default 60 ticks/sec)
			}
			return; // Skips the rest of the code (attackDelay must have been > 0)
		}

		// Send out sparks
		if (attackTime > 0) {
			xmov = ymov = 0;
			attackTime *= secondform ? 0.98 : 0.94; // attackTime will decrease by 7% every time and by 3% for second tier phase 1
			double dir = attackTime * 0.25 * (attackTime % 2 * 2 - 1); // Assigns a local direction variable from the attack time.
			double speed = (secondform ? 1.2 : 0.7); // speed is dependent on the attackType. (higher attackType, faster speeds)
			level.add(new Spark2(this, Math.cos(dir) * speed, Math.sin(dir) * speed)); // Adds a spark entity with the cosine and sine of dir times speed.
			return; // Skips the rest of the code (attackTime was > 0; ie we're attacking.)
		}

		Player player = getClosestPlayer();
		if (player != null && randomWalkTime == 0) { // If there is a player around, and the walking is not random
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
			} else if (xd * xd + yd * yd > 16*16 * 15*15) {// 15 squares away

				/// Drags the airwizard to the player, maintaining relative position.
				double hypot = Math.sqrt(xd * xd + yd * yd);
				int newxd = (int)(xd * Math.sqrt(16*16 * 15*15) / hypot);
				int newyd = (int)(yd * Math.sqrt(16*16 * 15*15) / hypot);
				x = player.x - newxd;
				y = player.y - newyd;
			}

			xd = player.x - x; // Recalculate these two
			yd = player.y - y;
			if (random.nextInt(4) == 0 && xd * xd + yd * yd < 50 * 50 && attackDelay == 0 && attackTime == 0) { // If a random number, 0-3, equals 0, and the player is less than 50 blocks away, and attackDelay and attackTime equal 0...
				attackDelay = 60 * 2; // ...then set attackDelay to 120 (2 seconds at default 60 ticks/sec)
			}
		}
	}

	@Override
	public void doHurt(int damage, Direction attackDir) {
		level.add(new Arrow(this, dir, 4));

		if(artime%3==0){
			if (direction == 0) {
				level.add(new Arrow(this, Direction.RIGHT, secondform ? 4 :3));
				level.add(new Arrow(this, Direction.LEFT, secondform ? 4 :3));
				direction++;
			}else if(direction==1){
				level.add(new Arrow(this, Direction.UP, secondform ? 4 :3));
				level.add(new Arrow(this, Direction.DOWN, secondform ? 4 :3));
				direction--;
			}
		}
		artime++;
		if(AirWizard.invulnerability<=0) {
			super.doHurt(damage, attackDir);
		}else{
			super.doHurt(0, attackDir);
		}
		if (attackDelay == 0 && attackTime == 0) {
			attackDelay = 60 * 2;
		}
	}

	@Override
		public void render(Screen screen) {
			renderTime++;

			// save actual sprite
			MobSprite[][] actualSprite = lvlSprites[lvl-1];

			if (AirWizard.invulnerability > 0 && renderTime % 3 == 0) {
				// apply invulnerable sprite
				lvlSprites[lvl-1] = (secondform ? MobSprite.compileMobSpriteAnimations(0, 22) : MobSprite.compileMobSpriteAnimations(0, 20));
			}

			super.render(screen);
			// restores previous sprite
			lvlSprites[lvl-1] = actualSprite;
		int textcol = Color.get(1, 0, 204, 0);
		int textcol2 = Color.get(1, 0, 51, 0);
		int percent = health / (maxHealth / 100);
		String h =percent + "%";

		if (percent < 1) h = "1%";

		if (percent < 16) {
			textcol = Color.get(1, 204, 0, 0);
			textcol2 = Color.get(1, 51, 0, 0);
		}
		else if (percent < 51) {
			textcol = Color.get(1, 204, 204, 9);
			textcol2 = Color.get(1, 51, 51, 0);
		}
		int textwidth = Font.textWidth(h);
		Font.draw(h, screen, (x - textwidth/2) + 1, y - 17, textcol2);
		Font.draw(h, screen, (x - textwidth/2), y - 18, textcol);
	}

	@Override
	protected void touchedBy(Entity entity) {
		if (entity instanceof Player) {
			// If the entity is the Player, then deal them 1 or 2 damage points.
			((Player)entity).hurt(this, (secondform ? 2 : 1));
		}
	}

	/** What happens when the air wizard dies */
	public void die() {
		Player[] players = level.getPlayers();
		Sound.fuseChests.play(); // Play boss-death sound.

		if(!secondform) {
			AirWizard.invulnerability=4;
			Game.notifications.add("Air!");
			level.add(new WraithA(3), x-8, y-8);
			level.add(new WraithA(3), x-8, y+8);
			level.add(new WraithA(3), x+8, y-8);
			level.add(new WraithA(3), x+8, y+8);
			level.add(new AirWizardPhase3(1), x, y);
		} else {
			AirWizard.invulnerability=4;
			Game.notifications.add("Curse!");
			level.add(new WraithA(4), x-8, y-8);
			level.add(new WraithA(4), x-8, y+8);
			level.add(new WraithA(4), x+8, y-8);
			level.add(new WraithA(3), x+8, y+8);
			level.add(new AirWizardPhase3(2), x, y);
		}

		super.die(); // Calls the die() method in EnemyMob.java
	}

	public int getMaxLevel() { return 2; }
}
