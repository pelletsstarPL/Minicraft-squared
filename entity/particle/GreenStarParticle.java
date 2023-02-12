package minicraft.entity.particle;

import minicraft.gfx.Sprite;

public class GreenStarParticle extends Particle {
	/// This is used for Spawners, when they spawn an entity.

	/**
	 * Creates a new particle at the given position. It has a lifetime of 30 ticks
	 * and a fire looking sprite.
	 *
	 * @param x X map position
	 * @param y Y map position
	 */
	public GreenStarParticle(int x, int y) {
		super(x, y, 30, new Sprite(6, 6, 3));
	}
}
