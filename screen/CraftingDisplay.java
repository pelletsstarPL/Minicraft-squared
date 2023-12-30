package minicraft.screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.gfx.*;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Sound;
import minicraft.entity.mob.Player;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.Recipe;
import minicraft.screen.entry.ItemListing;
import minicraft.screen.entry.StringEntry;


public class CraftingDisplay extends Display {
	
	private  Player player;
	private Recipe[] recipes;
	private List<Recipe>[] rEcipes;
	private int  recipesLen;

	private RecipeMenu recipeMenu;
	private   String title;
	private int index;
	private Menu.Builder itemCountMenu, costsMenu,scrollMenu;

	private boolean isPersonalCrafter;

	public CraftingDisplay(List<Recipe> recipes, String title, Player player) { this(recipes, title, player, false); }
	public CraftingDisplay(List<Recipe> recipes, String title, Player player, boolean isPersonal) {
		for(int i = 0; i < recipes.size(); ++i )
			recipes.get(i).checkCanCraft(player);

	//	System.out.println(recipes);
		this.title = title;
		this.isPersonalCrafter = isPersonal;

		recipeMenu = new RecipeMenu(recipes, title, player);
		
		this.player = player;
		this.recipes = recipes.toArray(new Recipe[recipes.size()]);
		
		itemCountMenu = new Menu.Builder(true, 0, RelPos.LEFT,3)
			.setTitle("Have:",Color.ORANGE)
			.setTitlePos(RelPos.TOP_LEFT)
			.setPositioning(new Point(recipeMenu.getBounds().getRight()+SpriteSheet.boxWidth, recipeMenu.getBounds().getTop()), RelPos.BOTTOM_RIGHT);
		
		costsMenu = new Menu.Builder(true, 0, RelPos.LEFT,2)
			.setTitle("Cost:",Color.ORANGE)
			.setTitlePos(RelPos.TOP_LEFT)
			.setPositioning(new Point(itemCountMenu.createMenu().getBounds().getLeft(), recipeMenu.getBounds().getBottom()), RelPos.TOP_RIGHT);
		
		menus = new Menu[] {recipeMenu, itemCountMenu.createMenu(), costsMenu.createMenu()};
		
		refreshData();
	}
	public CraftingDisplay(List<Recipe>[] recipes, String title, Player player, boolean isPersonal,byte id) {


		this.title = title;
			for (int j = 0; j < recipes[id].size() ; j++) //we only check current sublist
				recipes[id].get(j).checkCanCraft(player);

		this.isPersonalCrafter = isPersonal;
		recipeMenu = new RecipeMenu(recipes[id], title, player);

		this.recipes = recipes[id].toArray(new Recipe[recipes[id].size()]);

		this.player = player;
		this.rEcipes = recipes;
		this.recipes = recipes[id].toArray(new Recipe[recipes[id].size()]);
		this.recipesLen = recipes.length;

				itemCountMenu = new Menu.Builder(true, 0, RelPos.LEFT,3)
				.setTitle("Have:",Color.ORANGE)
				.setTitlePos(RelPos.TOP_LEFT)
				.setPositioning(new Point(recipeMenu.getBounds().getRight()+SpriteSheet.boxWidth, recipeMenu.getBounds().getTop()), RelPos.BOTTOM_RIGHT);

		costsMenu = new Menu.Builder(true, 0, RelPos.LEFT,2)
				.setTitle("Cost:",Color.ORANGE)
				.setTitlePos(RelPos.TOP_LEFT)
				.setPositioning(new Point(itemCountMenu.createMenu().getBounds().getLeft() , recipeMenu.getBounds().getBottom()), RelPos.TOP_RIGHT);
		scrollMenu = new Menu.Builder(true, 0, RelPos.LEFT,1,
				new StringEntry((id!=0 && id<=recipes.length-1 ? "< " : "  ")+" | " +  (id>=0 && id<recipes.length-1 ? " >" : "  "))
				).setTitlePos(RelPos.TOP_RIGHT).setTitle("SCROLL")
				.setPositioning(new Point(recipeMenu.getBounds().getRight()+SpriteSheet.boxWidth + 64, recipeMenu.getBounds().getTop()), RelPos.BOTTOM_RIGHT);

		menus = new Menu[] {recipeMenu, itemCountMenu.createMenu(), costsMenu.createMenu(), scrollMenu.createMenu()};

		refreshData();
	}
	private void refreshData() {
		Menu prev = menus[2];

		menus[2] = costsMenu
			.setEntries(getCurItemCosts())
			.createMenu();
		menus[2].setColors(prev);
		
		menus[1] = itemCountMenu
			.setEntries(new ItemListing(recipes[recipeMenu.getSelection()].getProduct(), String.valueOf(getCurItemCount())))
			.createMenu();
		menus[1].setColors(prev);

	}
	
	private int getCurItemCount() {
		return player.getInventory().count(recipes[recipeMenu.getSelection()].getProduct());
	}
	
	private ItemListing[] getCurItemCosts() {
		ArrayList<ItemListing> costList = new ArrayList<>();
		HashMap<String, Integer> costMap = recipes[recipeMenu.getSelection()].getCosts();

		//System.out.println(costMap.getClass());

		for(String itemName: costMap.keySet()) {
			Item cost = Items.get(itemName);
			 costList.add(new ItemListing(cost, player.getInventory().count(cost) + "/" + costMap.get(itemName)));
		}
		
		return costList.toArray(new ItemListing[costList.size()]);
	}

	@Override
	public void tick(InputHandler input) {
		String title = this.title;
		if(this.recipesLen>0) {
			if (input.getKey("move-right").clicked) {
				index = (index >= this.recipesLen - 1 ? this.recipesLen - 1 : index + 1);
				Game.setMenu(new CraftingDisplay( rEcipes,title,player,isPersonalCrafter,(byte)index));
				return;
			}
			if (input.getKey("move-left").clicked) {
				index = (index <= 0 ? 0 : index - 1);
				Game.exitMenu();
				Game.setMenu(new CraftingDisplay(rEcipes,title,player,isPersonalCrafter,(byte)index));
				return;
			}
		}
		int previousSelection = recipeMenu.getSelection();
		super.tick(input);
		if (previousSelection != recipeMenu.getSelection())
			refreshData();


		if (input.getKey("exit").clicked  ||  input.getKey("menu").clicked || (isPersonalCrafter && input.getKey("craft").clicked)) {
			Game.setMenu(null);
			return;
		}
		
		if ((input.getKey("select").clicked || input.getKey("attack").clicked) && recipeMenu.getSelection() >= 0) {
			// check the selected recipe
			Recipe selectedRecipe = recipes[recipeMenu.getSelection()];
			if (selectedRecipe.getCanCraft()) {
				selectedRecipe.craft(player);

				Sound.craft.play();

				refreshData();
				for (Recipe recipe: recipes) {
					recipe.checkCanCraft(player);
				}
			}
		}
	}
	public void render(final Screen screen) {
		super.render(screen);
		String text = "(" + Game.input.getMapping("SEARCHER-BAR") + ") " + Localization.getLocalized("to search.");
		Font.draw(text, screen, 24 - text.length(), 112, Color.WHITE);
		if(this.recipesLen>0) {
			text = Game.input.getMapping("move-left") + "," + Game.input.getMapping("move-right") + " to scroll";
			Font.draw(text, screen, 24 - text.length(), 120, Color.WHITE);
		}
	}
}
