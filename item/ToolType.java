package minicraft.item;

public enum ToolType {
	Shovel (0, 25), // If there's a second number, it specifies durability.
	Hoe (1, 21),
	FishingRod(1, 20),
	Sword (2, 42),
	Pickaxe (3, 29),
	Axe (4, 25),
	Bow (5, 21),
	Claymore (6, 36),
	Hammer (8, 51,new Integer[]{0,6}),
	Shears (7, 28,new Integer[]{0,1,6});

	public final int xPos; // X Position of origin
	public final int yPos; // Y position of origin
	public Integer[] skipLvls;
	public final int durability;
	public final boolean noLevel;

	/**
	 * Create a tool with four levels: wood, stone, iron, gold, and gem.
	 * All these levels are added automatically but sprites have to be added manually.
	 * Uses line 14 in the item spritesheet.
	 * @param xPos X position of the starting sprite in the spritesheet.
	 * @param dur Durabiltity of the tool.
	 */
	ToolType(int xPos, int dur,Integer[] skiplvls) {
		this.xPos = xPos;
		yPos = 13;
		skipLvls = skiplvls;
		durability = dur;
		noLevel = false;
	}
	ToolType(int xPos, int dur) {
		this.xPos = xPos;
		yPos = 13;
		skipLvls = new Integer[0]; //don't skip any lvl
		durability = dur;
		noLevel = false;
	}

	/**
	 * Create a tool without a specified level.
	 * Uses line 13 in the items spritesheet.
	 * @param xPos X position of the sprite in the spritesheet.
	 * @param dur Durabiltity of the tool.
	 * @param noLevel If the tool has only one level.
	 */
	ToolType(int xPos, int dur, boolean noLevel,int yPos) {
		this.xPos = xPos;
		this.yPos = yPos;
		durability = dur;
		this.noLevel = noLevel;
	}
}
