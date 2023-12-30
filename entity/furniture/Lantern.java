package minicraft.entity.furniture;

import minicraft.gfx.Sprite;

public class Lantern extends Furniture {
	public enum Type {
		NORM ("Lantern", 9, 0),
		IRON ("Iron lantern", 12, 2),
		GOLD ("Gold lantern", 15, 4),
		GEM ("Gem lantern", 19, 6);

		protected int light, offset;
		protected String title;
			
		Type(String title, int light, int offset) {
			this.title = title;
			this.offset = offset;
			this.light = light;
		}
	}
	
	public Lantern.Type type;
	
	/**
	 * Creates a lantern of a given type.
	 * @param type Type of lantern.
	 */
	public Lantern(Lantern.Type type) {
		super(type.title, new Sprite(18 + type.offset, 30, 2, 2, 2), 3, 2);
		this.type = type;
	}
	
	@Override
	public Furniture clone() {
		return new Lantern(type);
	}
	
	/** 
	 * Gets the size of the radius for light underground (Bigger number, larger light) 
	 */
	@Override
	public int getLightRadius() {
		return type.light;
	}
}
