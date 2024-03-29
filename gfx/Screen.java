package minicraft.gfx;

import java.util.Arrays;

import minicraft.item.PotionType;
import minicraft.level.tile.Tiles;
import org.jetbrains.annotations.NotNull;

import minicraft.core.Renderer;
import minicraft.core.Updater;
import sun.java2d.pipe.RenderQueue;

public class Screen {

	public static final int w = Renderer.WIDTH; // Width of the screen
	public static final int h = Renderer.HEIGHT; // Height of the screen
	public static final Point center = new Point(w/2, h/2);
	public static int reduce=0;

	private static final int MAXDARK = 128;
	private static int redu=0;
	private static final int MAXRED = Color.get(1,128,0,0);


	/// x and y offset of screen:
	private int xOffset;
	private int yOffset;

	// Used for mirroring an image:
	private static final int BIT_MIRROR_X = 0x01; // Written in hexadecimal; binary: 01
	private static final int BIT_MIRROR_Y = 0x02; // Binary: 10

	public int[] pixels; // Pixels on the screen

	// Since each sheet is 256x256 pixels, each one has 1024 8x8 "tiles"
	// So 0 is the start of the item sheet 1024 the start of the tile sheet, 2048 the start of the entity sheet,
	// And 3072 the start of the gui sheet

	private SpriteSheet[] sheets;

	public Screen(SpriteSheet itemSheet, SpriteSheet tileSheet, SpriteSheet entitySheet, SpriteSheet guiSheet, SpriteSheet skinsSheet) {

		sheets = new SpriteSheet[]{itemSheet, tileSheet, entitySheet, guiSheet, skinsSheet};

		/// Screen width and height are determined by the actual game window size, meaning the screen is only as big as the window.
		pixels = new int[Screen.w * Screen.h]; // Makes new integer array for all the pixels on the screen.
	}
	public Screen(Screen model) {
		this(model.sheets[0], model.sheets[1], model.sheets[2], model.sheets[3], model.sheets[4]);
	}

	@NotNull
	public void setSkinSheet(SpriteSheet skinSheet) {
		sheets[4] = skinSheet;
	}

	public void setSheets(SpriteSheet itemSheet, SpriteSheet tileSheet, SpriteSheet entitySheet, SpriteSheet guiSheet) {
		sheets[0] = itemSheet;
		sheets[1] = tileSheet;
		sheets[2] = entitySheet;
		sheets[3] = guiSheet;
	}

	public SpriteSheet getSpriteSheet() {
		return sheets[4];
	}

	/** Clears all the colors on the screen */
	public void clear(int color) {
		// Turns each pixel into a single color (clearing the screen!)
		Arrays.fill(pixels, color);
	}

	public void render(int[] pixelColors) {
		System.arraycopy(pixelColors, 0, pixels, 0, Math.min(pixelColors.length, pixels.length));
	}

	public void render(int xp, int yp, int tile, int bits) {
		render(xp, yp, tile, bits, 0);
	}

	public void render(int xp, int yp, int tile, int bits, int sheet) {
		render(xp, yp, tile, bits, sheet, -1);
	}

	public void render(int xp, int yp, int tile, int bits, int sheet, int whiteTint) {
		render(xp, yp, tile, bits, sheet, whiteTint, false);
	}

	public void render(int xp, int yp, int tile, int bits, int sheet, int whiteTint, boolean fullbright) {
		render(xp, yp, tile % 32, tile / 32, bits, sheet, whiteTint, fullbright, 0);
	}

	public void render(int xp, int yp, int tile, int bits, int sheet, int whiteTint, boolean fullbright, int color) {
		render(xp, yp, tile % 32, tile / 32, bits, sheet, -1, false, color);
	}

	public void render(int xp, int yp, Pixel pixel) {
		render(xp, yp, pixel, -1);
	}

	public void render(int xp, int yp, Pixel pixel, int whiteTint) {
		render(xp, yp, pixel, whiteTint, false);
	}

	public void render(int xp, int yp, Pixel pixel, int whiteTint, boolean fullbright) {
		render(xp, yp, pixel.getX(), pixel.getY(), pixel.getMirror(), pixel.getIndex(), whiteTint, fullbright, 0);
	}

	public void render(int xp, int yp, Pixel pixel, int whiteTint, boolean fullbright, int color) {
		render(xp, yp, pixel.getX(), pixel.getY(), pixel.getMirror(), pixel.getIndex(), whiteTint, fullbright, color);
	}

	public void render(int xp, int yp, Pixel pixel, int bits, int whiteTint, boolean fullbright) {
		render(xp, yp, pixel.getX(), pixel.getY(), bits, pixel.getIndex(), whiteTint, fullbright, 0);
	}

	public void render(int xp, int yp, Pixel pixel, int bits, int whiteTint, boolean fullbright, int color) {
		render(xp, yp, pixel.getX(), pixel.getY(), bits, pixel.getIndex(), whiteTint, fullbright, color);
	}


	/** Renders an object from the sprite sheet based on screen coordinates, tile (SpriteSheet location), colors, and bits (for mirroring).
	 *  I believe that xp and yp refer to the desired position of the upper-left-most pixel.
	 */
	private void render(int xp, int yp, int xTile, int yTile, int bits, int sheet, int whiteTint, boolean fullbright, int color) {
		// xp and yp are originally in level coordinates, but offset turns them to screen coordinates.

		xp -= xOffset; // account for screen offset
		yp -= yOffset;

		// determines if the image should be mirrored...
		boolean mirrorX = (bits & BIT_MIRROR_X) > 0; // horizontally.
		boolean mirrorY = (bits & BIT_MIRROR_Y) > 0; // vertically.

		SpriteSheet currentSheet;
		currentSheet = sheets[sheet];

		xTile %= currentSheet.width; // to avoid out of bounds
		yTile %= currentSheet.height; // ^

		// Gets the offset of the sprite into the spritesheet
		// pixel array, the 8's represent the size of the box.
		// (8 by 8 pixel sprite boxes)
		int toffs = xTile * 8 + yTile * 8 * currentSheet.width;

		/// THIS LOOPS FOR EVERY LITTLE PIXEL
		for (int y = 0; y < 8; y++) { // Loops 8 times (because of the height of the tile)
			int ys = y; // current y pixel
			if (mirrorY) {
				ys = 7 - y; // Reverses the pixel for a mirroring effect
			}
			if (y + yp < 0 || y + yp >= h) {
				continue; // If the pixel is out of bounds, then skip the rest of the loop.
			}
			for (int x = 0; x < 8; x++) { // Loops 8 times (because of the width of the tile)
				if (x + xp < 0 || x + xp >= w) {
					continue; // skip rest if out of bounds.
				}

				int xs = x; // current x pixel
				if (mirrorX) {
					xs = 7 - x; // Reverses the pixel for a mirroring effect
				}

				// Gets the color of the current pixel from the value stored in the sheet.
				int col = currentSheet.pixels[toffs + xs + ys * currentSheet.width];

				boolean isTransparent = (col >> 24 == 0);

				if (!isTransparent) {
					int position = (x + xp) + (y + yp) * w;

					if (whiteTint != -1 && col == 0x1FFFFFF) {
						// if this is white, write the whiteTint over it
						pixels[position] = Color.upgrade(whiteTint);
					} else {
						// Inserts the colors into the image
						if (fullbright) {
							pixels[position] = Color.WHITE; // mob color when hit
						} else {
							if (color != 0) { // full sprite color
								pixels[position] = color;
							} else {
								pixels[position] = Color.upgrade(col);
							}
						}
					}
				}
			}
		}
	}

	/** Sets the offset of the screen */
	public void setOffset(int xOffset, int yOffset) {
		// This is called in few places, one of which is level.renderBackground, right before all the tiles are rendered. The offset is determined by the Game class (this only place renderBackground is called), by using the screen's width and the player's position in the level.
		// In other words, the offset is a conversion factor from level coordinates to screen coordinates. It makes a certain coord in the level the upper left corner of the screen, when subtracted from the tile coord.

		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}

	/* Used for the scattered dots at the edge of the light radius underground.

		These values represent the minimum light level, on a scale from 0 to 25 (255/10), 0 being no light, 25 being full light (which will be portrayed as transparent on the overlay lightScreen pixels) that a pixel must have in order to remain lit (not black).
		each row and column is repeated every 4 pixels in the proper direction, so the pixel lightness minimum varies. It's highly worth note that, as the rows progress and loop, there's two sets or rows (1,4 and 2,3) whose values in the same column add to 15. The exact same is true for columns (sets are also 1,4 and 2,3), execpt the sums of values in the same row and set differ for each row: 10, 18, 12, 20. Which... themselves... are another set... adding to 30... which makes sense, sort of, since each column totals 15+15=30.
		In the end, "every other every row", will need, for example in column 1, 15 light to be lit, then 0 light to be lit, then 12 light to be lit, then 3 light to be lit. So, the pixels of lower light levels will generally be lit every other pixel, while the brighter ones appear more often. The reason for the variance in values is to provide EVERY number between 0 and 15, so that all possible light levels (below 16) are represented fittingly with their own pattern of lit and not lit.
		16 is the minimum pixel lighness required to ensure that the pixel will always remain lit.
	*/
	private static final int[] dither = new int[] {
			0, 8, 2, 10,
			12, 4, 14, 6,
			3, 11, 1, 9,
			15, 7, 13, 5
	};

	/** Overlays the screen with pixels */
	public void overlay(Screen screen2, int currentLevel, int xa, int ya) {
		double tintFactor = 0;
		if (Renderer.player.getRealmId() == 0) {
			if (currentLevel >= 6) {
				int transTime = Updater.dayLength / 4;
				double relTime = (Updater.tickCount % transTime) * 1.0 / transTime;
				switch (Updater.getTime()) {
					case Dawn:
						tintFactor = MAXDARK - 10 - (Updater.tickCount / 30);
						break;
					case Morning:
						tintFactor = MAXDARK - 10 - (Updater.tickCount / 30);
						break;
					case Day:
						tintFactor = 0;
						break;
					case Dusk:
					case Evening:
						tintFactor = (relTime * MAXDARK * (Updater.tickCount / (Updater.dayLength / 2.5)) + 12 > MAXDARK - 5 ? MAXDARK - 5 : (relTime * MAXDARK * (Updater.tickCount / (Updater.dayLength / 2.2)) + 12));
						break;
					case Night:
						tintFactor = MAXDARK;
						break;
				}

				if (currentLevel > 5) tintFactor -= (tintFactor < 10 ? tintFactor : 10);
				tintFactor *= -1; // All previous operations were assuming this was a darkening factor.
			} else if (currentLevel == 0)
				tintFactor = -MAXDARK;

			int[] oPixels = screen2.pixels;  // The Integer array of pixels to overlay the screen with.
			int i = 0; // Current pixel on the screen
			for (int y = 0; y < h; y++) { // loop through height of screen
				for (int x = 0; x < w; x++) { // loop through width of screen

					if (oPixels[i] / 10 <= dither[((x + xa) & 3) + ((y + ya) & 3) * 4]) {

						/// The above if statement is simply comparing the light level stored in oPixels with the minimum light level stored in dither. if it is determined that the oPixels[i] is less than the minimum requirements, the pixel is considered "dark", and the below is executed...
						if ((currentLevel < 6 && currentLevel > 0) || Renderer.player.potionEffects.containsKey(PotionType.Blind)) { // if in caves... or blind...
							/// in the caves, not being lit means being pitch black.
							pixels[i] = 0;
						} else {
							/// Outside the caves, not being lit simply means being darker.
							if (Renderer.player.potionEffects.containsKey(PotionType.Blind)) pixels[i] = 0;
							else
								pixels[i] = Color.tintColor(pixels[i], (int) tintFactor); // darkens the color one shade.
						}
					}
					boolean chck = Updater.isbloody ? (Updater.tickCount > 37800 || Updater.tickCount < 8000) : Updater.tickCount < 8000;
					if (currentLevel >= 6) { //basically surface and above
						if ((Updater.tickCount > 37800 || Updater.tickCount < 3500) && Updater.isbloody) {
							int r = pixels[i] >> 16 & 0xFF;
							int g = pixels[i] >> 8 & 0xFF;
							int b = pixels[i] & 0xFF;
							int red = reduce > 100 ? 100 : reduce;
							pixels[i] = Color.get(1, r, (g - red < 0 ? 0 : g - red), (b - red < 0 ? 0 : b - red));
						}
						if (Updater.tickCount < 8000 && !Updater.isbloody) {
							int r = pixels[i] >> 16 & 0xFF;
							int g = pixels[i] >> 8 & 0xFF;
							int b = pixels[i] & 0xFF;
							//	System.out.println(reduce);
							int reduceG = reduce - 8 < 0 ? 0 : reduce - 8;
							if (reduceG > 40) reduceG = 40;
							pixels[i] = Color.get(1, r, g - reduceG < 0 ? 0 : g - reduceG, (b - reduce < 0 ? 0 : b - reduce));
						}

					}
					if (Renderer.player.potionEffects.containsKey(PotionType.Time)) {
						int r = pixels[i] >> 16 & 0xFF;
						int g = pixels[i] >> 8 & 0xFF;
						int b = pixels[i] & 0xFF;
						// Normalize and gamma correct:
						double rr = Math.pow(r / 255.0, 2.2);
						double gg = Math.pow(g / 255.0, 2.2);
						double bb = Math.pow(b / 255.0, 2.2);

						// Calculate luminance:
						double lum = 0.2126 * rr + 0.7152 * gg + 0.0722 * bb;

						// Gamma compand and rescale to byte range:
						int grayLevel = (int) (255.0 * Math.pow(lum, 1.0 / 2.2));
						int gray = (grayLevel << 16) + (grayLevel << 8) + grayLevel;
						if (oPixels[i] / 10 <= dither[((x + xa) & 3) + ((y + ya) & 3) * 4]) {

							pixels[i] = gray;
							pixels[i] = Color.tintColor(pixels[i], ((Updater.tickCount % 31) - 15));
						} else {
							if (Updater.tickCount % 100 > 85) pixels[i] = gray;
							else pixels[i] = Color.tintColor(pixels[i], -50 + ((Updater.tickCount % 20) - 10));
						}
					}
					if (Renderer.player.potionEffects.containsKey(PotionType.AntiTime)) {
						if (oPixels[i] / 10 <= dither[((x + xa) & 3) + ((y + ya) & 3) * 4]) {

							pixels[i] = Color.tintColor(-pixels[i], ((Updater.tickCount % 31) - 15));
						} else {
							if (Updater.tickCount % 100 > 85) pixels[i] = -pixels[i];
							else pixels[i] = Color.tintColor(pixels[i], -50 + ((Updater.tickCount % 20) - 10));
						}
					}
					// Increase the tinting of all colors by 20.
					pixels[i] = Color.tintColor(pixels[i], 20);
					i++; // Moves to the next pixel.
				}
			}
		} else {
			tintFactor = -MAXDARK;

			int[] oPixels = screen2.pixels;  // The Integer array of pixels to overlay the screen with.
			int i = 0; // Current pixel on the screen
			for (int y = 0; y < h; y++) { // loop through height of screen
				for (int x = 0; x < w; x++) { // loop through width of screen

					if (oPixels[i] / 10 <= dither[((x + xa) & 3) + ((y + ya) & 3) * 4]) {

						/// The above if statement is simply comparing the light level stored in oPixels with the minimum light level stored in dither. if it is determined that the oPixels[i] is less than the minimum requirements, the pixel is considered "dark", and the below is executed...
						if (Renderer.player.potionEffects.containsKey(PotionType.Blind)) { // if in caves... or blind...
							/// in the caves, not being lit means being pitch black.
							pixels[i] = 0;
						} else {
							/// Outside the caves, not being lit simply means being darker.
							if (Renderer.player.potionEffects.containsKey(PotionType.Blind)) pixels[i] = 0;
							else
								pixels[i] = Color.tintColor(pixels[i], (int)( currentLevel > 1 ? tintFactor/2 : tintFactor)); // darkens the color one shade.
						}
					}
					if (Renderer.player.potionEffects.containsKey(PotionType.Time)) {
						int r = pixels[i] >> 16 & 0xFF;
						int g = pixels[i] >> 8 & 0xFF;
						int b = pixels[i] & 0xFF;
						// Normalize and gamma correct:
						double rr = Math.pow(r / 255.0, 2.2);
						double gg = Math.pow(g / 255.0, 2.2);
						double bb = Math.pow(b / 255.0, 2.2);

						// Calculate luminance:
						double lum = 0.2126 * rr + 0.7152 * gg + 0.0722 * bb;

						// Gamma compand and rescale to byte range:
						int grayLevel = (int) (255.0 * Math.pow(lum, 1.0 / 2.2));
						int gray = (grayLevel << 16) + (grayLevel << 8) + grayLevel;
						if (oPixels[i] / 10 <= dither[((x + xa) & 3) + ((y + ya) & 3) * 4]) {

							pixels[i] = gray;
							pixels[i] = Color.tintColor(pixels[i], ((Updater.tickCount % 31) - 15));
						} else {
							if (Updater.tickCount % 100 > 85) pixels[i] = gray;
							else pixels[i] = Color.tintColor(pixels[i], -50 + ((Updater.tickCount % 20) - 10));
						}
					}
					if (Renderer.player.potionEffects.containsKey(PotionType.AntiTime)) {
						if (oPixels[i] / 10 <= dither[((x + xa) & 3) + ((y + ya) & 3) * 4]) {

							pixels[i] = Color.tintColor(-pixels[i], ((Updater.tickCount % 31) - 15));
						} else {
							if (Updater.tickCount % 100 > 85) pixels[i] = -pixels[i];
							else pixels[i] = Color.tintColor(pixels[i], -50 + ((Updater.tickCount % 20) - 10));
						}
					}
					// Increase the tinting of all colors by 20.
					pixels[i] = Color.tintColor(pixels[i], 20);
					i++; // Moves to the next pixel.
				}
			}
		}
	}
	public void renderLight(int x, int y, int r) {
		// Applies offsets:
		x -= xOffset;
		y -= yOffset;
		// Starting, ending, x, y, positions of the circle (of light)
		int x0 = x - r;
		int x1 = x + r;
		int y0 = y - r;
		int y1 = y + r;

		// Prevent light from rendering off the screen:
		if (x0 < 0) x0 = 0;
		if (y0 < 0) y0 = 0;
		if (x1 > w) x1 = w;
		if (y1 > h) y1 = h;

		for (int yy = y0; yy < y1; yy++) { // Loop through each y position
			int yd = yy - y; // Get distance to the previous y position.
			yd = yd * yd; // Square that distance
			for (int xx = x0; xx < x1; xx++) { // Loop though each x pos
				int xd = xx - x; // Get x delta
				int dist = xd * xd + yd; // Square x delta, then add the y delta, to get total distance.

				if (dist <= r * r) {
					// If the distance from the center (x,y) is less or equal to the radius...
					int br = 255 - dist * 255 / (r * r); // area where light will be rendered. // r*r is becuase dist is still x*x+y*y, of pythag theorem.
					// br = brightness... literally. from 0 to 255.

					if (pixels[xx + yy * w] < br) pixels[xx + yy * w] = br; // Pixel cannot be smaller than br; in other words, the pixel color (brightness) cannot be less than br.

				}

			}
		}
	}

	public void setPixel(int xp, int yp, int color) {
		// Loops 8 times (because of the height of the tile)
		for (int y = 0; y < 8; y++) {
			if (y + yp < 0 || y + yp >= h) {
				// If the pixel is out of bounds, then skip the rest of the loop.
				continue;
			}

			// Loops 8 times (because of the width of the tile)
			for (int x = 0; x < 8; x++) {
				if (x + xp < 0 || x + xp >= w) {
					// skip rest if out of bounds.
					continue;
				}

				if (color >> 24 != 0) {
					pixels[(x + xp) + (y + yp) * w] = color;
				}
			}
		}
	}
	public void darken(Screen screen2,int lvl,int xa, int ya){
		int[] oPixels = screen2.pixels;  // The Integer array of pixels to overlay the screen with.
		int i = 0; // Current pixel on the screen
		for (int y = 0; y < h; y++) { // loop through height of screen
			for (int x = 0; x < w; x++) { // loop through width of screen
				// Increase the tinting of all colors by 20.
				int r = pixels[i] >> 16 & 0xFF;
				int g = pixels[i] >> 8 & 0xFF;
				int b = pixels[i] & 0xFF;
				int darken = 100;
				pixels[i] = Color.get(1, r - darken< 0 ? 0 : r - darken, g - darken< 0 ? 0 : g - darken, (b - darken < 0 ? 0 : b - darken));
				i++; // Moves to the next pixel.
			}
		}
	}

}



