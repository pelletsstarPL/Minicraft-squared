package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.*;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

/**

	This class is for tiles that will locking our stairs or other forms of moving across the world levels.
 Mostly destroying these tiles will uncover stairs or other forms of moving across the world levels allowing to use these for example
 iced downstairs - break ice around'em and you will be able to use these stairs and enter the caverns ( AND leave since if upstairs are normal and downstairs are frozen
 game won't allow you to use this path to move up one level.

*/
public class LockedStairsTile extends Tile {
	private LockedStairsType type;
	public enum LockedStairsType{
		IcedStairsDown(50,4,35,"Stairs Down",new String[] {"Ice"});
		//more tiles will be added in future updates

		private int spriteX,spriteY,hp;
		private String[] loot;
		private String uncover;

		LockedStairsType(int spriteX,int spriteY,int hp,String uncover,String[] loot){
			this.spriteX= spriteX;this.spriteY= spriteY;
			this.uncover = uncover;this.loot=loot;
		}
	}
	protected LockedStairsTile(String name, LockedStairsType type){
		super(name,new Sprite(type.spriteX, type.spriteY, 1));
		this.type= type;

		switch(type.name()){
			case "IcedStairsDown": connectsToGlacier = true; break;
		}
	}
	private Sprite spr(){
		return new Sprite(type.spriteX, type.spriteY, 2,2,1);
	};
	public void render(Screen screen, Level level, int x, int y) {

		x = x << 4;
		y = y << 4;
		Tiles.get("Glacier").render(screen, level, x, y);
		spr().render(screen, x, y);


	}
	@Override
	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		hurt(level, x, y, dmg);
		return true;
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
		int Health = type.hp; //all dead trees will have 15 hp
		if (Game.isMode("Creative")) dmg = damage = Health;

		level.add(new SmashParticle(x*16, y*16));
		Sound.monsterHurt.play();
		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= Health) {
			for(int i=0;i< type.loot.length;i++){
				switch(type.loot[i]){
					case "Ice":level.dropItem(x * 16 + 8, y * 16 + 8, 2, 4, Items.get(type.loot[i]));break;
					default:level.dropItem(x * 16 + 8, y * 16 + 8, 1, 2, Items.get(type.loot[i]));break;
				}
			}
			level.setTile(x, y, Tiles.get(type.uncover));
		} else {
			level.setData(x, y, damage);
		}
	}

	public boolean mayPass(Level level, int x, int y, Entity e){
		if((e instanceof NightWizard || e instanceof Wraith) && level.depth>=0)
			return true;
		else return false;
	}
}
