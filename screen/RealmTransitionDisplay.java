package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.World;
import minicraft.core.io.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;

public class RealmTransitionDisplay extends Display {
	
	private static final int DURATION = 50;
	
	private int dirR,dirL; // Direction that you are changing levels. (going up or down stairs)
	private int time = 0; // Time it spends on this menu
	
	public RealmTransitionDisplay(int dirR,int dirL) {
		super(false,false);
		this.dirR = dirR;
		this.dirL = dirL;
	}
	
	public void tick(InputHandler input) {
		time++; // Ticks up 2 times per tick
		if (time == DURATION ) World.changeRealm(dirR,dirL); // When time equals 100, it will change the level. Lets add up some duration
		if (time == DURATION * 2.5) Game.setMenu(null); // When time equals 60, it will get out of this menu
	}
	
	public void render(Screen screen) {
		for (int x = 0; x < 200; x++) { // Loop however many times depending on the width (It's divided by 3 because the pixels are scaled up by 3)
			for (int y = 0; y < 150; y++) { // Loop however many times depending on the height (It's divided by 3 because the pixels are scaled up by 3)
				int dd = (x + y % 2 * 2 + y / 3) - time*2; // Used as part of the positioning.
				if (dd < 0 && dd > -150) { //we want realm entrances/leavings be slightly longer for visuals
					if (dirR > 0) screen.render(x * 8, y * 8, 31 + 27 * 32, 0, 3); // If the direction is upwards then render the squares going up
					else screen.render(Screen.w - x * 8, y * 8 - 8, 31 + 27 * 32, 0, 3);  // If the direction is negative, then the squares will go left
				}
			}
			//If we will have more than two dimensions for sure this code will be improved. For now we have two so this solution will be enough.
			if(time < DURATION * 2.5 - 20)
			Font.drawCentered(dirR > 0 ? "Entering the Obsidian Void" : "Leaving the Obsidian Void", screen, Screen.h /2, time%20 > 10 ? Color.ORANGE : Color.YELLOW);
		}
	}
}
