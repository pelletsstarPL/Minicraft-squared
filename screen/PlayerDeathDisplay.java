package minicraft.screen;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import minicraft.core.Game;
import minicraft.core.World;
import minicraft.core.io.Settings;
import minicraft.gfx.Point;
import minicraft.gfx.SpriteSheet;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;

public class PlayerDeathDisplay extends Display {
	// this is an IMPORTANT bool, determines if the user should respawn or not. :)
	public static boolean shouldRespawn = true;
	
	public PlayerDeathDisplay() {
		super(false, false);
		
		ArrayList<ListEntry> entries = new ArrayList<>();
		entries.addAll(Arrays.asList(
			new StringEntry("Time: " + InfoDisplay.getTimeString()),
			new StringEntry("Score: " + Game.player.getScore()),
			new BlankEntry()
		));
		
		if(!Settings.get("mode").equals("Hardcore")) {
			entries.add(new SelectEntry("Respawn", () -> {
				World.resetGame();
				if (!Game.isValidClient())
					Game.setMenu(null); //sets the menu to nothing
			}));
		}
		
		if(Settings.get("mode").equals("Hardcore") || !Game.isValidClient()) {
			entries.add(new SelectEntry("Quit", () -> Game.setMenu(new TitleDisplay())));
			/*String worldName;
			String worldsDir = Game.gameDir + "/saves/";
			File world = new File(worldsDir + worldName);
			File[] list = world.listFiles();
			for (File file : list) {
				file.delete();
			}
			world.delete();*/
		}

		
		menus = new Menu[]{
			new Menu.Builder(true, 0, RelPos.LEFT, 1,entries)
				.setPositioning(new Point(SpriteSheet.boxWidth, SpriteSheet.boxWidth * 3), RelPos.BOTTOM_RIGHT)
				.setTitle("You died! Aww!")
				.setTitlePos(RelPos.TOP_LEFT)
				.createMenu()
		};
		
	}
}
