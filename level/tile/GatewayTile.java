package minicraft.level.tile;

import minicraft.core.Game;
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
import minicraft.item.*;
import minicraft.level.Level;

public class GatewayTile extends Tile {


	private int shardsCount;
	private static Sprite[] levelSprite = new Sprite[2];
	static {
		levelSprite[0] = new Sprite(49, 14, 2, 2, 1); //Horizontal
		levelSprite[1] = new Sprite(49, 16, 2, 2, 1); //Vertical
	}

	protected GatewayTile(String name) {
		super(name, levelSprite[0]);
		maySpawn = true;
	}


	@Override
	public void render(Screen screen, Level level, int x, int y) {

		levelSprite[(level.getData(x,y)/3)%2].render(screen, x * 16, y * 16);
		if(level.getData(x,y)%3>0 && level.getData(x,y)<3)new Sprite(54,18,2,1,1).render(screen,x * 16,y * 16);
		if(level.getData(x,y)%3>1 && level.getData(x,y)<3)new Sprite(54,19,2,1,1).render(screen,x * 16,(y * 16) + 8);
		if(level.getData(x,y)>2 && level.getData(x,y)%3>0)new Sprite(56,18,1,2,1).render(screen,x * 16,y * 16);
		if(level.getData(x,y)>2 && level.getData(x,y)%3>1)new Sprite(57,18,1,2,1).render(screen,(x * 16) + 8,y * 16);
	}


	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir,Item item) {
		ToolItem tool = (ToolItem) item;
		if(item instanceof ToolItem && tool.type==ToolType.Pickaxe) {
			hurt(level, x, y, dmg);
			return true;
		}
		return false;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {

		/*if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Pickaxe) {
				int staminaPay = (4 - tool.level < 2 ? 2 : 4 - tool.level);
				int dmg = random.nextInt(10) + tool.damage;
				if (tool.level != 6 && tool.type == ToolType.Pickaxe && player.payStamina(staminaPay) && tool.payDurability(dmg)) {
					// Drop coal since we use a pickaxe.
					dropCoal = true;
					hurt(level, xt, yt, tool.getDamage());
					return true;
				} else if (tool.level == 6 && tool.type == ToolType.Pickaxe && player.payStamina(staminaPay) && tool.payDurability(dmg)) {
					dmg = random.nextInt(8) + 2;
					hurt(level, xt, yt, dmg);
					return true;
				}
			}
		}*/
		if(player.activeItem.getDisplayName()=="Shard") {
			if (Math.abs(level.getData(xt, yt)) > 2) {
				Game.notifications.add("All slots have shards inside");
				System.out.println("DZIAłA");
			}else{
				level.setData(xt, yt,Math.abs(level.getData(xt,yt))+1); //input shard
				StackableItem activeItem = (StackableItem) (player.activeItem);//unequip shards
				activeItem.count--;
				if(Math.abs(activeItem.count)<1)player.activeItem = null; //unequip everything
				else player.activeItem = activeItem;
			}
			}
		return false;
	}


}
