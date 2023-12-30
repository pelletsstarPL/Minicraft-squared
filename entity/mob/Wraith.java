package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.furniture.Bed;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.level.Level;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;
import minicraft.entity.mob.Player;
import minicraft.level.tile.Tiles;

public class Wraith extends EnemyMob {
	private static MobSprite[][][] sprites;
	static {
		sprites = new MobSprite[5][4][2];
		for (int i = 0; i < 5; i++) {
			MobSprite[][] list = MobSprite.compileMobSpriteAnimations(24, 10 + (i * 2));
			sprites[i] = list;
		}
	}
	public int curseCooldown;
	public boolean canSwim() { return true; }
	public boolean canBurn() { return false; }
	@Override
	public boolean canFly() {
		return true;
	}
	public boolean isSolid() { return false; }
	/**
	 * Creates a wraith of the given level.
	 * @param lvl Wraith's level.
	 */
	public Wraith(int lvl,int immuneToWeakness) {

		super(lvl, sprites, lvl==4 ? 5 : 6, true,60,immuneToWeakness!=0 ? -1 : 4000*lvl,8,150);
		this.extradata = immuneToWeakness;
	}
	public boolean canBeAffectedByLava() { return false; }
	public void die() {
		if(this.extradata!=0)AirWizard.invulnerability--;//reduce invulnerability
		int xd = Renderer.player.x - x; // The horizontal distance between the player and the wraith
		int yd = Renderer.player.y - y; // The vertical distance between the player and the wraith
		if (xd * xd + yd * yd < 16 * 16 * 10 * 10) {
			///play if less than 10 blocks away
			Sound.wraithDeath.play(); // Play wraith-death sound
			if(level.depth>=0 && Updater.isbloody && Updater.getTime()==Updater.Time.Night)
				level.dropItem(x, y, random.nextInt(3), Items.get("blood shard"));
		}
		if(Renderer.player.curArmor!=null)
		if(Renderer.player.curArmor.getName()=="Night Armor" && Updater.getTime()==Updater.Time.Night && lvl>4)getClosestPlayer().heal(3);
		super.die();
	}
	@Override
	public void tick() {
		super.tick();
		curseCooldown--;
		if(level.depth>=0 && Updater.tickCount>12500 && Updater.tickCount<37800 && Updater.tickCount%100==0 && lvl<3 && this.extradata!=0)this.die(0);
		Player player = getClosestPlayer();
		if (player != null && !Bed.sleeping() && randomWalkTime <= 0 && !Game.isMode("Creative")) { // Checks if player is on zombie's level, if there is no time left on randonimity timer, and if the player is not in creative.
			int xd = player.x - x;
			int yd = player.y - y;
			if (xd * xd + yd * yd < detectDist * detectDist) {

				/// If player is less than 6.25 tiles away, then set move dir towards player
				int sig0 = 1; // This prevents too precise estimates, preventing mobs from bobbing up and down.
				this.xmov = this.ymov = 0;
				if (xd < sig0) this.xmov = -1;
				if (xd > sig0) this.xmov = +1;
				if (yd < sig0) this.ymov = -1;
				if (yd > sig0) this.ymov = +1;
			} else {
				// If the enemy was following the player, but has now lost it, it stops moving.
				// *That would be nice, but I'll just make it move randomly instead.
				randomizeWalkDir(false);
			}
		}
	}

	@Override
	protected void touchedBy(Entity entity) { // If an entity (like the player) touches the enemy mob
		super.touchedBy(entity);
		int dmg=lvl * (Settings.get("diff").equals("Hard") ? 2 : 1);
		int selfharm=health/(lvl*3);
		// Hurts the player, damage is based on lvl.
		if(entity instanceof Player) {
			((Player)entity).hurt(this, dmg);
			if(this.extradata!=0 && curseCooldown<=0) {
				level.add(new TextParticle("" + selfharm, x, y, Color.MAGENTA));
				this.health-=selfharm;
				curseCooldown=150;
			}
			//Night armor damage return
			if(Renderer.player.curArmor!=null) {
				if (Renderer.player.curArmor.getName() == "Night Armor" && Updater.getTime() == Updater.Time.Night && !Game.isMode("creative"))
					if (Math.random()<(Updater.isbloody ? 0.8 : 0.2)) {
						this.hurt(this, (int) (dmg + (maxHealth * 0.1)), Direction.NONE);

					}

			}

		}


	}
}
