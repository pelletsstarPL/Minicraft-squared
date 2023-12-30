package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.saveload.Save;
import minicraft.screen.entry.StringEntry;

public class OptionsPotions extends Display {

	public OptionsPotions() {
		super(true, new Menu.Builder(true, 6, RelPos.CENTER,7,
				Settings.getEntry("potionsn"),
				Settings.getEntry("potiontxtlen"),
				Settings.getEntry("displayside"),
				Settings.getEntry("displayicon"))
			.setTitle("Options - potion display",Color.GRAY)
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
