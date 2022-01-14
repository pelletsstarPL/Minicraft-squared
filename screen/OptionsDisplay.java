package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.saveload.Save;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;

public class OptionsDisplay extends Display {
	public OptionsDisplay() {
		super(true, new Menu.Builder(false, 6, RelPos.LEFT,
				Settings.getEntry("diff"),
				Settings.getEntry("fps"),
				Settings.getEntry("sound"),
				Settings.getEntry("autosave"),
				Settings.getEntry("skinon"),
				new SelectEntry("Change Key Bindings", () -> Game.setMenu(new KeyInputDisplay())),
				//Settings.getEntry("language"),
				//Settings.getEntry("textures") // old, If you want you can activate it, it does not affect the texture pack system, but it would not make much sense
				new SelectEntry("Texture packs", () -> Game.setMenu(new TexturePackDisplay())), // New texture packs system
				Settings.getEntry("coloredgui"),
				new StringEntry("-------------------------",Color.YELLOW)
			)
			.setTitle("-------- Options --------",Color.YELLOW)
			.createMenu()
		);
	}

	@Override
	public void onExit() {
		Localization.changeLanguage((String)Settings.get("language"));
		new Save();
		Game.MAX_FPS = (int)Settings.get("fps");
	}
}
