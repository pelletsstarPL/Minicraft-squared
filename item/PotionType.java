package minicraft.item;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.ObsidianKnight;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import org.jetbrains.annotations.Nullable;

public enum PotionType {
	None (Color.get(1, 22, 22, 137), 0,false),
	
	Speed (Color.get(1, 105, 209, 105), 4200,false,new Sprite(14,2,3)) {
		public boolean toggleEffect(Player player, boolean addEffect) {
			player.moveSpeed += (double)( addEffect ? 1 : (player.moveSpeed > 1 ? -1 : 0) );
			return true;
		}
	},
	Hunger (Color.get(1, 140, 255, 140), 1800,true,new Sprite(2,5,3)),
	Thirst (Color.get(1, 183, 255, 183), 1800,true,new Sprite(7,5,3)),
	Poison (Color.get(1, 1, 255, 5), 1700,true,new Sprite(14,3,3)),
	Light (Color.get(1, 183, 183, 91), 5500,false,new Sprite(14,4,3)),
	Blind (Color.get(1, 255, 0, 255), 5500,true,new Sprite(14,5,3)),
	Swim (Color.get(1, 51, 51, 255), 4800,false,new Sprite(14,6,3)),
	Energy (Color.get(1, 237, 110, 78), 5200,false,new Sprite(1,5,3)),
	WellFed (Color.get(1, 255, 255, 0), 5200,true,new Sprite(2,8,3)),
	WellHydrated (Color.get(1, 255, 255, 0), 5200,true,new Sprite(7,8,3)),
	Regen (Color.get(1, 219, 70, 139), 1700,false,new Sprite(14,7,3)),
	Power (Color.get(1, 168, 0, 0), 2400,false,new Sprite(14,8,3)),
	Weak (Color.get(1, 128, 128, 128), 2400,true,new Sprite(14,9,3)),
	//Vanish (Color.get(1, 200, 200, 255), 3948),

	Health (Color.get(1, 161, 46, 69), 0,true) {
		public boolean toggleEffect(Player player, boolean addEffect) {
			if(addEffect) player.heal(6);
			return true;
		}
	},
	Harm (Color.get(1, 0, 0, 0), 0,true) {
		public boolean toggleEffect(Player player, boolean addEffect) {
			if(addEffect) {
				player.hurt(player, 6, Direction.NONE);
			}
			return true;
		}
	},
	
	Time (Color.get(1, 163), 1500,false,new Sprite(14,10,3)),
	AntiTime (Color.get(1, 255), 1500,false,new Sprite(14,11,3)),
	Lava (Color.get(1, 199, 58, 58), 7200,false,new Sprite(14,12,3)),
	Shield (Color.get(1, 84, 84, 204), 5000,false,new Sprite(14,13,3)),
	Haste (Color.get(1, 106, 37, 106), 4000,false,new Sprite(14,14,3)),
	Fatigue (Color.get(1, 201, 77, 201), 4000,true,new Sprite(14,15,3)),

	Escape (Color.get(1, 85, 62, 62), 0,false) {
		public boolean toggleEffect(Player player, boolean addEffect) {
			Level lvlList[][] = {World.levels,World.obvLevels};
			int lvlIdxList[][] = {World.idxToDepth,World.idxToDepthObv};
			if (addEffect) {
				int playerDepth = player.getLevel().depth;
				
				if (playerDepth == 0 || ObsidianKnight.active) {
					if (!Game.isValidServer()) {
						// player is in overworld
						String note = "You can't escape from here!";
						Game.notifications.add(note);
					}
					return false;
				}
				
				int depthDiff = playerDepth > 0 ? -1 : 1;
				
				World.scheduleLevelChange(depthDiff, () -> {
					Level plevel = lvlList[player.getRealmId()][World.lvlIdx(playerDepth + depthDiff,lvlIdxList[player.getRealmId()])];
					if (plevel != null && !plevel.getTile(player.x >> 4, player.y >> 4).mayPass(plevel, player.x >> 4, player.y >> 4, player))
						player.findStartPos(plevel, false);
				});
			}
			return true;
		}
	},
	FireMark (Color.ORANGE, 1500,true,new Sprite(14,16,3));
	
	public int dispColor, duration;
	public String name;
	public boolean nat;
	public Sprite icon;

	PotionType(int col, int dur, boolean natural) {
		dispColor = col;
		duration = dur;
		nat = natural;
		icon = null;
		/* Parameter "natural" states if this effect is natural body's state like for instance food poisoning or fatigue.
		 Used to determine if player shall emit bubbles or not, if milk should affect its duration or not*/

		switch(this.toString()){
			case "None": name="Potion";break;
			case "Poison": name="Poison";break;
			default : name=this+" potion";break;
		}
	}
	PotionType(int col, int dur, boolean natural,Sprite ico) {
		dispColor = col;
		duration = dur;
		nat = natural;
		icon = ico;
		/* Parameter "natural" states if this effect is natural body's state like for instance food poisoning or fatigue.
		 Used to determine if player shall emit bubbles or not, if milk should affect its duration or not*/

		switch(this.toString()){
			case "None": name="Potion";break;
			case "Poison": name="Poison";break;
			default : name=this+" potion";break;
		}
	}
	public boolean isNatural(){
		return this.nat;
	}

	
	public boolean toggleEffect(Player player, boolean addEffect) {
		return duration > 0; // If you have no duration and do nothing, then you can't be used.
	}
	
	public boolean transmitEffect() {
		return true; // Any effect which could be duplicated and result poorly should not be sent to the server.
		// For the case of the Health potion, the player health is not transmitted separately until after the potion effect finishes, so having it send just gets the change there earlier.
	}
	
	public static final PotionType[] values = PotionType.values();
}
