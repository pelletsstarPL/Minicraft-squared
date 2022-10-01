package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.PowerGloveItem;
import minicraft.level.Level;

public class StairsTile extends Tile {
	private StairsType type;
	public enum StairsType {
		Normal(new Sprite(19, 0, 2, 2, 1, 0),new Sprite(21, 0, 2, 2, 1, 0)),
		Obsidian(new Sprite(19, 4, 2, 2, 1, 0),new Sprite(21, 4, 2, 2, 1, 0));

		private Sprite spriteUp,spriteDown;

		StairsType(Sprite spriteUp,Sprite spriteDown) {
			this.spriteUp = spriteUp;
			this.spriteDown = spriteDown;
		}

	}
	protected StairsTile(String name, boolean leadsUp,StairsType type){
		super(name,leadsUp ? type.spriteUp : type.spriteDown);
		this.type= type;
		maySpawn = false;
	}
	@Override
	public void render(Screen screen, Level level, int x, int y) {


		Tile[] areaTiles = level.getAreaTiles(x,y,1);
		switch (level.depth){
			case 1:Tiles.get("Cloud").render(screen,level,x,y);break; //Sky
			default:Tiles.get("Dirt").render(screen,level,x,y);break; //Sky
		}
		for(Tile t: areaTiles){
			if(t == Tiles.get("snow")){
				connectsToSnow = true;Tiles.get("snow").render(screen, level, x, y);
			}
			if(t == Tiles.get("Grass")){
				connectsToGrass = true;Tiles.get("Grass").render(screen, level, x, y);
			}
			if(t == Tiles.get("Moss")){
				connectsToMoss = true;Tiles.get("Moss").render(screen, level, x, y);
			}
			if(t == Tiles.get("Wood planks"))Tiles.get("Wood planks").render(screen, level, x, y);
			if(t == Tiles.get("Stone bricks"))Tiles.get("Stone bricks").render(screen, level, x, y);
			if(t == Tiles.get("ornate stone"))Tiles.get("ornate stone").render(screen, level, x, y);
			if(t == Tiles.get("ornate obsidian"))Tiles.get("ornate obsidian").render(screen, level, x, y);
			if(t == Tiles.get("obsidian"))Tiles.get("obsidian").render(screen, level, x, y);
			if(t == Tiles.get("decorated obsidian"))Tiles.get("decorated obsidian").render(screen, level, x, y);
			if(t == Tiles.get("rocky stone"))Tiles.get("rocky stone").render(screen, level, x, y);
			if(t == Tiles.get("Ground rock"))Tiles.get("Ground rock").render(screen, level, x, y);
		}
		/*if(level.getTile(x-1, y) == Tiles.get("Grass") || level.getTile(x+1, y) == Tiles.get("Grass") || level.getTile(x, y+1) == Tiles.get("Grass") || level.getTile(x, y-1) == Tiles.get("Grass")){
			Tiles.get("Grass").render(screen, level, x, y);
			sprite.render(screen, x << 4, y << 4, 0,0x80A560);
		}
		else if(level.depth<1) sprite.render(screen, x * 16, y * 16, 0, DirtTile.dCol(level.depth));
		else sprite.render(screen, x * 16, y * 16, 0, 0xC2C2C2);*/
		sprite.render(screen, x * 16,y * 16);
	}

	@Override
	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		super.interact(level, xt, yt, player, item, attackDir);

		// Makes it so you can remove the stairs if you are in creative and debug mode.
		if (item instanceof PowerGloveItem && Game.isMode("Creative") && Game.debug) {
			level.setTile(xt, yt, Tiles.get("Grass"));
			Sound.monsterHurt.play();
			return true;
		} else {
			return false;
		}
	}
}
