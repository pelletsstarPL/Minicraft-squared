package minicraft.screen.entry;

import java.util.List;
import java.util.Objects;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Sprite;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.screen.Menu;
import minicraft.screen.PlayerInvDisplay;
import org.jetbrains.annotations.Nullable;

public class ItemEntry extends ListEntry {
	int txtLen=12;
	int txtDispMv=0,tCnt=0;
	char[] ch = new char[txtLen];
	public static ItemEntry[] useItems(List<Item> items) {
		ItemEntry[] entries = new ItemEntry[items.size()];
		for (int i = 0; i < items.size(); i++)
			entries[i] = new ItemEntry(items.get(i));
		return entries;
	}
	
	private Item item;
	
	public ItemEntry(Item i) { this.item = i; }
	
	public Item getItem() { return item; }

	@Override
	public void tick(InputHandler input) {
		String curMenu = Objects.isNull(Game.getMenu()) ? "null" : Game.getMenu().toString();
		if(curMenu.contains("PlayerInv") || curMenu.contains("Container")) {
			String txt = trueToString();


			if (!Menu.searcherBarActive) {
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
	}


	public void render(Screen screen, int x, int y, boolean isSelected) {

		String curMenu = Objects.isNull(Game.getMenu()) ? "null" : Game.getMenu().toString();
		if(curMenu.contains("PlayerInv") || curMenu.contains("Container")) {
			x-=8;
			String txt = trueToString();
			if (isSelected) {
				String CH = String.valueOf(ch);
				//Font.draw(toString().substring(0,txt.length()<11 ? txt.length() : 10), screen, x, y, recipe.getCanCraft() ? Color.GREEN : Color.get(1,197,78,87));
				Font.draw((txt.length() < txtLen+2 || Menu.searcherBarActive ? "" : " ") + (txt.length() < txtLen+2 ? txt : (Menu.searcherBarActive ? toString() : CH)), screen, x, y, Color.ORANGE);
			} else
				Font.draw(txt.substring(0, txt.length() < txtLen+2 ? txt.length() : txtLen+1), screen, x, y, COL_SLCT);
			getItem().sprite.render(screen, x, y);
		}else{
		super.render(screen, x, y, true);
			getItem().sprite.render(screen, x, y);}
	}

	
	//to assign length of boxes for virtual display
	@Override
	public String toString() {return trueToString().substring(0,trueToString().length()<txtLen ? trueToString().length() : txtLen-1);}

	// If you add to the length of the string, and therefore the width of the entry, then it will actually move the entry RIGHT in the inventory, instead of the intended left, because it is auto-positioned to the left side.
	public String trueToString() {
		return item.getDisplayName();
	}
}
