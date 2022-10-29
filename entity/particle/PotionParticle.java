package minicraft.entity.particle;

import minicraft.gfx.Sprite;

public class PotionParticle extends Particle {

	/**
	 * Creates a new particle at the given position. It has a lifetime of 30 ticks
	 * and a fire looking sprite.
	 *
	 * @param x X map position
	 * @param y Y map position
	 */
	public PotionParticle(int x, int y) {
		super(x, y, 30, new Sprite(4, 5, 3));
	}
}
