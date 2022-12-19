package minicraft.screen.entry;

import java.util.List;

import minicraft.core.Updater;
import minicraft.core.io.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.item.Recipe;
import minicraft.screen.Menu;

public class RecipeEntry extends ItemEntry {
	int txtLen=12;
	int txtDispMv=0,tCnt=0;
	char[] ch = new char[txtLen];
	public static RecipeEntry[] useRecipes(List<Recipe> recipes) {
		RecipeEntry[] entries = new RecipeEntry[recipes.size()];
		for (int i = 0; i < recipes.size(); i++)
			entries[i] = new RecipeEntry(recipes.get(i));
		return entries;
	}
	
	private Recipe recipe;
	
	public RecipeEntry(Recipe r) {
		super(r.getProduct());
		this.recipe = r;
	}
	
	@Override
	public void tick(InputHandler input) {
		String txt = trueToString();
		super.tick(input);

		if(!Menu.searcherBarActive) {
			if (tCnt % 20 == 0) txtDispMv++;
			ch = new char[txtLen];
			if (txt.length() > txtLen)
				for (int i = 0; i < txtLen; i++) {
					ch[i] = txt.charAt((i + txtDispMv) % txt.length());
				}
			tCnt++;
			String CH = String.valueOf(ch);
		}
		//System.out.println(tCnt + CH);
	}

	
	@Override
	public void render(Screen screen, int x, int y, boolean isSelected) {

		String txt=trueToString();
		if (isVisible()) {
			if(isSelected ){
				String CH = String.valueOf(ch);
				//Font.draw(toString().substring(0,txt.length()<11 ? txt.length() : 10), screen, x, y, recipe.getCanCraft() ? Color.GREEN : Color.get(1,197,78,87));
				Font.draw((txt.length() < txtLen+1 || Menu.searcherBarActive ? "" : " ") + (txt.length()<txtLen+1  ? txt : (Menu.searcherBarActive ? txt.substring(0,txt.length()<txtLen+2 ? txt.length() : txtLen+1) : CH)), screen, x - 8, y, recipe.getCanCraft() ? Color.GREEN : Color.get(1,197,78,87));
			}else
			Font.draw(txt.substring(0,txt.length()<txtLen+2 ? txt.length() : txtLen+1), screen, x - 8, y, recipe.getCanCraft() ? COL_SLCT : COL_UNSLCT);
			getItem().sprite.render(screen, x - 8, y);
		}
	}
	
	@Override
	public String toString() { //basically debug for box width
		return trueToString().substring(0,trueToString().length()<txtLen ? trueToString().length() : txtLen-1);
	}
	public String trueToString() {
		return " " + recipe.getProduct().getName() + (recipe.getAmount() > 1 ? " X" + recipe.getAmount() : "") + (recipe.getCosts().containsKey("CHARCOAL") ? "(CH.)" : "");
	}
}
