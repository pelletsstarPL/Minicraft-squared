package minicraft.entity;

import java.util.List;

import com.sun.tools.classfile.ConstantPool;
import minicraft.entity.mob.*;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.item.Items;
import minicraft.item.PotionType;

import javax.swing.*;

public class Arrow extends Entity implements ClientTickable {
	private Direction dir;
	private int damage;
	private int pierce;
	public Mob owner;
	public static boolean criticalHit = false;
	private int speed;
	// Can walk fly over wool? OF the hecking course
	public boolean canWool() { return true; }
	public Arrow(Mob owner, Direction dir, int dmg) {
		this(owner, owner.x, owner.y, dir, dmg);
	}
	public Arrow(Mob owner, int x, int y, Direction dir, int dmg) {
		super(Math.abs(dir.getX())+1, Math.abs(dir.getY())+1);
		this.owner = owner;
		this.x = x;
		this.y = y;
		this.dir = dir;

		damage = dmg;
		col = Color.get(-1, 111, 222, 430);

		if (damage > 3) speed = 8;
		else if (damage >= 0) speed = 7;
		else speed = 6;
	}

	/**
	 * Generates information about the arrow.
	 * @return string representation of owner, xdir, ydir and damage.
	 */
	public String getData() {
		return owner.eid + ":" + dir.ordinal() + ":"+damage;
	}

	@Override
	public void tick() {
		if (x < 0 || x >> 4 > level.w || y < 0 || y >> 4 > level.h) {
			remove(); // Remove when out of bounds
			return;
		}
		if (this.pierce > 1) remove(); //limited pierce
		x += dir.getX() * speed;
		y += dir.getY() * speed;

		// TODO I think I can just use the xr yr vars, and the normal system with touchedBy(entity) to detect collisions instead.

		List<Entity> entitylist = level.getEntitiesInRect(new Rectangle(x, y, 0, 0, Rectangle.CENTER_DIMS));
		boolean criticalHit = random.nextInt(11) < 9;
		for (Entity hit : entitylist) {
			if (hit instanceof Mob && hit != owner) {
				Mob mob = (Mob) hit;
				int extradamage = (hit instanceof Player ? 0 : 3) * (criticalHit ? 1 : (hit instanceof Player ? 2 : 3));
				mob.hurt(owner, damage + extradamage, dir);
			}
		}


			if (getClosestPlayer().potionEffects.containsKey(PotionType.Time) && !(owner instanceof Player)) speed = 3;
			if (getClosestPlayer().potionEffects.containsKey(PotionType.AntiTime) && !(owner instanceof Player)) speed = 9;
			for (Entity hit : entitylist) {
				//limited pierce
				if (hit instanceof Mob && hit != owner) {
					criticalHit = random.nextInt(15) > 12;
					Mob mob = (Mob) hit;
					if (owner instanceof Player) {
						int dmge = (int) ((damage * 2.2) + random.nextInt(7));
						mob.hurt(owner, dmge, dir, criticalHit);
						this.pierce++;
						if (this.pierce > 1) remove();
					} else {

						if (hit instanceof Player) {
							criticalHit = random.nextInt(20) > 18;
							if (criticalHit) level.add(new TextParticle("CRIT!", x, y, Color.ORANGE, 60)); // CRIT!
							mob.hurt(owner, damage + (criticalHit ? 1 : 0), dir, criticalHit);

							//level.add(new TextParticle(""+(damage+(criticalHit ? 1 : 0)), x, y, Color.RED,60));
							criticalHit = false;
							this.pierce++;
							if (this.pierce > 1) remove();
						} else {
							int dmge = (int) ((damage * 2.2) + random.nextInt(7));
							mob.hurt(owner, dmge, dir, criticalHit);
							level.add(new TextParticle("" + dmge, x, y, Color.RED, 60));
							criticalHit = false;
							this.pierce++;
							if (this.pierce > 1) remove();
						}

					}
				}
			}

			if (!level.getTile(x / 16, y / 16).mayPass(level, x / 16, y / 16, this)
					&& !level.getTile(x / 16, y / 16).connectsToFluid
					&& level.getTile(x / 16, y / 16).id != 16) {
				this.remove();
				if (Math.random() < 0.6 / ((this.pierce * 2) + 1) && owner instanceof Player)
					level.dropItem(x, y, Items.get("Arrow"));
			}
			criticalHit = random.nextInt(11) > 9;
		}



	public boolean isSolid() {
		return false;
	}

	@Override
	public void render(Screen screen) {
		int xt = 0;
		int yt = 2;

		if(dir == Direction.LEFT) xt = 1;
		if(dir == Direction.UP) xt = 2;
		if(dir == Direction.DOWN) xt = 3;
		String own=owner.toString();
		if(own.startsWith("AirWiz")) screen.render(x - 4, y - 4, (6+xt) + yt * 32, 0);
		else screen.render(x - 4, y - 4, xt + yt * 32, 0);
	}

}
