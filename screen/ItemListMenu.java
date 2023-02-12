package minicraft.screen;

import minicraft.screen.entry.ItemEntry;
import minicraft.gfx.Point;
import minicraft.screen.entry.ListEntry;

class ItemListMenu extends Menu
{

    static Menu.Builder getBuilder() {
		return new Menu.Builder(true, 0, RelPos.LEFT, 19,new ListEntry[0]).setPositioning(new Point(9, 9), RelPos.BOTTOM_RIGHT).setDisplayLength(9).setSelectable(true).setScrollPolicies(1.0f, false).setSearcherBar(true);
	}
	static Menu.Builder getBuilder(int bg) {
		return new Menu.Builder(true, 0, RelPos.LEFT, bg,new ListEntry[0]).setPositioning(new Point(9, 9), RelPos.BOTTOM_RIGHT).setDisplayLength(9).setSelectable(true).setScrollPolicies(1.0f, false).setSearcherBar(true);
	}

	protected ItemListMenu(final Builder b, final ItemEntry[] entries, final String title) {
		super(b.setEntries((ListEntry[])entries).setTitle(title).createMenu());
	}


	protected ItemListMenu(final ItemEntry[] entries, final String title) {
		this(getBuilder(), entries, title);
	}
	protected ItemListMenu(final ItemEntry[] entries, final String title,int bg) {
		this(getBuilder(bg), entries, title);
	}

}