package minicraft.gfx;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.item.PotionType;

public class MobSprite extends Sprite {
	/**
	 This class is meant specifically for mobs, becuase they have a special way of flipping and such. It's not only the pixels, as much as the whole sprite flips.
	 */

	public MobSprite(int sx, int sy, int w, int h, int mirror, int sheet) {
		/// This assumes the pixels are all neatly laid out on the spreadsheet, and should be flipped in position according to their mirroring.
		super(new Pixel[h][w]);

		boolean flipX = (0x01 & mirror) > 0, flipY = (0x02 & mirror) > 0;

		for (int r = 0; r < spritePixels.length; r++) { // Loop down through each row
			for (int c = 0; c < spritePixels[r].length; c++) { // Loop across through each column
				// The offsets are there to determine the pixel that will be there: the one in order, or on the opposite side.
				int xOffset = flipX ? spritePixels[r].length-1 - c : c;
				int yOffset = flipY ? spritePixels.length-1 - r : r;
				spritePixels[r][c] = new Px(sx + xOffset, sy+yOffset, mirror, sheet);
			}
		}
	}
	public MobSprite(int sx, int sy, int w, int h, int mirror, SpriteSheet sheet) {
		/// This assumes the pixels are all neatly laid out on the spreadsheet, and should be flipped in position according to their mirroring.
		super(new Pixel[h][w]);

		boolean flipX = (0x01 & mirror) > 0, flipY = (0x02 & mirror) > 0;

		for (int r = 0; r < spritePixels.length; r++) { // Loop down through each row
			for (int c = 0; c < spritePixels[r].length; c++) { // Loop across through each column
				// The offsets are there to determine the pixel that will be there: the one in order, or on the opposite side.
				int xOffset = flipX ? spritePixels[r].length-1 - c : c;
				int yOffset = flipY ? spritePixels.length-1 - r : r;
				spritePixels[r][c] = new Px(sx+xOffset, sy+yOffset, mirror, sheet);
			}
		}
	}

	/** This is an easy way to make a list of sprites that are all part of the same "Sprite", so they have similar parameters, but they're just at different locations on the spreadsheet. */
	public static MobSprite[] compileSpriteList(int sheetX, int sheetY, int width, int height, int mirror, int number) {
		MobSprite[] sprites = new MobSprite[number];
		for (int i = 0; i < sprites.length; i++)
			sprites[i] = new MobSprite(sheetX + width * i, sheetY, width, height, mirror, 2);

		return sprites;
	}

	public static MobSprite[] compilePlayerSpriteList(int sheetX, int sheetY, int width, int height, int mirror, int number) {
		MobSprite[] sprites = new MobSprite[number];
		for (int i = 0; i < sprites.length; i++)
			sprites[i] = new MobSprite(sheetX + width * i, sheetY, width, height, mirror, 4);

		return sprites;
	}
	public static MobSprite[] compilePlayerSpriteList(int sheetX, int sheetY, int width, int height, int mirror, int number, SpriteSheet sheet) {
		MobSprite[] sprites = new MobSprite[number];
		for (int i = 0; i < sprites.length; i++)
			sprites[i] = new MobSprite(sheetX + width * i, sheetY, width, height, mirror, sheet);

		return sprites;
	}

	public static MobSprite[][] compileMobSpriteAnimations(int sheetX, int sheetY) {
		return compileMobSpriteAnimations(sheetX,sheetY,2,2);
	}
	public static MobSprite[][] compileMobSpriteAnimations(int sheetX, int sheetY,int spriteW,int spriteH) {
		MobSprite[][] sprites = new MobSprite[4][2];
		// dir numbers: 0=down, 1=up, 2=left, 3=right.
		/// On the spritesheet, most mobs have 4 sprites there, first facing down, then up, then right 1, then right 2. The first two get flipped to animate them, but the last two get flipped to change direction.

		// Contents: down 1, up 1, right 1, right 2
		MobSprite[] set1 = MobSprite.compileSpriteList(sheetX , sheetY, spriteW, spriteH, 0, 4);

		// Contents: down 2, up 2, left 1, left 2
		MobSprite[] set2 = MobSprite.compileSpriteList(sheetX, sheetY, spriteW, spriteH, 1, 4);

		// Down
		sprites[0][0] = set1[0];
		sprites[0][1] = set2[0];

		// Up
		sprites[1][0] = set1[1];
		sprites[1][1] = set2[1];

		// Left
		sprites[2][0] = set2[2];
		sprites[2][1] = set2[3];

		// Right
		sprites[3][0] = set1[2];
		sprites[3][1] = set1[3];

		return sprites;
	}

	public static MobSprite[][] compilePlayerSpriteAnimations(int sheetX, int sheetY) {
		MobSprite[][] sprites = new MobSprite[4][2];
		// dir numbers: 0=down, 1=up, 2=left, 3=right.
		/// On the spritesheet, most mobs have 4 sprites there, first facing down, then up, then right 1, then right 2. The first two get flipped to animate them, but the last two get flipped to change direction.

		// Contents: down 1, up 1, right 1, right 2
		MobSprite[] set1 = MobSprite.compilePlayerSpriteList(sheetX, sheetY, 2, 2, 0, 4);

		// Contents: down 2, up 2, left 1, left 2
		MobSprite[] set2 = MobSprite.compilePlayerSpriteList(sheetX, sheetY, 2, 2, 1, 4);

		// Down
		sprites[0][0] = set1[0];
		sprites[0][1] = set2[0];

		// Up
		sprites[1][0] = set1[1];
		sprites[1][1] = set2[1];

		// Left
		sprites[2][0] = set2[2];
		sprites[2][1] = set2[3];

		// Right
		sprites[3][0] = set1[2];
		sprites[3][1] = set1[3];

		return sprites;
	}

	public static MobSprite[][] compileCustomPlayerSpriteAnimations(int sheetX, int sheetY, SpriteSheet sheet) {
		MobSprite[][] sprites = new MobSprite[4][2];
		// dir numbers: 0=down, 1=up, 2=left, 3=right.
		/// On the spritesheet, most mobs have 4 sprites there, first facing down, then up, then right 1, then right 2. The first two get flipped to animate them, but the last two get flipped to change direction.

		// Contents: down 1, up 1, right 1, right 2
		MobSprite[] set1 = MobSprite.compilePlayerSpriteList(sheetX, sheetY, 2, 2, 0, 4, sheet);

		// Contents: down 2, up 2, left 1, left 2
		MobSprite[] set2 = MobSprite.compilePlayerSpriteList(sheetX, sheetY, 2, 2, 1, 4, sheet);

		// Down
		sprites[0][0] = set1[0];
		sprites[0][1] = set2[0];

		// Up
		sprites[1][0] = set1[1];
		sprites[1][1] = set2[1];

		// Left
		sprites[2][0] = set2[2];
		sprites[2][1] = set2[3];

		// Right
		sprites[3][0] = set1[2];
		sprites[3][1] = set1[3];

		return sprites;
	}

	public void render(Screen screen, int x, int y, boolean fullbright) {
		for (int row = 0; row < spritePixels.length; row++) { // Loop down through each row
			renderRow(row, screen, x, y + row * 8, fullbright);
		}
	}

	public void renderRow(int r, Screen screen, int x, int y, boolean fullbright) {
		Pixel[] row = spritePixels[r];
		for (int c = 0; c < row.length; c++) { // Loop across through each column
			screen.render(x + c*8, y, row[c], -1, fullbright); // Render the sprite pixel.
		}
	}
}
