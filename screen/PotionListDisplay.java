package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.Updater;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;
import minicraft.item.PotionType;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.ListEntry;

import java.util.ArrayList;
import java.util.Map;

public class PotionListDisplay extends Display {

	public PotionListDisplay() {
		
		ArrayList<ListEntry> entries = new ArrayList<>();

		for(int i=0;i< getEffects().length;i++)entries.add(new BlankEntry());
		
		menus = new Menu[] {
			new Menu.Builder(true, 6, RelPos.CENTER, entries)
				.setTitle("Potion effects list", Color.YELLOW)
				.createMenu()
		};
	}
	private Map.Entry[] getEffects(){
		return Renderer.player.potionEffects.entrySet().toArray(new Map.Entry[0]);
	}


	@Override
	public void init(Display parent) {
		super.init(null); // ignore; pause menus always lead back to the game
	}
	
	@Override
	public void tick(InputHandler input) {
		super.tick(input);
		if (input.getKey("pause").clicked || input.getKey("potionfulllist").clicked || input.getKey("menu").clicked)
			Game.exitMenu();
	}
	@Override
	public void render(Screen screen) {
		super.render(screen);
		Map.Entry<PotionType, Integer>[] effects = getEffects();
		for(int i=0;i< effects.length;i++) {
			int height=- effects.length * 4 + (Renderer.HEIGHT / 2)  + (i*9);
			PotionType pType = effects[i].getKey();
			int pTime = effects[i].getValue() / Updater.normSpeed;
			pType.icon.render(screen,Renderer.WIDTH/2 - (pType.name.length() * 5),height);
			Font.drawCentered(pType.name() + " (" + (pTime / 60) + ":" + ((pTime % 60 < 10) ? "0" + (pTime % 60) : (pTime % 60)) + ")", screen,height, pType.dispColor);
		}
	}
}
