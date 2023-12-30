package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.saveload.Save;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;

import static minicraft.core.Renderer.screen;

public class OptionsSound extends Display {

	public OptionsSound() {
		super(true, new Menu.Builder(true, 6, RelPos.CENTER,4,
				Settings.getEntry("sound"),
				new StringEntry("Specific sounds toggles", Color.ORANGE),
				Settings.getEntry("soundno"))
			//	Settings.getEntry("soundmob"),
			//	Settings.getEntry("soundambient"))
			.setTitle("Options - sound",Color.PINK)
			.createMenu()
		);


	}
	@Override
	public void render(Screen screen) {
		super.render(screen);
		Font.drawCentered("Note:", screen , Screen.h - 32, Color.RED);
		//Font.drawCentered("1. Ambience and mobs sounds", screen , Screen.h - 24, Color.CYAN);
		//Font.drawCentered("are planned for next updates", screen , Screen.h - 16, Color.CYAN);
		Font.drawCentered("1. First option affects all sounds", screen , Screen.h - 8, Color.RED);
	}
	@Override
	public void onExit() {
		Localization.changeLanguage((String)Settings.get("language"));
		new Save();
		Game.MAX_FPS = (int)Settings.get("fps");
	}


}
