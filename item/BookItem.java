package minicraft.item;

import java.util.ArrayList;

import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.screen.BookData;
import minicraft.screen.BookDisplay;
import minicraft.item.PotionType;

public class BookItem extends Item {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<Item>();
		items.add(new BookItem("Book", new Sprite(0, 8, 0), null,false,9));
		items.add(new BookItem("Antidious", new Sprite(1, 8, 0), BookData.antVenomBook, true,2));
		items.add(new BookItem("11th", new Sprite(2, 8, 0), BookData.eleventh, true,9));
		items.add(new BookItem("The hot day", new Sprite(4, 8, 0), BookData.hotday, true,11));
		items.add(new BookItem("Demonicolon", new Sprite(3, 8, 0), BookData.demonicolon, true,10));
		return items;
	}
	
	protected String book; // TODO this is not saved yet; it could be, for editable books.
	private final boolean hasTitlePage;
	private final int bg;
	private Sprite sprite;
	
	private BookItem(String title, Sprite sprite, String book) { this(title, sprite, book, false); }
	private BookItem(String title, Sprite sprite, String book, boolean hasTitlePage) {
		super(title,sprite);
		this.book = book;
		this.hasTitlePage = hasTitlePage;
		this.sprite = sprite;
		this.bg = 0;
	}
	private BookItem(String title, Sprite sprite, String book, boolean hasTitlePage,int background) {
		super(title, sprite);
		this.book = book;
		this.hasTitlePage = hasTitlePage;
		this.sprite = sprite;
		this.bg = background;
	}
	
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		if(player.potionEffects.containsKey(PotionType.Blind) && !(Game.isMode("Creative")) && !(player.potionEffects.containsKey(PotionType.Light)))Game.notifications.add("Can't read while blind");
			else Game.setMenu(new BookDisplay(book, hasTitlePage,bg));
		return true;
	}
	
	@Override
	public boolean interactsWithWorld() { return false; }
	
	public BookItem clone() {
		return new BookItem(getName(), sprite, book, hasTitlePage,bg);
	}
}
