package minicraft.level.tile;

import com.sun.jdi.connect.Connector;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.core.Game;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class GroundRockTile extends Tile {
	private ConnectorSprite sprite = new ConnectorSprite(GroundRockTile.class, new Sprite(42, 6, 3, 3, 1, 3), new Sprite(45, 8, 2, 2, 1, 3), new Sprite(45, 6, 2, 2, 1, 3));
		public boolean connectsTo(Tile tile, boolean isSide) {
			if (!isSide) return true;
			return tile.connectsToGroundRock;

	};
	private int maxHealth = 50;
	private boolean dropCoal = false;
	private int damage;
	protected GroundRockTile(String name) {
		super(name, (ConnectorSprite)null);
		csprite = sprite;
	}

	public boolean tick(Level level, int xt, int yt) {
		// TODO revise this method.
		if (random.nextInt(40) != 0) return false;

		int xn = xt;
		int yn = yt;

		if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
		else yn += random.nextInt(2) * 2 - 1;

		/*if (level.getTile(xn, yn) == Tiles.get("Dirt")) {
			level.setTile(xn, yn, this);
		} Moss won't spread */
		return false;
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		sprite.sparse.color = DirtTile.dCol(level.depth);
		sprite.render(screen, level, x, y);
	}

	/*public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Pickaxe) {
				double chance=Math.random()+(tool.level*0.05);
				int staminaPay=(4-tool.level < 2 ? 2 : 4-tool.level);
				if(chance>0.7 || Game.isMode("Creative")) {
					if (player.payStamina(staminaPay) && tool.payDurability()) {
						level.setTile(xt, yt, Tiles.get("Hole"));
						Sound.monsterHurt.play();
						level.dropItem(xt * 16 + 8, yt * 16 + 8, 1, 2, Items.get("Stone"));
						level.add(new TextParticle("" + random.nextInt(17), xt * 16 + 8, yt * 16 + 8, Color.RED));
						return true;
					}
				}else{
					if (player.payStamina(staminaPay) && tool.payDurability()) {
						level.add(new TextParticle("" + random.nextInt(17), xt * 16 + 8, yt * 16 + 8, Color.RED));
						Sound.monsterHurt.play();
						}
					}
				}
			}
		return false;
	}*/
	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir,Item item) {
		ToolItem tool = (ToolItem) item;
		if(item instanceof ToolItem && tool.type==ToolType.Pickaxe) {
			hurt(level, x, y, dmg);
			return true;
		}
		return false;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		ToolItem tool = (ToolItem) item;
		if (item instanceof ToolItem && tool.type==ToolType.Pickaxe) {

			int staminaPay=(4-tool.level < 2 ? 2 : 4-tool.level);
			int dmg = random.nextInt(10) + tool.damage;
			if (tool.level!=6 && tool.type == ToolType.Pickaxe && player.payStamina(staminaPay) && tool.payDurability(dmg)) {
				// Drop coal since we use a pickaxe.
				dropCoal = true;
				hurt(level, xt, yt, tool.getDamage());
				return true;
			}else if (tool.level==6 && tool.type == ToolType.Pickaxe && player.payStamina(staminaPay) && tool.payDurability(dmg)) {
				dmg=random.nextInt(8)+2;
				hurt(level, xt, yt, dmg);
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg) {
		damage = level.getData(x, y) + dmg;

		if (Game.isMode("Creative")) {
			dmg = damage = maxHealth;
			dropCoal = true;
		}

		level.add(new SmashParticle(x * 16, y * 16));
		Sound.monsterHurt.play();

		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= maxHealth) {
			if (dropCoal) {
				level.dropItem(x*16+8, y*16+8, 1, 3, Items.get("Stone"));
				int coal = 0;
				if(!Settings.get("diff").equals("Hard")) {
					coal++;
				}
				level.dropItem(x * 16 + 8, y * 16 + 8, coal, coal + 1, Items.get("Coal"));
			} else {
				level.dropItem(x * 16 + 8, y * 16 + 8, 2, 4, Items.get("Stone"));
			}
			level.setTile(x, y, Tiles.get("hole"));
		} else {
			level.setData(x, y, damage);
		}
	}


}
