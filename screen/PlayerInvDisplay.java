package minicraft.screen;

import minicraft.gfx.Font;
import minicraft.gfx.Color;
import minicraft.core.io.Localization;
import minicraft.gfx.Screen;
import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.entity.mob.Player;

public class PlayerInvDisplay extends Display {
	
	private Player player;
	
	public PlayerInvDisplay(Player player) {
		super(new InventoryMenu(player, player.getInventory(), "Inventory",4));
		this.player = player;
	}
	
	@Override
	public void tick(InputHandler input) {
		super.tick(input);

		if(input.getKey("menu").clicked) {
			Game.exitMenu();
			return;
		}
		
		if(input.getKey("attack").clicked && menus[0].getNumOptions() > 0) {
			player.activeItem = player.getInventory().remove(menus[0].getSelection());
			Game.exitMenu();
		}
	}
	public void render(final Screen screen) {
		super.render(screen);
		final String text = "(" + Game.input.getMapping("SEARCHER-BAR") + ") " + Localization.getLocalized("to search.");
		Font.draw(text, screen, 40 - text.length(), 100, Color.WHITE);
	}
}
