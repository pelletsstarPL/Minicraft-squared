package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.gfx.Color;
import minicraft.saveload.Save;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;

public class OptionsWorldDisplay extends Display {

	public OptionsWorldDisplay() {
		super(true, new Menu.Builder(true, 6, RelPos.LEFT,1,
				(Game.isMode("hardcore") ? new StringEntry("Difficulty: Hard", Color.RED) : Settings.getEntry("diff")),
				Settings.getEntry("fps"),
				new SelectEntry("Sound", () -> Game.setMenu(new OptionsSound())),
				new SelectEntry("Shortened potion display", () -> Game.setMenu(new OptionsPotions())),
				Settings.getEntry("autosave"),
				Settings.getEntry("statdisplay"),
				Settings.getEntry("coloredgui"),

				((Save.AirWizard2Beaten) ? Settings.getEntry("skinon") : new BlankEntry()),
				new SelectEntry("Change Key Bindings", () -> Game.setMenu(new KeyInputDisplay())))
				//Settings.getEntry("language"),
				//new SelectEntry("Change your skin", () -> Game.setMenu(new SkinDisplay())))
			.setTitle("Options - World")
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
