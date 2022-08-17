package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.FireParticle;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class DeepslateOreTile extends Tile {
	private static Sprite sprite = new Sprite(4, 2, 2, 2, 1);
	
	protected DeepslateOreTile(String name) {
		super(name, sprite);
		connectsToFluid=true;
	}
	
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e instanceof AirWizard;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		hurt(level, x, y, 0);
		return true;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			int staminaPay=(4-tool.level < 2 ? 2 : 4-tool.level);
			int dmg = random.nextInt(10) + tool.damage;
			if (tool.level!=6 && tool.type == ToolType.Pickaxe && player.payStamina(staminaPay) && tool.payDurability(dmg)) {
				// Drop coal since we use a pickaxe.
				hurt(level, xt, yt, dmg);
				return true;
			}else if (tool.level==6 && tool.type == ToolType.Pickaxe && player.payStamina(staminaPay) && tool.payDurability(dmg)) {
				dmg=random.nextInt(8)+2;
				hurt(level, xt, yt, dmg);
			}
		}
		return false;
	}
	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("Lava").render(screen, level, x, y);

		sprite.render(screen, x << 4, y << 4);
	}
	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + dmg;
		int health = 60;
		if (Game.isMode("creative")) dmg = damage = health;
		level.add(new SmashParticle(x * 16, y * 16));
		Sound.monsterHurt.play();
		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= health) {
			level.setTile(x, y, Tiles.get("Lava"));
			level.dropItem(x * 16 + 8, y * 16 + 8,1,7,Items.get("Coal"));
			level.dropItem(x * 16 + 8, y * 16 + 8,1,2,Items.get("Stone"));
		} else
			level.setData(x, y, damage);
	}

	public void bumpedInto(Level level, int x, int y, Entity entity) {
		if (entity instanceof AirWizard) return;
		
		if(entity instanceof Player)
			((Mob)entity).hurt(this, x, y, 1 + Settings.getIdx("diff"));
	}
	public boolean tick(Level level, int xt, int yt) {
		int xn = xt;
		int yn = yt;

		if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
		else yn += random.nextInt(2) * 2 - 1;

		if (level.getTile(xn, yn) == Tiles.get("hole") || level.getTile(xn, yn) == Tiles.get("wood wall") || level.getTile(xn, yn) == Tiles.get("wood door") || level.getTile(xn, yn) == Tiles.get("wood planks")) {
			level.setTile(xn, yn, Tiles.get("Lava"));
			for (int i = 0; i < 6; i++) {
				int randX = random.nextInt(16);
				int randY = random.nextInt(12);
				level.add(new FireParticle(xn - 8 + randX, yn - 6 + randY));
			}
		}
		if (level.getTile(xn, yn) == Tiles.get("snow") || level.getTile(xn, yn) == Tiles.get("tree") || level.getTile(xn, yn) == Tiles.get("birch") || level.getTile(xn, yn) == Tiles.get("Conifer") || level.getTile(xn, yn) == Tiles.get("Snowy conifer") || level.getTile(xn, yn) == Tiles.get("Small tree") || level.getTile(xn, yn) == Tiles.get("Dead tree")) {
			level.setTile(xn, yn, Tiles.get("dirt"));
		}
		return false;
	}
	public int getLightRadius(Level level, int x, int y) {
		return 6;
	}
}
