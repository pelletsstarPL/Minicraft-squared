package minicraft.level;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.level.tile.*;
import minicraft.screen.WorldGenDisplay;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

public class LevelGenObV {
	private static long worldSeed = 0;
	private static final Random random = new Random(worldSeed);
	public double[] values; // An array of doubles, used to help making noise for the map
	private int w, h; // Width and height of the map
	private static final int stairRadius = 20;
	/** This creates noise to create random values for level generation */
	public LevelGenObV(int w, int h, int featureSize) {
		this.w = w;
		this.h = h;

		values = new double[w * h];

		/// Feature size likely determines how big dungeon realm is.
		for (int y = 0; y < w; y += featureSize) {
			for (int x = 0; x < w; x += featureSize) {
				setSample(x, y, random.nextFloat() * 2 - 1); // This method sets the random value from -1 to 1 at the given coordinate.
			}
		}

		int stepSize = featureSize;
		double scale = 2 / w;
		double scaleMod = 1;
		do {
			int halfStep = stepSize / 2;
			for (int y = 0; y < h; y += stepSize) {
				for (int x = 0; x < w; x += stepSize) { // This loops through the values again, by a given increment...
					double a = sample(x, y); // Fetches the value at the coordinate set previously (it fetches the exact same ones that were just set above)
					double b = sample(x + stepSize, y); // Fetches the value at the next coordinate over. This could possibly loop over at the end, and fetch the first value in the row instead.
					double c = sample(x, y + stepSize); // Fetches the next value down, possibly looping back to the top of the column. 
					double d = sample(x + stepSize, y + stepSize); // Fetches the value one down, one right.

					/**
					 * This could probably use some explaining... Note: the number values are probably only good the first time around...
					 *
					 * This starts with taking the average of the four numbers from before (they form a little square in adjacent tiles), each of which holds a value from -1 to 1.
					 * Then, it basically adds a 5th number, generated the same way as before. However, this 5th number is multiplied by a few things first...
					 * ...by stepSize, aka featureSize, and scale, which is 2/size the first time. featureSize is 16 or 32, which is a multiple of the common level size, 128.
					 * Precisely, it is 128 / 8, or 128 / 4, respectively with 16 and 32. So, the equation becomes size / const * 2 / size, or, simplified, 2 / const.
					 * For a feature size of 32, stepSize * scale = 2 / 4 = 1/2. featureSize of 16, it's 2 / 8 = 1/4. Later on, this gets closer to 4 / 4 = 1, so... the 5th value may not change much at all in later iterations for a feature size of 32, which means it has an effect of 1, which is actually quite significant to the value that is set.
					 * So, it tends to decrease the 5th -1 or 1 number, sometimes making it of equal value to the other 4 numbers, sort of. It will usually change the end result by 0.5 to 0.25, perhaps; at max.
					 */
					double e = (a + b + c + d) / 4.0 + (random.nextFloat() * 2 - 1) * stepSize * scale;
					setSample(x + halfStep, y + halfStep, e); // This sets the value that is right in the middle of the other 4 to an average of the four, plus a 5th number, which makes it slightly off, differing by about 0.25 or so on average, the first time around.
				}
			}

			// This loop does the same as before, but it takes into account some of the half steps we set in the last loop.
			for (int y = 0; y < h; y += stepSize) {
				for (int x = 0; x < w; x += stepSize) {
					double a = sample(x, y); // middle (current) tile
					double b = sample(x + stepSize, y); // right tile
					double c = sample(x, y + stepSize); // bottom tile
					double d = sample(x + halfStep, y + halfStep); // mid-right, mid-bottom tile
					double e = sample(x + halfStep, y - halfStep); // mid-right, mid-top tile
					double f = sample(x - halfStep, y + halfStep); // mid-left, mid-bottom tile

					// The 0.5 at the end is because we are going by half-steps..?
					// The H is for the right and surrounding mids, and g is the bottom and surrounding mids. 
					double H = (a + b + d + e) / 4.0 + (random.nextFloat() * 2 - 1) * stepSize * scale * 0.5; // Adds middle, right, mr-mb, mr-mt, and random.
					double g = (a + c + d + f) / 4.0 + (random.nextFloat() * 2 - 1) * stepSize * scale * 0.5; // Adds middle, bottom, mr-mb, ml-mb, and random.
					setSample(x + halfStep, y, H); // Sets the H to the mid-right 
					setSample(x, y + halfStep, g); // Sets the g to the mid-bottom
				}
			}

			/**
			 * THEN... this stuff is set to repeat the system all over again!
			 * The featureSize is halved, allowing access to further unset mids, and the scale changes...
			 * The scale increases the first time, x1.8, but the second time it's x1.1, and after that probably a little less than 1. So, it generally increases a bit, maybe to 4 / w at tops. This results in the 5th random value being more significant than the first 4 ones in later iterations. 
			 */
			stepSize /= 2;
			scale *= (scaleMod + 0.8);
			scaleMod *= 0.3;
		} while (stepSize > 1); // This stops when the stepsize is < 1, aka 0 b/c it's an int. At this point there are no more mid values.
	}

	private double sample(int x, int y) {
		return values[(x & (w - 1)) + (y & (h - 1)) * w];
	} // This merely returns the value, like Level.getTile(x, y).

	private void setSample(int x, int y, double value) {
		/**
		 * This method is short, but difficult to understand. This is what I think it does:
		 *
		 * The values array is like a 2D array, but formatted into a 1D array; so the basic "x + y * w" is used to access a given value.
		 *
		 * The value parameter is a random number, above set to be a random decimal from -1 to 1.
		 *
		 * From above, we can see that the x and y values passed in range from 0 to the width/height, and increment by a certain constant known as the "featureSize".
		 * This implies that the locations chosen from this array, to put the random value in, somehow determine the size of biomes, perhaps.
		 * The x/y value is taken and AND'ed with the size-1, which could be 127. This just caps the value at 127; however, it shouldn't be higher in the first place, so it is merely a safety measure.
		 *
		 * In other words, this is just "values[x + y * w] = value;"
		 */
		values[(x & (w - 1)) + (y & (h - 1)) * w] = value;
	}

	@Nullable
	static short[][] createAndValidateMap(int w, int h, int level) {
		worldSeed = WorldGenDisplay.getSeed();
		if(level==1) return createAndValidateBridgeFloor(w,h);
		if(level==0) return createAndValidateSurface(w, h);
		else  if(level>=-2) return createAndValidateDungeon(w, h,level);


		System.err.println("LevelGen ERROR: level index is not valid. Could not generate a level.");

		return null;
	}

	private static short[][] createAndValidateSurface(int w, int h) {
		random.setSeed(worldSeed);
		do {
			short[][] result = createSurface(w, h);

			int[] count = new int[256];

			for (int i = 0; i < w * h; i++) {
				count[result[0][i] & 0xff]++;
			}
			if (count[Tiles.get("lava").id & 0xff] < 100) continue;
			if (count[Tiles.get("obsidian").id & 0xff] < 100) continue;
			if (count[Tiles.get("obsidian wall").id & 0xff] < 100) continue;
			if (count[Tiles.get("Obsidian Stairs Down").id & 0xff] == 0) continue; // size 128 = 6 and 1 more for each 128 in size above 128 stairs min

			return result;

		} while (true);
	}

	private static short[][] createAndValidateBridgeFloor(int w, int h) {
		random.setSeed(worldSeed);

		do {
			short[][] result = createBridgeFloor(w, h);

			int[] count = new int[256];

			for (int i = 0; i < w * h; i++) {
				count[result[0][i] & 0xffff]++;
			}
			if (count[Tiles.get("Obsidian Stairs Down").id & 0xffff] < w / 64)
				continue; // size 128 = 2 stairs min
			if (count[Tiles.get("obsidian").id & 0xffff] < 100)
				continue;

			return result;

		} while (true);
	}

	private static short[][] createAndValidateDungeon(int w, int h,int depth) {
		random.setSeed(worldSeed);

		do {
			short[][] result = createDungeon(w, h,depth);

			int[] count = new int[256];

			for (int i = 0; i < w * h; i++) {
				count[result[0][i] & 0xff]++;
			}
			if (count[Tiles.get("Obsidian").id & 0xff] < 100) continue;
			if(depth > -3) {
				if (count[Tiles.get("Raw Obsidian").id & 0xff] < 100) continue;
				if (count[Tiles.get("Lava Brick").id & 0xff] < 100) continue;
			}
			if (count[Tiles.get("Obsidian Wall").id & 0xff] < 100) continue;

			return result;

		} while (true);
	}

	private static short[][] createBridgeFloor(int w, int h) { // Create map of the bridge floor
		LevelGen noise1 = new LevelGen(w, h, 8);
		LevelGen noise2 = new LevelGen(w, h, 8);

		short[] map = new short[w * h];
		short[] data = new short[w * h];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int i = x + y * w;
				map[i]=Tiles.get("infinite void").id;
			}
		}



		for(int l=0;l<h/128;l++)  //horizontally
			for(int k=64;k<w-64;k++) {
				if(k%16 <= 1 || k%16==15){
					map[ k+((l*128) + 62) * w] = Tiles.get("Decorated Obsidian").id;
					map[ k+((l*128) + 65)* w] = Tiles.get("Decorated Obsidian").id;
				}
				map[ k+((l*128) + 64) * w] = Tiles.get("obsidian").id;
				map[ k+((l*128) + 63)* w] = Tiles.get("ornate obsidian").id;
				map[ k+((l*128) + 65)* w] = Tiles.get("ornate obsidian").id;
			}
		for(int l=64;l<h-64;l++)  //vertically
			for(int k=0;k<w/128;k++) {
				if(l%16 <= 1 || l%16==15){
					map[((k*128) + 62) + l * w] = Tiles.get("Decorated Obsidian").id;
					map[((k*128) + 66) + l * w] = Tiles.get("Decorated Obsidian").id;
				}
				map[((k*128) + 64) + l * w] = Tiles.get("obsidian").id;
				map[((k*128) + 63) + l * w] = Tiles.get("ornate obsidian").id;
				map[((k*128) + 65) + l * w] = Tiles.get("ornate obsidian").id;
			}
	Integer[] coordsX= new Integer[4*(w/128) + ((w/128)*(h/128))];
	Integer[] coordsY= new Integer[4*(h/128) + ((w/128)*(h/128))];
		int countBig =0;
		for(int l=0;l<h/128;l++)
			for(int k=0;k<w/128;k++) {
				coordsY[countBig] = (k * 128) + 64;
				coordsX[countBig] = (l* 128) + 64;
				if(countBig< ((w/128)*(h/128)))countBig++;
				Structure.dungeonBigTowerRoom.draw(map, (k * 128) + 64, (l * 128) + 64, w);
			}
		//generating big rooms but... only in one we will have sth special (maybe 3.3, 3.4 we'll see)

		int count =0;
		stairsLoop:
		for (int i = 0; i < w * h / 100; i++) { // Loops a certain number of times, more for bigger world sizes.
			int x = random.nextInt(w - 2) + 1;
			int y = random.nextInt(h - 2) + 1;

			// The first loop, which checks to make sure that a new stairs tile will be completely surrounded by rock.
			for (int yy = y - 1; yy <= y + 1; yy++)
				for (int xx = x - 1; xx <= x + 1; xx++)
					if (map[xx + yy * w] != Tiles.get("infinite void").id)
						continue stairsLoop;

			// This should prevent any stairsDown tile from being within 30 tiles of any other stairsDown tile.
			for (int yy = Math.max(0, y - stairRadius); yy <= Math.min(h - 1, y + stairRadius); yy++)
				for (int xx = Math.max(0, x - stairRadius); xx <= Math.min(w - 1, x + stairRadius); xx++)
					if (map[xx + yy * w] == Tiles.get("Obsidian Stairs Down").id)
						continue stairsLoop;


			if(x<16)x=16 ;
			if(x>w-16)x=w -16;
			if(y<16)y=16;
			if(y>h-16)y=h -  16;
			x =  (int)Math.floor(x/16) * 16;
			y =  (int)Math.floor(y/16) * 16;
			Structure.dungeonTowerTop.draw(map, x, y, w);
			map[x + y * w] = Tiles.get("Obsidian Stairs Down").id;
			count++;
			coordsX[count+countBig - 1] = x;
			coordsY[count+countBig  - 1] = y;
			if (count>=(4 *( w/128))) break;
		}
	for(int k=0;k<coordsX.length;k++)
	for(int i=0;i<coordsX.length;i++){
		int startX = coordsX[k];
		int startY = coordsY[k];
		int curX = startX;
		int curY = startY;
		int endX = coordsX[i% coordsX.length];
		int endY = coordsY[i%coordsY.length];
		//X
		if(i%2==0)
		if(startX<endX){ //right

			for(int j=startX;j<=(endX > (80 -(4 - (Math.ceil(i/4)*4))) ? 80-(4 - (Math.ceil(i/4)*4)) : endX);j++){

				if(map[j + startY * w] == Tiles.get("infinite void").id || map[j + startY * w] == Tiles.get("Obsidian bridge support").id){
					map[j +( startY-1) * w] = Tiles.get("obsidian").id;data[j +( startY-1) * w] =2;
					map[j + startY * w] = Tiles.get("ornate obsidian").id;data[j +( startY) * w] =2;
					map[j +( startY+1) * w] = Tiles.get("obsidian").id;data[j +( startY+1) * w] =2;
				}
				curX = j;
			}

		}else if(startX>endX) //left
			for(int j=startX;j>=(endX > (80 -(4 - (Math.ceil(i/4)*4))) ? 80-(4 - (Math.ceil(i/4)*4)) : endX);j--){
				if(map[j + startY * w] == Tiles.get("infinite void").id  || map[j + startY * w] == Tiles.get("Obsidian bridge support").id){
					map[j +( startY-1) * w] = Tiles.get("obsidian").id;data[j +( startY-1) * w] =2;
					map[j + startY * w] = Tiles.get("ornate obsidian").id;data[j +( startY) * w] =2;
					map[j +( startY+1) * w] = Tiles.get("obsidian").id;data[j +( startY+1) * w] =2;
				}
				curX = j;
			}
			//Y
		if(i%2==1)
		if(startY<endY){
			for(int j=startY;j<=(endY  > (80 -(4 - (Math.ceil(i/4)*4))) ? 80-(4 - (Math.ceil(i/4)*4)) : endY);j++) {//down
			if(map[curX + j * w] == Tiles.get("infinite void").id  || map[curX + j * w] == Tiles.get("Obsidian bridge support").id) {
				map[(curX-1) + j * w] = Tiles.get("obsidian").id;data[(curX-1) + j * w] =2;
				map[curX + j * w] = Tiles.get("ornate obsidian").id;data[(curX) + j * w] =2;
				map[(curX+1) + j * w] = Tiles.get("obsidian").id;data[(curX+1) + j * w] =2;
			}
			}
		}else if(startY>endY)
			for(int j=startY;j>=(endY  > (80 -(4 - (Math.ceil(i/4)*4))) ? 80-(4 - (Math.ceil(i/4)*4)) : endY );j--) { //up
				if(map[curX + j * w] == Tiles.get("infinite void").id  || map[curX + j * w] == Tiles.get("Obsidian bridge support").id) {
					map[(curX-1) + j * w] = Tiles.get("obsidian").id;data[(curX-1) + j * w] =2;
					map[curX + j * w] = Tiles.get("ornate obsidian").id;data[(curX) + j * w] =2;
					map[(curX+1) + j * w] = Tiles.get("obsidian").id;data[(curX+1) + j * w] =2;
				}
			}
	}

		for (int i = 0; i < w * h / 2500; i++) {
			int xs = random.nextInt(w);
			int ys = random.nextInt(h);
			for (int k = 0; k < 10; k++) {
				int x = xs + random.nextInt(21) - 10;
				int y = ys + random.nextInt(21) - 10;
				for (int j = 0; j < 100; j++) {
					int xo = x + random.nextInt(5) - random.nextInt(5);
					int yo = y + random.nextInt(5) - random.nextInt(5);
					for (int yy = yo - 1; yy <= yo + 1; yy++)
						for (int xx = xo - 1; xx <= xo + 1; xx++)
							if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
								if (map[xx + yy * w] == Tiles.get("ornate obsidian").id || map[xx + yy * w] == Tiles.get("obsidian").id || map[xx + yy * w] == Tiles.get("decorated obsidian").id){
									data[xx + yy * w] = map[xx + yy * w];
									map[xx + yy * w] = Tiles.get("dungeon tallgrass").id;
								}
							}

				}
			}
		}

		//System.out.println(Arrays.toString(coordsX));
		//System.out.println(Arrays.toString(coordsY));
		return new short[][]{map, data};
	}
	private static short[][] createDungeon(int w, int h, int depth) {
		LevelGenObV noise1 = new LevelGenObV(w, h, 8);
		LevelGenObV noise2 = new LevelGenObV(w, h, 8);
		int  wallConstant = 5; //n last blocks are always wall and infinite void indeed
		int wallX =  w/128;
		int wallY = h/128;
		short[] map = new short[w * h];
		short[] data = new short[w * h];

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int i = x + y * w;
				double randObs=Math.random();
				double val = Math.abs(noise1.values[i] - noise2.values[i]) * 3 - 2;

				double xd = x / (w - 1.1) * 2 - 1;
				double yd = y / (h - 1.1) * 2 - 1;
				if (xd < 0) xd = -xd;
				if (yd < 0) yd = -yd;
				double dist = xd >= yd ? xd : yd;
				dist = dist * dist * dist * dist;
				dist = dist * dist * dist * dist;
				val = -val * 1 - 2.2;
				val += 1 - dist * 2;

				if (val < -0.5 * (-depth)) {
					map[i] = Tiles.get("Obsidian Wall").id;
				}else map[i] = Tiles.get("Obsidian").id;

				 if (val < -0.35 * (-depth))
					map[i] = Tiles.get("Obsidian Wall").id;
				else if(val>=-0.35 * (-depth) && val < -0.22 * (-depth)) map[i] = Tiles.get(depth < -1 ? (Math.random() < 0.25 ? "Lava brick" : "infinite void") : "Lava Brick").id;
					else {
						if(depth < -1){
							if (val >= 0 && val < 0.1) map[i] = Tiles.get("infinite void").id;
							else if (randObs <= 0.33 && randObs > 0.014) map[i] = Tiles.get("Raw Obsidian").id;
							else if(randObs<0.014) map[i] = Tiles.get("infinite void").id;
						}else {
							if (randObs > 0.33 && randObs < 0.7) map[i] = Tiles.get("Obsidian").id;
							else if (randObs <= 0.33 && randObs > 0.014) map[i] = Tiles.get("Raw Obsidian").id;
							else  map[i] = Tiles.get("dirt").id;
						}
					}
				if((x<= wallConstant * wallX * 2.5  || x>= w - (wallConstant * wallX * 2.5) || ( y<= wallConstant *wallY*  2.5 || y>=h-(wallConstant * wallY*  2.5))))
					map[i] = Tiles.get("unbreakable wall").id;
				if((x<= wallConstant * wallX   || x>= w - wallConstant * wallX) || ( y<= wallConstant *wallY || y>=h-wallConstant * wallY))
					map[i] = Tiles.get("infinite void").id;

				if(val < 0.5 * (-depth) && val>=0.05 * (-depth) && map[i] != Tiles.get("Unbreakable wall").id  && map[i] != Tiles.get("lava").id && map[i] != Tiles.get("infinite void").id && Math.random()<0.4878) {
					data[i] = map[i];
					map[i] = Tiles.get("Dungeon tallgrass").id;
				}
			}
		}

		lavaLoop:
		for (int i = 0; i < w * h / 450; i++) {
			int x = random.nextInt(w - 20 - (wallConstant * wallX)) + 10 + (wallConstant * wallX);
			int y = random.nextInt(h - 20 - (wallConstant * wallY)) + 10 + (wallConstant * wallY);

			for (int yy = y - 1; yy <= y + 1; yy++)
				for (int xx = x - 1; xx <= x + 1; xx++) {
					if (map[xx + yy * w] != Tiles.get("Obsidian Wall").id) continue lavaLoop;
				}
			double chance=Math.random();
				if(chance>0.4)
			Structure.lavaPool.draw(map, x, y, w);
				 else Structure.obsidianFountainClassic.draw(map, x, y, w);
		}
		for (int i = 0; i < w * h / 1500; i++) {
			int xs = random.nextInt(w);
			int ys = random.nextInt(h);
			for (int k = 0; k < 10; k++) {
				int x = xs + random.nextInt(21) - 10;
				int y = ys + random.nextInt(21) - 10;
				for (int j = 0; j < 100; j++) {
					int xo = x + random.nextInt(5) - random.nextInt(5);
					int yo = y + random.nextInt(5) - random.nextInt(5);
					for (int yy = yo - 1; yy <= yo + 1; yy++)
						for (int xx = xo - 1; xx <= xo + 1; xx++)
							if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
								if (map[xx + yy * w] != Tiles.get("unbreakable wall").id && map[xx + yy * w] != Tiles.get("Obsidian wall").id && map[xx + yy * w] != Tiles.get("Obsidian Deepslate").id && map[xx + yy * w]  != Tiles.get("infinite void").id)
									map[xx + yy * w] = Tiles.get("dirt").id;
							}

				}
			}
		}
		if(depth==-1)
		for (int i = 0; i < w * h / 4500; i++) {
			int xs = random.nextInt(w);
			int ys = random.nextInt(h);
			for (int k = 0; k < 10; k++) {
				int x = xs + random.nextInt(21) - 10;
				int y = ys + random.nextInt(21) - 10;
				for (int j = 0; j < 100; j++) {
					int xo = x + random.nextInt(5) - random.nextInt(5);
					int yo = y + random.nextInt(5) - random.nextInt(5);
					for (int yy = yo - 1; yy <= yo + 1; yy++)
						for (int xx = xo - 1; xx <= xo + 1; xx++)
							if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
								if (map[xx + yy * w] == Tiles.get("dirt").id || map[xx + yy * w] == Tiles.get("dungeon tallgrass").id)
									map[xx + yy * w] = Tiles.get(Math.random()<0.01 ? ("gemnf ore") :  Math.random()<0.06 ?"obsidian ore" : "lava").id;
							}

				}
			}
		}
		for (int i = 0; i < w * h / 10; i++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("dirt").id) {
					map[xx + yy * w] = Tiles.get("obsidian ore").id;
				}
			}
		}
		int r = 4;
		String[] types = new String[] {"", "G", "B"};
		int type=random.nextInt(3);
		String gemnf="gem"+types[type]+"NF Ore";
		String gem="gem"+types[type]+" Ore";

		for (int j = 0; j < 70; j++) {
			int x = random.nextInt(w);
			int y = random.nextInt(h);
			int xx = x + random.nextInt(5) - random.nextInt(5);
			int yy = y + random.nextInt(5) - random.nextInt(5);
			if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
				if (map[xx + yy * w] == Tiles.get("Obsidian Deepslate").id) {
					double chance = Math.random();
					if (chance > 0.33) {
						if (chance > 0.2) map[xx + yy * w] = (short) (Tiles.get(gem).id & 0xff);
						else map[xx + yy * w] = (short) (Tiles.get(gemnf).id & 0xff);
					} else map[xx + yy * w] = (short) (Tiles.get("obsidiumnf ore").id & 0xff);
				}
			}
		}
		for (int i = 0; i < 120 * Math.ceil(w/128); i++) {
			int x = random.nextInt(w);
			int y = random.nextInt(h);
			for (int j = 0; j < 80; j++) {
				int xx = x + random.nextInt(5) - random.nextInt(5);
				int yy = y + random.nextInt(5) - random.nextInt(5);
				double chance = Math.random();
				if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
					 if(map[xx + yy * w] == Tiles.get("dirt").id || map[xx + yy * w] == Tiles.get("lava brick").id) map[xx + yy * w] = (short) ((Tiles.get(chance <0.35 ? "obsidium ore" : "coal Ore").id & 0xff));
				}
			}
		}

		int count = 0;
		if(depth == -1)
		stairsLoop:
		for (int i = 0; i < w * h / 100; i++) { // Loops a certain number of times, more for bigger world sizes.
			int x = random.nextInt(w - 2) + 1;
			int y = random.nextInt(h - 2) + 1;

			// The first loop, which checks to make sure that a new stairs tile will be completely surrounded by rock.
			for (int yy = y - 1; yy <= y + 1; yy++)
				for (int xx = x - 1; xx <= x + 1; xx++)
					if (map[xx + yy * w] != Tiles.get("obsidian wall").id)
						continue stairsLoop;

			// This should prevent any stairsDown tile from being within 30 tiles of any other stairsDown tile.
			for (int yy = Math.max(0, y - stairRadius); yy <= Math.min(h - 1, y + stairRadius); yy++)
				for (int xx = Math.max(0, x - stairRadius); xx <= Math.min(w - 1, x + stairRadius); xx++)
					if (map[xx + yy * w] == Tiles.get("Obsidian Stairs Down").id)
						continue stairsLoop;

			double xP=x;
			double yP=y;


			if(x<wallConstant)x=wallConstant;
			if(x>w-wallConstant)x=w - wallConstant;
			if(y<wallConstant)y=wallConstant;
			if(y>h-wallConstant)y=h - wallConstant;
			if(map[x + y * w] == Tiles.get("Obsidian Wall").id) {
				Structure.dungeonLockInside.draw(map, x, y, w);
				map[x + y * w] = Tiles.get("Obsidian Stairs Down").id;
				count++;
			}
			if (count >=2 * w/128) break;


		}
			if(depth == -2)
		for (int i = 0; i < w * h / 4400; i++) {
			int xs = random.nextInt(w);
			int ys = random.nextInt(h);
			for (int k = 0; k < 10; k++) {
				int x = xs + random.nextInt(21) - 10;
				int y = ys + random.nextInt(21) - 10;
				for (int j = 0; j < 100; j++) {
					int xo = x + random.nextInt(5) - random.nextInt(5);
					int yo = y + random.nextInt(5) - random.nextInt(5);
					for (int yy = yo - 1; yy <= yo + 1; yy++)
						for (int xx = xo - 1; xx <= xo + 1; xx++)
							if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
								if (map[xx + yy * w] == Tiles.get("dirt").id || map[xx + yy * w] == Tiles.get("coarse dirt").id) {
									map[xx + yy * w] = Tiles.get("Fungus").id;

								}
							}
				}
			}
		}
		return new short[][]{map, data};

	}
	private static short[][] createSurface(int w, int h) {
		LevelGen mnoise1 = new LevelGen(w, h, 16);
		LevelGen mnoise2 = new LevelGen(w, h, 16);
		LevelGen mnoise3 = new LevelGen(w, h, 16);

		// ...and some with larger size.
		LevelGen noise1 = new LevelGen(w, h, 32);
		LevelGen noise2 = new LevelGen(w, h, 32);

		int  wallConstant = 5; //n last blocks are always wall and infinite void indeed
		int wallX =  w/128;
		int wallY = h/128;

		short[] map = new short[w * h];
		short[] data = new short[w * h];

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int i = x + y * w;
				double val = Math.abs(noise1.values[i] - noise2.values[i]) * 3 - 2;
				double mval = Math.abs(mnoise1.values[i] - mnoise2.values[i]);

				double xd = x / (w - 1.09) * 2 - 1;
				double yd = y / (h - 1.1) * 2 - 1;
				if (xd < 0) xd = -xd;
				if (yd < 0) yd = -yd;
				double dist = Math.max(xd, yd);
				dist = dist * dist * dist * dist;
				dist = dist * dist * dist * dist;
				val = -val * 1 - 2.2;
				val += 1 - dist * 2;

				if (val < -0.05) {
					map[i] = Tiles.get("ground rock").id;
				} else if (val >= -0.05 && val < -0.03) {
					map[i] = Tiles.get("Lava").id;
				} else {
					if (random.nextInt(2) == 1) {
						if (random.nextInt(15) == 1) {
							map[i] = Tiles.get("Obsidian wall").id;
						} else {
							map[i] = Tiles.get("raw obsidian").id;
						}
					} else {
						map[i] = Tiles.get("obsidian").id;
					}
				}
				if(val<-0.33)map[i] = Tiles.get("lava").id;
				if((x<= wallConstant * wallX  || x>= w - wallConstant * wallX) || ( y<= wallConstant *wallY || y>=h-wallConstant * wallY))
					map[i] = Tiles.get("infinite void").id;
			}
			}
		for (int i = 0; i < w * h /3700; i++) {
			int xs = random.nextInt(w);
			int ys = random.nextInt(h);
			for (int k = 0; k < 20; k++) {
				int x = xs + random.nextInt(21) - 10;
				int y = ys + random.nextInt(21) - 10;
				for (int j = 0; j < 100; j++) {
					int xo = x + random.nextInt(5) - random.nextInt(5);
					int yo = y + random.nextInt(5) - random.nextInt(5);
					for (int yy = yo - 1; yy <= yo + 1; yy++) {
						for (int xx = xo - 1; xx <= xo + 1; xx++) {

							if (xx >=wallConstant * wallX && yy >= (wallConstant * wallY) && xx < w - (wallConstant * wallX) && yy < h -( wallConstant * wallY)) {

								if(map[xx + yy * w] != Tiles.get("lava").id &&map[xx + yy * w] != Tiles.get("Infinite void").id){
									map[xx + yy * w] = Tiles.get(((xx+random.nextInt(5)) %7 ==0 || (yy+random.nextInt(5) )% 14 < 3) ? "dirt" :  "coarse dirt").id;
								}
							}
						}
					}

				}
			}
		}
		for (int i = 0; i < w * h /3700; i++) {
			int xs = random.nextInt(w);
			int ys = random.nextInt(h);
			for (int k = 0; k < 20; k++) {
				int x = xs + random.nextInt(21) - 10;
				int y = ys + random.nextInt(21) - 10;
				for (int j = 0; j < 100; j++) {
					int xo = x + random.nextInt(5) - random.nextInt(5);
					int yo = y + random.nextInt(5) - random.nextInt(5);
					for (int yy = yo - 1; yy <= yo + 1; yy++) {
						for (int xx = xo - 1; xx <= xo + 1; xx++) {

							if (xx >=wallConstant * wallX && yy >= (wallConstant * wallY) && xx < w - (wallConstant * wallX) && yy < h -( wallConstant * wallY)) {

								if(map[xx + yy * w] != Tiles.get("lava").id &&map[xx + yy * w] != Tiles.get("Infinite void").id){
									map[xx + yy * w] = Tiles.get((xx+random.nextInt(25)) %7 ==0 ? "lava brick" : "obsidian").id;
								}
							}
						}
					}

				}
			}
		}
		for (int i = 0; i < w * h /3700; i++) {
			int xs = random.nextInt(w);
			int ys = random.nextInt(h);
			for (int k = 0; k < 20; k++) {
				int x = xs + random.nextInt(21) - 10;
				int y = ys + random.nextInt(21) - 10;
				for (int j = 0; j < 100; j++) {
					int xo = x + random.nextInt(5) - random.nextInt(5);
					int yo = y + random.nextInt(5) - random.nextInt(5);
					for (int yy = yo - 1; yy <= yo + 1; yy++) {
						for (int xx = xo - 1; xx <= xo + 1; xx++) {

							if (xx >=wallConstant * wallX && yy >= (wallConstant * wallY) && xx < w - (wallConstant * wallX) && yy < h -( wallConstant * wallY)) {

								if(map[xx + yy * w] == Tiles.get("lava").id && random.nextInt(58)==7)map[xx + yy * w] = Tiles.get("deepslate spiky stone").id;
								else if( random.nextInt(28)==7 && map[xx + yy * w] != Tiles.get("infinite void").id )map[xx + yy * w] = Tiles.get("obsidian rock").id;
							}
						}
					}

				}
			}
		}
		int r = 5;
		for (int i = 0; i < w * h / 1400; i++) {
			int x = random.nextInt(w);
			int y = random.nextInt(h);
			for (int j = 0; j < 50; j++) {
				int xx = x + random.nextInt(5) - random.nextInt(5);
				int yy = y + random.nextInt(5) - random.nextInt(5);
				if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
					if (xx < wallConstant * 2 * wallX) x = wallConstant * 2 * wallX;
					if (xx > w - wallConstant * 2 * wallX) x = w - wallConstant * 2 * wallX;
					if (yy < wallConstant * 2 * wallY) y = wallConstant * 2 * wallY;
					if (yy > h - wallConstant * 2 * wallY) y = h - wallConstant * 2 * wallY;
					if (map[xx + yy * w] == Tiles.get("lava").id) {
						map[xx + yy * w] = Tiles.get("obsidium Ore").id;
					}else  map[xx + yy * w] = Tiles.get(Math.random() < 0.1 ? "gemG Ore" : (( xx/(yy+1)%8==0) ? "obsidian ore" : ("Lapis"+(random.nextInt(24)>20 ? "NF ore" : "")))).id;
				}
			}
		}
		for (int i = 0; i < w * h / 2500; i++) {
			int xs = random.nextInt(w);
			int ys = random.nextInt(h);
			for (int k = 0; k < 10; k++) {
				int x = xs + random.nextInt(21) - 10;
				int y = ys + random.nextInt(21) - 10;
				for (int j = 0; j < 100; j++) {
					int xo = x + random.nextInt(5) - random.nextInt(5);
					int yo = y + random.nextInt(5) - random.nextInt(5);
					for (int yy = yo - 1; yy <= yo + 1; yy++)
						for (int xx = xo - 1; xx <= xo + 1; xx++)
							if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
								if ((map[xx + yy * w] == Tiles.get("dirt").id || map[xx + yy * w] == Tiles.get("obsidian").id || map[xx + yy * w] == Tiles.get("coarse dirt").id)&& Math.random()>0.8){
									data[xx + yy * w] = map[xx + yy * w];
								map[xx + yy * w] = Tiles.get("dungeon tallgrass").id;
							}
							}

				}
			}
		}
		int count = 0;
		stairsLoop:
		for (int i = 0; i < w * h / 100; i++) { // Loops a certain number of times, more for bigger world sizes.
			int x = random.nextInt(w - 2) + 1;
			int y = random.nextInt(h - 2) + 1;

			// The first loop, which checks to make sure that a new stairs tile will be completely surrounded by rock.
			for (int yy = y - 1; yy <= y + 1; yy++)
				for (int xx = x - 1; xx <= x + 1; xx++)
					if (map[xx + yy * w] != Tiles.get("lava").id)
						continue stairsLoop;

			// This should prevent any stairsDown tile from being within 30 tiles of any other stairsDown tile.
			for (int yy = Math.max(0, y - stairRadius); yy <= Math.min(h - 1, y + stairRadius); yy++)
				for (int xx = Math.max(0, x - stairRadius); xx <= Math.min(w - 1, x + stairRadius); xx++)
					if (map[xx + yy * w] == Tiles.get("Obsidian Stairs Down").id)
						continue stairsLoop;

				double xP=x;
				double yP=y;
				if(x<wallConstant * 2.5 + 5)x=wallConstant + 5;
				if(x>w-wallConstant * 2.5 - 5)x=w - wallConstant - 5;
				if(y<wallConstant * 2.5 + 5)y=wallConstant + 5;
				if(y>h-wallConstant * 2.5 - 5)y=h - wallConstant - 5;
				Structure.dungeonLock.draw(map, x, y, w);


				String[] tileNames={"Obsidian wall","Obsidian","Ornate obsidian","dirt","obsidian ore","lava","obsidian rock"};
				for(int k=0;k<120 * (w/128);k+=(random.nextInt(5))){
					int xT = x + (random.nextInt(17 * (w / 128)) - 7 * (w / 128));
					int yT = y + (random.nextInt(17 * (h / 128)) - 7 * (h / 128));
					if(xT > 0 && xT < w && yT > 0 && yT < h)
					if(map[xT + yT * w]==Tiles.get("lava").id) { //replace only lava tiles to prevent breachings

						map[xT + yT * w] = Tiles.get(tileNames[random.nextInt(tileNames.length)]).id;
					}
				}
				map[x + y * w] = Tiles.get("Obsidian Stairs Down").id;
			count++;
			if (count >=3 * w/128) break;
		}
		for (int i = 0; i < w * h / 2400; i++) {
			int xs = random.nextInt(w);
			int ys = random.nextInt(h);
			for (int k = 0; k < 10; k++) {
				int x = xs + random.nextInt(21) - 10;
				int y = ys + random.nextInt(21) - 10;
				for (int j = 0; j < 100; j++) {
					int xo = x + random.nextInt(5) - random.nextInt(5);
					int yo = y + random.nextInt(5) - random.nextInt(5);
					for (int yy = yo - 1; yy <= yo + 1; yy++)
						for (int xx = xo - 1; xx <= xo + 1; xx++)
							if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
								if (map[xx + yy * w] == Tiles.get("obsidian").id) {
									map[xx + yy * w] = Tiles.get("Fungus").id;

								}
							}
				}
			}
		}
		return new short[][]{map, data};

	}


	
	public static void main(String[] args) {
		LevelGenObV.worldSeed = 0x100;
		
		// Fixes to get this method to work
		
		// AirWizard needs this in constructor
		Game.gameDir = "";
		
		Tiles.initTileList();
		// End of fixes
		
		int idx = -1;
		
		int[] maplvls = new int[args.length];
		boolean valid = true;
		if (maplvls.length > 0) {
			for (int i = 0; i < args.length; i++) {
				try {
					int lvlnum = Integer.parseInt(args[i]);
					maplvls[i] = lvlnum;
				} catch (Exception ex) {
					valid = false;
					break;
				}
			}
		} else valid = false;
		
		if (!valid) {
			maplvls = new int[1];
			maplvls[0] = 0;
		}
		
		//noinspection InfiniteLoopStatement
		while (true) {
			int w = 512;
			int h = 512;

			int lvl = maplvls[idx++ % maplvls.length];
			if (lvl > 1 || lvl < -2) continue;
			
			short[][] fullmap = LevelGenObV.createAndValidateMap(w,h ,-0 );
			
			if (fullmap == null) continue;
			short[] map = fullmap[0];
			BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			int[] pixels = new int[w * h];
			for (int y = 0; y <h; y++) {
				for (int x = 0; x < w; x++) {
					int i = x + y * w;

					if (map[i] == Tiles.get("gem Ore").id) pixels[i] = 0x00D4D8;
					if (map[i] == Tiles.get("gemnf Ore").id) pixels[i] = 0x00D4D8;
					if (map[i] == Tiles.get("gemg Ore").id) pixels[i] = 0x75B65B;
					if (map[i] == Tiles.get("gemgnf Ore").id) pixels[i] = 0x75B56B;
					if (map[i] == Tiles.get("gemb Ore").id) pixels[i] = 0x283BC5;
					if (map[i] == Tiles.get("gembnf Ore").id) pixels[i] = 0x283BC5;
					if (map[i] == Tiles.get("dungeon tallgrass").id) pixels[i] = 0x000080;
					if (map[i] == Tiles.get("Bramble").id) pixels[i] = 0x544426;
					if (map[i] == Tiles.get("coal Ore").id) pixels[i] = 0x555555;
					if (map[i] == Tiles.get("lapisNF ore").id) pixels[i] = 0xAAAAFF;
					if (map[i] == Tiles.get("lapis").id) pixels[i] = 0x0000FF;
					if (map[i] == Tiles.get("obsidian Ore").id) pixels[i] = 0x4C1364;
					if (map[i] == Tiles.get("obsidium Ore").id) pixels[i] = 0xFFAAFF;
					if (map[i] == Tiles.get("obsidiumnf Ore").id) pixels[i] = 0xFFBBFF;
					if (map[i] == Tiles.get("rock").id) pixels[i] = 0xa0a0a0;
					if (map[i] == Tiles.get("Obsidian rock").id) pixels[i] = 0xBB00BB;
					if (map[i] == Tiles.get("rockG").id) pixels[i] = 0x909090;
					if (map[i] == Tiles.get("deepslate").id) pixels[i] = 0x454545;
					if (map[i] == Tiles.get("deepslateG").id) pixels[i] = 0x343434;
					if (map[i] == Tiles.get("deepslate spiky stone").id) pixels[i] = 0x454545;
					if (map[i] == Tiles.get("deepslate spiky stone-L").id) pixels[i] = 0x454545;
					if (map[i] == Tiles.get("dirt").id) pixels[i] = 0x440044;
					if (map[i] == Tiles.get("coarse dirt").id) pixels[i] = 0x400040;
					if (map[i] == Tiles.get("Stone Bricks").id) pixels[i] = 0xa0a040;
					if (map[i] == Tiles.get("Obsidian Wall").id) pixels[i] = 0x550055;
					if (map[i] == Tiles.get("Obsidian").id) pixels[i] = 0x330033;
					if (map[i] == Tiles.get("ornate Obsidian").id) pixels[i] = 0x440044;
					if (map[i] == Tiles.get("decorated Obsidian").id) pixels[i] = 0x330039;
					if (map[i] == Tiles.get("Raw Obsidian").id) pixels[i] = 0x220122;
					if (map[i] == Tiles.get("lava").id) pixels[i] = 0xff2020;
					if (map[i] == Tiles.get("lava brick").id) pixels[i] = 0xff4040;
					if (map[i] == Tiles.get("Obsidian Stairs Down").id) pixels[i] = 0xffffff;
					if (map[i] == Tiles.get("Obsidian Stairs Up").id) pixels[i] = 0xffffff;
					if (map[i] == Tiles.get("infinite void").id) pixels[i] = 0x220022;
					if (map[i] == Tiles.get("red wool").id) pixels[i] = 0xff0000;
					if (map[i] == Tiles.get("fungus").id) pixels[i] = 0x2B422B;

				}
			}
			img.setRGB(0, 0, w, h, pixels, 0, w);
			JOptionPane.showMessageDialog(null, null, "Another Map: OBV", JOptionPane.PLAIN_MESSAGE, new ImageIcon(img.getScaledInstance(w * 4, h * 4, Image.SCALE_AREA_AVERAGING)));
			if (LevelGenObV.worldSeed == 0x100)
				LevelGenObV.worldSeed = 0xAAFF20;
			else
				LevelGenObV.worldSeed = 0x100;
		}
	}
}
