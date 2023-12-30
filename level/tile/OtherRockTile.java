
package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.entity.mob.NightWizard;

// This is the normal stone you see underground and on the surface, that drops coal and stone.
//We will use this class as placeholder for many other rock types not just sky one
public class OtherRockTile extends Tile {
	private OtherRockType type;
	public enum OtherRockType{
		Sky(45,new ConnectorSprite(OtherRockTile.class, new Sprite(18, 6, 3, 3, 1, 3), new Sprite(21, 8, 2, 2, 1, 3), new Sprite(21, 6, 2, 2, 1, 3)),new String[] {"Stone"},new int[] {1},new int[]{3},"Cloud"),
		Iced(32,new ConnectorSprite(OtherRockTile.class, new Sprite(47, 6, 3, 3, 1, 3), new Sprite(50, 8, 2, 2, 1, 3), new Sprite(50, 6, 2, 2, 1, 3)),new String[] {"Ice"},new int[] {2},new int[]{4},"Snow"),
		IcedRock(55,new ConnectorSprite(OtherRockTile.class, new Sprite(54, 6, 3, 3, 1, 3), new Sprite(52, 8, 2, 2, 1, 3), new Sprite(52, 6, 2, 2, 1, 3)),new String[] {"Ice","Stone","Coal"},new int[] {1,1,0},new int[]{2,2,1},"Dirt");
		private int health;
		private ConnectorSprite sprite;
		private int[] lootMi,lootMa;
		private String[] loot;
		private String baseTile;
		public boolean connectsTo(Tile tile, boolean isSide) {
			if (!isSide) return true;
			return tile.connectsToGlacier;

		};
		OtherRockType(int health,ConnectorSprite sprite,String[] loot,int[] lootMi,int[] lootMa,String baseTile){
			this.health = health;
			this.sprite = sprite;
			this.loot = loot;
			this.lootMi = lootMi;
			this.lootMa = lootMa;
			this.baseTile = baseTile;
		}
	}

	private int damage;

	protected OtherRockTile(String name, OtherRockType type) {
		super(name, type.sprite);
		connectsToSnow = type==OtherRockType.Iced;
		connectsToRock = type==OtherRockType.IcedRock;
		this.type = type;
	}

	public void render(Screen screen, Level level, int x, int y) {

		Tiles.get(type.baseTile).render(screen, level, x, y);
		type.sprite.render(screen, level, x, y);
	}

	public boolean mayPass(Level level, int x, int y, Entity e){
		if(e instanceof NightWizard && level.depth>=0)
			return true;
		else return false;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		hurt(level, x, y, dmg);
		return true;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			int staminaPay=(4-tool.level < 2 ? 2 : 4-tool.level);
			int dmg = random.nextInt(10) + tool.damage;
			if (tool.level!=6 && tool.type == ToolType.Pickaxe && player.payStamina(staminaPay) && tool.payDurability(dmg)) {
				// Drop coal since we use a pickaxe.
				hurt(level, xt, yt, tool.getDamage());
				return true;
			}else if (tool.level==6 && tool.type == ToolType.Pickaxe && player.payStamina(staminaPay) && tool.payDurability(dmg)) {
				dmg=random.nextInt(8)+2;
				hurt(level, xt, yt, dmg);
			}
		}
		return false;
	}
	private void loot(Level level, int x, int y){
		for(int j=0;j <= type.loot.length;j++){
			//System.out.println(j + " " + type.loot.length + " " + type.loot[j]);
			level.dropItem(x * 16 + 8, y * 16 + 8, type.lootMi[j], type.lootMa[j], Items.get(type.loot[j]));break;
		}
	}
	public void hurt(Level level, int x, int y, int dmg) {
		int maxHealth = type.health;
		damage = level.getData(x, y) + dmg;

		if (Game.isMode("Creative")) {
			dmg = damage = maxHealth;
		}

		level.add(new SmashParticle(x * 16, y * 16));
		Sound.monsterHurt.play();

		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= maxHealth) {
			loot(level,x,y);
			level.setTile(x,y,type.baseTile);
		} else {
			level.setData(x, y, damage);
		}
	}

	public boolean tick(Level level, int xt, int yt) {
		damage = level.getData(xt, yt);
		if (damage > 0) {
			level.setData(xt, yt, damage);
			return true;
		}
		return false;
	}

}
