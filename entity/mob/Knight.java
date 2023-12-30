package minicraft.entity.mob;

import minicraft.core.Renderer;
import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Sprite;
import minicraft.item.Items;
import minicraft.gfx.Screen;

public class Knight extends EnemyMob {
	private static MobSprite[][][] sprites;
	private static final MobSprite[][] ponytail = MobSprite.compileMobSpriteAnimations(16, 38);
	static {
		sprites = new MobSprite[5][4][2];
		for (int i = 0; i < 5; i++) {
			MobSprite[][] list  = MobSprite.compileMobSpriteAnimations(0, 10 + (i * 2));
			sprites[i] = list;
		}
	}

	
	/**
	 * Creates a knight of a given level.
	 * @param lvl The knights level.
	 */
	public Knight(int lvl) {
		super(lvl, sprites, lvl > 2 ? 3 : 4, 100);
		this.extracolor=random.nextInt(0xFFFFFF);
		this.usesCustomColor=true;
	}
	@Override
	public void render(Screen screen) {
		super.render(screen);
		if((this.extracolor+this.lvl)%4==0) {
			MobSprite ptail = ponytail[dir.getDir()][(walkDist >> 3) % ponytail[dir.getDir()].length];
			ptail.render(screen, x - 8, y - 11, -1, this.extracolor);
		}
	}
	@Override
	protected void touchedBy(Entity entity) {
		if (entity instanceof Player) {
			int damage = lvl + Settings.getIdx("diff");
			((Player)entity).hurt(this, damage);
		}
	}
	public void die() {
		if (Settings.get("diff").equals("Easy"))
			dropItem(1, 3, Items.get("shard"));
		else
			dropItem(0, 2, Items.get("shard")
			);
		
		if(random.nextInt(24/lvl/(Settings.getIdx("diff")+1)) == 0)
			dropItem(1, 1, Items.get("key"));
		if(getClosestPlayer().curArmor!=null)
		if(Renderer.player.curArmor.getName()=="Night Armor" && Updater.getTime()==Updater.Time.Night && lvl>4)getClosestPlayer().heal(3);
		super.die();
	}
	@Override
	public void doHurt(int damage, Direction attackDir,boolean chance) { //if we can crit

		boolean canCrit=chance;
		if (isRemoved() || hurtTime > 0) return; // If the mob has been hurt recently and hasn't cooled down, don't continue
		Player player = getClosestPlayer();
		if (player != null) { // If there is a player in the level

			/// Play the hurt sound only if the player is less than 80 entity coordinates away; or 5 tiles away.
			int xd = player.x - x;
			int yd = player.y - y;
			if (xd * xd + yd * yd < 80 * 80) {
				Sound.monsterHurt.play();
			}
		}
		this.hurtTime--;
		super.doHurt((int)(damage / 1.9), attackDir, chance);
	}
	public int getMaxLevel() { return 5; }

	@Override
	public void tick() {
		super.tick();
		if(this.extradata>0)this.extradata--; //cooldown within knight can get healed again from fire spark

	}
}
