package minicraft.entity.furniture;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Entity;
import minicraft.entity.mob.Knight;
import minicraft.entity.mob.Mimic;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.entity.Direction;
import minicraft.entity.mob.ObsidianKnight;
import minicraft.entity.mob.Player;
import minicraft.item.Items;
import minicraft.item.StackableItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KnightStatue extends Furniture {
	private int touches = 0; // >= 0
	private int bossHealth;


	public KnightStatue(int health) {
		super("Knight poppet", new Sprite(36 , 30, 2, 2, 2), 3, 2);
		this.bossHealth = health;
		this.setRealmId(1);

	}


	public boolean use(Player player) {
		if (!ObsidianKnight.active) {
			if (touches == 0) { // Touched the first time.
				Game.notifications.add("Spooky whispers have resounded");
				Sound.statuePh1.play();
				touches++;
			} else if (touches == 1) { // Touched the second time.
				Game.notifications.add("Statue rumbles and hums");
				Sound.statuePh2.play();
				touches++;
			} else { // Touched the third time.
				Sound.statuePh3.play();
				// Awoken notifications is in Boss class
				// Summon the Obsidian Knight boss
				for(int xi = (x >> 4) - 11;xi<(x >> 4) + 11;xi++)
					for(int yi = (y >> 4) - 11;yi<(y >> 4) + 11;yi++){
						if (level.getTile(xi,yi).name.contains("OBSIDIAND DOOR"))level.setData(xi,yi,0);
					}
				Updater.notifyAll(Localization.getLocalized("Obsidian knight has awoken")); // On spawn tell player.
				ObsidianKnight obk = new ObsidianKnight(bossHealth);
				obk.setRealmId(1);
				level.add(obk, x, y, false,1);
				super.remove(); // Removing this statue.
			}

			return true;
		} else { // The boss is active.
			Game.notifications.add("Cannot interact");
			return false;
		}
	}
	@Override
	public void tryPush(Player player) {} // Nothing happens.
	@Override
	protected void touchedBy(Entity entity) {
		return;
	}


	@Override
	public void die() {

		super.die();
	}
	@Override
	public Furniture clone() {
		return new KnightStatue(bossHealth);
	}

	@Override
	public void render(Screen screen) {
		/*if(this.health <= 2200 && this.health>1100)this.sprite = new Sprite(36 , 32, 2, 2, 2);
		else if(this.health<=1100)this.sprite = new Sprite(36 , 34, 2, 2, 2);
		else this.sprite =new Sprite(36 , 30, 2, 2, 2);*/
		super.render(screen);

	}

	public int getBossHealth(){
		return this.bossHealth;
	}
	public void setBossHealth(int health){
		 this.bossHealth = health;
	}

	@Override
	public boolean interact(Player player, @Nullable Item item, Direction attackDir) {
		return false;
	}

}
