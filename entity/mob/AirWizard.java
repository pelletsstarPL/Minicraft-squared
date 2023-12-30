package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.*;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.item.Items;
import minicraft.item.PotionType;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.network.Analytics;
import minicraft.saveload.Save;

public class AirWizard extends EnemyMob {
	public static int invulnerability;
	private static MobSprite[][][] sprites;
	static {
		sprites = new MobSprite[2][4][2];
		for (int i = 0; i < 2; i++) {
				MobSprite[][] list = MobSprite.compileMobSpriteAnimations(8, 20 + (i * 2));
				sprites[i] = list;

		}
	}

	public static boolean beaten = false;
	public boolean canBeAffectedByLava() { return false; }
	@Override
	public boolean canFly() {
		return true;
	}
	public boolean secondform;
	private int artime;
	int direction;
	private int attackDelay = 0;
	private int attackTime = 0;
	private int attackType = 0;
	public boolean phaseTriggered;
	private int renderTime;
	public  static boolean active;
	private float hpLevel=health/maxHealth;
	private int nextPhase = this.maxHealth-(secondform ? 1000 : 500);

	/**
	 * Constructor for the AirWizard. Will spawn as secondary form if lvl>1.
	 * @param lvl The AirWizard level.
	 */
	public AirWizard(int lvl) {
		this(lvl > 1);
	}

	/**
	 * Constructor for the AirWizard.
	 * @param secondform determines if the wizard should be level 2 or 1.
	 */
	public AirWizard(boolean secondform) {
		super(secondform ? 2 : 1, sprites, secondform ? 5000 : 2000, false, 16 * 8, -1, 10, 50);

		this.secondform = secondform;
		if (secondform) speed = speedS =3;
		else speed= speedS =2;
		walkTime = 2;
		//if(secondform && health==maxHealth)nextPhase=maxHealth-1000;
	}

	void phase(){
		if(invulnerability==0 && phaseTriggered) {
			String[] quotes={"Rise!","Air!","Curse you!"};
			hpLevel = (float) health / maxHealth;
			Sound.fuseChests.play(); // Play boss-rise/awakening sound.
			invulnerability = (secondform && hpLevel < 0.41 ? 6 : 4);
			Game.notifications.add(quotes[random.nextInt(quotes.length)]);
			level.add(new Wraith(3 + (hpLevel < 0.21 ? 1 : 0),1), x - 12, y - 12);
			level.add(new Wraith(3 + (hpLevel < 0.21 ? 1 : 0),1), x - 12, y + 12);
			level.add(new Wraith(3 + (hpLevel < 0.21 ? 1 : 0),1), x + 12, y - 12);
			level.add(new Wraith(3 + (hpLevel < 0.26 ? 1 : 0),1), x + 12, y + 12);
			if (secondform && hpLevel < 0.41) {
				level.add(new Wraith(4,1), x - 12, y - 12);
				level.add(new Wraith(4,1), x - 12, y + 12);
			}
			this.health--;
			phaseTriggered=false;
		}
	}
	public boolean canSwim() { return true; }

	public boolean canWool() { return true; }

	@Override
	public void tick() {

		super.tick();
		active = true;
		Player player = getClosestPlayer();
		if(health==nextPhase && health!=0){
			phaseTriggered=true;
			phase();
			nextPhase=(int)Math.floor((health-1)/(secondform ? 1000 : 500))*(secondform ? 1000 : 500);
		}
		nextPhase=(int)Math.floor(health-1)/(secondform ? 1000 : 500)*(secondform ? 1000 : 500);

		if (Game.isMode("Creative")) return; // Should not attack if player is in creative

		if (attackDelay > 0) {
			xmov = ymov = 0;
			int dir = (attackDelay - 45) / 4 % 4; // The direction of attack.
			dir = (dir * 2 % 4) + (dir / 2); // Direction attack changes
			if (attackDelay < 45)
				dir = 0; // Direction is reset, if attackDelay is less than 45; prepping for attack.

			this.dir = Direction.getDirection(dir);

			if(player!=null)
			attackDelay-=player.potionEffects.containsKey(PotionType.AntiTime) ? 2 : player.potionEffects.containsKey(PotionType.Time) ? (Updater.tickCount%2==0 ? 1 : 0) : 1;
			if (attackDelay <= 0) {
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
			attackTime *= secondform ? 0.97 : 0.92; // attackTime will decrease by 7% every time and by 3% for second tier phase 1
			double dir = attackTime * 0.25 * (attackTime % 2 * 2 - 1); // Assigns a local direction variable from the attack time.
			double speed = (secondform ? 1.2 : 0.7); // speed is dependent on the attackType. (higher attackType, faster speeds)
			double speedMult=player.isWithin(8,this) && player.potionEffects.containsKey(PotionType.Time) ? 2 : player.potionEffects.containsKey(PotionType.AntiTime) ? 0.25 : 1;
			level.add(new Spark(this, (Math.cos(dir) * speed) / speedMult, (Math.sin(dir) * speed) /speedMult)); // Adds a spark entity with the cosine and sine of dir times speed.
			return; // Skips the rest of the code (attackTime was > 0; ie we're attacking.)
		}


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
	public void doHurt(int damage, Direction attackDir, boolean canCrit) {
		if(hpLevel<0.75) {
			if(secondform && hpLevel<0.21){
				level.add(new Arrow(this, Direction.RIGHT, 4));
				level.add(new Arrow(this, Direction.LEFT, 4));
				level.add(new Arrow(this, Direction.UP, 4));
				level.add(new Arrow(this, Direction.DOWN, 4));
			}else if (artime % 3 == 0 && hpLevel<0.75) {
				if (direction == 0) {
					level.add(new Arrow(this, Direction.RIGHT, secondform ? 4 : 3));
					level.add(new Arrow(this, Direction.LEFT, secondform ? 4 : 3));
					direction++;
				} else if (direction == 1) {
					level.add(new Arrow(this, Direction.UP, secondform ? 4 : 3));
					level.add(new Arrow(this, Direction.DOWN, secondform ? 4 : 3));
					direction--;
				}
			}
		}
		artime++;
		if(invulnerability<=0) {
			boolean crit= !getClosestPlayer().potionEffects.containsKey(PotionType.Weak) && random.nextInt(22)>20 ;
			super.doHurt(health-damage<nextPhase ? health-nextPhase  : damage, attackDir,crit);
			crit=false;
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
			lvlSprites[lvl-1] = (secondform ? MobSprite.compileMobSpriteAnimations(0, 22+(hpLevel<0.5 ? 4 :0)) : MobSprite.compileMobSpriteAnimations(0, 20+(hpLevel<0.5 ? 4 :0)));
		}else lvlSprites[lvl-1] =(secondform ? MobSprite.compileMobSpriteAnimations(8, 22+(hpLevel<0.5 ? 4 :0)) : MobSprite.compileMobSpriteAnimations(8, 20+(hpLevel<0.5 ? 4 :0)));
		super.render(screen);
		renderHPPercent(screen);

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
		super.die(); // Calls the die() method in EnemyMob.java
		active = false;
		Player[] players = level.getPlayers();
		Sound.bossDeath.play(); // Play boss-death sound.
		if (!secondform) {
			AirWizard.invulnerability = 0;
			Analytics.AirWizardDeath.ping();
			dropItem(10, 20, Items.get("Cloud shard"));
			Updater.notifyAll("Air Wizard: Defeated!");
			if (!beaten) {
				Analytics.FirstAirWizardDeath.ping();
				Updater.notifyAll("The Dungeon is now open!", -400);
			}
			beaten = true;
		} else {
			Save.AirWizard2Beaten = true;
			AirWizard.invulnerability = 0;
			Analytics.AirWizardIIDeath.ping();
			Updater.notifyAll("Air Wizard II: Defeated!");
			Updater.notifyAll("But he cursed you");
			dropItem(20, 30, Items.get("Cloud shard"));
			dropItem(1, 1, Items.get("Demonicolon"));
			Game.player.potionEffects.put(PotionType.Blind, 9500);
			if (!(boolean) Settings.get("unlockedskin")) {
				Analytics.FirstAirWizardIIDeath.ping();
				Updater.notifyAll("Some stuff lies on the ground...", -400);

			}



		}
	}
		@Override
		public boolean isSwimming(){
		return false; //AW always flies . They never swim
		}
	public int getMaxLevel() { return 2; }
}
