package minicraft.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.RemotePlayer;
import minicraft.entity.particle.FireParticle;
import minicraft.entity.particle.GreenStarParticle;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.level.tile.farming.Plant;

public class TileItem extends StackableItem {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
		
		/// TileItem sprites all have 1x1 sprites.
		items.add(new TileItem("Flower", (new Sprite(4, 0, 0)), "flower", "grass","tall grass"));
		items.add(new TileItem("Cloud flower", (new Sprite(9, 1, 0)), "cloud flower", "cloud tallgrass","cloud"));
		items.add(new TileItem("Rose", (new Sprite(5, 0, 0)), "rose", "grass","tall grass"));
		items.add(new TileItem("Azalea", (new Sprite(13, 1, 0)), "azalea", "moss"));
		items.add(new TileItem("Small rose", (new Sprite(5, 1, 0)), "small rose", "grass","tall grass"));
		items.add(new TileItem("Small flower", (new Sprite(12, 1, 0)), "small flower", "grass","tall grass"));
		items.add(new TileItem("Sunflower", (new Sprite(4, 1, 0)), "sunflower", "grass","tall grass"));
		items.add(new TileItem("Acorn", (new Sprite(7, 3, 0)), "oak Sapling", "grass","tall grass"));
		items.add(new TileItem("Catkin", (new Sprite(13, 3, 0)), "birch Sapling", "grass","tall grass"));
		items.add(new TileItem("Dirt", (new Sprite(0, 0, 0)), "dirt", "hole", "water", "lava","empty patch vase"));
		items.add(new TileItem("Lily pad", (new Sprite(7, 1, 0)), "lily pad", "water"));
		items.add(new TileItem("Coarse dirt", (new Sprite(0, 1, 0)), "coarse dirt", "hole", "water", "lava","empty patch vase"));
		items.add(new TileItem("Natural rock", (new Sprite(2, 0, 0)), "rock", "hole", "dirt", "sand", "grass", "path", "water", "lava"));
		items.add(new TileItem("Deepslate", (new Sprite(2, 0, 0)), "deepslate", "hole", "dirt", "sand", "grass", "path", "water", "lava"));
		items.add(new TileItem("Hard rock", (new Sprite(2, 0, 0)), "hard rock", "hole", "dirt", "sand", "grass", "path", "water", "lava","cloud","dark cloud"));
		items.add(new TileItem("Hard rock II", (new Sprite(2, 0, 0)), "hard rock II", "hole", "dirt", "sand", "grass", "path", "water", "lava","cloud","dark cloud"));

		items.add(new TileItem("Plank", (new Sprite(0, 5, 0)), "Wood Planks", "hole", "water", "cloud"));
		items.add(new TileItem("Plank wall", (new Sprite(1, 5, 0)), "Wood Wall", "Wood Planks"));
		items.add(new TileItem("Wood door", (new Sprite(2, 5, 0)), "Wood Door", "Wood Planks"));
		items.add(new TileItem("Stone brick", (new Sprite(3, 5, 0)), "Stone Bricks", "hole", "water", "cloud", "lava"));
		items.add(new TileItem("Stone wall", (new Sprite(4, 5, 0)), "Stone Wall", "Stone Bricks"));
		items.add(new TileItem("Stone door", (new Sprite(5, 5, 0)), "Stone Door", "Stone Bricks"));
		items.add(new TileItem("Obsidian brick", (new Sprite(6, 5, 0)), "Obsidian", "hole", "water", "cloud", "lava"));
		items.add(new TileItem("Obsidian wall", (new Sprite(7, 5, 0)), "Obsidian Wall", "Obsidian"));
		items.add(new TileItem("Obsidian door", (new Sprite(8, 5, 0)), "Obsidian Door", "Obsidian"));
		items.add(new TileItem("Dungeon brick", (new Sprite(9, 5, 0)),"Dungeon Bricks", "hole", "water", "cloud", "lava"));
		items.add(new TileItem("Dungeon wall", (new Sprite(10, 5, 0)), "Dungeon Wall", "Dungeon Bricks"));
		items.add(new TileItem("Dungeon door", (new Sprite(11, 5, 0)), "Dungeon Door", "Dungeon Bricks"));
		items.add(new TileItem("Ornate stone", (new Sprite(0, 25, 0)), "Ornate stone", "hole","water","lava","cloud","dark cloud"));
		items.add(new TileItem("Ornate obsidian", (new Sprite(2, 25, 0)), "Ornate obsidian", "hole","water","lava","cloud","dark cloud"));
		items.add(new TileItem("Rocky stone", (new Sprite(1, 25, 0)), "Rocky stone", "hole","water","lava","cloud","dark cloud"));
		items.add(new TileItem("Cross obsidian", (new Sprite(3, 25, 0)), "Decorated obsidian", "hole","water","lava","cloud","dark cloud"));
		items.add(new TileItem("Lapis block", (new Sprite(13, 5, 0)), "Lapis block", "dirt","sand","snow","grass","tallgrass","skygrass","coarse dirt","tallgrass","cloud","darkcloud","fungus"));
		items.add(new TileItem("Iron block", (new Sprite(12, 5, 0)), "Iron block", "dirt","sand","snow","grass","tallgrass","skygrass","coarse dirt","tallgrass","cloud","darkcloud","fungus"));
		items.add(new TileItem("Gold block", (new Sprite(14, 5, 0)), "Gold block", "dirt","sand","snow","grass","tallgrass","skygrass","coarse dirt","tallgrass","cloud","darkcloud","fungus"));
		items.add(new TileItem("Gem block", (new Sprite(15, 5, 0)), "Gem block", "dirt","sand","snow","grass","tallgrass","skygrass","coarse dirt","tallgrass","cloud","darkcloud","fungus"));
		items.add(new TileItem("Coal block", (new Sprite(16, 5, 0)), "Coal block", "dirt","sand","snow","grass","tallgrass","skygrass","coarse dirt","tallgrass","cloud","darkcloud","fungus"));
		items.add(new TileItem("Obsidium block", (new Sprite(17, 5, 0)), "Obsidium block", "dirt","sand","snow","grass","tallgrass","skygrass","coarse dirt","tallgrass","cloud","darkcloud","fungus"));

		//WOOLS
		items.add(new TileItem("Wool", (new Sprite(5, 3, 0)), "Wool", "hole", "water"));
		items.add(new TileItem("Red wool", (new Sprite(4, 3, 0)), "Red Wool", "hole", "water"));
		items.add(new TileItem("Blue wool", (new Sprite(3, 3, 0)), "Blue Wool", "hole", "water"));
		items.add(new TileItem("Green wool", (new Sprite(28, 3, 0)), "Green Wool", "hole", "water"));
		items.add(new TileItem("Yellow wool", (new Sprite(1, 3, 0)), "Yellow Wool", "hole", "water"));
		items.add(new TileItem("Black wool", (new Sprite(0, 3, 0)), "Black Wool", "hole", "water"));
		//add recipes to those
		items.add(new TileItem("Orange wool", (new Sprite(22, 3, 0)), "Orange Wool", "hole", "water"));
		items.add(new TileItem("Magenta wool", (new Sprite(25, 3, 0)), "Magenta Wool", "hole", "water"));
		items.add(new TileItem("Purple wool", (new Sprite(24, 3, 0)), "Purple Wool", "hole", "water"));
		items.add(new TileItem("Light blue wool", (new Sprite(29, 3, 0)), "Light Blue Wool", "hole", "water"));
		items.add(new TileItem("Lime wool", (new Sprite(2, 3, 0)), "Lime Wool", "hole", "water"));
		items.add(new TileItem("Cyan wool", (new Sprite(23, 3, 0)), "Cyan Wool", "hole", "water"));
		items.add(new TileItem("Light gray wool", (new Sprite(26, 3, 0)), "Light Gray Wool", "hole", "water"));
		items.add(new TileItem("Pink wool", (new Sprite(27, 3, 0)), "Pink Wool", "hole", "water"));
		items.add(new TileItem("Gray wool", (new Sprite(21, 3, 0)), "Gray Wool", "hole", "water"));

		items.add(new TileItem("Sand", (new Sprite(6, 3, 0)), "sand", "dirt","empty patch vase"));
		items.add(new TileItem("Cactus", (new Sprite(8, 3, 0)), "cactus Sapling", "sand","desert grass","empty patch vase"));
		items.add(new TileItem("Cloud cactus", (new Sprite(19, 4, 0)), "cloud cactus sapling", "cloud","empty patch vase"));
		items.add(new TileItem("Cloud", (new Sprite(10, 3, 0)), "cloud", "Infinite Fall","Empty patch vase","Aerocloud"));

		items.add(new TileItem("Wheat seeds", (new Sprite(3, 0, 0)), "wheat", "farmland"));
		items.add(new TileItem("Potato", (new Sprite(18, 0, 0)), "potato", "farmland"));
		items.add(new TileItem("Carrot", (new Sprite(20, 0, 0)), "carrot", "farmland"));
		items.add(new TileItem("Grass seeds", (new Sprite(3, 0, 0)), "grass", "dirt"));
		items.add(new TileItem("Dark cloud", (new Sprite(12, 3, 0)), "dark cloud", "Infinite Fall"));
		items.add(new TileItem("Reed", (new Sprite(6, 1, 0)), "reed", "water"));
		items.add(new TileItem("Moss layer", (new Sprite(17, 3, 0)), "moss", "dirt","empty patch vase"));
		items.add(new TileItem("IceT", (new Sprite(19, 3, 0)), "ice", "water"));
		items.add(new TileItem("Snow layer", (new Sprite(18, 3, 0)), "snow", "dirt"));
		items.add(new TileItem("Beetroot seeds", (new Sprite(3, 1, 0)), "beetroot", "farmland"));
		items.add(new TileItem("Cone", (new Sprite(20, 3, 0)), "Conifer sapling", "grass","tall grass"));
		items.add(new TileItem("Snow cone", (new Sprite(20, 3, 0)), "Snowy Conifer sapling", "snow"));
		items.add(new TileItem("Fern spores", (new Sprite(8, 1, 0)), "Fern spores", "grass"));
		items.add(new TileItem("Fungus spores", (new Sprite(11, 1, 0)), "Fungus spores", "Moss","Empty patch vase"));
		items.add(new TileItem("Patch vase", (new Sprite(0, 31, 0)), "Empty patch vase", "hole","water","Cloud"));
		return items;
	}
	
	public final String model;
	public final List<String> validTiles;
	
	protected TileItem(String name, Sprite sprite, String model, String... validTiles) {
		this(name, sprite, 1, model, Arrays.asList(validTiles));
	}
	protected TileItem(String name, Sprite sprite, int count, String model, String... validTiles) {
		this(name, sprite, count, model, Arrays.asList(validTiles));
	}
	public boolean displayBox(){return true;}
	protected TileItem(String name, Sprite sprite, int count, String model, List<String> validTiles) {
		super(name, sprite, count);
		this.model = model.toUpperCase();
		this.validTiles = new ArrayList<>();
		for (String tile: validTiles)
			 this.validTiles.add(tile.toUpperCase());
	}
	
	/*public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {

		for (String tilename : validTiles) {
			if (tile.matches(level.getData(xt, yt), tilename)) {
				if (getName() == "Lily pad") {
					level.setTile(xt, yt, model); //place lily
					level.setData(xt, yt, 3); //lily will have no flower
				}

				} else level.setTile(xt, yt, model); // TODO maybe data should be part of the saved tile..?
			}

			Sound.place.play();

			return super.interactOn(true, true);

		}
		
		if (Game.debug) System.out.println(model + " cannot be placed on " + tile.name);
		
		String note = "";
		if (model.contains("WALL")) {
			note = "Can only be placed on " + Tiles.getName(validTiles.get(0)) + "!";
		}
		else if (model.contains("DOOR")) {
			note = "Can only be placed on " + Tiles.getName(validTiles.get(0)) + "!";
		}
		else if ((model.contains("BRICK") || model.contains("PLANK"))) {
			note = "Dig a hole first!";
		}
		if (note.length() > 0) {
			if (!Game.isValidServer())
				Game.notifications.add(note);
			else
				Game.server.getAssociatedThread((RemotePlayer)player).sendNotification(note, 0);
		}
		
		return super.interactOn(false,true);
	}*/
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		int age=level.getData(xt,yt);
		int maxA=level.getTile(xt,yt)==Tiles.get("Wheat") || level.getTile(xt,yt)==Tiles.get("Carrot") || level.getTile(xt,yt)==Tiles.get("Reed") || level.getTile(xt,yt)==Tiles.get("Potato") || level.getTile(xt,yt)==Tiles.get("Beetroot") ? 80 : level.getTile(xt,yt)==Tiles.get("Cloud cactus sapling") ? 250 : level.getTile(xt,yt)==Tiles.get("oak sapling") || level.getTile(xt,yt)==Tiles.get("Birch sapling") || level.getTile(xt,yt)==Tiles.get("Cactus sapling") || level.getTile(xt,yt)==Tiles.get("Conifer sapling") || level.getTile(xt,yt)==Tiles.get("Snowy Conifer sapling") ? 100 : 1000;
		for (String tilename : validTiles) {
			if (tile.matches(level.getData(xt, yt), tilename)) {
				if(getName()=="Lily pad"){
					level.setTile(xt, yt, model); //place lily
					level.setData(xt,yt,3); //lily will have no flower
				}else if (level.getTile(xt, yt).name.contains("EMPTY PATCH VASE")) { //patch vases
					if (level.getData(xt, yt) == Tiles.get(getName()).id)
						return super.interactOn(false); //do not remove sand if you put sand soil into patch already
					if (level.getData(xt, yt) == 0) {//prevents from planting sapling on empty patch
						if (getName() != "Cloud Cactus")
							level.setData(xt, yt, Tiles.get(model.contains("MOSS") ? "MOSS" : model).id);
						return super.interactOn(true, true);
					} else {
						switch (getName()) {
							case "Cloud Cactus":
								if (level.getData(xt, yt) == Tiles.get("Cloud").id)
									level.setTile(xt, yt, Tiles.get("Patch cloud cactus sapling"));
								else return super.interactOn(false, true);
								break;
							case "Cactus":
								if (level.getData(xt, yt) == Tiles.get("Sand").id)
									level.setTile(xt, yt, Tiles.get("Patch cactus sapling"));
								else return super.interactOn(false, true);
								break;
							case "Fungus spores":
								if (level.getData(xt, yt) == Tiles.get("Moss").id)
									level.setTile(xt, yt, Tiles.get("Patch fungus spores"));
								else return super.interactOn(false, true);
								break;
							default:
								level.setData(xt, yt, Tiles.get(model).id);
								break;//we will mostly use it for filling up patch with a soil
						}
					}
				}else level.setTile(xt, yt, model); // TODO maybe data should be part of the saved tile..?

				Sound.place.play();

				return super.interactOn(true,true);
			}
		}

		if (Game.debug) System.out.println(model + " cannot be placed on " + tile.name);

		String note = "";
		if (model.contains("WALL")) {
			note = "Can only be placed on " + Tiles.getName(validTiles.get(0)) + "!";
		}
		else if (model.contains("DOOR")) {
			note = "Can only be placed on " + Tiles.getName(validTiles.get(0)) + "!";
		}
		else if ((model.contains("BRICK") || model.contains("PLANK"))) {
			note = "Dig a hole first!";
		}
		else if((model.contains("BONEMEAL"))) {
			note = "Only on plants or grass!";
		}
		if (note.length() > 0) {
			if (!Game.isValidServer())
				Game.notifications.add(note);
			else
				Game.server.getAssociatedThread((RemotePlayer)player).sendNotification(note, 0);
		}

		return super.interactOn(false);
	}
	
	@Override
	public boolean equals(Item other) {
		return super.equals(other) && model.equals(((TileItem)other).model);
	}
	
	@Override
	public int hashCode() { return super.hashCode() + model.hashCode(); }
	
	public TileItem clone() {
		return new TileItem(getName(), sprite, count, model, validTiles);
	}
}
