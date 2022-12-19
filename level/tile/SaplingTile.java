package minicraft.level.tile;

import minicraft.core.Updater;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Mob;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;

public class SaplingTile extends Tile {
	private static Sprite sprite = new Sprite(12, 1, 1);
	private static Sprite spriteCactus = new Sprite(11, 7, 1);
	private static Sprite spriteConifer = new Sprite(23, 0, 1);
	private static Sprite spriteFern = new Sprite(8, 1, 0);
	private static Sprite spriteFungus = new Sprite(11, 1, 0);
	private static Sprite spriteBirch = new Sprite(11, 8, 1);
	private static Sprite spriteCloud = new Sprite(10, 8, 1);

	private Tile onType;
	private Tile growsTo;
	
	protected SaplingTile(String name, Tile onType, Tile growsTo) {
		super(name, sprite);
		this.onType = onType;
		this.growsTo = growsTo;
		connectsToSand = onType.connectsToSand;
		connectsToGrass = onType.connectsToGrass;
		connectsToMoss = onType.connectsToMoss;
		connectsToFluid = onType.connectsToFluid;
		connectsToSnow = onType.connectsToSnow;
		maySpawn = true;
	}

	public void render(Screen screen, Level level, int x, int y) {
		onType.render(screen, level, x, y);
		switch(growsTo.name) {
			case "OAK":sprite.render(screen, (x * 16)+4, (y * 16)+4);break;
			case "BIRCH":spriteBirch.render(screen, (x * 16)+4, (y * 16)+4);break;
			case "CACTUS":spriteCactus.render(screen, (x * 16)+4, (y * 16)+4);break;
			case "CONIFER": case "SNOWY CONIFER":spriteConifer.render(screen, (x * 16)+4, (y * 16)+4);break;
			case "CLOUD CACTUS":spriteCloud.render(screen, (x * 16)+4, (y * 16)+4);break;
			case "BIG FUNGUS":spriteFungus.render(screen, (x * 16)+4, (y * 16)+4);break;
			case "FERN":spriteFern.render(screen, (x * 16)+4, (y * 16)+4);break;
		}
	}

	public boolean tick(Level level, int x, int y) {
		int age;
		if(growsTo==Tiles.get("Big fungus")){
			age = level.getData(x, y) + (Updater.tickCount%(random.nextInt(6)+1)==0 ? 1 : 0) ;
		}else age = level.getData(x, y) + 1;
		if (age > 100 && growsTo!=Tiles.get("Cloud cactus")) {
			level.setTile(x, y, growsTo);
		} else if(age > 250 && (growsTo==Tiles.get("Cloud cactus"))){
			level.setTile(x, y, growsTo);
		}else{
			level.setData(x, y, age);
		}
		return true;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		level.setTile(x, y, onType);
		Sound.monsterHurt.play();
		return true;
	}
}
