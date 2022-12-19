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
import minicraft.item.*;
import minicraft.level.Level;
import org.jetbrains.annotations.Nullable;

public class StairsTile extends Tile {
	private StairsType type;
	public enum StairsType {
		Normal(new Sprite(19, 0, 2, 2, 1, 0),new Sprite(21, 0, 2, 2, 1, 0),44,0),
		Obsidian(new Sprite(19, 4, 2, 2, 1, 0),new Sprite(21, 4, 2, 2, 1, 0),0,0);

		private Sprite spriteUp,spriteDown;
		private int burySpriteX,burySpriteY;

		StairsType(Sprite spriteUp, Sprite spriteDown, @Nullable int burySpriteX, @Nullable int burySpriteY) {
			this.spriteUp = spriteUp;
			this.spriteDown = spriteDown;
			this.burySpriteX = burySpriteX; //rumble is basically stone that prevents player from using stairs
			this.burySpriteY = burySpriteY; //rumble is basically stone that prevents player from using stairs
		}

	}
	protected StairsTile(String name, boolean leadsUp,StairsType type){
		super(name,leadsUp ? type.spriteUp : type.spriteDown);
		this.type= type;
		maySpawn = false;
	}
	private Sprite sprB(){
		return new Sprite(type.burySpriteX, type.burySpriteY,2,2, 1);
	};
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
		sprite.render(screen, x*16, y*16);
		if(level.getData(x,y)>0)sprB().render(screen, x*16,y*16); //not every stairs type will have stones to remove
	}

	@Override
	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		super.interact(level, xt, yt, player, item, attackDir);
		//allows for harming if there is anything to harm.
		if (level.getData(xt, yt) > 0) {


			if (Game.isMode("Creative"))
				return false; // Go directly to hurt method
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				if (tool.type == ToolType.Pickaxe) {
					if (player.payStamina(6 - tool.level) && tool.payDurability()) {
						hurt(level, xt, yt, random.nextInt(10) + 5 + tool.level);
						return true;
					}
				}
			}
		}

		// Makes it so you can remove the stairs if you are in creative and debug mode.
		if (item instanceof PowerGloveItem && Game.isMode("Creative") && Game.debug) {
			level.setTile(xt, yt, Tiles.get("Grass"));
			Sound.monsterHurt.play();
			return true;
		} else {
			return false;
		}
	}

	public boolean mayPass(Level level, int x, int y, Entity e){
		return level.getData(x,y)<=0;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		if(level.getData(x,y)>0) {
			hurt(level, x, y, dmg);
			return true;
		}else return false;
	}
	public void hurt(Level level, int x, int y, int dmg) {
		if (level.getData(x, y) > 0) {
			if (Game.isMode("Creative")) {
				dmg = level.getData(x, y);
			}
			int damage = level.getData(x, y) - dmg;
			//System.out.println(damage);


			level.add(new SmashParticle(x * 16, y * 16));
			Sound.monsterHurt.play();
			level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
			level.setData(x, y, damage < 0 ? 0 : damage);
			if(level.getData(x,y)==0)level.dropItem(x * 16 + 8, y * 16 + 8, 1, 2, Items.get("Stone"));
		}
	}

}
