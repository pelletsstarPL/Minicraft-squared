package minicraft.level;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import minicraft.core.Updater;
import minicraft.entity.furniture.Spawner;
import minicraft.entity.mob.MobAi;
import minicraft.entity.mob.Skeleton;
import minicraft.entity.mob.Slime;
import minicraft.entity.mob.Zombie;
import minicraft.level.Level;
import minicraft.level.tile.*;
import org.jetbrains.annotations.Nullable;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.screen.WorldGenDisplay;

public class LevelGen {
	private static long worldSeed = 0;
	private static final Random random = new Random(worldSeed);
	public double[] values; // An array of doubles, used to help making noise for the map
	private int w, h; // Width and height of the map
	private static final int stairRadius = 14;
	/** This creates noise to create random values for level generation */
	public LevelGen(int w, int h, int featureSize) {
		Updater.tickCount=1500; //starting time
		Updater.gameTime=1500;
		this.w = w;
		this.h = h;
		
		values = new double[w * h];
		
		/// Feature size likely determines how big the biomes are, in some way. It tends to be 16 or 32, in the code below. 
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
		
		if (level == 1)
			return createAndValidateSkyMap(w, h);
		if (level == 0)
			return createAndValidateTopMap(w, h);
		if (level == -6)
			return createAndValidateDungeon(w, h);
		if (level > -6 && level < 0)
			return createAndValidateUndergroundMap(w, h, -level);
		System.err.println("LevelGen ERROR: level index is not valid. Could not generate a level.");
		
		return null;
	}
	
	private static short[][] createAndValidateTopMap(int w, int h) {
		random.setSeed(worldSeed);
		do {
			short[][] result = createTopMap(w, h);
			
			int[] count = new int[256];
			
			for (int i = 0; i < w * h; i++) {
				count[result[0][i] & 0xff]++;
			}
			if (count[Tiles.get("rock").id & 0xff] < 100) continue;
			if (count[Tiles.get("sand").id & 0xff] < 100) continue;
			if (count[Tiles.get("grass").id & 0xff] < 100) continue;

			if (count[Tiles.get("oak").id & 0xff] < 100) continue;
			if (count[Tiles.get("Stairs Down").id & 0xff] == 0) continue; // size 128 = 6 and 1 more for each 128 in size above 128 stairs min
			
			return result;
			
		} while (true);
	}
	
	private static short[][] createAndValidateUndergroundMap(int w, int h, int depth) {
		random.setSeed(worldSeed);
		do {
			short[][] result = createUndergroundMap(w, h, depth);

			int[] count = new int[256];

			for (int i = 0; i < w * h; i++) {
				count[result[0][i] & 0xff]++;
			}
			if(depth<5) {
				if (count[Tiles.get("rock").id & 0xff] + count[Tiles.get("rockG").id & 0xff]< 100) continue;
			}else if(depth==4){
				if (count[Tiles.get("deepslate").id & 0xff] + count[Tiles.get("rock").id & 0xff]< 100) continue;
			}else  if (count[Tiles.get("deepslate").id & 0xff] < 100) continue;
			if(depth!=3) {
				if (count[Tiles.get("dirt").id & 0xff] < 100) continue;
			}else{
				if (count[Tiles.get("Moss").id & 0xff]+count[Tiles.get("dirt").id & 0xff] < 100) continue;
			}
			switch(depth){
				case 1:if (count[(Tiles.get("iron Ore").id & 0xff)] < 100) continue;break;
				case 2:if (count[(Tiles.get("iron Ore").id & 0xff)] + count[(Tiles.get("gold Ore").id & 0xff)] < 100) continue;break;
				case 3:if (count[(Tiles.get("gold Ore").id & 0xff)] < 100) continue;break;
				case 4:if (count[(Tiles.get("gold Ore").id & 0xff)] + count[(Tiles.get("gem Ore").id & 0xff)]< 100) continue;break;
				case 5:if (count[(Tiles.get("gem Ore").id & 0xff)] < 100) continue;break;
			}

			if (depth < 5 && count[Tiles.get("Stairs Down").id & 0xff] < w / 32)
				continue; // Size 128 = 4 stairs min

			return result;

		} while (true);
	}
	
	private static short[][] createAndValidateDungeon(int w, int h) {
		random.setSeed(worldSeed);
		
		do {
			short[][] result = createDungeon(w, h);
			
			int[] count = new int[256];
			
			for (int i = 0; i < w * h; i++) {
				count[result[0][i] & 0xff]++;
			}
			if (count[Tiles.get("Obsidian").id & 0xff] < 100) continue;
			if (count[Tiles.get("Raw Obsidian").id & 0xff] < 100) continue;
			if (count[Tiles.get("Lava Brick").id & 0xff] < 100) continue;
			if (count[Tiles.get("Obsidian Wall").id & 0xff] < 100) continue;
			
			return result;
			
		} while (true);
	}
	private static short[][] createAndValidateSkyMap(int w, int h) {
		random.setSeed(worldSeed);
		
		do {
			short[][] result = createSkyMap(w, h);
			
			int[] count = new int[256];
			
			for (int i = 0; i < w * h; i++) {
				count[result[0][i] & 0xff]++;
			}
			if (count[Tiles.get("cloud").id & 0xff] + count[Tiles.get("aerocloud").id & 0xff] + count[Tiles.get("skygrass").id & 0xff]< 2000) continue;
			if (count[Tiles.get("Stairs Down").id & 0xff] < w / 64)
				continue; // size 128 = 2 stairs min
			
			return result;
			
		} while (true);
	}

	private static short[][] createTopMap(int w, int h) { // Create surface map
		Settings.set("skinon",false); //fix
		// creates a bunch of value maps, some with small size...
		LevelGen mnoise1 = new LevelGen(w, h, 16);
		LevelGen mnoise2 = new LevelGen(w, h, 16);
		LevelGen mnoise3 = new LevelGen(w, h, 16);
		
		// ...and some with larger size.
		LevelGen noise1 = new LevelGen(w, h, 32);
		LevelGen noise2 = new LevelGen(w, h, 32);
		
		short[] map = new short[w * h];
		short[] data = new short[w * h];

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int i = x + y * w;

				double val = Math.abs(noise1.values[i] - noise2.values[i]) * 3 - 2;
				double mval = Math.abs(mnoise1.values[i] - mnoise2.values[i]);
				mval = Math.abs(mval - mnoise3.values[i]) * 3 - 2;
				double nval = Math.abs(mnoise1.values[i] - noise2.values[i]);
				nval = Math.abs(nval - mnoise3.values[i]) * 3 - 2;

				// This calculates a sort of distance based on the current coordinate.
				double xd = x / (w - 1.0) * 2 - 1;
				double yd = y / (h - 1.0) * 2 - 1;
				if (xd < 0) xd = -xd;
				if (yd < 0) yd = -yd;
				double dist = xd >= yd ? xd : yd;
				dist = dist * dist * dist * dist;
				dist = dist * dist * dist * dist;
				val += 1 - dist * 20;
				String text = "Box"; //debug var


				switch ((String) Settings.get("Type")) {
					case "Island":

						if (val < -0.5) {
							if (Settings.get("Theme").equals("Hell"))
								map[i] = Tiles.get("lava").id;
							else {
								map[i] = Tiles.get("water").id;
							}
						} else if (val > 0.1 - (((float) (int) Settings.get("stonemass")) * 0.05) && mval < -1.5 + (((float) (int) Settings.get("stonemass")) * 0.05)) {
							if (val > 0.7 && mval < -1.75) map[i] = Tiles.get("rockG").id;
							else map[i] = Tiles.get("rock").id;
						} else {
							double chance = Math.random();
							if (chance > 0.3) map[i] = Tiles.get("grass").id;
						}

						break;
					case "Box":

						if (val < -1.5) {
							if (Settings.get("Theme").equals("Hell")) {
								map[i] = Tiles.get("lava").id;
							} else {
								map[i] = Tiles.get("water").id;
							}
						} else if (val > 0.32 - ((float) (int) Settings.get("stonemass") * 0.05) && mval < -1.5 + ((float) (int) Settings.get("stonemass") * 0.05)) {
							if (val > 0.75 && mval < -1.75) map[i] = Tiles.get("rockG").id;
							else map[i] = Tiles.get("rock").id;
						} else {
							double chance = Math.random();
							if (chance > 0.3) map[i] = Tiles.get("grass").id;
						}

						break;
					case "Mountain":

						if (val < -0.4) {
							double chance = Math.random();
							if (chance > 0.3) map[i] = Tiles.get("grass").id;
						} else if (val > 0.5 && mval < -1.5) {
							if (Settings.get("Theme").equals("Hell")) {
								map[i] = Tiles.get("lava").id;
							} else if (Settings.get("Theme").equals("Tundra")) {
								map[i] = Tiles.get("Ice").id;
							} else {
								map[i] = Tiles.get("water").id;
							}
							//} else if(mval>=1.5-((float)(int)Settings.get("stonemass")*0.05) && mval<1.62+((float)(int)Settings.get("stonemass")*0.05)){
						} else if (mval >= 1.5 - ((float) (int) Settings.get("stonemass") * 0.05) && mval < 1.62 + ((float) (int) Settings.get("stonemass") * 0.05)) {
							double chance = Math.random();
							if (chance > 0.011) map[i] = Tiles.get("Spiky stone-L").id;
							else map[i] = Tiles.get("Iron ore").id;
						} else if (val > 0.2 && mval < -1.2) {
							map[i] = Tiles.get("rockG").id;
						} else {
							map[i] = Tiles.get("rock").id;
						}
						break;

					case "Irregular":
						if (val < -0.5 && mval < -0.5) {
							if (Settings.get("Dominantbiome").equals("Tundra") && !Settings.get("Theme").equals("Hell")) {
								map[i] = Tiles.get("ice").id;
							}
							if (Settings.get("Theme").equals("Hell")) {
								map[i] = Tiles.get("lava").id;
							}
							if (!Settings.get("Theme").equals("Hell") && !Settings.get("Dominantbiome").equals("Tundra")) {
								map[i] = Tiles.get("water").id;
							}
						} else if (val > 0.45 && mval < -1.55) {
							if (val > 0.75 && mval < -1.75)
								map[i] = Tiles.get("rockG").id;
							else map[i] = Tiles.get("rock").id;
						} else {
							double chance = Math.random();
							if (chance > 0.3) map[i] = Tiles.get("grass").id;

						}
						break;
				}
			}

		}
		//Swamps
		int divider=(Settings.get("Dominantbiome").equals("Swamp") ? 10 : 1);
		int multipH=(Settings.get("theme").equals("hell") ? 3 : 1); //since Swamps are going to be very ruined on hell worlds anyway why should we even let them be very big
		for (int i = 0; i < w * h / (w> 128  && h >128 ? (6000/divider)*multipH : (24000/divider)*multipH); i++) {
			int xs = random.nextInt(w);
			int ys = (h/2)+(worldSeed%8 > 5 ? -random.nextInt(h/4) : random.nextInt(h/4));
			for (int k = 0; k < 7; k++) {
				int x = xs + random.nextInt(21) - 10;
				int y = ys + random.nextInt(21) - 10;
				for (int j = 0; j < 100; j++) {
					int xo = x + random.nextInt(5) - random.nextInt(5);
					int yo = y + random.nextInt(5) - random.nextInt(5);
					for (int yy = yo - 1; yy <= yo + 1; yy++)
						for (int xx = xo - 1; xx <= xo + 1; xx++)

							if (xx >= 10*Math.ceil(w/128) && yy >= 10*Math.ceil(w/128) && xx < w-(10*Math.ceil(w/128)) && yy < h-(10*Math.ceil(w/128))) {
								if (map[xx + yy * w] == Tiles.get("grass").id) {
									map[xx + yy * w] = Tiles.get("moss").id;
								}
							}
				}
			}
		}

		boolean des=Settings.get("Dominantbiome").equals("Desert");
			for (int i = 0; i < w * h / (des ? 400 : 2800); i++) {
				int xs = random.nextInt(w);
				int ys = des ? (h/2) + (random.nextInt(121)-60) :(h/2) + (random.nextInt(41)-20);
				for (int k = 0; k < 10; k++) {
					int x = xs + random.nextInt(21) - 10;
					int y = ys + random.nextInt(21) - 10;
					for (int j = 0; j < 100; j++) {
						int xo = x + random.nextInt(5) - random.nextInt(5);
						int yo = y + random.nextInt(5) - random.nextInt(5);
						for (int yy = yo - 1; yy <= yo + 1; yy++)
							for (int xx = xo - 1; xx <= xo + 1; xx++)
								if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
									if (map[xx + yy * w] == Tiles.get("grass").id) {map[xx + yy * w] = Tiles.get("sand").id;
									}
								}
					}
				}
			}
			boolean dry=Settings.get("Dominantbiome").equals("Drylands");
			for (int i = 0; i < w * h / (dry ? 1000 : 7400); i++) {
				int xs = random.nextInt(w);
				int ys = dry ? (h/2) + (random.nextInt(61)-60) :(h/2) + (random.nextInt(41)-20);
				for (int k = 0; k < 10; k++) {
					int x = xs + random.nextInt(21) - 10;
					int y = ys + random.nextInt(21) - 10;
					for (int j = 0; j < 100; j++) {
						int xo = x + random.nextInt(5) - random.nextInt(5);
						int yo = y + random.nextInt(5) - random.nextInt(5);
						for (int yy = yo - 1; yy <= yo + 1; yy++)
							for (int xx = xo - 1; xx <= xo + 1; xx++)
								if (xx > 3 && yy > 3 && xx < w && yy < h) {
									if (map[xx + yy * w] == Tiles.get("grass").id) {
										map[xx + yy * w] = Tiles.get("coarse dirt").id;
									}
								}
					}
				}
			}

		





		// Tundra biome
			boolean tun=Settings.get("Dominantbiome").equals("Tundra");
			for (int i = 0; i < w * h / (tun ? 600 : 3200); i++) {
				int xs = random.nextInt(w);
				int ys = (int)(i%2==0 ? random.nextInt(h/4) + h*0.8 : random.nextInt(h/6));
				for (int k = 0; k < 20; k++) {
					int x = xs + random.nextInt(21) - 10;
					int y = ys + random.nextInt(21) - 10;
					for (int j = 0; j < 100; j++) {
						int xo = x + random.nextInt(5) - random.nextInt(5);
						int yo = y + random.nextInt(5) - random.nextInt(5);
						for (int yy = yo - 1; yy <= yo + 1; yy++) {
							for (int xx = xo - 1; xx <= xo + 1; xx++) {
								double val = Math.abs(noise1.values[i] - noise2.values[i]) * 3 - 2;
								double mval = Math.abs(mnoise1.values[i] - mnoise2.values[i]);
								mval = Math.abs(mval - mnoise3.values[i]) * 3 - 2;
								if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
										if (map[xx + yy * w] == Tiles.get("grass").id || map[xx + yy * w] == Tiles.get("tall grass").id || map[xx + yy * w] == Tiles.get("Grass Small stones").id || map[xx + yy * w] == Tiles.get("conifer").id) {
											map[xx + yy * w] = Tiles.get("snow").id;
										}
								}
							}
						}

					}
				}
			}
		for (int j = 0; j < h; j++) { // Makes a separation with the tundra and desert biome adding the plains biome between these
			int tundra_desert_separator = 7;
			for (int x = 0; x < w; x++) {
				// tiles SIDE A and SIDE B
				if ((map[x + j * w] != Tiles.get("Snow").id)  && (map[x + j * w] == Tiles.get("Sand").id || map[x + j * w] == Tiles.get("Coarse dirt").id)) {
					boolean replace2 = false;
					int tx;
					check_biome_a:

					for (tx = x - tundra_desert_separator + random.nextInt(4); tx <= x + tundra_desert_separator + random.nextInt(4); tx++) {
						for (int ty = j - tundra_desert_separator + random.nextInt(2); ty <= j + tundra_desert_separator + random.nextInt(2); ty++) {
							if (tx >= 0 && ty >= 0 && tx <= w && ty <= h && (tx != x || ty != j) && (map[tx + ty * w] == Tiles.get("Snow").id || map[tx + ty * w] == Tiles.get("Glacier").id)) { // start in the SIDE A
								replace2 = true;
								break check_biome_a;
							}
						}
					}

					if (replace2) {
						if((x+(j*2))%(6+(j%2))==0)map[x + j * w] = Tiles.get("Grass small stones").id; // Add the separation
						else map[x + j * w] = Tiles.get("Grass").id;
					}

				}
			}
		}
		for(int j=0;j<2;j++) { //first: big then smol
			for (int i = 0; i < w * h /300; i++) {
				int xx = random.nextInt(w);
				int yy = des ? (h/2) + (random.nextInt(61)-60) :(h/2) + (random.nextInt(41)-20);

				if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
					if (map[xx + yy * w] == Tiles.get("sand").id) {
						map[xx + yy * w] = Tiles.get((j%2==1?"Small ":"")+"cactus").id;
					}
				}
			}
			for (int i = 0; i < w * h / 90; i++) {
				int xx = random.nextInt(w);
				int yy = random.nextInt(h);

				if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
					if (map[xx + yy * w] == Tiles.get("sand").id) {
						map[xx + yy * w] = Tiles.get("cactus sapling").id;
						data[xx + yy * w] = (short) -(random.nextInt(200)+50);
					}
				}
			}
		}

		//BEACHES and ice rifts
		if(!Settings.get("type").equals("Mountain"))
			for (int j = 0; j < h; j++) {

				int beaches_thickness = 1;
				int iceRift_thickness = 4;
				String fluid = Settings.get("theme").equals("Hell") ? "Lava" : "Water";
				for (int x = 0; x < w; x++) {
					if (x >= 15 * (w / 128) && j >= 15 * (w / 128) && x <= w - (15 * (h / 128)) && j <= h - (15 * (h / 128))) {
						if (x > (w / 2) - (15 * (w / 128)) && x < (w / 2) + (15 * (w / 128)) && j > (h / 2) - (15 * (h / 128)) && j < (h / 2) + (15 * (h / 128)))
							beaches_thickness = 2;//double the thick beaches
						else beaches_thickness = 1;
						//System.out.println((w/2)-(10*(w/128)));
						//System.out.println((w/2)+(10*(w/128)));
						//System.out.println((h/2)-(10*(h/128)));
						//	System.out.println((h/2)+(10*(h/128)));
						if ((map[x + j * w] != Tiles.get(fluid).id && map[x + j * w] == Tiles.get("Grass").id) ||
								(map[x + j * w] != Tiles.get(fluid).id && map[x + j * w] == Tiles.get("Oak").id) ||
								(map[x + j * w] != Tiles.get(fluid).id && map[x + j * w] == Tiles.get("Conifer").id) ||
								(map[x + j * w] != Tiles.get(fluid).id && map[x + j * w] == Tiles.get("Birch").id) ||
								(map[x + j * w] != Tiles.get(fluid).id && map[x + j * w] == Tiles.get("Flower").id) ||
								(map[x + j * w] != Tiles.get(fluid).id && map[x + j * w] == Tiles.get("small flower").id) ||
								(map[x + j * w] != Tiles.get(fluid).id && map[x + j * w] == Tiles.get("small rose").id) ||
								(map[x + j * w] != Tiles.get(fluid).id && map[x + j * w] == Tiles.get("rose").id) ||
								(map[x + j * w] != Tiles.get(fluid).id && map[x + j * w] == Tiles.get("fern").id) ||
								(map[x + j * w] != Tiles.get(fluid).id && map[x + j * w] == Tiles.get("grass small stones").id) ||
								(map[x + j * w] != Tiles.get(fluid).id && map[x + j * w] == Tiles.get("Sunflower").id)) {
							boolean replace = false;

							check_ocean:
							for (int tx = x - beaches_thickness; tx <= x + beaches_thickness; tx++) { // left - right
								for (int ty = j - beaches_thickness; ty <= j + beaches_thickness; ty++) { // up - down
									if ((tx != x || ty != j)) { //we'll make some landmass have no beaches
										if (map[tx + ty * w] == Tiles.get(fluid).id) {
											replace = true;
											break check_ocean;
										}
									}
								}
							}

							if (replace) {
								map[x + j * w] = Tiles.get((x + j * w) % 6 == 0 ? "Desert grass" : "Sand").id;
							}

						}
						if ((map[x + j * w] != Tiles.get("Water").id && map[x + j * w] == Tiles.get("Snow").id) ||
								(	map[x + j * w] != Tiles.get("Water").id && map[x + j * w] == Tiles.get("Snowy Conifer").id) ||
								(map[x + j * w] != Tiles.get("Water").id && map[x + j * w] == Tiles.get("Snowy dead tree").id) ||
								(map[x + j * w] != Tiles.get("Water").id && map[x + j * w] == Tiles.get("Small glacier spikes").id) ||
								(map[x + j * w] != Tiles.get("Water").id && map[x + j * w] == Tiles.get("Small glacier").id)) {
							boolean replace = false;

							check_ocean:
							for (int tx = x - iceRift_thickness; tx <= x + iceRift_thickness; tx++) { // left - right
								for (int ty = j - iceRift_thickness; ty <= j + iceRift_thickness; ty++) { // up - down
									if (tx != x || ty != j) {
										if (map[tx + ty * w] == Tiles.get("Water").id) {
											replace = true;
											break check_ocean;
										}
									}
								}
							}

							if (replace) {
								map[x + j * w] = Tiles.get("Ice").id;
								if(map[(x+1) + (j+1) * w]==Tiles.get("Water").id)map[(x+1) + (j+1) * w] = Tiles.get("Ice").id;
								if(map[x + (j+1) * w]==Tiles.get("Water").id)map[x + (j+1) * w] = Tiles.get("Ice").id;
								if(map[(x-1) + (j-1) * w]==Tiles.get("Water").id)map[(x-1) + (j-1) * w] = Tiles.get("Ice").id;
								if(map[x + (j-1) * w]==Tiles.get("Water").id)map[x + (j-1) * w] = Tiles.get("Ice").id;
							}

						}
					}
				}
			}
		for (int i = 0; i < w * h /(Settings.get("Dominantbiome").equals("Taiga") ? 200 : 2970); i++) {
			int x = random.nextInt(w);
			int y = random.nextInt(h);
			for (int j = 0; j < 200; j++) {
				int xx = x + random.nextInt(15) - random.nextInt(15);
				int yy = y + random.nextInt(15) - random.nextInt(15);
				if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
					if (map[xx + yy * w] == Tiles.get("grass").id || map[xx + yy * w] == Tiles.get("tall grass").id || map[xx + yy * w] == Tiles.get("Grass Small stones").id) {
						double chance=Math.random();
						if(chance>0.003) {
							String extraProp="";
							if((xx+j)%4==3)extraProp="Small ";
							map[xx + yy * w] = Tiles.get(extraProp+"conifer").id;
						}else{
							String extraProp="";
							if(Math.random()<0.04) extraProp="Small ";
							if(chance<=0.003 && chance>=0.001) map[xx + yy * w] = Tiles.get(extraProp+"oak").id;
							else map[xx + yy * w] = Tiles.get(extraProp+"birch").id;
						}
					}
				}
			}
		}
		for (int i = 0; i < w * h / (4600-((w-h)*2)); i++) {
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
							if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
								if (map[xx + yy * w] == Tiles.get("snow").id || map[xx + yy * w] == Tiles.get("snowy conifer").id)map[xx + yy * w] = Tiles.get("glacier").id;
								/*if (map[xx + yy * w] == Tiles.get("rock").id || map[xx + yy * w] == Tiles.get("rockG").id){
									Tile[] areaTiles = Level.getAreaTiles(x,y,1);
								for(Tile t: areaTiles){
									if(t == Tiles.get("snow") || t == Tiles.get("glacier") ){
											map[xx + yy * w] = Tiles.get("iced rock").id;
										}
									}

								} postponed for 3.2 */
						}
					}

				}
			}
		}
		}



			//populate forests
		if (!Settings.get("Dominantbiome").equals("Forest") && !Settings.get("Dominantbiome").equals("Plain")) {
			for (int i = 0; i < w * h / 1200; i++) {
				int x = random.nextInt(w);
				int y = random.nextInt(h);
				for (int j = 0; j < 200; j++) {
					int xx = x + random.nextInt(15) - random.nextInt(15);
					int yy = y + random.nextInt(15) - random.nextInt(15);
					if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
						if (map[xx + yy * w] == Tiles.get("grass").id) {
							int typetree=random.nextInt(4);
							if(typetree<3)
								map[xx + yy * w] = Tiles.get(((i+yy+j)%5==0 ? "Small ":"")+"oak").id;
							else
								map[xx + yy * w] = Tiles.get(((i+xx+j)%5==3 ? "Small ":"")+"birch").id;
						}
					}
				}
			}
		}
		if (Settings.get("Dominantbiome").equals("Forest")) {
			for (int i = 0; i < w * h / 200; i++) {
				int x = random.nextInt(w);
				int y = random.nextInt(h);
				for (int j = 0; j < 200; j++) {
					int xx = x + random.nextInt(15) - random.nextInt(15);
					int yy = y + random.nextInt(15) - random.nextInt(15);
					if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
						if (map[xx + yy * w] == Tiles.get("grass").id) {
							boolean typetree=random.nextBoolean();
							if(typetree)
								map[xx + yy * w] = Tiles.get(((i+j)%7==0 ? "Small ":"")+"oak").id;
							else
								map[xx + yy * w] = Tiles.get((i%7==3 ? "Small ":"")+"birch").id;
						}
					}
				}
			}
		}
		for (int i = 0; i < w * h / 5; i++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("coarse dirt").id) {
					map[xx + yy * w] = Tiles.get("rock").id;
				}
			}
		}

		// Tundra biome, add fir trees
			for (int i = 0; i < w * h / 200; i++) {
				int x = random.nextInt(w);
				int y = random.nextInt(h);
				for (int j = 0; j < 60; j++) {
					int xx = x + random.nextInt(15) - random.nextInt(10+(Settings.get("Dominantbiome").equals("Tundra") ? 1 : 0));
					int yy = y + random.nextInt(15) - random.nextInt(10+(Settings.get("Dominantbiome").equals("Tundra") ? 1 : 0));
					if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
						if (map[xx + yy * w] == Tiles.get("snow").id) {
							String extraProp="";
							if((xx+j)%5>=3)extraProp="Small ";
							map[xx + yy * w] = Tiles.get(extraProp+"snowy conifer").id;
						}
					}
				}
			}
		//populate terrain with flowers
		for (int i = 0; i < w * h / 300; i++) {
			int x = random.nextInt(w);
			int y = random.nextInt(h);
			int col = i%4;
			int flotype = (i+x+y)%6;
			for (int j = 0; j < 30; j++) {
				int xx = x + random.nextInt(5) - random.nextInt(5);
				int yy = y + random.nextInt(5) - random.nextInt(5);
				if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
					if (map[xx + yy * w] == Tiles.get("grass").id || map[xx + yy * w] == Tiles.get("Tall grass").id) {

						switch(flotype){
							case 0:map[xx + yy * w] = Tiles.get("flower").id;break;
							case 1:map[xx + yy * w] = Tiles.get("rose").id;break;
							case 2:map[xx + yy * w] = Tiles.get("small flower").id;break;
							case 3:map[xx + yy * w] = Tiles.get("small rose").id;break;
							case 4:map[xx + yy * w] = Tiles.get("sunflower").id;break;
							case 5:int willReed=(xx+yy)%6;
							if(willReed==5)map[xx + yy * w] = Tiles.get("reed").id;
							else map[xx + yy * w] = Tiles.get("grass").id;
						};
						data[xx + yy * w] = (short) (col + j%(4+i%3) * 16); // Data determines which way the flower faces
					}
				}
			}
		}
		for (int i = 0; i < w * h / 3000; i++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("grass").id) {
					map[xx + yy * w] = Tiles.get("fern").id;
				}
			}
		}

		for (int i = 0; i < w * h / 12; i++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("glacier").id) {
					map[xx + yy * w] = Tiles.get("snow").id;
				}
			}
		}
		if(!Settings.get("theme").equals("Hell")) {
			for (int i = 0; i < w * h / 40; i++) {
				int xx = random.nextInt(w);
				int yy = random.nextInt(h);

				if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
					if (map[xx + yy * w] == Tiles.get("glacier").id) {
							map[xx + yy * w] = Tiles.get("ice").id;
					}
				}
			}
		}
		for (int i = 0; i < w * h / 20; i++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("glacier").id) {
					map[xx + yy * w] = Tiles.get(random.nextInt(2)==0 ? "small glacier" : "small glacier spikes").id;
				}
			}
		}

		for (int i = 0; i < w * h / 50; i++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("sand").id){
					map[xx + yy * w] = Tiles.get("dead tree").id;
				}
			}
		}
		for (int i = 0; i < w * h / 50; i++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("snow").id){
					map[xx + yy * w] = Tiles.get("snowy dead tree").id;
				}
			}
		}
		for (int i = 0; i < w * h / 40; i++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("sand").id) {
					map[xx + yy * w] = Tiles.get("desert grass").id;
				}
			}
		}

		for (int i = 0; i < w * h / 60; i++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("coarse dirt").id) {
					map[xx + yy * w] = Tiles.get("bramble").id;
				}
			}
		}
		for (int i = 0; i < w * h / 30; i++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("coarse dirt").id) {
					map[xx + yy * w] = Tiles.get("dead tree c").id;
				}
			}
		}
		for (int i = 0; i < w * h / 30; i++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("grass").id) {
					data[xx + yy * w] = (short) random.nextInt(1024);
					map[xx + yy * w] = Tiles.get("tall grass").id;
				}
			}
		}
		for (int i = 0; i < w * h / 50; i++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("grass").id) {
					map[xx + yy * w] = Tiles.get("grass small stones").id;
				}
			}
		}
		if(Settings.get("theme").equals("Hell")) {
			for (int i = 0; i < w * h / 30; i++) {
				int xx = random.nextInt(w);
				int yy = random.nextInt(h);

				if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
					if (map[xx + yy * w] == Tiles.get("moss").id) {
							map[xx + yy * w] = Tiles.get("lava").id;
					}
				}
			}
		}else{
			for (int i = 0; i < w * h / 40; i++) {
				int xx = random.nextInt(w);
				int yy = random.nextInt(h);

				if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
					if (map[xx + yy * w] == Tiles.get("moss").id) {
						map[xx + yy * w] = Tiles.get("fern").id;
					}
				}
			}
			for (int i = 0; i < w * h / 3; i++) {
				int xx = random.nextInt(w);
				int yy = random.nextInt(h);

				if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
					if (map[xx + yy * w] == Tiles.get("moss").id) {
						if (Math.random() < 0.06) map[xx + yy * w] = Tiles.get("Spiky stone").id;
						else if (Math.random() < 0.23) {
							map[xx + yy * w] = Tiles.get("reed").id;
							data[xx + yy * w] = (short) (-(random.nextInt(200)) + 20);
						}
						else if (Math.random()<0.42) {
							map[xx + yy * w] = Tiles.get("lily pad").id;
							data[xx + yy * w] = (short)random.nextInt(3);
						}
						else map[xx + yy * w] = Tiles.get("water").id;
					}
				}
			}
			for (int i = 0; i < w * h / 3; i++) {
				int xx = random.nextInt(w);
				int yy = random.nextInt(h);

				if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
					if (map[xx + yy * w] == Tiles.get("moss").id) {
						map[xx + yy * w] = Tiles.get("MangroveW").id;
					}
				}
			}
		}
		for (int i = 0; i < w * h / 1.15; i++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("moss").id) {

					map[xx + yy * w] = Tiles.get(Settings.get("Theme").equals("Hell") ? "Dirt" :"Mangrove").id;
				}
			}
		}
		for (int i = 0; i < w * h / 120; i++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("moss").id) {
					map[xx + yy * w] = Tiles.get((i%3==0?"Small ":"")+"oak").id;
				}
			}
		}
		for (int i = 0; i < w * h / 16; i++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("moss").id || map[xx + yy * w] == Tiles.get("Mangrove").id) {
					map[xx + yy * w] = Tiles.get(Settings.get("Theme").equals("Hell") ? "Coarse dirt" :"Fungus tree").id;
				}
			}
		}
		for (int i = 0; i < w * h / 12; i++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("moss").id) {
					map[xx + yy * w] = Tiles.get("dirt").id;
				}
			}
		}
		for (int i = 0; i < w * h / 30; i++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("moss").id) {
					map[xx + yy * w] = Tiles.get("Azalea").id;
				}
			}
		}

		//ice removal
		for (int i = 0; i < w * h / 6; i++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("ice").id) {
					map[xx + yy * w] = Tiles.get("water").id;
				}
			}
		}
		for (int i = 0; i < 4 * (w/128); i++) { //grasslands
			int xs = random.nextInt(w);
			int ys = (int)(i%2==0 ? random.nextInt(h/4) + h*0.8 : random.nextInt(h/4));
			for (int k = 0; k < 20; k++) {
				int x = xs + random.nextInt(21) - 10;
				int y = ys + random.nextInt(21) - 10;
				for (int j = 0; j < 100; j++) {
					int xo = x + random.nextInt(5) - random.nextInt(5);
					int yo = y + random.nextInt(5) - random.nextInt(5);
					for (int yy = yo - 1; yy <= yo + 1; yy++) {
						for (int xx = xo - 1; xx <= xo + 1; xx++) {
							if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
								if (map[xx + yy * w] == Tiles.get("grass").id ) {
									if(random.nextInt(15)< random.nextInt(10))
									map[xx + yy * w] = Tiles.get(random.nextInt(15)==0 ? "Fern" : "Tall grass").id;
								}
							}
						}
					}

				}
			}
		}
		for (int i = 0; i < w * h / (3150 * w/128); i++) { //Desert wastes subbiome
			int xs = random.nextInt(w);
			int ys = des ? (h/2) + (random.nextInt(61)-60) :(h/2) + (random.nextInt(41)-20);
			for (int k = 0; k < 10; k++) {
				int x = xs + random.nextInt(21) - 10;
				int y = ys + random.nextInt(21) - 10;
				for (int j = 0; j < 100; j++) {
					int xo = x + random.nextInt(5) - random.nextInt(5);
					int yo = y + random.nextInt(5) - random.nextInt(5);
					for (int yy = yo - 1; yy <= yo + 1; yy++)
						for (int xx = xo - 1; xx <= xo + 1; xx++)
							if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
								if (map[xx + yy * w] == Tiles.get("dead tree").id || map[xx + yy * w] == Tiles.get("cactus").id || map[xx + yy * w] == Tiles.get("cactus sapling").id || map[xx + yy * w] == Tiles.get("small cactus").id) map[xx + yy * w] = Tiles.get("sand").id;

							}
				}
			}
		}
		int count = 0;

		//if (Game.debug) System.out.println("Generating stairs for surface level...");
		
		stairsLoop:
		for (int i = 0; i < w * h / 100; i++) { // Loops a certain number of times, more for bigger world sizes.
			int x = random.nextInt(w - 2) + 1;
			int y = random.nextInt(h - 2) + 1;
			
			// The first loop, which checks to make sure that a new stairs tile will be completely surrounded by rock or glacier
			for (int yy = y - 1; yy <= y + 1; yy++)
				for (int xx = x - 1; xx <= x + 1; xx++)
					if (map[xx + yy * w] != Tiles.get("Rock").id && map[xx + yy * w] != Tiles.get("Glacier").id && map[xx + yy * w] != Tiles.get("RockG").id)
						continue stairsLoop;
			// This should prevent any stairsDown tile from being within 30 tiles of any other stairsDown tile.
			for (int yy = Math.max(0, y - stairRadius); yy <= Math.min(h - 1, y + stairRadius); yy++)
				for (int xx = Math.max(0, x - stairRadius); xx <= Math.min(w - 1, x + stairRadius); xx++)
					if (map[xx + yy * w] == Tiles.get("Stairs Down").id)
						continue stairsLoop;

					if(map[x + y * w] == Tiles.get("Glacier").id || map[x + y * w] == Tiles.get("Iced rock").id) map[x + y * w] = Tiles.get("Iced Stairs Down").id;
			else map[x + y * w] = Tiles.get("Stairs Down").id;
			if(count%(w>128 ? 9 : 6)==0) data[x + y * w] = (short) (random.nextInt(11)+ 30); //even stairs are buried

			count++;
			if (count >= w / 21) break;
		}
		
		//System.out.println("min="+min);
		//System.out.println("max="+max);
		//average /= w*h;
		//System.out.println(average);

		return new short[][]{map, data};
	}
	private static short[][] createDungeon(int w, int h) {
		LevelGen noise1 = new LevelGen(w, h, 8);
		LevelGen noise2 = new LevelGen(w, h, 8);
		
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

				if (val < -0.35) {
					map[i] = Tiles.get("Obsidian Wall").id;
				} else {
					map[i] = Tiles.get("Obsidian").id;
				}
				if ( val <= -1.7)
					map[i] = Tiles.get("Obsidian deepslate").id;
				else if (val < -0.35 && val > -1.7)
					map[i] = Tiles.get("Obsidian Wall").id;
				else if(val>=-0.35 && val < -0.22) map[i] = Tiles.get("Lava Brick").id;
					else {
					if(randObs>0.33) map[i] = Tiles.get("Obsidian").id;
					else if(randObs<=0.33 && randObs>0.014)map[i] = Tiles.get("Raw Obsidian").id;
					else map[i] = Tiles.get("dirt").id;
					}
			}
		}
		
		lavaLoop:
		for (int i = 0; i < w * h / 450; i++) {
			int x = random.nextInt(w - 20) + 10;
			int y = random.nextInt(h - 20) + 10;
			
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
								if (map[xx + yy * w] != Tiles.get("Obsidian wall").id && map[xx + yy * w] != Tiles.get("Obsidian Deepslate").id)
									map[xx + yy * w] = Tiles.get(Math.random() <0.004 ? "bramble" : "dirt").id;
							}
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
							if (xx >= 0 && yy >= 0 && xx < w && yy < h && Math.random()<0.07) {
								if ( map[xx + yy * w] == Tiles.get("dirt").id){
									map[xx + yy * w] = Tiles.get("dungeon tallgrass").id;
								}
							}

				}
			}
		}
		for (int i = 0; i < w * h / 5000; i++) {
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
								if (map[xx + yy * w] == Tiles.get("Obsidian").id)
									map[xx + yy * w] = Tiles.get(Math.random() <0.03 ? "gemB ore" : "lava").id;
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
			for (int j = 0; j <70; j++) {
				int xx = x + random.nextInt(5) - random.nextInt(5);
				int yy = y + random.nextInt(5) - random.nextInt(5);
				double chance = Math.random();
				if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
					 if(map[xx + yy * w] == Tiles.get("dirt").id || map[xx + yy * w] == Tiles.get("lava brick").id) map[xx + yy * w] = (short) ((Tiles.get(chance <0.01 ? "obsidium ore" :  (chance<0.2 ? "coal Ore" : "coarse dirt")).id & 0xff));
				}
			}
		}

		for (int i = 0; i < w * h / 5400; i++) {
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
								if (map[xx + yy * w] == Tiles.get("dirt").id) {
									map[xx + yy * w] = Tiles.get("Fungus").id;

								}
							}
				}
			}
		}
		return new short[][]{map, data};

	}
	private static short[][] createUndergroundMap(int w, int h, int depth) {
		LevelGen mnoise1 = new LevelGen(w, h, 16);
		LevelGen mnoise2 = new LevelGen(w, h, 16);
		LevelGen mnoise3 = new LevelGen(w, h, 16);
		
		LevelGen nnoise1 = new LevelGen(w, h, 16);
		LevelGen nnoise2 = new LevelGen(w, h, 16);
		LevelGen nnoise3 = new LevelGen(w, h, 16);
		
		LevelGen wnoise1 = new LevelGen(w, h, 16);
		LevelGen wnoise2 = new LevelGen(w, h, 16);
		LevelGen wnoise3 = new LevelGen(w, h, 16);
		
		LevelGen noise1 = new LevelGen(w, h, 32);
		LevelGen noise2 = new LevelGen(w, h, 32);
		String[] types = new String[] {"", "G", "B"};
		int type=random.nextInt(3);
		String gemnf="gem"+types[type]+"NF Ore";
		String gem="gem"+types[type]+" Ore";
		short[] map = new short[w * h];
		short[] data = new short[w * h];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int i = x + y * w;
				/// for the x=0 or y=0 i's, values[i] is always between -1 and 1.
				/// so, val is between -2 and 4.
				/// the rest are between -2 and 7.

				double val = Math.abs(noise1.values[i] - noise2.values[i]) * 3 - 2;

				double mval = Math.abs(mnoise1.values[i] - mnoise2.values[i]);
				mval = Math.abs(mval - mnoise3.values[i]) * 3 - 2;

				double nval = Math.abs(nnoise1.values[i] - nnoise2.values[i]);
				nval = Math.abs(nval - nnoise3.values[i]) * 3 - 2;

				double wval = Math.abs(wnoise1.values[i] - wnoise2.values[i]);
				wval = Math.abs(nval - wnoise3.values[i]) * 3 - 2;

				double xd = x / (w - 1.0) * 2 - 1;
				double yd = y / (h - 1.0) * 2 - 1;
				if (xd < 0) xd = -xd;
				if (yd < 0) yd = -yd;
				double dist = xd >= yd ? xd : yd;
				dist = Math.pow(dist, 8);
				val += 1 - dist * 20;
				if (val > -1 && wval < -1 + (depth > 3 ? 3 : depth) / 2 * 3) {
					switch(depth){
						case 5:if(val>-1 && val<-0.78)map[i] = Tiles.get("dirt").id;
						else if(val>=-0.78 && val<-0.74)map[i] = Tiles.get(Math.random() < 0.15 ? "Obsidian rock" : "Raw Obsidian").id;
						else map[i] = Tiles.get("lava").id;
							if(i>10 && i<(w*h)-10) {
								double chance = Math.random();
								int spikeGen = (int) Math.round(Math.random() * 3);
								if (map[i - spikeGen] == Tiles.get("lava").id && chance < 0.01)
									map[i - spikeGen] = Tiles.get("deepslate spiky stone").id;
								else if(map[i - spikeGen] == Tiles.get("dirt").id && chance < 0.03)
									map[i - spikeGen] = Tiles.get("deepslate spiky stone-L").id;
							}
							for (int l = 0; l < w * h / 12800; l++) {
								int xx = random.nextInt(w);
								int yy = random.nextInt(h);

								if (xx >= 0 && yy >= 0 && xx < w && yy < h && random.nextInt(4)==3) {
									if (map[xx + yy * w] == Tiles.get("dirt").id) {
										map[xx + yy * w] = Tiles.get("small stones").id;
									}
								}
							}break;
						case 4:double extraGrnd=Settings.get("Theme").equals("Hell") ? 2 : 0;
							if(mval > 0.2) {
								map[i] = Tiles.get("water").id;
							}else if(val<0.5){
								if(wval > -0.5) map[i] = Tiles.get("deepslateG").id;
								else map[i] = Tiles.get("coarse dirt").id;
								if(wval> -0.5 && Math.random()<0.1)map[i] = Tiles.get(val < -3 ? "gemNF ore" : "goldNf ore").id;
							}else if(val>=0.5 && val<1.2)map[i] = Tiles.get("moss").id;
								else map[i] = Tiles.get("deepslate").id;
							for (int l = 0; l < 5; l++) { //duplicating the loop to populate waters
								int xx = random.nextInt(w);
								int yy = random.nextInt(h);

								if (xx >= 0 && yy >= 0 && xx < w && yy < h && val > 1 && val <=2 && yy%(random.nextInt(4)+2)==0 && xx%(random.nextInt(4)+2)==1) {
									if (map[xx + yy * w] == Tiles.get("water").id) {
										map[xx + yy * w] = Tiles.get("reed").id;
										data[xx + yy * w] = (short) (-(random.nextInt(300))+30);
									}
								}
							}


							for (int l = 0; l < 4; l++) {
								int xx = random.nextInt(w);
								int yy = random.nextInt(h);

								if (xx >= 0 && yy >= 0 && xx < w && yy < h && val < -0.6 && val > -0.65 && xx%(random.nextInt(2)+1)==0 && yy%(random.nextInt(4)+2)==0) {
									if (map[xx + yy * w] == Tiles.get("moss").id) {
										map[xx + yy * w] = Tiles.get("bramble").id;
									}
								}
							};
							for (int l = 0; l < 2; l++) {
								int xx = random.nextInt(w);
								int yy = random.nextInt(h);

								if (xx >= 0 && yy >= 0 && xx < w && yy < h && wval<0.4 && val>0) {
									if (map[xx + yy * w] == Tiles.get("moss").id) {
										map[xx + yy * w] = Tiles.get("spiky stone-L").id;
										data[xx + yy * w] = (short)random.nextInt(3);
									}
								}
							}
							for (int l = 0; l < 4; l++) {
								int xx = random.nextInt(w);
								int yy = random.nextInt(h);

								if (xx >= 0 && yy >= 0 && xx < w && yy < h && wval<1)
									if (map[xx + yy * w] == Tiles.get("moss").id) {
										map[xx + yy * w] = Tiles.get("coarse dirt").id;
									}

							}
							for (int l = 0; l < 2; l++) {
								int xx = random.nextInt(w);
								int yy = random.nextInt(h);

								if (xx >= 0 && yy >= 0 && xx < w && yy < h && wval<-1)
									if (map[xx + yy * w] == Tiles.get("moss").id) {
										map[xx + yy * w] = Tiles.get(val < 0 ? "dirt" : "azalea").id;
									}

							}
							break;
						case 3:extraGrnd=Settings.get("Theme").equals("Hell") ? 0.5 : 0;
							if(val > 2.5) {

								map[i] = Tiles.get("water").id;
							}else if(val>2.1-extraGrnd && val<=3 || (mval>-0.5 && mval<1)){
								map[i] = Tiles.get("Ground rock").id;
							}else{
								map[i] = Tiles.get("water").id;
							}
							for (int l = 0; l < 5; l++) { //duplicating the loop to populate waters
								int xx = random.nextInt(w);
								int yy = random.nextInt(h);

								if (xx >= 0 && yy >= 0 && xx < w && yy < h && val > 1 && val <=2 && yy%(random.nextInt(4)+2)==0 && xx%(random.nextInt(4)+2)==1) {
									if (map[xx + yy * w] == Tiles.get("water").id) {
										map[xx + yy * w] = Tiles.get("reed").id;
										data[xx + yy * w] = (short) (-(random.nextInt(300))+30);
									}
								}
							}
							double chance = Math.random();
							if(mval<1.4 && mval>-1.4 && chance<0.33 && map[i]==Tiles.get("Ground rock").id) {
								if (chance < 0.05) map[i] = Tiles.get(Math.random() > 0.6 ? "azalea" : "Moss").id;
								else map[i] = Tiles.get("small stones").id;
								if (i % 4 == 0) map[i] = Tiles.get("bramble").id;
							}

							for (int l = 0; l < 2; l++) {
								int xx = random.nextInt(w);
								int yy = random.nextInt(h);

								if (xx >= 0 && yy >= 0 && xx < w && yy < h && val < 0.3 && val > 0 && xx%(random.nextInt(2)+2)==0 && yy%(random.nextInt(3)+1)==0) {
									if (map[xx + yy * w] == Tiles.get("water").id) {
										map[xx + yy * w] = Tiles.get("spiky stone").id;
									}
								}
							}
							for (int l = 0; l < 4; l++) {
								int xx = random.nextInt(w);
								int yy = random.nextInt(h);

								if (xx >= 0 && yy >= 0 && xx < w && yy < h && val < -0.6 && val > -0.65 && xx%(random.nextInt(2)+1)==0 && yy%(random.nextInt(4)+2)==0) {
									if (map[xx + yy * w] == Tiles.get("water").id) {
										map[xx + yy * w] = Tiles.get("lily pad").id;
										data[xx + yy * w] = (short)random.nextInt(3);
									}
								}
							};break;
						case 2:if(val >-0.8 && val <-0.64) {
							map[i] = Tiles.get("dirt").id;
						}else if(val >-0.64 && val <-0.3) {
							chance = Math.random();
							if(mval<1.4 && mval>-1.4 && chance<0.7) {
								if (chance < 0.15) map[i] = Tiles.get(Math.random() > 0.6 ? "azalea" : "Moss").id;
								else map[i] = Tiles.get("small stones").id;
								if ((i-(x*y))% 4 == 0 && chance>0.4) map[i] = Tiles.get("bramble").id;
							}else
							map[i] = Tiles.get("coarse dirt").id;
						}else if(val>=-0.3 && val<-0.24){
							if(Settings.get("Theme").equals("Hell"))
								map[i] = Tiles.get(i%3==2 ? "water" : "hole").id;
							else map[i] = Tiles.get("water").id;
						}else{
							map[i] = Tiles.get("rock").id;

						};break;
						case 1:
							chance = Math.random();
							if(val > -.23 && val<-.14)map[i] = Tiles.get("Bramble").id;
							else if((val>=1 && val<3) || (val>=-0.1 && val<0.5) || (wval>-0.8 && wval<1.1)) {
								map[i] = Tiles.get("Ground rock").id;

							}else if(mval<1.4 && mval>-1.4 && chance<0.7) {
								if (chance < 0.15) map[i] = Tiles.get("coarse dirt").id;
								else map[i] = Tiles.get("small stones").id;
								if ((i-(x*y))% 4 == 0 && chance>0.4) map[i] = Tiles.get("bramble").id;
							}
							else map[i] = Tiles.get("dirt").id;

							break;
					}

				} else if (val > -2 && (mval < -1.7 || nval < -1.4)) {
					switch(depth) {
						case 5:
							map[i] = Tiles.get("coarse dirt").id;
							break;
						case 4:
							if (val > -2 && val < -1 || (mval>0 && mval<0.5)) map[i] = Tiles.get( "moss").id;
							else if (val >= -1 && val < 0) map[i] = Tiles.get(Math.random() < 0.04 ? (Math.random() < 0.25 ? "deepslate spiky stone-L" : "rock"): (Math.random()<0.01 ? "fungus" :"dirt")).id;
							else map[i] = Tiles.get("deepslateG").id;
							if (x >= 0 && y >= 0 && x < w && y < h) {
								if(Math.random()<0.05)map[i] = Tiles.get("rock").id;
							}
							for (int l = 0; l < 2; l++) {
								int xx = random.nextInt(w);
								int yy = random.nextInt(h);

								if (xx >= 0 && yy >= 0 && xx < w && yy < h && val < 0.3 && val > 0 && xx%(random.nextInt(2)+2)==0 && yy%(random.nextInt(3)+1)==0) {
									if (map[xx + yy * w] == Tiles.get("water").id || map[xx + yy * w] == Tiles.get("moss").id) {
										map[xx + yy * w] = Tiles.get("spiky stone").id;
									}
								}
							}
							for (int l = 0; l < 5; l++) {
								int xx = random.nextInt(w);
								int yy = random.nextInt(h);

								if (xx >= 0 && yy >= 0 && xx < w && yy < h && val < 0.3 && val > 0 && xx%(random.nextInt(2)+2)==0 && yy%(random.nextInt(3)+1)==0) {
									if (map[xx + yy * w] == Tiles.get("moss").id) {
										map[xx + yy * w] = Tiles.get("deepslate spiky stone").id;
									}
								}
							}
							for (int l = 0;l <= w * h / (w/4); l++) {
								int xx = random.nextInt(w);
								int yy = random.nextInt(h);

									if (map[xx + yy * w] == Tiles.get("moss").id && Math.random()<0.05) {
										map[xx + yy * w] = Tiles.get(Math.random() < 0.2 ? "azalea" : (Math.random() < 0.6 ? "small fungus tree" : "fungus tree")).id;
									}

							}

							break;
						case 3:
							if (val > -2 && val < -0.5) map[i] = Tiles.get("moss").id;
							else map[i] = Tiles.get("dirt").id;


							double chance = Math.random();
							if (x >= 0 && y >= 0 && x < w && y < h) {
								if (map[i] == Tiles.get("Moss").id && chance < 0.34) {
									int col = random.nextInt(4); //plants
									if (chance >= 0.16 && chance <= 0.28) map[i] = Tiles.get("Fungus tree").id;
									else if (chance > 0.09 && chance < 0.16) map[i] = Tiles.get("Azalea").id;
									else if (chance > 0.04 && chance <= 0.09) map[i] = Tiles.get("Bramble").id;
									else if (chance > 0.01 && chance <= 0.04) {
										map[i] = Tiles.get("Dirt").id;
									} else map[i] = Tiles.get("Spiky stone-L").id;
									data[i] = (short) (col + random.nextInt(4) * 16); // Data determines which way the Azalea faces
								} else if (map[i] == Tiles.get("dirt").id) { //new biome
									int col = random.nextInt(35); //plants and other stuff
									switch (col) {
										case 1:
										case 7:
										case 5:map[i] = Tiles.get("fungus").id;break;
										case 11:
										case 23:
											map[i] = Tiles.get("RockG").id;
											break;
										case 2:
											map[i] = Tiles.get("spiky stone-l").id;
											break;
										case 4:
											map[i] = Tiles.get("small stones").id;
											break;
										case 20:
										case 15:
											map[i] = Tiles.get("coarse dirt").id;
											break;
									}
								}
							}
							;
							for (int l = 0; l < 3; l++) { //duplicating the loop to populate moss
								int xx = random.nextInt(w);
								int yy = random.nextInt(h);

								if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
									if (map[xx + yy * w] == Tiles.get("moss").id) {
										if (xx % 3 == 0) map[xx + yy * w] = Tiles.get("small fungus Tree").id;
										else {
											map[xx + yy * w] = Tiles.get("fungus spores").id;
											data[xx + yy * w] = (short) (-(random.nextInt(300)) + 30);
										}
									}
								}
							}


							break;
						case 2:
							chance = Math.random();
							if (chance > 0.2 && chance < 0.67) map[i] = Tiles.get("dirt").id;
							else if (chance >= 0.67 && chance < 0.75) map[i] = Tiles.get("water").id;
							else map[i] = Tiles.get("rock").id;
							break;
						case 1:
							chance = Math.random();
							if (chance > 0.012) map[i] = Tiles.get("dirt").id;
							else map[i] = Tiles.get("Spiky stone-L").id;
							break;


					}
				} else {
					switch(depth){
						case 5: if(val<-4.5  ||( wval>-0.3 && wval>-0.1))map[i] = Tiles.get("deepslateG").id;
						else map[i] = Tiles.get("deepslate").id;break;
						case 4: if(mval<-2 || wval<0){
							map[i] = Tiles.get("deepslate" + (val>-4 ? "G" : "")).id;
							if(Math.random()<0.02)map[i] = Tiles.get(val < -3 ? ("gem"+((y%3==0) ? "G" : "")+"NF ore") : "goldNf ore").id;
						}
							else if(val<-4.5)map[i] = Tiles.get("rockG").id;
							else map[i] = Tiles.get("rock").id;break;
						case 3:

							map[i] = Tiles.get(val > -4 || mval>1 ? "rock" : "rockG").id;
							if(Math.random()<0.05 && val > -3)map[i] = Tiles.get(val < -2  ? ((i%4==0) ? ("lapis"+(mval>-0.5 ? "NF ore" : "")) : "goldNF ore") : "iron ore").id;
							if(val>-1 && val<-0.2)map[i] = Tiles.get("deepslateG").id;
						break;
						default: map[i] = Tiles.get(val > -4 || mval>1 ? "rock" : "rockG").id;break;

					}
				}
				if(depth>=4)
				for (int l = 0; l < w / 64; l++) { //duplicating the loop to populate land
					int xx = random.nextInt(w);
					int yy = random.nextInt(h);

					if (xx >= 0 && yy >= 0 && xx < w && yy < h && val > -1.7 && (mval < -2.4 || nval < -1.9)) {
						if (map[xx + yy * w] == Tiles.get("coarse dirt").id) {
							map[xx + yy * w] = Tiles.get((xx % 4 <2 || yy % 4 == 1) ? "rockG" : "deepslateG").id;
						}
					}
				}
			}
			}
		{

			int r = 2;
			for (int i = 0; i < w * h / 400; i++) {
				type = random.nextInt(3);
				gemnf = "gem" + types[type] + "NF Ore";
				gem = "gem" + types[type] + " Ore";
				int x = random.nextInt(w);
				int y = random.nextInt(h);
				switch (depth) {
					case 1:
						for (int j = 0; j < 30; j++) {
							int xx = x + random.nextInt(5) - random.nextInt(5);
							int yy = y + random.nextInt(5) - random.nextInt(5);
							if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
								if (map[xx + yy * w] == Tiles.get("rock").id || map[xx + yy * w] == Tiles.get("rockG").id) {
									double chance = Math.random();
									if (chance > 0.2)
										map[xx + yy * w] = (short) ((Tiles.get("iron Ore").id & 0xff));
									else map[xx + yy * w] = (short) ((Tiles.get("ironnf Ore").id & 0xff));
								}
							}
						}
						for (int j = 0; j < 10; j++) {
							int xx = x + random.nextInt(3) - random.nextInt(2);
							int yy = y + random.nextInt(3) - random.nextInt(2);
							if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
								if (map[xx + yy * w] == Tiles.get("rock").id || map[xx + yy * w] == Tiles.get("rockG").id) {
									map[xx + yy * w] = (short) (Tiles.get("Lapis"+(random.nextInt(19)==7 ? "NF ore" : "")).id & 0xff);
								}
								if (((xx*11)*(yy/3))%4==0 && map[xx + yy * w] == Tiles.get("ground rock").id) map[(xx+3 > w ? xx : xx+3) + yy * w] = (short) (Tiles.get("Lapis").id & 0xff);
							}
						}
						break;
					case 2:
						for (int j = 0; j < 30; j++) {
							int xx = x + random.nextInt(5) - random.nextInt(5);
							int yy = y + random.nextInt(5) - random.nextInt(5);
							if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
								if (map[xx + yy * w] == Tiles.get("rock").id || map[xx + yy * w] == Tiles.get("rockG").id) {
									double chance = Math.random();
									if (chance > 0.2 && chance < 0.45)
										map[xx + yy * w] = (short) ((Tiles.get("iron Ore").id & 0xff));
									else if (chance >= 0.45) map[xx + yy * w] = Tiles.get("ironNF Ore").id;
									else map[xx + yy * w] = (short) ((Tiles.get("gold Ore").id & 0xff));
								}
							}
						}
						for (int j = 0; j < 10; j++) {
							int xx = x + random.nextInt(3) - random.nextInt(2);
							int yy = y + random.nextInt(3) - random.nextInt(2);
							if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
								if (map[xx + yy * w] == Tiles.get("rock").id || map[xx + yy * w] == Tiles.get("rockG").id) {
									map[xx + yy * w] = (short) (Tiles.get("Lapis" +(random.nextInt(19)==7 ? "NF ore" : "")).id & 0xff);
								}
							}
						}
						break;
					case 3:
						for (int j = 0; j < 30; j++) {
							int xx = x + random.nextInt(5) - random.nextInt(5);
							int yy = y + random.nextInt(5) - random.nextInt(5);
							if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
								if (map[xx + yy * w] == Tiles.get("rock").id || map[xx + yy * w] == Tiles.get("rockG").id) {
									double chance = Math.random();
									if (chance > 0.2)
										map[xx + yy * w] = (short) ((Tiles.get("gold Ore").id & 0xff));
									else map[xx + yy * w] = (short) ((Tiles.get("goldnf Ore").id & 0xff));
								}
							}
						}
						for (int j = 0; j < 10; j++) {
							int xx = x + random.nextInt(3) - random.nextInt(2);
							int yy = y + random.nextInt(3) - random.nextInt(2);
							if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
								if (map[xx + yy * w] == Tiles.get("rock").id || map[xx + yy * w] == Tiles.get("rockG").id) {
									map[xx + yy * w] = (short) (Tiles.get("Lapis"+(random.nextInt(29)%11==0 ? "NF ore" : "")).id & 0xff);
								}
								if (((xx*11)*(yy/3))%4==0 && map[xx + yy * w] == Tiles.get("ground rock").id) map[(xx+3 > w ? xx : xx+3) + yy * w] = (short) (Tiles.get("Lapis").id & 0xff);
							}
						}
						break;
					case 4:
						for (int j = 0; j < 30; j++) {
							int xx = x + random.nextInt(5) - random.nextInt(5);
							int yy = y + random.nextInt(5) - random.nextInt(5);
							if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
								if (map[xx + yy * w] == Tiles.get("rock").id || map[xx + yy * w] == Tiles.get("rockG").id) {
									double chance = Math.random();
									if (chance > 0.2 && chance < 0.45)
										map[xx + yy * w] = (short) ((Tiles.get("gold Ore").id & 0xff));
									else if (chance >= 0.45) map[xx + yy * w] = Tiles.get("goldNF Ore").id;
									else map[xx + yy * w] = (short) ((Tiles.get(gem).id & 0xff));
								}
							}
						}
						for (int j = 0; j < 10; j++) {
							int xx = x + random.nextInt(3) - random.nextInt(2);
							int yy = y + random.nextInt(3) - random.nextInt(2);
							if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
								if (map[xx + yy * w] == Tiles.get("rock").id || map[xx + yy * w] == Tiles.get("rockG").id) {
									map[xx + yy * w] = (short) (Tiles.get("Lapis").id & 0xff);
								}
							}
						}
						break;
					case 5:
						for (int j = 0; j < 30; j++) {
							int xx = x + random.nextInt(5) - random.nextInt(5);
							int yy = y + random.nextInt(5) - random.nextInt(5);
							if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
								if (map[xx + yy * w] == Tiles.get("deepslate").id || map[xx + yy * w] == Tiles.get("deepslateG").id) {
									double chance = Math.random();
									if (chance > 0.04) {
										if (chance > 0.2) map[xx + yy * w] = (short) (Tiles.get(gem).id & 0xff);
										else map[xx + yy * w] = (short) (Tiles.get(gemnf).id & 0xff);
									} else map[xx + yy * w] = (short) (Tiles.get("Lapis"+(random.nextInt(24)==7 ? "NF ore" : "")).id & 0xff);
									if (((xx*11)*(yy/3))%4==0 && map[xx + yy * w] == Tiles.get("ground rock").id) map[(xx+3 > w ? xx : xx+3) + yy * w] = (short) (Tiles.get("Lapis").id & 0xff);
								}
							}
						}
						for (int j = 0; j < 10; j++) {
							int xx = x + random.nextInt(3) - random.nextInt(2);
							int yy = y + random.nextInt(3) - random.nextInt(2);
							if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
								if (map[xx + yy * w] == Tiles.get("deepslate").id || map[xx + yy * w] == Tiles.get("deepslateG").id) {
									if ((xx*yy)%4==0) map[xx + yy * w] = (short) (Tiles.get("Lapis" + (random.nextInt(5)==4 ? "NF ore" : "")).id & 0xff);
									else map[xx + yy * w] = (short) (Tiles.get("Obsidian Ore").id & 0xff);
								}
							}
						}
						break;
				}
				//generate coal ores across all floors
				 r = 4;
				for (int j = 0; j < 30; j++) {
					int xx = x + random.nextInt(5) - random.nextInt(5);
					int yy = y + random.nextInt(5) - random.nextInt(5);
					if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
						double chance = Math.random();
						if (map[xx + yy * w] == Tiles.get("Iron ore").id || map[xx + yy * w] == Tiles.get("gold ore").id || map[xx + yy * w] == Tiles.get("Obsidium ore").id || map[xx + yy * w] == Tiles.get("gemB ore").id || map[xx + yy * w] == Tiles.get("gemg ore").id) {

							if (chance < 0.15) map[xx + yy * w] = (short) (Tiles.get("coal ore").id & 0xff);

						}
					}
				}
				for (int j = 0; j < 20 * (w/128); j++) {
					int xx = x + random.nextInt(8) - random.nextInt(8);
					int yy = y + random.nextInt(8) - random.nextInt(8);
					double chance = Math.random();
					if (chance < 0.2)
					if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
						if (map[xx + yy * w] == Tiles.get("dirt").id || map[xx + yy * w] == Tiles.get("coarse dirt").id) {

								map[xx + yy * w] = (short) ((Tiles.get("coal Ore").id & 0xff));
						}
					}
				}
			}
		}
		if (depth == 5) {
			int r = 1;
			double xP=(w/128);
			double yP=(h/128);
			int xx = (int)(w/2-((worldSeed%(26*xP))-(13*xP)));
			int yy = (int)(h/2-((worldSeed%(26*xP))-(13*yP)));
			String[] tileNames={"Obsidian wall","Obsidian","Ornate obsidian","dirt","obsidian ore","lava","obsidian rock"};
			for(int i=0;i<220 * (w/128);i+=(random.nextInt(5))){
				int xT = xx+(random.nextInt(27*(w/128)) - 13*(w/128));
				int yT = yy+(random.nextInt(27*(h/128)) - 13*(h/128));
				map[xT + yT * w]=Tiles.get(tileNames[random.nextInt(tileNames.length)]).id;
			}

			for (int i = 0; i < w * h / 380; i++) {
				for (int j = 0; j < 10; j++) {
					if (xx < w - r && yy < h - r) {
						
						Structure.dungeonLock.draw(map, xx, yy, w);
						
						/// The "& 0xff" is a common way to convert a byte to an unsigned int, which basically prevents negative values... except... this doesn't do anything if you flip it back to a byte again...
						map[xx + yy * w] = (short) (Tiles.get("Obsidian Stairs Down").id & 0xff);
					}
				}
			}
		}

		if (depth < 5) {
			int count = 0;
			stairsLoop:
			for (int i = 0; i < w * h / 100; i++) {
				int x,y;
				int wmod128=w/128;
				int hmod128=h/128;
				//Old disbalanced downstairs generating... will be used by 128x128 worlds
				if(w<256 && h<256) {
					x = random.nextInt(w - 20) + 10;
					y = random.nextInt(h - 20) + 10;
				}else{
					//new more balanced and less painful stairs generating for bigger worlds
					x = random.nextInt(w - (20*wmod128) - (count*4*(wmod128-1))) + (10*wmod128)+(count*4*(wmod128-1));
					y = random.nextInt(h - (20*hmod128) - (count*4*(hmod128-1))) + (10*hmod128)+(count*4*(hmod128-1));
				}



				for (int yy = y - 1; yy <= y + 1; yy++)
					for (int xx = x - 1; xx <= x + 1; xx++)
						if (map[xx + yy * w] != Tiles.get("rock").id && map[xx + yy * w] != Tiles.get("RockG").id) continue stairsLoop;

				
				// This should prevent any stairsDown tile from being within 30 tiles of any other stairsDown tile.
				for (int yy = Math.max(0, y - stairRadius); yy <= Math.min(h - 1, y + stairRadius); yy++)
					for (int xx = Math.max(0, x - stairRadius); xx <= Math.min(w - 1, x + stairRadius); xx++)
						if (map[xx + yy * w] == Tiles.get("Stairs Down").id) continue stairsLoop;
				
				map[x + y * w] = Tiles.get("Stairs Down").id;
				if(count%(w>128 ? 4 : 2)==0)data[x + y * w]=30;
				count++;
				if (count >= (w / 32)+((w/128)-1)) break; //at least 4 on 128x128 , more on bigger worlds
			}
		}
		//ground rock beaches
		if(depth>=3){
			for (int j = 0; j < h; j++) {
				int beaches_thickness=(depth==4 ? 2 : 1);
				String fluid=(depth==4 ? "Lava" : "Water");
				for (int xi = 0; xi < w; xi++) {
					if ((map[xi + j * w] != Tiles.get(fluid).id && map[xi + j * w] == Tiles.get("dirt").id) ||
							(map[xi + j * w] != Tiles.get(fluid).id && map[xi + j * w] == Tiles.get("coarse dirt").id) ||
							(map[xi + j * w] != Tiles.get(fluid).id && map[xi + j * w] == Tiles.get("RockG").id)
					) {
						boolean replace = false;
						int tx;
						check_ocean:
						for (tx = xi - beaches_thickness; tx <= xi + beaches_thickness; tx++) { // left - right
							for (int ty = j - beaches_thickness; ty <= j + beaches_thickness; ty++) { // up - down
								if (tx > 0 && ty > 0 && tx < w && ty < h && (tx != xi || ty != j) && map[tx + ty * w] == Tiles.get(fluid).id) {
									replace = true;
									break check_ocean;
								}
							}
						}

						if (replace) {
							map[xi + j * w] = Tiles.get("Ground rock").id;
						}

					}

				}

			}

		}
		if(depth==3 || depth==4) {
			int x,y;
			for (int l = 0; l < w * h / 3000; l++) { //Cave wastes
				int xs = random.nextInt(w);
				int ys = random.nextInt(h);
				for (int k = 0; k < 10; k++) {
					x = xs + random.nextInt(15) - 10;
					y = ys + random.nextInt(15) - 10;
					for (int j = 0; j < 100; j++) {
						int xo = x + random.nextInt(5) - random.nextInt(5);
						int yo = y + random.nextInt(5) - random.nextInt(5);
						for (int yy = yo - 1; yy <= yo + 1; yy++)
							for (int xx = xo - 1; xx <= xo + 1; xx++)
								if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
									if (map[xx + yy * w] != Tiles.get("rockG").id && map[xx + yy * w] != Tiles.get("rock").id && map[xx + yy * w] != Tiles.get("water").id) {
										map[xx + yy * w] = Tiles.get("dirt").id;
									}
								}
					}
				}
			}
			for (int l = 0;l< w * h / 12500; l++) {
				int xs = random.nextInt(w);
				int ys = random.nextInt(h);
				for (int k = 0; k < 10; k++) {
					int X = xs + random.nextInt(21) - 10;
					int Y = ys + random.nextInt(21) - 10;
					for (int j = 0; j < 100; j++) {
						int xo = X + random.nextInt(5) - random.nextInt(5);
						int yo = Y + random.nextInt(5) - random.nextInt(5);
						for (int yy = yo - 1; yy <= yo + 1; yy++)
							for (int xx = xo - 1; xx <= xo + 1; xx++)
								if (xx >= 0 && yy >= 0 && xx < w && yy < h ) {
									if ( map[xx + yy * w] == Tiles.get("dirt").id || map[xx + yy * w] == Tiles.get("moss").id && Math.random()<Math.random()){
										map[xx + yy * w] = Tiles.get("fungus").id;
									}
								}

					}
				}
			}
		}

		return new short[][]{map, data};
	}

	private static short[][] createSkyMap(int w, int h) {
		LevelGen noise1 = new LevelGen(w, h, 8);
		LevelGen noise2 = new LevelGen(w, h, 8);
		LevelGen mnoise1 = new LevelGen(w, h, 16);
		LevelGen mnoise2 = new LevelGen(w, h, 16);
		LevelGen mnoise3 = new LevelGen(w, h, 16);
		
		short[] map = new short[w * h];
		short[] data = new short[w * h];
		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int i = x + y * w;

				double val = Math.abs(noise1.values[i] - noise2.values[i]) * 3 - 2;
				double mval = Math.abs(mnoise1.values[i] - mnoise2.values[i]);
				double xd = x / (w - 1.0) * 2 - 1;
				double yd = y / (h - 1.0) * 2 - 1;
				if (xd < 0) xd = -xd;
				if (yd < 0) yd = -yd;
				double dist = xd >= yd ? xd : yd;
				dist = dist * dist * dist * dist;
				dist = dist * dist * dist * dist;
				val = -val * 1 - 2.2;
				val += 1 - dist * 20;

				if (val < -0.27) {
					map[i] = Tiles.get("Infinite Fall").id;
				} else {
					int differ=12+(((x+y)%15)-7);
					if(!(val>=-0.16 && val<-0.1)){
						if(val>=-0.27 && val <=-0.06 || ((x<differ*(w/128) || x>w-(differ*(w/128))) || (y<differ*(h/128) || y>h-(differ*(h/128)))))
						map[i] = Tiles.get("aerocloud").id;
						else map[i] = Tiles.get("cloud").id;
					}else if(val>-0.06 && val<0.4)map[i] = Tiles.get("cloud").id;
					else {
						if(mval>0.2 && mval<1.5 && val < 0.5) map[i] = Tiles.get("cloud").id;
						else  map[i] = Tiles.get("skygrass").id;
					}
					}

				}

			}
		for (int i = 0; i < w * h / 2000; i++) { //new biome
			int xs = random.nextInt(w);
			int ys = random.nextInt(h);
			for (int k = 0; k < 10; k++) {
				int x = xs + random.nextInt(15) - 10;
				int y = ys + random.nextInt(15) - 10;
				for (int j = 0; j < 100; j++) {
					int xo = x + random.nextInt(5) - random.nextInt(5);
					int yo = y + random.nextInt(5) - random.nextInt(5);
					for (int yy = yo - 1; yy <= yo + 1; yy++)
						for (int xx = xo - 1; xx <= xo + 1; xx++)
							if (xx >= 0 && yy >= 0 && xx < w && yy < h && (yy < h/4 || y > (h - (h/4)))) {
								double mval = Math.abs(mnoise1.values[i] - mnoise2.values[i]);
								if (map[xx + yy * w] == Tiles.get("cloud").id ||  map[xx + yy * w] == Tiles.get("skygrass").id){
									int r=random.nextInt(15);
									if(r<3 && mval>-0.2 && mval<0.4)map[xx + yy * w] = Tiles.get("sky conifer").id;
									else map[xx + yy * w] = Tiles.get("aerocloud").id;
								}
							}
				}
			}
		}
		for (int i = 0; i < w * h / 2000; i++) { //new biome
			int xs = random.nextInt(w);
			int ys = random.nextInt(h);
			for (int k = 0; k < 10; k++) {
				int x = xs + random.nextInt(15) - 10;
				int y = ys + random.nextInt(15) - 10;
				for (int j = 0; j < 100; j++) {
					int xo = x + random.nextInt(5) - random.nextInt(5);
					int yo = y + random.nextInt(5) - random.nextInt(5);
					for (int yy = yo - 1; yy <= yo + 1; yy++)
						for (int xx = xo - 1; xx <= xo + 1; xx++)
							if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
								if (map[xx + yy * w] != Tiles.get("sky rock").id &&  map[xx + yy * w] != Tiles.get("infinite fall").id){
									int r=random.nextInt(15);
									if(r>11)map[xx + yy * w] = Tiles.get((r==12 ? "small " : "" )+ "cloud cactus").id;
									else if(r<2)map[xx + yy * w] = Tiles.get("sky rock").id;
									else map[xx + yy * w] = Tiles.get("cloud tallgrass").id;
								}
							}
				}
			}
		}
		for (int i = 0; i < w * h / 1400; i++) {
			int xs = random.nextInt(w);
			int ys = random.nextInt(h);
			for (int k = 0; k < 10; k++) {
				int x = xs + random.nextInt(15) - 10;
				int y = ys + random.nextInt(15) - 10;
				for (int j = 0; j < 100; j++) {
					int xo = x + random.nextInt(5) - random.nextInt(5);
					int yo = y + random.nextInt(5) - random.nextInt(5);
					for (int yy = yo - 1; yy <= yo + 1; yy++)
						for (int xx = xo - 1; xx <= xo + 1; xx++)
							if (xx > 20 * (Math.ceil(w / 128)) && yy > 20 * (Math.ceil(h / 128)) && xx < w - 20 * (Math.ceil(w / 128)) && yy < h - 20 * (Math.ceil(h / 128))) {
								if (map[xx + yy * w] == Tiles.get("infinite fall").id) {
									map[xx + yy * w] = Tiles.get("sky rock").id;
								}
							}
				}
			}
		}
		for (int i = 0; i < w * h / 1200; i++) {
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
								if (map[xx + yy * w] == Tiles.get("cloud").id) {
									double chance=Math.random();
									/*if(chance>0.15)*/map[xx + yy * w] = Tiles.get("skygrass").id;

								}
							}
				}
			}
		}

		for(int p = 0;p<2;p++)
		for (int l = 0; l < (w*h / 7); l++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("skygrass").id) {
					map[xx + yy * w] = Tiles.get("sky "+(p==1 ? "conifer" : "tree")).id;
				}
			}
		}
		for (int l = 0; l < w*h/12; l++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("cloud").id) {
					map[xx + yy * w] = Tiles.get("cloud tallgrass").id;
				}
			}
		}
		for (int l = 0; l < w*h/30; l++) { //duplicating the loop to populate waters
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("cloud").id) {
					map[xx + yy * w] = Tiles.get("cloud flower").id;
				}
			}
		}
		for (int l = 0; l < w*h/60; l++) { //duplicating the loop to populate waters
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("cloud").id) {
					map[xx + yy * w] = Tiles.get("cloud cactus").id;
				}
			}
		}
		for (int l = 0; l < w*h/60; l++) { //duplicating the loop to populate waters
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("cloud").id) {
					map[xx + yy * w] = Tiles.get("small cloud cactus").id;
				}
			}
		}
		stairsLoop:
		for (int i = 0; i < w * h / 50; i++) {
			int x = random.nextInt(w - 2) + 1;
			int y = random.nextInt(h - 2) + 1;
			
			for (int yy = y - 1; yy <= y + 1; yy++)
				for (int xx = x - 1; xx <= x + 1; xx++) {
					if (map[xx + yy * w] != Tiles.get("cloud").id || map[xx + yy * w] != Tiles.get("cloud tallgrass").id || map[xx + yy * w] != Tiles.get("sky tree").id || map[xx + yy * w] != Tiles.get("skygrass").id) continue stairsLoop;
				}
			
			map[x + y * w] = Tiles.get("Cloud Cactus").id;
		}
		
		int count = 0;
		stairsLoop:
		for (int i = 0; i < w * h; i++) {
			int x = random.nextInt(w - 2) + 1;
			int y = random.nextInt(h - 2) + 1;
			
			for (int yy = y - 1; yy <= y + 1; yy++)
				for (int xx = x - 1; xx <= x + 1; xx++) {
					if (map[xx + yy * w] != Tiles.get("cloud").id) continue stairsLoop;
				}
			
			// This should prevent any stairsDown tile from being within 30 tiles of any other stairsDown tile.
			for (int yy = Math.max(0, y - stairRadius); yy <= Math.min(h - 1, y + stairRadius); yy++)
				for (int xx = Math.max(0, x - stairRadius); xx <= Math.min(w - 1, x + stairRadius); xx++)
					if (map[xx + yy * w] == Tiles.get("Stairs Down").id) continue stairsLoop;
			
			map[x + y * w] = Tiles.get("Stairs Down").id;
			count++;
			if (count >= w / 64) break;
		}
		
		return new short[][]{map, data};
	}


	public static void main(String[] args) {
		LevelGen.worldSeed = 0x100;

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
			if (lvl > 1 || lvl < -6) continue;

			short[][] fullmap = LevelGen.createAndValidateMap(w, h, -3);

			if (fullmap == null) continue;
			short[] map = fullmap[0];

			BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			int[] pixels = new int[w * h];
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int i = x + y * w;

					if (map[i] == Tiles.get("water").id) pixels[i] = 0x000080;
					if (map[i] == Tiles.get("Rose").id) pixels[i] = 0x631D1d;
					if (map[i] == Tiles.get("Fungus tree").id) pixels[i] = 0x507e38;
					if (map[i] == Tiles.get("Small Fungus tree").id) pixels[i] = 0x509e38;
					if (map[i] == Tiles.get("Fungus spores").id) pixels[i] = 0x38507e;
					if (map[i] == Tiles.get("Small rose").id) pixels[i] = 0xA52E2e;
					if (map[i] == Tiles.get("dead tree").id) pixels[i] = 0xA52E2e;
					if (map[i] == Tiles.get("Flower").id) pixels[i] = 0xeeeeee;
					if (map[i] == Tiles.get("Small flower").id) pixels[i] = 0xdddddd;
					if (map[i] == Tiles.get("Sunflower").id) pixels[i] = 0xFFD800;
					if (map[i] == Tiles.get("Azalea").id) pixels[i] = 0xFF93F0;
					if (map[i] == Tiles.get("dungeon tallgrass").id) pixels[i] = 0xFF93F0;
					if (map[i] == Tiles.get("Bramble").id) pixels[i] = 0x544426;
					if (map[i] == Tiles.get("coal Ore").id) pixels[i] = 0x555555;
					if (map[i] == Tiles.get("lapis").id) pixels[i] = 0x0000FF;
					if (map[i] == Tiles.get("lapisNF ore").id) pixels[i] = 0xAAAAFF;
					if (map[i] == Tiles.get("iron Ore").id) pixels[i] = 0xDFC8C8;
					if (map[i] == Tiles.get("ironnf Ore").id) pixels[i] = 0xDFC8C8;
					if (map[i] == Tiles.get("gold Ore").id) pixels[i] = 0xB7B75B;
					if (map[i] == Tiles.get("goldnf Ore").id) pixels[i] = 0xB7B75B;
					if (map[i] == Tiles.get("gem Ore").id) pixels[i] = 0x00D4D8;
					if (map[i] == Tiles.get("gemnf Ore").id) pixels[i] = 0x00D4D8;
					if (map[i] == Tiles.get("gemg Ore").id) pixels[i] = 0x75B65B;
					if (map[i] == Tiles.get("gemgnf Ore").id) pixels[i] = 0x75B56B;
					if (map[i] == Tiles.get("gemb Ore").id) pixels[i] = 0x283BC5;
					if (map[i] == Tiles.get("gembnf Ore").id) pixels[i] = 0x283BC5;
					if (map[i] == Tiles.get("obsidian Ore").id) pixels[i] = 0x4C1364;
					if (map[i] == Tiles.get("obsidium Ore").id) pixels[i] = 0xFFFFFF;
					if (map[i] == Tiles.get("obsidiumnf Ore").id) pixels[i] = 0xCCCCCC;
					if (map[i] == Tiles.get("grass").id) pixels[i] = 0x208020;
					if (map[i] == Tiles.get("Skygrass").id) pixels[i] = 0xB3C8BC;
					if (map[i] == Tiles.get("tall grass").id) pixels[i] = 0x207020;
					if (map[i] == Tiles.get("Moss").id) pixels[i] = 0x206020;
					if (map[i] == Tiles.get("reed").id) pixels[i] = 0x208020;
					if (map[i] == Tiles.get("rock").id) pixels[i] = 0xa0a0a0;
					if (map[i] == Tiles.get("glacier").id) pixels[i] = 0xa4a7d7;
					if (map[i] == Tiles.get("iced rock").id) pixels[i] = 0xafb1cf;
					if (map[i] == Tiles.get("small glacier").id) pixels[i] = 0xc2c4e1;
					if (map[i] == Tiles.get("iced stairs down").id) pixels[i] = 0xc2c4e1;
					if (map[i] == Tiles.get("small glacier spikes").id) pixels[i] = 0xc2c4e1;
					if (map[i] == Tiles.get("rockG").id) pixels[i] = 0x909090;
					if (map[i] == Tiles.get("Sky rock").id) pixels[i] = 0xE2E2E2;
					if (map[i] == Tiles.get("wood planks").id) pixels[i] = 0x7e6739;
					if (map[i] == Tiles.get("wood wall").id) pixels[i] = 0x725d34;
					if (map[i] == Tiles.get("wood door").id) pixels[i] = 0xa9934D;
					if (map[i] == Tiles.get("deepslate").id) pixels[i] = 0x454545;
					if (map[i] == Tiles.get("deepslateG").id) pixels[i] = 0x343434;
					if (map[i] == Tiles.get("deepslate spiky stone").id) pixels[i] = 0x454545;
					if (map[i] == Tiles.get("deepslate spiky stone-L").id) pixels[i] = 0x454545;
					if (map[i] == Tiles.get("dirt").id) pixels[i] = 0x604040;
					if (map[i] == Tiles.get("coarse dirt").id) pixels[i] = 0x503030;
					if (map[i] == Tiles.get("sand").id) pixels[i] = 0xa0a040;
					if (map[i] == Tiles.get("Stone Bricks").id) pixels[i] = 0xa0a040;
					if (map[i] == Tiles.get("oak").id) pixels[i] = 0x003000;
					if (map[i] == Tiles.get("small oak").id) pixels[i] = 0x005000;
					if (map[i] == Tiles.get("birch").id) pixels[i] = 0x769E76;
					if (map[i] == Tiles.get("small birch").id) pixels[i] = 0x76E976;
					if (map[i] == Tiles.get("conifer").id) pixels[i] = 0x327732;
					if (map[i] == Tiles.get("small conifer").id) pixels[i] = 0x32AA32;
					if (map[i] == Tiles.get("sky tree").id) pixels[i] = 0xbbffbb;
					if (map[i] == Tiles.get("Dead Tree").id) pixels[i] = 0x003000;
					if (map[i] == Tiles.get("Obsidian rock").id) pixels[i] = 0xBB00BB;
					if (map[i] == Tiles.get("Dead Tree C").id) pixels[i] = 0x003000;
					if (map[i] == Tiles.get("Snowy dead tree").id) pixels[i] = 0x503030;
					if (map[i] == Tiles.get("Obsidian Wall").id) pixels[i] = 0x550055;
					if (map[i] == Tiles.get("Obsidian").id) pixels[i] = 0x000000;
					if (map[i] == Tiles.get("Raw Obsidian").id) pixels[i] = 0x111111;
					if (map[i] == Tiles.get("lava").id) pixels[i] = 0xff2020;
					if (map[i] == Tiles.get("lava brick").id) pixels[i] = 0xff2020;
					if (map[i] == Tiles.get("cloud").id) pixels[i] = 0xa0a0a0;
					if (map[i] == Tiles.get("aerocloud").id) pixels[i] = 0x707070;
					if (map[i] == Tiles.get("cloud tallgrass").id) pixels[i] = 0x939393;
					if (map[i] == Tiles.get("Stairs Down").id) pixels[i] = 0xffffff;
					if (map[i] == Tiles.get("Stairs Up").id) pixels[i] = 0xffffff;
					if (map[i] == Tiles.get("Obsidian Stairs Down").id) pixels[i] = 0xffffff;
					if (map[i] == Tiles.get("Obsidian Stairs Up").id) pixels[i] = 0xffffff;
					if (map[i] == Tiles.get("Cloud Cactus").id) pixels[i] = 0xff00ff;
					if (map[i] == Tiles.get("Small Cloud Cactus").id) pixels[i] = 0xffbbff;
					if (map[i] == Tiles.get("cactus").id) pixels[i] = 0x88ff88;
					if (map[i] == Tiles.get("cactus sapling").id) pixels[i] = 0xddffdd;
					if (map[i] == Tiles.get("snow").id) pixels[i] = 0xeeeeee;
					if (map[i] == Tiles.get("snowy conifer").id) pixels[i] = 0xbbbbbb;
					if (map[i] == Tiles.get("sky conifer").id) pixels[i] = 0xbb0bb;
					if (map[i] == Tiles.get("desert grass").id) pixels[i] = 0xB3BEa6;
					if (map[i] == Tiles.get("wool").id) pixels[i] = 0xffffff;
					if (map[i] == Tiles.get("Ice").id) pixels[i] = 0x6E72C7;
					if (map[i] == Tiles.get("Mangrove").id) pixels[i] = 0x004000;
					if (map[i] == Tiles.get("MangroveW").id) pixels[i] = 0x004440;
					if (map[i] == Tiles.get("lily pad").id) pixels[i] = 0xaaffaa;
					if (map[i] == Tiles.get("fern").id) pixels[i] = 0x55dd55;
					if (map[i] == Tiles.get("cloud flower").id) pixels[i] = 0x935E5E;
					if (map[i] == Tiles.get("red wool").id) pixels[i] = 0xff0000;
					if (map[i] == Tiles.get("green wool").id) pixels[i] = 0x55ff55;
					if (map[i] == Tiles.get("blue wool").id) pixels[i] = 0x0000ff;
					if (map[i] == Tiles.get("black wool").id) pixels[i] = 0x000000;
					if (map[i] == Tiles.get("fungus").id) pixels[i] = 0x2B422B;
					if (map[i] == Tiles.get("Obsidian void portal frame").id) pixels[i] = 0xBBBBFF;

				}
			}
			img.setRGB(0, 0, w, h, pixels, 0, w);
			JOptionPane.showMessageDialog(null, null, "Another Map: Overworld", JOptionPane.PLAIN_MESSAGE, new ImageIcon(img.getScaledInstance(w * 4, h * 4, Image.SCALE_AREA_AVERAGING)));
			if (LevelGen.worldSeed == 0x100)
				LevelGen.worldSeed = 0xAAFF20;
			else
				LevelGen.worldSeed = 0x100;
		}
	}
}
