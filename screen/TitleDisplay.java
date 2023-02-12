package minicraft.screen;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Calendar;
import java.util.Random;
import java.util.Date;

import org.jetbrains.annotations.NotNull;

import minicraft.core.Game;
import minicraft.core.Network;
import minicraft.core.Renderer;
import minicraft.core.VersionInfo;
import minicraft.core.World;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.entity.mob.RemotePlayer;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.level.Level;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.LinkEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.screen.SkinDisplay;

public class TitleDisplay extends Display {
	private static final Random random = new Random();
	private int rand;
	private String SplashText;
	private int count = 0; // This and reverse are for the logo; they produce the fade-in/out effect.
	private boolean reverse = false;
	public static String version="2.8.0";

	public TitleDisplay() {

		super(true, false, new Menu.Builder(true, 2, RelPos.CENTER,
			new BlankEntry(),
			new BlankEntry(),
			new SelectEntry( "Play", () -> /*Game.setMenu(new PlayDisplay())*/{
				if (WorldSelectDisplay.getWorldNames().size() > 0)
					Game.setMenu(new Display(true, new Menu.Builder(true, 2,RelPos.CENTER,
						new SelectEntry("Load World", () -> Game.setMenu(new WorldSelectDisplay())),
						new SelectEntry("New World", () -> Game.setMenu(new WorldGenDisplay()))
					).createMenu()));
				else Game.setMenu(new WorldGenDisplay());
			}),
			new SelectEntry("Options", () -> Game.setMenu(new OptionsMainMenuDisplay())),
            new SelectEntry("Skins", () -> Game.setMenu(new SkinDisplay())),
				new SelectEntry( "Help", () -> /*Game.setMenu(new PlayDisplay())*/{
						Game.setMenu(new Display(true, new Menu.Builder(true, 2, RelPos.CENTER,25,
								new BlankEntry(),
								new SelectEntry("Instructions", () -> Game.setMenu(new BookDisplay(BookData.instructions))),
								new BlankEntry(),
								new SelectEntry("Storyline Guide", () -> Game.setMenu(new BookDisplay(BookData.storylineGuide))),
								new BlankEntry(),
								new SelectEntry("About", () -> Game.setMenu(new BookDisplay(BookData.about))),
								new BlankEntry()
						).setTitle("Help",Color.WHITE).createMenu()));
				}),
			new SelectEntry("Quit", Game::quit)
			).setSize(196, 88)
			.setPositioning(new Point(Screen.w/2, Screen.h*3/5), RelPos.CENTER)
			.createMenu()
		);
		menus[0].setSelection(2);
	}
	@Override
	public void init(Display parent) {
		super.init(null); // The TitleScreen never has a parent.
		Renderer.readyToRenderGameplay = false;
		// Check version
		checkVersion();

		/// This is useful to just ensure that everything is really reset as it should be. 
		if (Game.server != null) {
			if (Game.debug) System.out.println("Wrapping up loose server ends.");
			Game.server.endConnection();
			Game.server = null;
		}
		if (Game.client != null) {
			if (Game.debug) System.out.println("Wrapping up loose client ends.");
			Game.client.endConnection();
			Game.client = null;
		}
		Game.ISONLINE = false;
		LocalDateTime time = LocalDateTime.now();
		boolean isBday = (time.getMonth() == Month.OCTOBER && time.getDayOfMonth() == 9) || (time.getMonth() == Month.NOVEMBER && (time.getDayOfMonth() == 1 || time.getDayOfMonth() == 16 ));
		boolean isXmas=time.getMonth() == Month.DECEMBER && time.getDayOfMonth() >= 19 && time.getDayOfMonth() <=25;
		boolean isHalloween=time.getMonth() == Month.OCTOBER && time.getDayOfMonth() >= 10;
		boolean isValentines=time.getMonth() == Month.FEBRUARY && time.getDayOfMonth() >= 10 && time.getDayOfMonth() <= 16;
		//we'll set ids
		if(random.nextInt(3)==0)
		if (isXmas) {
			 rand = random.nextInt(5)+1;
			 SplashText = seasonalSplashes[rand];
		} else if(isHalloween) {
			rand = random.nextInt(6) + 6;
			SplashText = seasonalSplashes[rand];
		}else if(isValentines){
			rand = (seasonalSplashes.length-3)+random.nextInt(2);
			SplashText = seasonalSplashes[rand];
		}else if(isBday){
			if(time.getMonth() == Month.OCTOBER && time.getDayOfMonth() == 9) SplashText = birthdaySplashes[0];
			if(time.getMonth() == Month.NOVEMBER && time.getDayOfMonth() == 1) SplashText = birthdaySplashes[1];
			if(time.getMonth() == Month.NOVEMBER && time.getDayOfMonth() == 16) SplashText = birthdaySplashes[2];
		}else{
			rand = random.nextInt(splashes.length - 3);
			SplashText = splashes[rand];
		}
		else{
			rand = random.nextInt(splashes.length - 3);
			SplashText = splashes[rand];
		}
		
		World.levels = new Level[World.levels.length];
		
		if(Game.player == null || Game.player instanceof RemotePlayer)
			// Was online, need to reset player
			World.resetGame(false);
	}
	
	private void checkVersion() {
		VersionInfo latestVersion = Network.getLatestVersion();
		if(latestVersion == null) {
			Network.findLatestVersion(this::checkVersion);
		}
		else {
			if(latestVersion.version.compareTo(Game.VERSION) > 0) { // Link new version
				menus[0].updateEntry(0, new StringEntry("New:"+latestVersion.releaseName, Color.GREEN));
				menus[0].updateEntry(1, new LinkEntry(Color.CYAN, "Select here to Download", latestVersion.releaseUrl, "Direct link to latest version: " + latestVersion.releaseUrl + "\nCan also be found here with change log: https://github.com/pelletsstarPL/Minicraft-squared/releases"));
			}
			else if(latestVersion.releaseName.length() > 0) {
				menus[0].updateEntry(0, new StringEntry("You have the", Color.ORANGE));
				menus[0].updateEntry(1, new StringEntry("latest version.", Color.ORANGE));
			}else {
				menus[0].updateEntry(0, new StringEntry("No connection :(", Color.RED));
				menus[0].updateEntry(1, new StringEntry("couldn't look 4 updates", Color.RED));
			}
		}
	}
	
	@NotNull
	private static SelectEntry displayFactory(String entryText, ListEntry... entries) {
		return new SelectEntry(entryText, () -> Game.setMenu(new Display(true, new Menu.Builder(false, 2, RelPos.CENTER, entries).createMenu())));
	}
	
	@Override
	public void tick(InputHandler input) {
		if (input.getKey("r").clicked && Game.debug) rand = random.nextInt(splashes.length - 3) + 3;

		if (reverse) {
			count--;
			if (count == 0) reverse = false;
		} else {
			count++;
			if (count == 25) reverse = true;
		}

		super.tick(input);
	}
	
	@Override
	public void render(Screen screen) {
		super.render(screen);
		String upString = "(" + Game.input.getMapping("cursor-up") + ", "+ Game.input.getMapping("cursor-down")+Localization.getLocalized(" to select") +")";
		String selectString = "(" + Game.input.getMapping("select") + Localization.getLocalized(" to accept") +")";
		String exitString = "(" + Game.input.getMapping("exit") + Localization.getLocalized(" to return") +")";
		Font.drawCentered(upString, screen, Screen.h - 32, Color.get(1, 51));
		Font.drawCentered(selectString, screen, Screen.h - 22, Color.get(1, 51));
		Font.drawCentered(exitString, screen, Screen.h - 12, Color.get(1, 51));
		int h = 2; // Height of squares (on the spritesheet)
		int w = 15; // Width of squares (on the spritesheet)
		int xo = (Screen.w - w * 8) / 2; // X location of the title
		int yo = 28; // Y location of the title
		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				screen.render(xo + x * 8, yo + y * 8, x + y * 32, 0, 3);
			}
		}
		LocalDateTime time = LocalDateTime.now();
		boolean isSeason= (time.getMonth() == Month.DECEMBER && time.getDayOfMonth() >= 19 && time.getDayOfMonth() <=25) || (time.getMonth() == Month.OCTOBER && time.getDayOfMonth() >= 10) || (time.getMonth() == Month.FEBRUARY && time.getDayOfMonth() >= 10 && time.getDayOfMonth() >= 16);
		boolean isblue = SplashText.contains("blue");
		boolean isGreen = SplashText.contains("Green");
		boolean isRed = SplashText.contains("Red") || (time.getMonth() == Month.OCTOBER && time.getDayOfMonth() >= 10);
		boolean isPink = SplashText.contains("FTW!") || (time.getMonth() == Month.FEBRUARY && time.getDayOfMonth() >= 10 && time.getDayOfMonth() <= 16);
		boolean isWhite = SplashText.contains("by");
		boolean isBday = (time.getMonth() == Month.OCTOBER && time.getDayOfMonth() == 9) || (time.getMonth() == Month.NOVEMBER && (time.getDayOfMonth() == 1 || time.getDayOfMonth() == 16));

		/// This isn't as complicated as it looks. It just gets a color based off of count, which oscilates between 0 and 25.
		int bcol = 5 - count / 5; // This number ends up being between 1 and 5, inclusive.
		int splashColor = isblue ? Color.BLUE : isRed ? Color.RED : isGreen ? Color.GREEN : isWhite ? Color.WHITE : isPink ? Color.PINK : Color.get(1, bcol*51, bcol*51, bcol*25);

		if(isBday)splashColor=random.nextInt(16777216);

		Font.drawCentered(SplashText, screen, 52, splashColor);
		Font.drawCentered(":)", screen, 65, Color.BLUE);

		Font.draw("Version " + version, screen, 1, 1, Color.get(1, 51));
		Font.draw("Mod by:",screen,1,9,Color.get(1,100));
		Font.draw("pelletsstar",screen,55,9,Color.get(1,200));
		Font.draw("PL",screen,144,9,Color.get(1,0x660000));


		new TitleDisplay();
	}
	private static final String[] birthdaySplashes = {
			"Happy birthday pelletsstarPL",//pellet\'s BDAY
			"Happy birthday Minicraft^2!",//Mod\'s bday
			"Happy birthday Miu Iruma!"
	};

	private static final String[] seasonalSplashes = {
			"Sweet Secret Splash!", //when any event
			"Wish you merry XMAS", //xmas
			"Happy XMAS!",
			"Snowmen? Wuzzat?",
			"So snowy, so cold",
			"Where is Pauls santa hat?",
			"Happy Spooktober!", //halloween
			"Spooky terra",
			"Trick or treat!",
			"Jumpscares not included",
			"Caves are scary",
			"Jack o lanterns? Wuzzat?",
			"7 Spooky Surface biomes",
			"Love is in the air",//valentines
			"Happy Valentines",
			"^2 is lovely mod"
	};
	private static final String[] splashes = {
			"Drink some water.",
        "Now with Skins!",
        "Have you seen Ben? Ben Forge?",
		"Now with thirst!",
			"7 SURFACE BIOMES!",
			"3 CAVE BIOMES!",
		"Now with beautiful caves!",
		"Now with deepslate!",
		"Now with swampy swamps!",
		"Now with better dungeons!",
		"Look closely",
			"To the MAX!... kratt",
		"squared by pelletsstarPL",
		"The Wiki is weak!",
		"Notch is Cool!",
		"Ma4kus is the man!",
		"I <3 playminicraft",
		"Christoffer is great mod",
		"pelletsstarPL <3",
		"You should read Antidious Venomi!",
		"You should read 11th!",
		"You should read The Hot Day!",
		"Use the force!",
		"Keep calm!",
		"Get him, Steve!",
			"Hi, Paul!",
		"Neat-O",
		"Plant cloud cacti!",
		"Kill Cow, get Beef!",
			"Loot castle,get great loot",
		"Kill Zombie, get Cloth!",
		"Kill Slime, get Slime!",
		"Pixels stolen from James",
		"get bloodshards during bloodmoon",
		"Kill Pig, get Porkchop!",
			"Airwizard looks for revenge",
		"Gold > Iron",
		"Gem > Gold",
		"Test == InDev!",
		"Story? Don't ask for it.",
		"Infinite terrain? What's that?",
		"Lazy parallel world",
		"Minecarts? What are those?",
		"Windows? I prefer Doors!",
		"2.5D FTW!",
		"3rd dimension not included!",
		"Null not included",
			"Pink colour FTW!",
		"Unplug your mouse",
		"No spiders included!",
			"Eye is watching you!",
		"No Endermen included!",
		"No chickens included!",
		"Grab your friends!",
		"Creepers included!",
		"Skeletons included!",
		"Knights included!",
		"Green spiky cactus! Ow!",
		"Cows included!",
		"Sheep included!",
			"Explore new caves",
		"Pigs included!",
		"Bigger Worlds FTW!",
		"World types!",
		"n+1 world combinations",
		"Reed included!",
		"Terra Floppa",
			"Ghosts included! Wraiths too!",
		"So we back in the mine,",
		"Pickaxe swinging from side to side",
		"In search of Gems!",
		"Life itself suspended by a thread",
		"saying ay-oh, that creeper's KO'd!",
		"Gimmie a bucket!",
		"Farming with water!",
		"Get the High-Score!",
		"Potions FTW!",
		"Beds FTW!",
		"Defeat the Air Wizard!",
		"Defeat the Night Wizard!",
		"Conquer the Dungeon!",
		"One down, one to go...",
		"Loom + Wool = String!",
		"String + Wood = Rod!",
		"Sand + Gunpowder = TNT!",
		"Don't oversleep the Bloodmoon!",
		"Farm at Day!",
		"!",
		"!sdrawkcab si sihT",
		"This is forwards!",
		"Why is this blue?",
		"Green is a nice color!",
		"Red is my favorite color!",
		"Made with 10000% Vitamin Z!",
		"Punch the Moon!",
		"Find the stairs!",
		"This is String qq!",
			"blue da ba dee da ba da...",
		"Why?",
		"hello down there!",
		"Delcian Foxy",
		"Hola senor!",
		"Sonic Boom!",
		"Hakuna Matata!",
		"One truth prevails!",
		"Awesome!",
		"Sweet!",
		"Great!",
		"Glad that Glad is here :)",
		"Cool!",
		"Radical!",
		"011011000110111101101100!",
		"001100010011000000110001!",
		"011010000110110101101101?",
		"...zzz...",
	};
}
