package minicraft.entity.particle;

import minicraft.gfx.Sprite;

public class SplashParticle extends Particle {

	/**
	 * Creates a smash particle at the given position. Has a lifetime of 10 ticks.
	 * Will also play a monsterhurt sound when created.
	 *
	 * @param x X map position
	 * @param y Y map position
	 */
	public SplashParticle(int x, int y) {
		super(x, y, 10, new Sprite(5, 3, 3));
	}
}
