package minicraft.screen.entry;

import java.util.List;

import minicraft.core.io.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.screen.Menu;

public class ItemEntry extends ListEntry {

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
	public void tick(InputHandler input) {}


	public void render(Screen screen, int x, int y, boolean isSelected) {
		super.render(screen, x, y, true);
			getItem().sprite.render(screen, x, y);
	}

	
	// If you add to the length of the string, and therefore the width of the entry, then it will actually move the entry RIGHT in the inventory, instead of the intended left, because it is auto-positioned to the left side.

	public String toString2() {
		return toString2().substring(0,toString2().length()<12 ? toString2().length() : 11);
	}
	@Override
	public String toString() {
		return item.getDisplayName();
	}
}
