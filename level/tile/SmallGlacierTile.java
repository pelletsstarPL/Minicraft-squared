package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.*;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;


public class SmallGlacierTile extends Tile {
	private SmallGlacierType type;
	public enum SmallGlacierType{
		Block(46,4),
		Spikes(44,4);

		private int spriteX,spriteY;


		SmallGlacierType(int spriteX,int spriteY){
			this.spriteX= spriteX;this.spriteY= spriteY;
		}
	}
	protected SmallGlacierTile(String name, SmallGlacierType type){
		super(name,new Sprite(type.spriteX, type.spriteY,2,2, 1));
		this.type= type;
		connectsToSnow=true;
	}
	private Sprite spr(){
		return new Sprite(type.spriteX, type.spriteY, 2,2,1);
	};
	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("Snow").render(screen, level, x, y);
		int data = level.getData(x, y);
		int shape = (data / 16) % 2;

		x = x << 4;
		y = y << 4;
		spr().render(screen, x + 8 * shape, y);


	}
	@Override
	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if(Game.isMode("Creative"))
			return false; // Go directly to hurt method
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Pickaxe) {
				int staminaPay=(4-tool.level < 2 ? 2 : 4-tool.level);
				if(4-tool.level<2)staminaPay=2;
				int dmg=random.nextInt(10) + tool.damage;
				if (player.payStamina(staminaPay) && tool.payDurability() && tool.level!=6) {
					hurt(level, xt, yt, dmg);
					return true;
				}else if(player.payStamina(staminaPay) && tool.payDurability() && tool.level==6){
					hurt(level, xt, yt, random.nextInt(3)+2);
				}
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg) {

		int damage = level.getData(x, y) + dmg;
		int iceHealth = random.nextInt(11)+10; //health of ice will vary between 10 and 20 hp
		if (Game.isMode("Creative")) dmg = damage = iceHealth;

		level.add(new SmashParticle(x*16, y*16));
		Sound.monsterHurt.play();
		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= iceHealth) {
			level.dropItem(x * 16 + 8, y * 16 + 8, 1, 2, Items.get("Ice"));
			level.setTile(x, y, Tiles.get("Snow"));
		} else {
			level.setData(x, y, damage);
		}
	}
	@Override
	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		hurt(level, x, y, dmg);
		return true;
	}

	public boolean mayPass(Level level, int x, int y, Entity e){
		if((e instanceof NightWizard || e instanceof Wraith || e instanceof WraithA) && level.depth>=0)
			return true;
		else return false;
	}
}
