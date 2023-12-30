package minicraft.level.tile;

import minicraft.core.Updater;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.*;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import org.jetbrains.annotations.Nullable;

public class SaplingTile extends Tile {


	public enum SaplingType {
		Oak(new Sprite(12, 1,1),new Sprite(56, 4,2,2,1),"Grass","Oak", 110,false,true,0.07,false),
		Birch(new Sprite(11, 8,1),new Sprite(56, 2,2,2,1),"Grass","Birch", 110,false,true,0.07,false),
		Conifer(new Sprite(23,0 ,1),new Sprite(58, 4,2,2,1),"Grass","Conifer", 115,false,true,0.04,false),
		SnowyConifer(new Sprite(23, 0,1),new Sprite(58, 2,2,2,1),"Snow","Snowy conifer", 115,false,true,0.04,false),
		Cactus(new Sprite(11, 7,1),new Sprite(54, 2,2,2,1),"Sand","Cactus", 120,true,true,0.07,false),
		CloudCactus(new Sprite(10, 8,1),new Sprite(54, 0,2,2,1),"Cloud","Cloud cactus", 250,true,true,0.05,false),
		Fern(new Sprite(8, 1,0),new Sprite(56, 0,2,2,1),"Grass","Fern", 80,false,false,0,true),
		Fungus(new Sprite(11, 1,0),new Sprite(54, 4,2,2,1),"Moss","Fungus Tree", 100,false,true,0.15,true);

		private int maxAge;
		private double SmallChance;
		private Sprite st1,st2;
		private String baseTile,growsTo;
		private boolean harmOnTouch,hasSmall,canPass;

		SaplingType(Sprite st1, Sprite st2, String baseTile, String growsTo, int maxAge, boolean harmOnTouch, boolean hasSmall, double SmallChance,boolean canPass){
			this.st1=st1;this.st2=st2;
			this.baseTile=baseTile;this.growsTo=growsTo;this.maxAge=maxAge;
			this.harmOnTouch=harmOnTouch;this.hasSmall=hasSmall;
			this.SmallChance=SmallChance;this.canPass=canPass;
		}
	}
	private SaplingType type;
	private Tile onType;
	private Tile growsTo;

	protected SaplingTile(String name, SaplingType type) {
		super(name, (Sprite) null);
		this.type= type;
		this.onType = Tiles.get(type.baseTile);
		connectsToGrass = onType.connectsToGrass;
		connectsToMoss = onType.connectsToMoss;
		connectsToFluid = onType.connectsToFluid;
		connectsToSnow = onType.connectsToSnow;
		connectsToSand = onType.connectsToSand;
		maySpawn = true;
	}

	public void render(Screen screen, Level level, int x, int y) {
		int xa = x << 4, ya = y << 4;
		Tiles.get(type.baseTile).render(screen, level, x, y);
		if(level.getData(x,y)>type.maxAge/2)type.st2.render(screen,xa,ya);
		else type.st1.render(screen,xa+4,ya+4);
	}

	public boolean tick(Level level, int x, int y) {
if( level.realm.contains("overworld")) {
	int age = 0;
	if (type == SaplingType.Fungus)
		age = level.getData(x, y) + (Updater.tickCount % (random.nextInt(6) + 1) == 0 ? 1 : 0);
	else age = level.getData(x, y) + (random.nextInt(19) > 16 ? 2 : 1);
	level.setData(x, y, age);
	String extraProp = "";
	if (type.hasSmall && Math.random() < type.SmallChance) extraProp = "Small ";
	if (level.getData(x, y) > type.maxAge) level.setTile(x, y, extraProp + type.growsTo);
	return true;
}else return false;
	}
	public boolean mayPass(Level level, int x, int y, Entity e){
        if(type.canPass)return true;
		if((e instanceof NightWizard || e instanceof Wraith) || level.getData(x,y) < type.maxAge/2) //the stage 2 is solid , we cannot pass then
			return true;
		else return false;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		level.setTile(x, y, onType);
		Sound.monsterHurt.play();
		return true;
	}
	public void bumpedInto(Level level, int x, int y, Entity entity) {
		if(level.getData(x,y)>type.maxAge/2 && type.harmOnTouch) { //only harm when cactus is growing and only while plant is at its second stage
			if (entity instanceof AirWizard || entity instanceof Wraith || entity instanceof Ghost || entity instanceof Clallay)
				return;

			if (entity instanceof Mob)
				((Mob) entity).hurt(this, x, y, 1);
		}
	}
	public int hitboxExtraSmall(Level level,int x, int y){ return 3;};
}
