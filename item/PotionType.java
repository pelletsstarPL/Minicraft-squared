package minicraft.item;

import minicraft.core.Game;
import minicraft.core.World;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.level.Level;

public enum PotionType {
	None (Color.get(1, 22, 22, 137), 0,false),
	
	Speed (Color.get(1, 105, 209, 105), 4200,false) {
		public boolean toggleEffect(Player player, boolean addEffect) {
			player.moveSpeed += (double)( addEffect ? 1 : (player.moveSpeed > 1 ? -1 : 0) );
			return true;
		}
	},
	Hunger (Color.get(1, 140, 255, 140), 1800,true),
	Thirst (Color.get(1, 183, 255, 183), 1800,true),
	Poison (Color.get(1, 1, 255, 5), 1700,true),
	Light (Color.get(1, 183, 183, 91), 5500,false),
	Blind (Color.get(1, 255, 0, 255), 5500,true),
	Swim (Color.get(1, 51, 51, 255), 4800,false),
	Energy (Color.get(1, 237, 110, 78), 5200,false),
	WellFed (Color.get(1, 255, 255, 0), 5200,true),
	WellHydrated (Color.get(1, 255, 255, 0), 5200,true),
	Regen (Color.get(1, 219, 70, 139), 1700,false),
	Power (Color.get(1, 168, 0, 0), 2400,false),
	Weak (Color.get(1, 128, 128, 128), 2400,true),
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
	
	Time (Color.get(1, 163), 1500,false),
	AntiTime (Color.get(1, 255), 1500,false),
	Lava (Color.get(1, 199, 58, 58), 7200,false),
	Shield (Color.get(1, 84, 84, 204), 5000,false),
	Haste (Color.get(1, 106, 37, 106), 4000,false),
	Fatigue (Color.get(1, 201, 77, 201), 4000,true),

	Escape (Color.get(1, 85, 62, 62), 0,false) {
		public boolean toggleEffect(Player player, boolean addEffect) {
			if (addEffect) {
				int playerDepth = player.getLevel().depth;
				
				if (playerDepth == 0) {
					if (!Game.isValidServer()) {
						// player is in overworld
						String note = "You can't escape from here!";
						Game.notifications.add(note);
					}
					return false;
				}
				
				int depthDiff = playerDepth > 0 ? -1 : 1;
				
				World.scheduleLevelChange(depthDiff, () -> {
					Level plevel = World.levels[World.lvlIdx(playerDepth + depthDiff)];
					if (plevel != null && !plevel.getTile(player.x >> 4, player.y >> 4).mayPass(plevel, player.x >> 4, player.y >> 4, player))
						player.findStartPos(plevel, false);
				});
			}
			return true;
		}
	};
	
	public int dispColor, duration;
	public String name;
	public boolean nat;
	
	PotionType(int col, int dur,boolean natural) {
		dispColor = col;
		duration = dur;
		nat = natural;
		/* Parameter "natural" states if this effect is natural body's state like for instance food poisoning or fatigue.
		 Used to determine if player shall emit bubbles or not, if milk should affect its duration or not*/

		switch(this.toString()){
			case "None": name="Potion";break;
			case "Poison": name="Poison";break;
			default : name=this+" Potion";break;
		}
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
