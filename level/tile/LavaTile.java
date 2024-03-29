package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.entity.particle.FireParticle;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;

import java.util.Arrays;

public class LavaTile extends Tile {
	private ConnectorSprite sprite = new ConnectorSprite(LavaTile.class, new Sprite(12, 9, 3, 3, 1, 3), Sprite.dots(0))
	{
		public boolean connectsTo(Tile tile, boolean isSide) {
			return tile.connectsToFluid;
		}
	};
	String[] toBurn={"OAK","BIRCH","CONIFER","SNOWYCONIFER","MANGROVEW","MANGROVE","SMALL TREE","DEAD TREE","GLACIER","BRAMBLE","REED"};
	protected LavaTile(String name) {
		super(name, (ConnectorSprite)null);
		super.csprite = sprite;
		isSurface =false;
		connectsToSand = true;
		connectsToFluid = true;
	}
	
	public void render(Screen screen, Level level, int x, int y) {
		long seed = (tickCount + (x / 2 - y) * 4311) / 10 * 54687121l + x * 3271612l + y * 3412987161l;
		sprite.full = Sprite.randomDots(seed, 1);
		Tiles.get("Dirt").render(screen, level, x, y);
		sprite.render(screen, level, x, y);
	}
	
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canSwim()||e.canFly();
	}

	public boolean tick(Level level, int xt, int yt) {
		int xn = xt;
		int yn = yt;

		if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
		else yn += random.nextInt(2) * 2 - 1;

		if ((level.getTile(xn, yn).name.contains("PLANK")  || level.getTile(xn, yn).name.contains("WOOD") || level.getTile(xn, yn).name.contains("WOOL"))  || level.getTile(xn, yn) == Tiles.get("lily pad")) {
			level.setTile(xn,yn,Tiles.get("hole"));
			for(int i=0;i<1+random.nextInt(3);i++) {
				int randX = random.nextInt(16);
				int randY = random.nextInt(12);
				level.add(new FireParticle(xn*16 - 8 + randX, yn*16 - 6 + randY));
			}
		}else if(level.getTile(xn, yn) == Tiles.get("hole")) level.setTile(xn, yn, this);//why do we need particles for normal spreading?
		if (Arrays.asList(toBurn).contains(level.getTile(xn,yn).name)) {
			level.setTile(xn, yn, Tiles.get("dirt"));
			int randX = random.nextInt(16);
			int randY = random.nextInt(12);
			level.add(new FireParticle(xn*16 - 8 + randX, yn*16 - 6 + randY));
		}
		return false;
	}

	public int getLightRadius(Level level, int x, int y) {
		return 6;
	}
}
