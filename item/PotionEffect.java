package minicraft.item;

import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.RemotePlayer;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

import java.util.ArrayList;

public class PotionEffect {

	private PotionType type;
	private int duration;
	private boolean natural;

	public PotionEffect(PotionType type, int duration,boolean natural) {
		//super(type,duration);
		this.type = type;
		this.duration = duration;
		this.natural = natural;
	}

	public PotionType getPotionType() {
		return type;
	}

	public int getDuration() {
		return this.duration;
	}
	public boolean getNatural() {
		return this.natural;
	}

	public PotionEffect addDuration(int time) {
		this.duration += time;
		return this;
	}

	public PotionEffect substractDuration(int time) {
		this.duration -= time;
		return this;
	}
	public PotionEffect setNatural(boolean natural) {
		this.natural = natural;
		return this;
	}
}
