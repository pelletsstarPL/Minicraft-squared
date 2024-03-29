package minicraft.core;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;
import javax.tools.Tool;

import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.furniture.Bed;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Ellipsis;
import minicraft.gfx.Ellipsis.DotUpdater.TickUpdater;
import minicraft.gfx.Ellipsis.SmoothEllipsis;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;
import minicraft.item.*;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;
import minicraft.saveload.Load;
import minicraft.screen.LoadingDisplay;
import minicraft.screen.TitleDisplay;
import minicraft.screen.RelPos;


public class Renderer extends Game {
	private Renderer() {}

	public static final int HEIGHT = 192;
	public static final int WIDTH = 288;
	static float SCALE = 3;

	public static Screen screen; // Creates the main screen

	static Canvas canvas = new Canvas();
	private static BufferedImage image; // Creates an image to be displayed on the screen.
	private static int[] pixels; // The array of pixels that will be displayed on the screen.

	private static Screen lightScreen; // Creates a front screen to render the darkness in caves (Fog of war).
	private static Screen red; // Creates a front screen to render the darkness in caves (Fog of war).

	public static boolean readyToRenderGameplay = false;
	public static boolean showinfo = false;

	public static int damageWiggleTime = 0; //hearts and armor will wiggle if player will take damage

	private static Ellipsis ellipsis = new SmoothEllipsis(new TickUpdater());

	public static SpriteSheet[] loadDefaultSpriteSheets() {
		SpriteSheet itemSheet, tileSheet, entitySheet, guiSheet, skinsSheet;
		try {
			// These set the sprites to be used.
			itemSheet = new SpriteSheet(ImageIO.read(Objects.requireNonNull(Game.class.getResourceAsStream("/resources/textures/items.png"))));
			tileSheet = new SpriteSheet(ImageIO.read(Objects.requireNonNull(Game.class.getResourceAsStream("/resources/textures/tiles.png"))));
			entitySheet = new SpriteSheet(ImageIO.read(Objects.requireNonNull(Game.class.getResourceAsStream("/resources/textures/entities.png"))));
			guiSheet = new SpriteSheet(ImageIO.read(Objects.requireNonNull(Game.class.getResourceAsStream("/resources/textures/gui.png"))));
			skinsSheet = new SpriteSheet(ImageIO.read(Objects.requireNonNull(Game.class.getResourceAsStream("/resources/textures/skins.png"))));
		} catch (NullPointerException e) {
			// If a provided InputStream has no name. (in practice meaning it cannot be found.)
			e.printStackTrace();
			System.out.println("A sprite sheet was not found.");
			System.exit(-1);
			return null;
		} catch (IOException | IllegalArgumentException e) {
			// If there is an error reading the file.
			e.printStackTrace();
			System.out.println("Could not load a sprite sheet.");
			System.exit(-1);
			return null;
		}

		return new SpriteSheet[] { itemSheet, tileSheet, entitySheet, guiSheet, skinsSheet };
	}

	static void initScreen() {
		if (!HAS_GUI) return;

		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

		SpriteSheet[] sheets = loadDefaultSpriteSheets();
		screen = new Screen(sheets[0], sheets[1], sheets[2], sheets[3], sheets[4]);
		lightScreen = new Screen(sheets[0], sheets[1], sheets[2], sheets[3], sheets[4]);
		red = new Screen(sheets[0], sheets[1], sheets[2], sheets[3], sheets[4]);

		screen.pixels = pixels;

		if (HAS_GUI) {
			canvas.createBufferStrategy(3);
			canvas.requestFocus();
		}
	}


	/** Renders the current screen. Called in game loop, a bit after tick(). */
	public static void render() {
		if ((!HAS_GUI || screen == null)) return; // No point in this if there's no gui... :P

		if (readyToRenderGameplay) {
			if (isValidServer()) {
				screen.clear(0);
				Font.drawCentered("Awaiting client connections"+ ellipsis.updateAndGet(), screen, 10, Color.get(-1, 444));
				Font.drawCentered("So far:", screen, 20, Color.get(-1, 444));
				int i = 0;
				for (String playerString: server.getClientInfo()) {
					Font.drawCentered(playerString, screen, 30 + i * 10, Color.get(-1, 134));
					i++;
				}

				renderDebugInfo();
			}
			else {
				if(currentLevel>0)renderLevel(currentLevel-1,true);
				renderLevel(currentLevel,false); //we want to fully see it

				renderGui();
			}
		}

		if (menu != null) // Renders menu, if present.
			menu.render(screen);

		if (!canvas.hasFocus() && !ISONLINE) renderFocusNagger(); // Calls the renderFocusNagger() method, which creates the "Click to Focus" message.


		BufferStrategy bs = canvas.getBufferStrategy(); // Creates a buffer strategy to determine how the graphics should be buffered.
		Graphics g = bs.getDrawGraphics(); // Gets the graphics in which java draws the picture
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight()); // Draws a rect to fill the whole window (to cover last?)

		// Scale the pixels.
		int ww = getWindowSize().width;
		int hh = getWindowSize().height;

		// Get the image offset.
		int xOffset = (canvas.getWidth() - ww) / 2 + canvas.getParent().getInsets().left;
		int yOffset = (canvas.getHeight() - hh) / 2 + canvas.getParent().getInsets().top;

		// Draw the image on the window.
		g.drawImage(image, xOffset, yOffset, ww, hh, null);

		// Release any system items that are using this method. (so we don't have crappy framerates)
		g.dispose();

		// Make the picture visible.
		bs.show();
	}


	private static void renderLevel(int lvlId,boolean visualOnly) {
		Level lvlList[][] = {World.levels,World.obvLevels};
		Level level = lvlList[player.getRealmId()][lvlId];
		if (level == null) return;
		int xScroll = player.x - Screen.w / 2; // Scrolls the screen in the x axis.
		int yScroll = player.y - (Screen.h - 8) / 2; // Scrolls the screen in the y axis.

		// Stop scrolling if the screen is at the ...
		if (xScroll < 0) xScroll = 0; // ...Left border.
		if (yScroll < 0) yScroll = 0; // ...Top border.
		if (xScroll > level.w * 16 - Screen.w) xScroll = level.w * 16 - Screen.w; // ...Right border.
		if (yScroll > level.h * 16 - Screen.h) yScroll = level.h * 16 - Screen.h; // ...Bottom border.
		if ( (player.getRealmId()==1) &&(currentLevel == 0 ||  visualOnly)) { // If the current level is second floor of obsidian dungeon - 1.7
			for (int y = 0; y < 28; y++)
				for (int x = 0; x < 48; x++) {
					// Creates the void bg for dungeon level - 1.7
					int xmod=(int)(Math.round(Math.random() * 20));
					screen.render(x * 8 - ((xScroll / 4) & 7), y * 8 - ((yScroll / 4) & 7),  xmod + (currentLevel == 0 ? 26 : 27) * 32, 0,1);
				}
		}
		level.renderBackground(screen, xScroll, yScroll); // Renders current level background
		level.renderSprites(screen, xScroll, yScroll,visualOnly); // Renders level sprites on screen

				// This creates the darkness in the caves and blindness
				//if (((Updater.tickCount < Updater.dayLength/4 || Updater.tickCount > Updater.dayLength/2) /*&& !isMode("creative")*/) || player.potionEffects.containsKey(PotionType.Blind) && (!Game.isMode("creative"))) {
				lightScreen.clear(0);        // This doesn't mean that the pixel will be black; it means that the pixel will be DARK, by default; lightScreen is about light vs. dark, not necessarily a color. The light level it has is compared with the minimum light values in dither to decide whether to leave the cell alone, or mark it as "dark", which will do different things depending on the game level and time of day.
				int brightnessMultiplier = player.potionEffects.containsKey(PotionType.Light) ? 12 : player.potionEffects.containsKey(PotionType.Blind) ? 3 : 8; // Brightens all light sources by a factor of 1.5 when the player has the Light potion effect. (8 above is normal)
				if (player.potionEffects.containsKey(PotionType.Light) && player.potionEffects.containsKey(PotionType.Blind))
					brightnessMultiplier = 2;
				//System.out.println(currentLevel);
				if (!visualOnly)
					level.renderLight(lightScreen, xScroll, yScroll, brightnessMultiplier); // Finds (and renders) all the light from objects (like the player, lanterns, and lava).

				if (!isMode("creative") || player.toggleLight) {
					if (!visualOnly)
						screen.overlay(lightScreen, lvlId, visualOnly ? player.x : xScroll, yScroll); // Overlays the light screen over the main screen. on creative you see everything in caves
				} else {
					if (currentLevel > 5)
						screen.overlay(lightScreen, lvlId, xScroll, yScroll); // Overlays the light screen over the main screen.
				}

				if (visualOnly) screen.darken(screen, lvlId, xScroll, yScroll);
				//level.renderLight(lightScreen, xScroll, yScroll, brightnessMultiplier); // Finds (and renders) all the light from objects (like the player, lanterns, and lava).
				//}

	}


	/** Renders the main game GUI (hearts, Stamina bolts, name of the current item, etc.) */
	private static void renderGui() {
		// ARROWS COUNT STATUS
		if(player.toggleGui) {
			if (damageWiggleTime > 0) damageWiggleTime--;
			if (player.activeItem instanceof ToolItem) {
				if (((ToolItem) player.activeItem).type == ToolType.Bow) {
					int ac = player.getInventory().count(Items.arrowItem);

					int xx = 16 + ((Screen.w) / 2) - 48 - player.activeItem.arrAdjusted; // the width of the box
					int yy = (Screen.h - 8) - 13; // the height of the box
					String aC = ac + "";
					int w = Game.isMode("Creative") || ac >= 1000 ? 4 : aC.length() + 2; // length of message in characters.
					int h = 1;

					int x = 170;
					int y = 25;

					// Renders the four corners of the box
					screen.render(xx - 8, yy - 8, 15 + 0 * 32, 0, 3);
					screen.render(xx + w * 8, yy - 8, 15 + 0 * 32, 1, 3);
					screen.render(xx - 8, yy + 8, 15 + 0 * 32, 2, 3);
					screen.render(xx + w * 8, yy + 8, 15 + 0 * 32, 3, 3);

					// Renders each part of the box...
					for (x = 0; x < w; x++) {
						screen.render(xx + x * 8, yy - 8, 16 + 0 * 32, 0, 3); // ...top part
						screen.render(xx + x * 8, yy + 8, 16 + 0 * 32, 2, 3); // ...bottom part
					}
					for (y = 0; y < h; y++) {
						screen.render(xx - 8, yy + y * 8, 17 +0* 32, 0, 3); // ...left part
						screen.render(xx + w * 8, yy + y * 8, 17 + 0 * 32, 1, 3); // ...right part
					}

					// The middle
					for (x = 0; x < w; x++) {
						screen.render(xx + x * 8, yy, 18 + 0 * 32, 0, 3);
					}

					if (isMode("creative") || ac >= 1000) {
						Font.drawTransparentBackground(" ∞", screen, 108 - player.activeItem.arrAdjusted, Screen.h - 24);
					} else {
						Font.drawTransparentBackground(" x" + ac, screen, 108 - player.activeItem.arrAdjusted, Screen.h - 24);
					}

					// Displays the arrow icon
					screen.render(108 - player.activeItem.arrAdjusted, Screen.h - 24, 0 + 2 * 32, 0, 0);
				}
			}

			// TOOL DURABILITY STATUS
			if ( player.activeItem instanceof ToolItem || player.activeItem instanceof FishingRodItem ) {
				Item tool = player.activeItem;
				byte dura;
				// Draws the text
				if(player.activeItem instanceof ToolItem)dura = (byte)((((ToolItem) tool).dur * 100) /((ToolItem) tool).maxDur); //its <0,100> so we can use byte
				else dura = (byte)((((FishingRodItem) tool).dur * 100) / ((FishingRodItem) tool).maxDur); //its <0,100> so we can use byte
				String Dura = dura + "%";
				int w = Dura.length(); // Length of message in characters.
				int h = 1;
				int xx = ((Screen.w) / 2) + player.activeItem.durAdjusted; // The width of the box
				int yy = (Screen.h - 8) - 13; // The height of the box


				int x = 200;
				int y = 25;

				// Renders the four corners of the box
				screen.render(xx - 8, yy - 8, 15 +0 * 32, 0, 3);
				screen.render(xx + w * 8, yy - 8, 15 +0 * 32, 1, 3);
				screen.render(xx - 8, yy + 8, 15 +0 * 32, 2, 3);
				screen.render(xx + w * 8, yy + 8, 15 +0* 32, 3, 3);

				// Renders each part of the box...
				for (x = 0; x < w; x++) {
					screen.render(xx + x * 8, yy - 8, 16 +0 * 32, 0, 3); // ...top part
					screen.render(xx + x * 8, yy + 8, 16 +0 * 32, 2, 3); // ...bottom part
				}
				for (y = 0; y < h; y++) {
					screen.render(xx - 8, yy + y * 8, 17 +0 * 32, 0, 3); // ...left part
					screen.render(xx + w * 8, yy + y * 8, 17 +0 * 32, 1, 3); // ...right part
				}

				// The middle
				for (x = 0; x < w; x++) {
					screen.render(xx + x * 8, yy, 18 +0 * 32, 0, 3);
				}

				Font.drawTransparentBackground(dura + "%", screen, 140 + player.activeItem.durAdjusted, Screen.h - 24, Color.generatePercentageColor(dura));
			}


			// This draws the black square where the selected item would be if you were holding it
			if (!isMode("creative") || player.activeItem != null) {
				for (int x = 20; x < 36; x++) {
					screen.render(x * 8, Screen.h - 8, 0 + 28 * 32, 0, 3);
				}
			}

			// Shows active item sprite and name in bottom toolbar.
			if (player.activeItem != null) {
				player.activeItem.renderHUD(screen, 20 * 8, Screen.h - 8, Color.GRAY);
			}

			ArrayList<String> permStatus = new ArrayList<>();

			if (Updater.saving) {
				permStatus.add("Saving... " + Math.round(LoadingDisplay.getPercentage()) + "%");
			}
			if (Bed.sleeping()) {
				permStatus.add("Sleeping...");
			} else if (!Game.isValidServer() && Bed.getPlayersAwake() > 0) {
				int numAwake = Bed.getPlayersAwake();

				if (Bed.inBed(Game.player)) {
					permStatus.add(MyUtils.plural(numAwake, "player") + " still awake");
					permStatus.add(" ");
					permStatus.add("Press " + input.getMapping("exit") + " to cancel");

				} else if (Game.isValidClient()) {
					// Draw it in a corner
					int total = Game.client.getPlayerCount();
					int sleepCount = total - numAwake;

					if (sleepCount > 0) {
						new FontStyle(Color.WHITE).setRelTextPos(RelPos.BOTTOM_LEFT).setAnchor(Screen.w, 0).draw(sleepCount + "/" + total + " players sleeping", screen);
					}
				}
			}

			if (permStatus.size() > 0) {
				FontStyle style = new FontStyle(Color.WHITE).setYPos(Screen.h / 2 - 25).setRelTextPos(RelPos.TOP).setShadowType(Color.DARK_GRAY, false);

				Font.drawParagraph(permStatus, screen, style, 1);
			}

			/// NOTIFICATIONS
			FontStyle styleBlood = new FontStyle(Color.get(1, 255, 0, 0)).setShadowType(Color.RED, false).setYPos(Screen.h * 2 / 10).setRelTextPos(RelPos.TOP, false);
			if (Updater.isbloody && Updater.tickCount > 37400 && Updater.tickCount <= 38400)
				Font.drawParagraph("The Bloodmoon is rising!", screen, styleBlood, 0);
			if (permStatus.size() == 0 && notifications.size() > 0) {
				Updater.notetick++;
				if (notifications.size() > 3) { // Only show 3 notifs max at one time; erase old notifs.
					notifications = notifications.subList(notifications.size() - 3, notifications.size());
				}

				if (Updater.notetick > (notifications.contains("The Bloodmoon is rising!") ? 600 : 120)) { // Display time per notification.
					notifications.remove(0);
					Updater.notetick = 0;
				}

				// draw each current notification, with shadow text effect.
				FontStyle style = new FontStyle(Color.WHITE).setShadowType(Color.DARK_GRAY, false).setYPos(Screen.h * 2 / 5).setRelTextPos(RelPos.TOP, false);


				Font.drawParagraph(notifications, screen, style, 0);

			}

			// SCORE MODE ONLY:
			if (isMode("score")) {
				int seconds = (int) Math.ceil(Updater.scoreTime / (double) Updater.normSpeed);
				int minutes = seconds / 60;
				int hours = minutes / 60;
				minutes %= 60;
				seconds %= 60;

				int timeCol;
				if (Updater.scoreTime >= 18000) {
					timeCol = Color.get(0, 555);
				} else if (Updater.scoreTime >= 3600) {
					timeCol = Color.get(330, 555);
				} else {
					timeCol = Color.get(400, 555);
				}

				Font.draw("Time left " + (hours > 0 ? hours + "h " : "") + minutes + "m " + seconds + "s", screen, Screen.w / 2 - 9 * 8, 2, timeCol);

				String scoreString = "Current score: " + player.getScore();
				Font.draw(scoreString, screen, Screen.w - Font.textWidth(scoreString) - 2, 3 + 8, Color.WHITE);

				if (player.getMultiplier() > 1) {
					int multColor = player.getMultiplier() < Player.MAX_MULTIPLIER ? Color.get(-1, 540) : Color.RED;
					String mult = "X" + player.getMultiplier();

					Font.draw(mult, screen, Screen.w - Font.textWidth(mult) - 2, 4 + 2 * 8, multColor);
				}
			}

			/// This renders the potions overlay
			if (player.showpotioneffects && player.potionEffects.size() > 0) {
				String typeTxt="";
				Map.Entry<PotionType, Integer>[] effects = player.potionEffects.entrySet().toArray(new Map.Entry[0]);
				int nmbr=(int) Settings.get("potionsn");
				String side=Settings.get("displayside").toString();
				int txtLen=(int)Settings.get("potiontxtlen");
				// The key is potion type, value is remaining potion duration.
				for (int i = 0; i < (effects.length > nmbr  ? nmbr : effects.length); i++) {
					int index=effects.length <= nmbr ? i : (i+(Updater.tickCount/100))% effects.length;
					PotionType pType = effects[index].getKey();
					int pTime = effects[index].getValue() / Updater.normSpeed;
					if(txtLen!=0)typeTxt=pType.toString().length()>(int)Settings.get("potiontxtlen")  ? pType.toString().substring(0,(int)Settings.get("potiontxtlen")) : pType.toString();
					if(Updater.tickCount%300>150 || effects.length <= nmbr)
					Font.drawTransparentBackground("(" + input.getMapping("potionEffects") + ") to hide", screen, (side=="Left" ? 16 : 172), 9);
					else Font.drawTransparentBackground("(" + input.getMapping("potionFullList") + ") for full list", screen, (side=="Left" ? 8 : 146), 9);
					if(pType.icon!=null && (boolean)Settings.get("displayicon"))pType.icon.render(screen,(side=="Left" ? 16 : 172) - (typeTxt.length() > txtLen ? (typeTxt.length() - txtLen) * 8 : 0), 17 + i * Font.textHeight());
					Font.drawTransparentBackground(typeTxt + " (" + (pTime / 60) + ":" + ((pTime % 60 < 10) ? "0" + (pTime % 60) : (pTime % 60)) + ")", screen, (side=="Left" ? 24 : 180) - (typeTxt.length() > txtLen ? (typeTxt.length() - txtLen) * 8 : 0), 17 + i * Font.textHeight(), pType.dispColor);
				}
			}
			// This is the status icons, like health hearts, stamina bolts, and hunger "burgers".
			if (!isMode("creative")) {
				int wiggleHP = !Updater.paused && (player.health < 5 || damageWiggleTime > 0) ? (int) Math.round(Math.random() * 4) - 4 : 0;
				int wiggleArmor = !Updater.paused && damageWiggleTime > 0 ? (int) Math.round(Math.random() * 4) - 4 : 0;
				int wiggleHU = !Updater.paused && (player.hunger < 5 || (player.stamHungerTicks < 40 && player.hungerStamCnt < 2)) ? (int) Math.round(Math.random() * 2) - 2 : 0;
				int wiggleTH = !Updater.paused && (player.thirst < 5 || (player.stamThirstTicks < 40 && player.thirstStamCnt < 2)) ? (int) Math.round(Math.random() * 2) - 2 : 0;
				if (Settings.get("statdisplay") == "Shortened") {

					int poisoned = 0;
					int hungered = 0;
					int thirsted = 0;
					int regen = 0;
					int shield = 0;
					int hardcore = 0;

					if (player.getPotionEffects().containsKey(PotionType.Poison)) poisoned = 3;
					if (player.getPotionEffects().containsKey(PotionType.Hunger)) hungered = 3;
					if (player.getPotionEffects().containsKey(PotionType.Thirst)) thirsted = 3;
					if (isMode("Hardcore")) hardcore = 8;
					int heartIndexX = (0 + hardcore );
					int heartIndexY =  (2 + poisoned);
					if(player.health > 20){
						heartIndexX =(((player.health/20)-1) > 6 ? 6 : (player.health/20)-1) +  19 + hardcore ;
						heartIndexY = 2 +poisoned;
					}
					screen.render(0, Screen.h - (16 + wiggleHP),  heartIndexX + heartIndexY * 32, 0, 3); //heart
					if (player.getPotionEffects().containsKey(PotionType.Regen))
						screen.render(0, Screen.h - (16 + wiggleHP), 0 + 10 * 32, 0, 3);
					if (player.getPotionEffects().containsKey(PotionType.Shield))
						screen.render(0, Screen.h - (16 + wiggleHP), 0 + 8 * 32, 0, 3);
					Font.draw((player.health < 0 ? "0/" + player.getCurrentMaxHp() : player.health + "/" + player.getCurrentMaxHp()), screen, 10, Screen.h - 16, Color.get(Color.WHITE_CODE));

					//Stamina
					if (player.staminaRechargeDelay > 0 && player.stamina <= 0) {
						// Creates the white/gray blinking effect when you run out of stamina.
						if (player.staminaRechargeDelay / 4 % 2 == 0) {
							screen.render(0, Screen.h - 8, 1 + 4 * 32, 0, 3);

						} else {
							screen.render(0, Screen.h - 8, 1 + 3 * 32, 0, 3);

						}
					} else {
						if (player.getPotionEffects().containsKey(PotionType.Energy))
							screen.render(0, Screen.h - 8, 1 + 5 * 32, 0, 3);
						else screen.render(0, Screen.h - 8, 1 + 2 * 32, 0, 3);
					}
					Font.draw((player.stamina < 0 ? "0/" + player.maxStamina : player.stamina + "/" + player.maxStamina), screen, 10, Screen.h - 8, Color.get(Color.WHITE_CODE));
					if (player.curArmor != null) {
						if (player.curArmor.getName() == "Night Armor") {

							if (Updater.getTime() == Updater.Time.Night || currentLevel < 4) {
								screen.render(0, Screen.h - (24 + wiggleArmor), (6) + 9 * 32, 0, 0);
								Font.draw((player.armor < 0 ? "0" : String.valueOf(player.armor)), screen, 10, Screen.h - 24, Color.get(Color.WHITE_CODE));
							} else {
								screen.render(0, Screen.h - (24 + wiggleArmor), (8) + 9 * 32, 0, 0);
								Font.draw("0", screen, 10, Screen.h - 24, Color.get(Color.WHITE_CODE));
							}
						} else if (player.curArmor != null) {
							screen.render(0 * 8, Screen.h - (24 + wiggleArmor), (player.curArmor.level - 1) + 9 * 32, 0, 0);
							Font.draw((player.armor < 0 ? "0" : String.valueOf(player.armor)), screen, 10, Screen.h - 24, Color.get(Color.WHITE_CODE));
						}
					}


					screen.render(Screen.w - 8, Screen.h - (16 + wiggleHU), 2 + (2 + hungered) * 32, 0, 3); //full
					if (player.getPotionEffects().containsKey(PotionType.WellFed))
						screen.render(Screen.w - 8, Screen.h - (16 + wiggleHU), 2 + 8 * 32, 0, 3); //wellfed
					Font.draw((player.hunger < 0 ? "0/" + player.maxHunger : player.hunger + "/" + player.maxHunger), screen, Screen.w - 48, Screen.h - 16, Color.get(Color.WHITE_CODE));


					screen.render(Screen.w - 8, Screen.h - (8 + wiggleTH), 7 + (2 + thirsted) * 32, 0, 3); //full
					if (player.getPotionEffects().containsKey(PotionType.WellHydrated))
						screen.render(Screen.w - 8, Screen.h - (8 + wiggleTH), 7 + 8 * 32, 0, 3); //wellhydrated
					Font.draw((player.thirst < 0 ? "0/" + player.maxThirst : player.thirst + "/" + player.maxThirst), screen, Screen.w - 48, Screen.h - 8, Color.get(Color.WHITE_CODE));


				} else { //TRADITIONAL
					int hpmod = player.health;
					int hunmod = player.hunger;
					int thimod = player.thirst;
					for (int i = 0; i < 10; i++) { //we will go 10 times
						int poisoned = 0;
						int hungered = 0;
						int thirsted = 0;
						int regen = 0;
						int shield = 0;
						int hardcore = 0;
						if (player.getPotionEffects().containsKey(PotionType.Poison)) poisoned = 3;
						if (player.getPotionEffects().containsKey(PotionType.Hunger)) hungered = 3;
						if (player.getPotionEffects().containsKey(PotionType.Thirst)) thirsted = 3;

						if (isMode("Hardcore")) hardcore = 8;
						// Renders armor
						int armor = player.armor * Player.maxStat / Player.maxArmor;
						if (i <= armor && player.curArmor != null) {
							if (player.curArmor.getName() == "Night Armor") {

								if (Updater.getTime() == Updater.Time.Night || currentLevel < 4) {
									screen.render(i * 8, Screen.h - (24 + wiggleArmor), (6) + 9 * 32, 0, 0);
								} else {
									screen.render(i * 8, Screen.h - (24 + wiggleArmor), (8) + 9 * 32, 0, 0);
								}
							} else if (i <= armor && player.curArmor != null) {
								screen.render(i * 8, Screen.h - (24 + wiggleArmor), (player.curArmor.level - 1) + 9 * 32, 0, 0);

							}
						}
						wiggleHP = !Updater.paused && (player.health < 5 || damageWiggleTime > 0) ? (int) Math.round(Math.random() * 4) - 4 : 0;
						// Renders your current red hearts, or black hearts for damaged health.
						if (i < player.health && hpmod >= 2) {
							screen.render(i * 8, Screen.h - (16 + wiggleHP), (0 + hardcore) + (2 + poisoned) * 32, 0, 3); //full
							if (player.getPotionEffects().containsKey(PotionType.Regen))
								screen.render(i * 8, Screen.h - (16 + wiggleHP), 0 + 10 * 32, 0, 3);
							if (player.getPotionEffects().containsKey(PotionType.Shield))
								screen.render(i * 8, Screen.h - (16 + wiggleHP), 0 + 8 * 32, 0, 3);
						} else if (hpmod == 1) {

							screen.render(i * 8, Screen.h - (16 + wiggleHP), (0 + hardcore) + (3 + poisoned) * 32, 0, 3); //half
							if (player.getPotionEffects().containsKey(PotionType.Regen))
								screen.render(i * 8, Screen.h - (16 + (wiggleHP)), 0 + 11 * 32, 0, 3);
							if (player.getPotionEffects().containsKey(PotionType.Shield))
								screen.render(i * 8, Screen.h - (16 + (wiggleHP)), 0 + 9 * 32, 0, 3);
							hpmod -= 1;
						} else if (hpmod <= 0) {
							screen.render(i * 8, Screen.h - 16, (0 + hardcore) + (4 + poisoned) * 32, 0, 3); //empty
						}
						hpmod -= 2;


						if (player.staminaRechargeDelay > 0) {
							// Creates the white/gray blinking effect when you run out of stamina.
							if (player.staminaRechargeDelay / 4 % 2 == 0) {
								screen.render(i * 8, Screen.h - 8, 1 + 4 * 32, 0, 3);

							} else {
								screen.render(i * 8, Screen.h - 8, 1 + 3 * 32, 0, 3);

							}
						} else {
							// Renders your current stamina, and uncharged gray stamina.
							if (i < player.stamina) {
								if (player.getPotionEffects().containsKey(PotionType.Energy))
									screen.render(i * 8, Screen.h - 8, 1 + 5 * 32, 0, 3);
								else screen.render(i * 8, Screen.h - 8, 1 + 2 * 32, 0, 3);
							} else {
								if (player.getPotionEffects().containsKey(PotionType.Energy))
									screen.render(i * 8, Screen.h - 8, 1 + 6 * 32, 0, 3);
								else screen.render(i * 8, Screen.h - 8, 1 + 3 * 32, 0, 3);
							}
						}

						// Renders hunger

						wiggleHU = !Updater.paused && (player.hunger < 5 || (player.stamHungerTicks < 40 && player.hungerStamCnt < 2)) ? (int) Math.round(Math.random() * 2) - 2 : 0;
						// Renders your current burgers, or null burgers.
						if (i < player.hunger && hunmod >= 2) {
							screen.render(i * 8 + (Screen.w - 80), Screen.h - (16 + wiggleHU), 2 + (2 + hungered) * 32, 0, 3); //full
							if (player.getPotionEffects().containsKey(PotionType.WellFed))
								screen.render(i * 8 + (Screen.w - 80), Screen.h - (16 + wiggleHU), 2 + 8 * 32, 0, 3); //wellfed
						} else if (hunmod == 1) {
							screen.render(i * 8 + (Screen.w - 80), Screen.h - (16 + wiggleHU), 2 + (3 + hungered) * 32, 0, 3); //half
							if (player.getPotionEffects().containsKey(PotionType.WellFed))
								screen.render(i * 8 + (Screen.w - 80), Screen.h - (16 + wiggleHU), 2 + 9 * 32, 0, 3); //wellfed
							hunmod -= 1;
						} else if (hunmod <= 0) {
							screen.render(i * 8 + (Screen.w - 80), Screen.h - 16, 2 + (4 + hungered) * 32, 0, 3); //empty
						}
						hunmod -= 2;


						// Renders your current droplets, or null droplets.
						wiggleTH = !Updater.paused && (player.thirst < 5 || (player.stamThirstTicks < 40 && player.thirstStamCnt < 2)) ? (int) Math.round(Math.random() * 2) - 2 : 0;
						if (i < player.thirst && thimod >= 2) {
							screen.render(i * 8 + (Screen.w - 80), Screen.h - (8 + wiggleTH), 7 + (2 + thirsted) * 32, 0, 3); //full
							if (player.getPotionEffects().containsKey(PotionType.WellHydrated))
								screen.render(i * 8 + (Screen.w - 80), Screen.h - (8 + wiggleTH), 7 + 8 * 32, 0, 3); //wellfed
						} else if (thimod == 1) {
							screen.render(i * 8 + (Screen.w - 80), Screen.h - (8 + wiggleTH), 7 + (3 + thirsted) * 32, 0, 3); //half
							if (player.getPotionEffects().containsKey(PotionType.WellHydrated))
								screen.render(i * 8 + (Screen.w - 80), Screen.h - (8 + wiggleTH), 7 + 9 * 32, 0, 3); //wellfed
							thimod -= 1;
						} else if (thimod <= 0) {
							screen.render(i * 8 + (Screen.w - 80), Screen.h - 8, 7 + (4 + thirsted) * 32, 0, 3); //empty
						}
						thimod -= 2;

					}
				}
			}
			renderDebugInfo();
		}
	}

	private static void renderDebugInfo() {
		Level lvlList[][] = {World.levels,World.obvLevels};
		int textcol = Color.WHITE;

		if (showinfo) { // Renders show debug info on the screen.
			ArrayList<String> info = new ArrayList<>();
			info.add("VERSION: "+TitleDisplay.version);
			info.add(Initializer.fra + " fps");
				info.add("Cur day ticks: " + Updater.tickCount + " (" +(player.getRealmId() == 0 ? Updater.getTime() : "N/A" )+")");
				info.add("Day: " + (int) (Math.floor(Updater.gameTime / Updater.dayLength) + 1));
			info.add((Updater.normSpeed * Updater.gamespeed) + " tps");

			if (!isValidServer()) {
				//info.add("walk spd: " + player.moveSpeed);
				info.add("X: " + (player.x / 16) + "-" + (player.x % 16));
				info.add("Y: " + (player.y / 16) + "-" + (player.y % 16));
				if ( lvlList[player.getRealmId()][currentLevel] != null)
					info.add("Tile: " +  lvlList[player.getRealmId()][currentLevel].getTile(player.x >> 4, player.y >> 4).name);
				//	info.add("Data: " + levels[currentLevel].getData(player.x >> 4, player.y >> 4));
				if (isMode("score")) info.add("Score: " + player.getScore());
			}

			if (lvlList[player.getRealmId()][currentLevel] != null) {
				if (!isValidClient())
					info.add("Mob cnt: " + lvlList[player.getRealmId()][currentLevel].mobCount + "/" + lvlList[player.getRealmId()][currentLevel].maxMobCount);
				else
					info.add("Mob load cnt: " + lvlList[player.getRealmId()][currentLevel].mobCount);
			}
			// Displays number of chests left, if on dungeon level.
			if (lvlList[player.getRealmId()][currentLevel] != null /*&& (isValidServer() && !isValidClient())*/) {
				if (player.getLevel().chestCount > 0)
					info.add("Chests: " + player.getLevel().chestCount);
				else if((player.getRealmId() == 1 && currentLevel < 2) || (player.getRealmId()==0 && currentLevel ==0))
					info.add("Chests: Complete!");
			}

			if (!isValidServer()) {
				info.add("Hunger stam: " + player.getDebugHunger());
				info.add("Thirst stam: " + player.getDebugThirst());
				if (player.armor > 0) {
					info.add("Armor: " + player.armor);
					info.add("Dam buffer: " + player.armorDamageBuffer);
				}
			}

			if ( lvlList[player.getRealmId()][currentLevel] != null) {
				info.add("Seed: " +  lvlList[player.getRealmId()][currentLevel].getSeed());
				info.add("Level name: " + Level.getLevelName( lvlList[player.getRealmId()][currentLevel].depth,player.getRealmId()));
				info.add("Level realm: " + World.realms[player.getRealmId()]);
			}

			FontStyle style = new FontStyle(textcol).setShadowType(Color.BLACK, true).setXPos(1);

			if (Game.isValidServer()) {
				style.setYPos(Screen.h).setRelTextPos(RelPos.TOP_RIGHT, true);
				for (int i = 1; i < info.size(); i++) // Reverse order
					info.add(0, info.remove(i));
			} else
				style.setYPos(2);
			Font.drawParagraph(info, screen, style, 2);
		}
	}

	/** Renders the "Click to focus" box when you click off the screen. */
	private static void renderFocusNagger() {
		int count=0;
		String msg = "Click to focus!"; // The message when you click off the screen.

		Updater.paused = true; // Perhaps paused is only used for this.
		int xx = (Screen.w - Font.textWidth(msg)) / 2; // The width of the box
		int yy = (HEIGHT - 8) / 2; // The height of the box
		int w = msg.length(); // Length of message in characters.
		int h = 1;

		// Renders the four corners of the box
		screen.render(xx - 8, yy - 8, 15 + 0 * 32, 0, 3);
		screen.render(xx + w * 8, yy - 8, 15 + 0 * 32, 1, 3);
		screen.render(xx - 8, yy + 8, 15 + 0 * 32, 2, 3);
		screen.render(xx + w * 8, yy + 8, 15 + 0 * 32, 3, 3);

		// Renders each part of the box...
		for (int x = 0; x < w; x++) {
			screen.render(xx + x * 8, yy - 8, 16 + 0 * 32, 0, 3); // ...Top part
			screen.render(xx + x * 8, yy + 8, 16 + 0 * 32, 2, 3); // ...Bottom part
		}
		for (int y = 0; y < h; y++) {
			screen.render(xx - 8, yy + y * 8, 17 + 0 * 32, 0, 3); // ...Left part
			screen.render(xx + w * 8, yy + y * 8, 17 + 0 * 32, 1, 3); // ...Right part
		}

		// The middle
		for (int x = 0; x < w; x++) {
			screen.render(xx + x * 8, yy, 18 + 0 * 32, 0, 3);
		}
		Font.draw(msg, screen, xx, yy, Color.get(1, 5*51, 5*51, 5*25));
	}


	static java.awt.Dimension getWindowSize() {
		return new java.awt.Dimension(new Float(WIDTH * SCALE).intValue(), new Float(HEIGHT * SCALE).intValue());
	}
}