package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.Initializer;
import minicraft.core.Renderer;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.saveload.Save;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;

public class OptionsMainMenuDisplay extends Display {

    public OptionsMainMenuDisplay() {
        super(true);

        Menu optionsMenu = new Menu.Builder(true, 6, RelPos.LEFT,21,
            Settings.getEntry("fps"),
                new SelectEntry("Sound", () -> Game.setMenu(new OptionsSound())),
            Settings.getEntry("statdisplay"),
                Settings.getEntry("coloredgui"),
            new SelectEntry("Change Key Bindings", () -> Game.setMenu(new KeyInputDisplay())),
            //Settings.getEntry("language"),
            new SelectEntry("Resource packs", () -> Game.setMenu(new TexturePackDisplay())))
            .setTitle("Options - Main Menu")
            .createMenu();

        menus = new Menu[]{
            optionsMenu
        };
    }

    @Override
    public void onExit() {
        Localization.changeLanguage((String)Settings.get("language"));
        new Save();
        Game.MAX_FPS = (int)Settings.get("fps");
    }
}
