package minicraft.level;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import minicraft.entity.furniture.Spawner;
import minicraft.entity.mob.MobAi;
import minicraft.entity.mob.Skeleton;
import minicraft.entity.mob.Slime;
import minicraft.entity.mob.Zombie;
import minicraft.level.Level;
import minicraft.level.tile.LilyPadTile;
import minicraft.level.tile.StairsTile;
import minicraft.level.tile.Tile;
import org.jetbrains.annotations.Nullable;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.level.tile.Tiles;
import minicraft.screen.WorldGenDisplay;

public class LevelGen {
	private static long worldSeed = 0;
	private static final Random random = new Random(worldSeed);
	public double[] values; // An array of doubles, used to help making noise for the map
	private int w, h; // Width and height of the map
	private static final int stairRadius = 14;
	/** This creates noise to create random values for level generation */
	public LevelGen(int w, int h, int featureSize) {
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
		if (level == -5)
			return createAndValidateDungeon(w, h);
		if (level > -5 && level < 0)
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
			if(depth!=4) {
				if (count[Tiles.get("rock").id & 0xff] < 100) continue;
			}else{
				if (count[Tiles.get("deepslate").id & 0xff] < 100) continue;
			}
			if(depth!=3) {
				if (count[Tiles.get("dirt").id & 0xff] < 100) continue;
			}else{
				if (count[Tiles.get("Moss").id & 0xff]+count[Tiles.get("dirt").id & 0xff] < 100) continue;
			}
			if(depth==1) if (count[(Tiles.get("iron Ore").id & 0xff)] < 100) continue;
			if(depth==2) if (count[(Tiles.get("iron Ore").id & 0xff)]+count[(Tiles.get("gold Ore").id & 0xff)] < 100) continue;
			if(depth==3) if (count[(Tiles.get("gold Ore").id & 0xff)] < 100) continue;
			if(depth==4) if (count[(Tiles.get("gem ore").id & 0xff )] < 100) continue;

			if (depth < 4 && count[Tiles.get("Stairs Down").id & 0xff] < w / 32)
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
			if (count[Tiles.get("cloud").id & 0xff] < 2000) continue;
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
				
				// This calculates a sort of distance based on the current coordinate.
				double xd = x / (w - 1.0) * 2 - 1;
				double yd = y / (h - 1.0) * 2 - 1;
				if (xd < 0) xd = -xd;
				if (yd < 0) yd = -yd;
				double dist = xd >= yd ? xd : yd;
				dist = dist * dist * dist * dist;
				dist = dist * dist * dist * dist;
				val += 1 - dist*20;
				
				switch ((String) Settings.get("Type")) {
					case "Island":
						
						if (val < -0.5) {
							if (Settings.get("Theme").equals("Hell"))
								map[i] = Tiles.get("lava").id;
							else{
								map[i] = Tiles.get("water").id;
							}
						} else if (val > 0.1-(((float)(int)Settings.get("stonemass"))*0.05) && mval < -1.5+(((float)(int)Settings.get("stonemass"))*0.05)) {
							if(val > 0.7 && mval < -1.75) map[i] = Tiles.get("rockG").id;
							else map[i] = Tiles.get("rock").id;
						} else {
							double chance=Math.random();
							if(chance>0.3) map[i] = Tiles.get("grass").id;
						}
						
						break;
					case "Box":
						
						if (val < -1.5) {
							if (Settings.get("Theme").equals("Hell")) {
								map[i] = Tiles.get("lava").id;
							} else {
								map[i] = Tiles.get("water").id;
							}
						} else if (val > 0.32-((float)(int)Settings.get("stonemass")*0.05) && mval < -1.5+((float)(int)Settings.get("stonemass")*0.05)) {
							if(val > 0.75 && mval < -1.75) map[i] = Tiles.get("rockG").id;
							else map[i] = Tiles.get("rock").id;
						} else {
							double chance=Math.random();
							if(chance>0.3) map[i] = Tiles.get("grass").id;
						}
						
						break;
					case "Mountain":
						
						if (val < -0.4) {
							double chance=Math.random();
							if(chance>0.3) map[i] = Tiles.get("grass").id;
						} else if (val > 0.5 && mval < -1.5) {
							if (Settings.get("Theme").equals("Hell")) {
								map[i] = Tiles.get("lava").id;
							}else if(Settings.get("Theme").equals("Tundra")){
								map[i] = Tiles.get("Ice").id;
							} else {
								map[i] = Tiles.get("water").id;
							}
						//} else if(mval>=1.5-((float)(int)Settings.get("stonemass")*0.05) && mval<1.62+((float)(int)Settings.get("stonemass")*0.05)){
						} else if(mval>=1.5-((float)(int)Settings.get("stonemass")*0.05) && mval<1.62+((float)(int)Settings.get("stonemass")*0.05)){
							double chance=Math.random();
							if(chance>0.011) map[i] = Tiles.get("Spiky stone-L").id;
							else map[i] = Tiles.get("Iron ore").id;
						}else if(val>0.2 && mval<-1.2){
							map[i] = Tiles.get("rockG").id;
						}else{
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
							if(val>0.75 && mval < -1.75)
							map[i] = Tiles.get("rockG").id;
							else map[i] = Tiles.get("rock").id;
						} else {
							double chance=Math.random();
							if(chance>0.3) map[i] = Tiles.get("grass").id;

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
			int ys = random.nextInt(h);
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

			
			for (int i = 0; i < w * h / (Settings.get("Dominantbiome").equals("Desert") ? 200 : 2800); i++) {
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
									if (map[xx + yy * w] == Tiles.get("grass").id) {
										double chance=Math.random();
										/*if(chance>0.15)*/map[xx + yy * w] = Tiles.get("sand").id;
										/*else if(chance<=0.15 && chance>0.14)map[xx + yy * w] = Tiles.get("cactus").id;
										else if(chance<=0.14 && chance>0.13)map[xx + yy * w] = Tiles.get("Dead tree").id;
										else map[xx + yy * w] = Tiles.get("Desert grass").id;*/
									}
								}
					}
				}
			}

			for (int i = 0; i < w * h / (Settings.get("Dominantbiome").equals("Drylands") ? 1000 : 7400); i++) {
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
								if (xx > 3 && yy > 3 && xx < w && yy < h) {
									if (map[xx + yy * w] == Tiles.get("grass").id) {
										map[xx + yy * w] = Tiles.get("coarse dirt").id;
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
								map[xx + yy * w] = Tiles.get("conifer").id;
							}else{
								if(chance<=0.003 && chance>=0.001) map[xx + yy * w] = Tiles.get("oak").id;
								else map[xx + yy * w] = Tiles.get("birch").id;
							}
						}
					}
				}
			}


		// Tundra biome

			for (int i = 0; i < w * h / ((Settings.get("Dominantbiome").equals("Tundra")) ? 600 : 3800); i++) {
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
								if (map[xx + yy * w] == Tiles.get("snow").id || map[xx + yy * w] == Tiles.get("snowyconifer").id)map[xx + yy * w] = Tiles.get("glacier").id;
								/*if (map[xx + yy * w] == Tiles.get("rock").id || map[xx + yy * w] == Tiles.get("rockG").id){
									Tile[] areaTiles = Level.getAreaTiles(x,y,1);
								for(Tile t: areaTiles){
									if(t == Tiles.get("snow") || t == Tiles.get("glacier") ){
											map[xx + yy * w] = Tiles.get("iced rock").id;
										}
									}

								} postponed for 3.0 */
						}
					}

				}
			}
		}
		}


			for (int i = 0; i < w * h / (Settings.get("Dominantbiome").equals("Plain") ? 400 : 2800); i++) {
				int x = random.nextInt(w);
				int y = random.nextInt(h);
				for (int j = 0; j < 200; j++) {
					int xx = x + random.nextInt(15) - random.nextInt(15);
					int yy = y + random.nextInt(15) - random.nextInt(15);
					if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
						if (map[xx + yy * w] == Tiles.get("grass").id) {
							double typetree=Math.round(Math.random());
							if(typetree==0) {
								map[xx + yy * w] = Tiles.get("oak").id;
							}else{
								map[xx + yy * w] = Tiles.get("birch").id;
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
							double typetree=Math.round(Math.random());
							if(typetree==0) {
								map[xx + yy * w] = Tiles.get("oak").id;
							}else{
								map[xx + yy * w] = Tiles.get("birch").id;
							}
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
							double typetree=Math.round(Math.random());
							if(typetree==0) {
								map[xx + yy * w] = Tiles.get("oak").id;
							}else{
								map[xx + yy * w] = Tiles.get("birch").id;
							}
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
						map[x + j * w] = Tiles.get("Grass").id; // Add the separation
					}

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
							map[xx + yy * w] = Tiles.get("snowyconifer").id;
						}
					}
				}
			}
		//populate terrain with flowers
		for (int i = 0; i < w * h / 300; i++) {
			int x = random.nextInt(w);
			int y = random.nextInt(h);
			int col = random.nextInt(4);
			int flotype = random.nextInt(5);
			for (int j = 0; j < 30; j++) {
				int xx = x + random.nextInt(5) - random.nextInt(5);
				int yy = y + random.nextInt(5) - random.nextInt(5);
				if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
					if (map[xx + yy * w] == Tiles.get("grass").id || map[xx + yy * w] == Tiles.get("Tall grass").id) {

						switch(flotype){
							case 0:map[xx + yy * w] = Tiles.get(yy%4==1 ? "small flower" : "flower").id;break;
							case 1:map[xx + yy * w] = Tiles.get(xx%4==1 ? "flower" :"rose").id;break;
							case 2:if(yy%2==0)map[xx + yy * w] = Tiles.get(random.nextInt(2)==0 ? "small rose" : "small flower").id;break;
							case 3:if(xx%2==0)map[xx + yy * w] = Tiles.get(random.nextInt(2)==0 ? "small rose" : "sunflower").id;break;
							case 4:if(xx%2==1 || yy%2==1)map[xx + yy * w] = Tiles.get(random.nextInt(3)<2 ? "sunflower" : "flower").id;break;
							case 5:double chance1=Math.random();
								if(chance1>0.8)map[xx + yy * w] = Tiles.get("reed").id;
								else map[xx + yy * w] = Tiles.get("grass").id;break;
						}
						data[xx + yy * w] = (short) (col + random.nextInt(4) * 16); // Data determines which way the flower faces
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
		for(int j=0;j<2;j++) {
			for (int i = 0; i < w * h / 360; i++) {
				int xx = random.nextInt(w);
				int yy = random.nextInt(h);

				if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
					if (map[xx + yy * w] == Tiles.get("sand").id) {
						map[xx + yy * w] = Tiles.get("cactus").id;
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
						map[xx + yy * w] = Tiles.get("MangroveWater").id;
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
					map[xx + yy * w] = Tiles.get("oak").id;
				}
			}
		}
		for (int i = 0; i < w * h / 16; i++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("moss").id || map[xx + yy * w] == Tiles.get("Mangrove").id) {
					map[xx + yy * w] = Tiles.get(Settings.get("Theme").equals("Hell") ? "Coarse dirt" :"Big fungus").id;
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
							(	map[x + j * w] != Tiles.get("Water").id && map[x + j * w] == Tiles.get("SnowyConifer").id) ||
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
				}else if(val>=-0.35 && val < -0.22) map[i] = Tiles.get("Lava Brick").id;
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
				else Structure.lavaFountain.draw(map, x, y, w);
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
								if (map[xx + yy * w] != Tiles.get("Obsidian wall").id)
									map[xx + yy * w] = Tiles.get("dirt").id;
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
		/*oreLoop:
		for (int i = 0; i < w * h / 450; i++) {
			int x = random.nextInt(w - 15) + 10;
			int y = random.nextInt(h - 15) + 10;

			for (int yy = y - 1; yy <= y + 1; yy++)
				for (int xx = x - 1; xx <= x + 1; xx++) {
					if (map[xx + yy * w] == Tiles.get("Raw obsidian").id || map[xx + yy * w] == Tiles.get("obsidian").id) Structure.dungeonOre.draw(map, x, y, w);
				}

		}*/
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
					if (depth == 4) {
						if(val>-1 && val<-0.78)map[i] = Tiles.get("dirt").id;
						else if(val>=-0.78 && val<-0.76)map[i] = Tiles.get("Raw Obsidian").id;
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
						}

					}else if (depth == 2) {
						if(val >-0.8 && val <-0.64) {
							map[i] = Tiles.get("dirt").id;
						}else if(val >-0.64 && val <-0.3) {
							map[i] = Tiles.get("coarse dirt").id;
						}else if(val>=-0.3 && val<-0.24){
								map[i] = Tiles.get("water").id;
						}else{
							map[i] = Tiles.get("rock").id;
						}
					}else if (depth == 1) {
						if(val > 0 && val<0.04)map[i] = Tiles.get("Bramble").id;
						else if(val>=0.2 && val<0.4)map[i] = Tiles.get("Ground rock").id;
						else if(val>=0.4 && val<1.2) map[i] = Tiles.get("Coarse dirt").id;
						else map[i] = Tiles.get("dirt").id;
						double chance = Math.random();
						if (i - 1 > 1) {
							if (map[i - 1] == Tiles.get("rock").id && map[i] == Tiles.get("dirt").id && chance < 0.4)
								map[i - 1] = Tiles.get("Small stones").id;
							if (map[i + 1] == Tiles.get("rock").id && map[i] == Tiles.get("dirt").id && chance < 0.4)
								map[i + 1] = Tiles.get("Small stones").id;
						}
					} else {

						if(val > 2.5) {

							map[i] = Tiles.get("water").id;
						}else if(val>2.1 && val<=2.5){
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
                        }

						//if((map[i-spikeGenLand]==Tiles.get("dirt").id || map[i-spikeGenLand]==Tiles.get("Moss").id || map[i-spikeGenLand]==Tiles.get("Azalea").id) && chance2<0.05)map[i-spikeGenLand] = Tiles.get("Spiky stone-L").id; //to make sugar obtainable in hell worlds
					}

				} else if (val > -2 && (mval < -1.7 || nval < -1.4)) {
					if(depth==4){
						map[i] = Tiles.get("coarse dirt").id;
						double chance = Math.random();

					} else if (depth == 3) {
						if(val > -2 && val < -0.5)  map[i] = Tiles.get("dirt").id;
						else map[i] = Tiles.get("moss").id;



						double chance = Math.random();
						if (x >= 0 && y >= 0 && x < w && y < h) {
							if (map[i] == Tiles.get("Moss").id && chance < 0.34) {
								int col = random.nextInt(4); //plants
								if (chance >= 0.16 && chance<=0.28) map[i] = Tiles.get("Big Fungus").id;
								else if (chance > 0.09 && chance < 0.16) map[i] = Tiles.get("Azalea").id;
								else if(chance>0.04 && chance<=0.09)map[i] = Tiles.get("Bramble").id;
								else if (chance > 0.01 && chance <= 0.04){
									map[i] = Tiles.get("Dirt").id;
								}
								else map[i] = Tiles.get("Spiky stone-L").id;
								data[i] = (short) (col + random.nextInt(4) * 16); // Data determines which way the Azalea faces
							}else if(map[i] == Tiles.get("dirt").id){ //new biome
								int col = random.nextInt(35); //plants and other stuff
								switch(col) {
									case 1:case 7:case 5:case 11:case 23: map[i] = Tiles.get("RockG").id;break;
									case 2: map[i] = Tiles.get("spiky stone-l").id;break;
									case 4: map[i] = Tiles.get("small stones").id;break;
									case 20: case 15: map[i] = Tiles.get("coarse dirt").id;break;
								}
							}

						}
						for (int l = 0; l < 3; l++) { //duplicating the loop to populate moss
							int xx = random.nextInt(w);
							int yy = random.nextInt(h);

							if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
								if (map[xx + yy * w] == Tiles.get("moss").id) {
									map[xx + yy * w] = Tiles.get("fungus spores").id;
									data[xx + yy * w] = (short) (-(random.nextInt(300))+30);
								}
							}
						}

					}else {
						if (depth == 1) {
							double chance = Math.random();
							if (chance > 0.012) map[i] = Tiles.get("dirt").id;
							else map[i] = Tiles.get("Spiky stone-L").id;
						} else if (depth == 2){
							double chance=Math.random();
							if(chance>0.2 && chance<0.67) map[i] = Tiles.get("dirt").id;
							else if(chance>=0.67 && chance<0.75)  map[i] = Tiles.get("water").id;
							else map[i] = Tiles.get("rock").id;
						}

					}
				} else {
					if (depth == 4) {
						if(val<-4.5)map[i] = Tiles.get("deepslateG").id;
						else map[i] = Tiles.get("deepslate").id;
					} else {

							if (val < -2.78) map[i] = Tiles.get("RockG").id;
							else map[i] = Tiles.get("rock").id;

					}
				}
				if(depth==4)
				for (int l = 0; l < w / 64; l++) { //duplicating the loop to populate land
					int xx = random.nextInt(w);
					int yy = random.nextInt(h);

					if (xx >= 0 && yy >= 0 && xx < w && yy < h && val > -1.7 && (mval < -2.4 || nval < -1.9)) {
						if (map[xx + yy * w] == Tiles.get("coarse dirt").id) {
							map[xx + yy * w] = Tiles.get((xx % 4 == 1 || yy % 4 == 1) ? "rockG" : "deepslateG").id;
						}
					}
				}
			}
			}
		{
			int r = 2;
			for (int i = 0; i < w * h / 400; i++) {
				type=random.nextInt(3);
				gemnf="gem"+types[type]+"NF Ore";
				gem="gem"+types[type]+" Ore";
				int x = random.nextInt(w);
				int y = random.nextInt(h);
				if(depth<4) {
					switch(depth) {
						case 1:for (int j = 0; j < 30; j++) {
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
									map[xx + yy * w] = (short) (Tiles.get("Lapis").id & 0xff);
								}
							}
						}break;
						case 2:for (int j = 0; j < 30; j++) {
							int xx = x + random.nextInt(5) - random.nextInt(5);
							int yy = y + random.nextInt(5) - random.nextInt(5);
							if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
								if (map[xx + yy * w] == Tiles.get("rock").id || map[xx + yy * w] == Tiles.get("rockG").id) {
									double chance = Math.random();
									if (chance > 0.2 && chance<0.45)
										map[xx + yy * w] = (short) ((Tiles.get("iron Ore").id & 0xff));
									else if(chance>=0.45)map[xx + yy * w] = Tiles.get("ironNF Ore").id;
									else map[xx + yy * w] = (short) ((Tiles.get("gold Ore").id & 0xff));
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
							}break;
						case 3:for (int j = 0; j < 30; j++) {
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
										map[xx + yy * w] = (short) (Tiles.get("Lapis").id & 0xff);
									}
								}
							}break;
					}
				}else {
					for (int j = 0; j < 30; j++) {
						int xx = x + random.nextInt(5) - random.nextInt(5);
						int yy = y + random.nextInt(5) - random.nextInt(5);
						if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
							if (map[xx + yy * w] == Tiles.get("deepslate").id || map[xx + yy * w] == Tiles.get("deepslateG").id) {
								if(depth==4) {
									double chance=Math.random();
									if(chance>0.04) {
										if (chance > 0.2) map[xx + yy * w] = (short) (Tiles.get(gem).id & 0xff);
										else map[xx + yy * w] = (short) (Tiles.get(gemnf).id & 0xff);
									}
									else map[xx + yy * w] = (short) (Tiles.get("Lapis").id & 0xff);
								}
							}
						}
					}
					for (int j = 0; j < 10; j++) {
						int xx = x + random.nextInt(3) - random.nextInt(2);
						int yy = y + random.nextInt(3) - random.nextInt(2);
						if (xx >= r && yy >= r && xx < w - r && yy < h - r) {
							if (map[xx + yy * w] == Tiles.get("deepslate").id || map[xx + yy * w] == Tiles.get("deepslateG").id) {
								if(depth!=4) map[xx + yy * w] = (short) (Tiles.get("Lapis").id & 0xff);
								else map[xx + yy * w] =(short) (Tiles.get("Obsidian Ore").id & 0xff);
							}
						}
					}
				}
			}
		}

		if (depth > 3) {
			int r = 1;
			int xx = (w/2)-4;
			int yy = (h/2)-4;
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

		if (depth < 4) {
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

		} //tu

		return new short[][]{map, data};
	}

	private static short[][] createSkyMap(int w, int h) {
		LevelGen noise1 = new LevelGen(w, h, 8);
		LevelGen noise2 = new LevelGen(w, h, 8);
		
		short[] map = new short[w * h];
		short[] data = new short[w * h];
		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int i = x + y * w;

				double val = Math.abs(noise1.values[i] - noise2.values[i]) * 3 - 2;

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
						/*if(val>=-0.27 && val<0.2) {
							double c = Math.random();
							if (c < 0.03) map[i] = Tiles.get("cloud cactus").id;
							else if(c>=0.03 && c<0.3)map[i] = Tiles.get("cloud tallgrass").id;
							else map[i] = Tiles.get("cloud").id;
						}else if(val>=0.2 && val<0.5)map[i] = Tiles.get("Skygrass").id;
						else if(val>=0.5 && val<0.78) {
							if (Math.random() < 0.33) map[i] = Tiles.get("sky conifer").id;
							else map[i] = Tiles.get("Sky tree").id;
						}else  map[i] = Tiles.get("Sky rock").id;*/
					if(val>=-0.27 && val<0.2) {
						map[i] = Tiles.get("cloud").id;
					}else if(val>=0.2 && val<0.5)map[i] = Tiles.get("Skygrass").id;
					else if(val>=0.5 && val<0.78) map[i] = Tiles.get("Sky tree").id;
					else  map[i] = Tiles.get("Sky rock").id;
					}

				}

			}

		for (int i = 0; i < w * h / 2000; i++) {
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
		for (int l = 0; l < w*h/2; l++) {
			int xx = random.nextInt(w);
			int yy = random.nextInt(h);

			if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
				if (map[xx + yy * w] == Tiles.get("sky tree").id) {
					map[xx + yy * w] = Tiles.get("sky conifer").id;
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
			int w = 256;
			int h = 256;
			
			int lvl = maplvls[idx++ % maplvls.length];
			if (lvl > 1 || lvl < -5) continue;
			
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
					if (map[i] == Tiles.get("Big fungus").id) pixels[i] = 0x507e38;
					if (map[i] == Tiles.get("Fungus spores").id) pixels[i] = 0x38507e;
					if (map[i] == Tiles.get("Small rose").id) pixels[i] = 0xA52E2e;
					if (map[i] == Tiles.get("Flower").id) pixels[i] = 0xeeeeee;
					if (map[i] == Tiles.get("Small flower").id) pixels[i] = 0xdddddd;
					if (map[i] == Tiles.get("Sunflower").id) pixels[i] = 0xFFD800;
					if (map[i] == Tiles.get("Azalea").id) pixels[i] = 0xFF93F0;
					if (map[i] == Tiles.get("Bramble").id) pixels[i] = 0x544426;
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
					if (map[i] == Tiles.get("deepslate").id) pixels[i] = 0x343434;
					if (map[i] == Tiles.get("deepslate spiky stone").id) pixels[i] = 0x454545;
					if (map[i] == Tiles.get("deepslate spiky stone-L").id) pixels[i] = 0x454545;
					if (map[i] == Tiles.get("dirt").id) pixels[i] = 0x604040;
					if (map[i] == Tiles.get("coarse dirt").id) pixels[i] = 0x503030;
					if (map[i] == Tiles.get("sand").id) pixels[i] = 0xa0a040;
					if (map[i] == Tiles.get("Stone Bricks").id) pixels[i] = 0xa0a040;
					if (map[i] == Tiles.get("oak").id) pixels[i] = 0x003000;
					if (map[i] == Tiles.get("birch").id) pixels[i] = 0x769E76;
					if (map[i] == Tiles.get("conifer").id) pixels[i] = 0x327732;
					if (map[i] == Tiles.get("sky tree").id) pixels[i] = 0xbbffbb;
					if (map[i] == Tiles.get("Dead Tree").id) pixels[i] = 0x003000;
					if (map[i] == Tiles.get("Dead Tree C").id) pixels[i] = 0x003000;
					if (map[i] == Tiles.get("Snowy dead tree").id) pixels[i] = 0x503030;
					if (map[i] == Tiles.get("Obsidian Wall").id) pixels[i] = 0x550055;
					if (map[i] == Tiles.get("Obsidian").id) pixels[i] = 0x000000;
					if (map[i] == Tiles.get("Raw Obsidian").id) pixels[i] = 0x111111;
					if (map[i] == Tiles.get("lava").id) pixels[i] = 0xff2020;
					if (map[i] == Tiles.get("lava brick").id) pixels[i] = 0xff2020;
					if (map[i] == Tiles.get("cloud").id) pixels[i] = 0xa0a0a0;
					if (map[i] == Tiles.get("cloud tallgrass").id) pixels[i] = 0x939393;
					if (map[i] == Tiles.get("Stairs Down").id) pixels[i] = 0xffffff;
					if (map[i] == Tiles.get("Stairs Up").id) pixels[i] = 0xffffff;
					if (map[i] == Tiles.get("Obsidian Stairs Down").id) pixels[i] = 0xffffff;
					if (map[i] == Tiles.get("Obsidian Stairs Up").id) pixels[i] = 0xffffff;
					if (map[i] == Tiles.get("Cloud Cactus").id) pixels[i] = 0xff00ff;
					if (map[i] == Tiles.get("cactus").id) pixels[i] = 0x88ff88;
					if (map[i] == Tiles.get("cactus sapling").id) pixels[i] = 0xddffdd;
					if (map[i] == Tiles.get("snow").id) pixels[i] = 0xeeeeee;
					if (map[i] == Tiles.get("snowyconifer").id) pixels[i] = 0xbbbbbb;
					if (map[i] == Tiles.get("sky conifer").id) pixels[i] = 0xbb0bb;
					if (map[i] == Tiles.get("desert grass").id) pixels[i] = 0xB3BEa6;
					if (map[i] == Tiles.get("wool").id) pixels[i] = 0xffffff;
					if (map[i] == Tiles.get("Ice").id) pixels[i] = 0x6E72C7;
					if (map[i] == Tiles.get("Mangrove").id) pixels[i] = 0x004000;
					if (map[i] == Tiles.get("MangroveWater").id) pixels[i] = 0x004440;
					if (map[i] == Tiles.get("lily pad").id) pixels[i] = 0xaaffaa;
					if (map[i] == Tiles.get("fern").id) pixels[i] = 0x55dd55;
					if (map[i] == Tiles.get("cloud flower").id) pixels[i] = 0x935E5E;
					if (map[i] == Tiles.get("red wool").id) pixels[i] = 0xff0000;
					if (map[i] == Tiles.get("green wool").id) pixels[i] = 0x55ff55;
					if (map[i] == Tiles.get("blue wool").id) pixels[i] = 0x0000ff;
					if (map[i] == Tiles.get("black wool").id) pixels[i] = 0x000000;

				}
			}
			img.setRGB(0, 0, w, h, pixels, 0, w);
			JOptionPane.showMessageDialog(null, null, "Another Map", JOptionPane.PLAIN_MESSAGE, new ImageIcon(img.getScaledInstance(w * 4, h * 4, Image.SCALE_AREA_AVERAGING)));
			if (LevelGen.worldSeed == 0x100)
				LevelGen.worldSeed = 0xAAFF20;
			else
				LevelGen.worldSeed = 0x100;
		}
	}
}
