package minicraft.entity.furniture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import minicraft.core.Game;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.item.Recipe;
import minicraft.item.Recipes;
import minicraft.screen.CraftingDisplay;

import static minicraft.core.Game.input;
import static minicraft.core.Game.player;

public class Crafter extends Furniture {
	public static  int index;
	public enum Type {
		Workbench (new Sprite(16, 30, 2, 2, 2), 3, 2, new ArrayList[]{Recipes.workbenchRecipes, Recipes.craftRecipes}),
		Stonecutter (new Sprite(34, 30, 2, 2, 2), 3, 2, Recipes.stonecutterRecipes),
		Oven (new Sprite(12, 30, 2, 2, 2), 3, 2, Recipes.ovenRecipes),
		Furnace (new Sprite(14, 30, 2, 2, 2), 3, 2, Recipes.furnaceRecipes),
		Anvil (new Sprite(8, 30, 2, 2, 2), 3, 2, Recipes.anvilRecipes),
		Enchanter (new Sprite(26, 30, 2, 2, 2), 7, 2, Recipes.enchantRecipes),
		Shardforge (new Sprite(10, 32, 2, 2, 2), 7, 2, new ArrayList[]{Recipes.shardForgeRecipes, Recipes.anvilRecipes}),
		Loom (new Sprite(28, 30, 2, 2, 2), 7, 2, Recipes.loomRecipes);
		
		public ArrayList<Recipe> recipes;
		public ArrayList<Recipe>[] rEcipes;
		protected Sprite sprite;
		protected int xr, yr;
		
		
		Type(Sprite sprite, int xr, int yr, ArrayList<Recipe> list) {
			this.sprite = sprite;
			this.xr = xr;
			this.yr = yr;
			recipes = list;
			Crafter.names.add(this.name());
		}
		Type(Sprite sprite, int xr, int yr, ArrayList<Recipe>[] list) {
			this.sprite = sprite;
			this.xr = xr;
			this.yr = yr;
			rEcipes = list;
			Crafter.names.add(this.name());
		}
	}
	public static ArrayList<String> names = new ArrayList<>();
	
	public Crafter.Type type;
	
	/**
	 * Creates a crafter of a given type.
	 * @param type What type of crafter this is.
	 */
	public Crafter(Crafter.Type type) {
		super(type.name(), type.sprite, type.xr, type.yr);
		this.type = type;
	}

	public boolean use(Player player) {

		if(type.rEcipes!=null) Game.setMenu(new CraftingDisplay(type.rEcipes, type.name(), player,true,(byte)0));
		else  Game.setMenu(new CraftingDisplay(type.recipes, type.name(), player));

		return true;
	}
	
	@Override
	public Furniture clone() {
		return new Crafter(type);
	}
	
	@Override
	public String toString() { return type.name()+getDataPrints(); }

}
