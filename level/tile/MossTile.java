package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class MossTile extends Tile {
	private static ConnectorSprite sprite = new ConnectorSprite(MossTile.class, new Sprite(0, 18, 3, 3, 1, 3), new Sprite(3, 18, 2, 2, 1))
	{
		public boolean connectsTo(Tile tile, boolean isSide) {
			if(!isSide) return true;
			return tile.connectsToMoss;
		}
	};
	
	protected MossTile(String name) {
		super(name, sprite);
		connectsToMoss=true;
		connectsToGrass=false;
		csprite.sides = csprite.sparse;
		maySpawn = true;
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
		if(level.depth==0)connectsToGrass=true;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					level.setTile(xt, yt, Tiles.get("Dirt"));
					Sound.monsterHurt.play();
						level.dropItem(xt * 16 + 8, yt * 16 + 8, 1,2, Items.get("Moss"));
					return true;
				}
			}
		}
		return false;
	}
}
