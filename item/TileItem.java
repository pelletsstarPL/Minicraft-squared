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
		items.add(new TileItem("Rose", (new Sprite(5, 0, 0)), "rose", "grass","tall grass"));
		items.add(new TileItem("Azalea", (new Sprite(5, 2, 0)), "azalea", "moss"));
		items.add(new TileItem("Small rose", (new Sprite(5, 1, 0)), "small rose", "grass","tall grass"));
		items.add(new TileItem("Small flower", (new Sprite(4, 2, 0)), "small flower", "grass","tall grass"));
		items.add(new TileItem("Sunflower", (new Sprite(4, 1, 0)), "sunflower", "grass","tall grass"));
		items.add(new TileItem("Acorn", (new Sprite(7, 3, 0)), "tree Sapling", "grass","tall grass"));
		items.add(new TileItem("Catkin", (new Sprite(13, 3, 0)), "birch Sapling", "grass","tall grass"));
		items.add(new TileItem("Dirt", (new Sprite(0, 0, 0)), "dirt", "hole", "water", "lava"));
		items.add(new TileItem("Coarse Dirt", (new Sprite(0, 1, 0)), "coarse dirt", "hole", "water", "lava"));
		items.add(new TileItem("Natural Rock", (new Sprite(2, 0, 0)), "rock", "hole", "dirt", "sand", "grass", "path", "water", "lava"));
		items.add(new TileItem("Deepslate", (new Sprite(2, 0, 0)), "deepslate", "hole", "dirt", "sand", "grass", "path", "water", "lava"));
		items.add(new TileItem("Hard rock", (new Sprite(2, 0, 0)), "hard rock", "hole", "dirt", "sand", "grass", "path", "water", "lava","cloud","dark cloud"));
		items.add(new TileItem("Hard Rock II", (new Sprite(2, 0, 0)), "hard rock II", "hole", "dirt", "sand", "grass", "path", "water", "lava","cloud","dark cloud"));

		items.add(new TileItem("Plank", (new Sprite(0, 5, 0)), "Wood Planks", "hole", "water", "cloud"));
		items.add(new TileItem("Plank Wall", (new Sprite(1, 5, 0)), "Wood Wall", "Wood Planks"));
		items.add(new TileItem("Wood Door", (new Sprite(2, 5, 0)), "Wood Door", "Wood Planks"));
		items.add(new TileItem("Stone Brick", (new Sprite(3, 5, 0)), "Stone Bricks", "hole", "water", "cloud", "lava"));
		items.add(new TileItem("Stone Wall", (new Sprite(4, 5, 0)), "Stone Wall", "Stone Bricks"));
		items.add(new TileItem("Stone Door", (new Sprite(5, 5, 0)), "Stone Door", "Stone Bricks"));
		items.add(new TileItem("Obsidian Brick", (new Sprite(6, 5, 0)), "Obsidian", "hole", "water", "cloud", "lava"));
		items.add(new TileItem("Obsidian Wall", (new Sprite(7, 5, 0)), "Obsidian Wall", "Obsidian"));
		items.add(new TileItem("Obsidian Door", (new Sprite(8, 5, 0)), "Obsidian Door", "Obsidian"));
		items.add(new TileItem("Dungeon Brick", (new Sprite(9, 5, 0)),"Dungeon Bricks", "hole", "water", "cloud", "lava"));
		items.add(new TileItem("Dungeon Wall", (new Sprite(10, 5, 0)), "Dungeon Wall", "Dungeon Bricks"));
		items.add(new TileItem("Dungeon Door", (new Sprite(11, 5, 0)), "Dungeon Door", "Dungeon Bricks"));
		items.add(new TileItem("Ornate stone", (new Sprite(0, 25, 0)), "Ornate stone", "hole","water","lava","cloud","dark cloud"));
		items.add(new TileItem("Tile obsidian", (new Sprite(2, 25, 0)), "Ornate obsidian", "hole","water","lava","cloud","dark cloud"));
		items.add(new TileItem("Rocky stone", (new Sprite(1, 25, 0)), "Rocky stone", "hole","water","lava","cloud","dark cloud"));
		items.add(new TileItem("Cross obsidian", (new Sprite(3, 25, 0)), "Decorated obsidian", "hole","water","lava","cloud","dark cloud"));
		items.add(new TileItem("Lapis Block", (new Sprite(13, 5, 0)), "Lapis block", "dirt","sand","snow","grass","tallgrass","skygrass","coarse dirt","tallgrass","cloud","darkcloud"));
		items.add(new TileItem("Iron Block", (new Sprite(12, 5, 0)), "Iron block", "dirt","sand","snow","grass","tallgrass","skygrass","coarse dirt","tallgrass","cloud","darkcloud"));
		items.add(new TileItem("Gold Block", (new Sprite(14, 5, 0)), "Gold block", "dirt","sand","snow","grass","tallgrass","skygrass","coarse dirt","tallgrass","cloud","darkcloud"));

		//WOOLS
		items.add(new TileItem("Wool", (new Sprite(5, 3, 0)), "Wool", "hole", "water"));
		items.add(new TileItem("Red Wool", (new Sprite(4, 3, 0)), "Red Wool", "hole", "water"));
		items.add(new TileItem("Blue Wool", (new Sprite(3, 3, 0)), "Blue Wool", "hole", "water"));
		items.add(new TileItem("Green Wool", (new Sprite(28, 3, 0)), "Green Wool", "hole", "water"));
		items.add(new TileItem("Yellow Wool", (new Sprite(1, 3, 0)), "Yellow Wool", "hole", "water"));
		items.add(new TileItem("Black Wool", (new Sprite(0, 3, 0)), "Black Wool", "hole", "water"));
		//add recipes to those
		items.add(new TileItem("Orange Wool", (new Sprite(22, 3, 0)), "Orange Wool", "hole", "water"));
		items.add(new TileItem("Magenta Wool", (new Sprite(25, 3, 0)), "Magenta Wool", "hole", "water"));
		items.add(new TileItem("Purple Wool", (new Sprite(24, 3, 0)), "Purple Wool", "hole", "water"));
		items.add(new TileItem("Light Blue Wool", (new Sprite(29, 3, 0)), "Light Blue Wool", "hole", "water"));
		items.add(new TileItem("Lime Wool", (new Sprite(2, 3, 0)), "Lime Wool", "hole", "water"));
		items.add(new TileItem("Cyan Wool", (new Sprite(23, 3, 0)), "Cyan Wool", "hole", "water"));
		items.add(new TileItem("Light Gray Wool", (new Sprite(26, 3, 0)), "Light Gray Wool", "hole", "water"));
		items.add(new TileItem("Pink Wool", (new Sprite(27, 3, 0)), "Pink Wool", "hole", "water"));
		items.add(new TileItem("Gray Wool", (new Sprite(21, 3, 0)), "Gray Wool", "hole", "water"));

		items.add(new TileItem("Sand", (new Sprite(6, 3, 0)), "sand", "dirt"));
		items.add(new TileItem("Cactus", (new Sprite(8, 3, 0)), "cactus Sapling", "sand","desert grass"));
		items.add(new TileItem("Cloud Cactus", (new Sprite(19, 4, 0)), "cloud cactus Sapling", "cloud"));
		items.add(new TileItem("Bone", (new Sprite(9, 3, 0)), "tree", "tree Sapling"));
		items.add(new TileItem("Cloud", (new Sprite(10, 3, 0)), "cloud", "Infinite Fall"));

		items.add(new TileItem("Wheat Seeds", (new Sprite(3, 0, 0)), "wheat", "farmland"));
		items.add(new TileItem("Potato", (new Sprite(18, 0, 0)), "potato", "farmland"));
		items.add(new TileItem("Carrot", (new Sprite(20, 0, 0)), "carrot", "farmland"));
		items.add(new TileItem("Grass Seeds", (new Sprite(3, 0, 0)), "grass", "dirt"));
		items.add(new TileItem("Dark Cloud", (new Sprite(12, 3, 0)), "dark cloud", "Infinite Fall"));
		items.add(new TileItem("Reed", (new Sprite(6, 1, 0)), "reed", "water"));
		items.add(new TileItem("Moss layer", (new Sprite(17, 3, 0)), "moss", "dirt"));
		items.add(new TileItem("IceT", (new Sprite(19, 3, 0)), "ice", "water"));
		items.add(new TileItem("Snow layer", (new Sprite(18, 3, 0)), "snow", "dirt"));
		items.add(new TileItem("Beetroot Seeds", (new Sprite(3, 1, 0)), "beetroot", "farmland"));
		items.add(new TileItem("Cone", (new Sprite(20, 3, 0)), "Conifer sapling", "grass","tall grass"));
		items.add(new TileItem("Snow Cone", (new Sprite(20, 3, 0)), "Snowy Conifer sapling", "snow"));
		items.add(new TileItem("Bonemeal", (new Sprite(18, 4, 0)), "Snowy Conifer sapling", "wheat","potato","carrot","beetroot","reed","cactus sapling","tree sapling","conifer sapling","birch sapling","cloud cactus sapling"));
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
	protected TileItem(String name, Sprite sprite, int count, String model, List<String> validTiles) {
		super(name, sprite, count);
		this.model = model.toUpperCase();
		this.validTiles = new ArrayList<>();
		for (String tile: validTiles)
			 this.validTiles.add(tile.toUpperCase());
	}
	
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		int age=level.getData(xt,yt);
		int maxA=level.getTile(xt,yt)==Tiles.get("Wheat") || level.getTile(xt,yt)==Tiles.get("Carrot") || level.getTile(xt,yt)==Tiles.get("Reed") || level.getTile(xt,yt)==Tiles.get("Potato") || level.getTile(xt,yt)==Tiles.get("Beetroot") ? 80 : level.getTile(xt,yt)==Tiles.get("Cloud cactus sapling") ? 250 : level.getTile(xt,yt)==Tiles.get("Tree sapling") || level.getTile(xt,yt)==Tiles.get("Birch sapling") || level.getTile(xt,yt)==Tiles.get("Cactus sapling") || level.getTile(xt,yt)==Tiles.get("Conifer sapling") || level.getTile(xt,yt)==Tiles.get("Snowy Conifer sapling") ? 100 : 1000;
		for (String tilename : validTiles) {
			if (tile.matches(level.getData(xt, yt), tilename)) {;
				if(getName()=="Bonemeal") {


					if(age< maxA) {
						for (int i = 0; i < 3; i++) {
							int randX = (int) Math.ceil(Math.random() * 12) - 4;
							int randY = (int) Math.ceil(Math.random() * 12) - 4;
							level.add(new GreenStarParticle(xt * 16 + randX, yt * 16 + randY));
						}
						level.setData(xt, yt, age + 40 >= maxA ? maxA : age+40);
						return super.interactOn(true);
					}else{
						return super.interactOn(false);
					}
				}else level.setTile(xt, yt, model); // TODO maybe data should be part of the saved tile..?

				Sound.place.play();

				return super.interactOn(true);
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
			note = "Only on plants!";
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
