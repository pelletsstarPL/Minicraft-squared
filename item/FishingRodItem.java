package minicraft.item;

import java.util.ArrayList;
import java.util.Random;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class FishingRodItem extends Item {

    protected static ArrayList<Item> getAllInstances() {
        ArrayList<Item> items = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            items.add(new FishingRodItem(i));
        }

        return items;
    }
   // private int uses = 0; // The more uses, the higher the chance of breaking
    public int level; // The higher the level the lower the chance of breaking
    public int durability=13;
    public int maxDur;
    public int dur;
    public boolean displayBox() {
        return true;
    }
    private Random random = new Random();

    /* These numbers are a bit confusing, so here's an explanation
    * If you want to know the percent chance of a category (let's say tool, which is third)
    * You have to subtract 1 + the "tool" number from the number before it (for the first number subtract from 100)*/
    private static final int[][] LEVEL_CHANCES = {
            {44, 14, 9, 4}, // They're in the order "fish", "junk", "tools", "rare"
            {24, 14, 9, 4}, // Iron has very high chance of fish
            {59, 49, 9, 4}, // Gold has very high chance of tools
            {79, 69, 59, 4} ,// Gem has very high chance of rare items
            {82, 72, 62, 4} ,// Zanite - Gem but a little bit better
    };

    private static final String[] LEVEL_NAMES = {
            "Wood",
            "Iron",
            "Gold",
            "Gem",
            "Obsidium",
    };

    public FishingRodItem(int level) {
        super(LEVEL_NAMES[level] + " fishing rod", new Sprite(level, 11, 0));
        this.level = level;

        this.durability=13 + (level - 5 < 0 ? 0 : level); // Initial durability fetched from the ToolType
        this.dur = durability * (level + 1) + (level==0 ? 2 : 0); // Initial durability fetched from the ToolType
        maxDur = dur;
    }

    public static int getChance(int idx, int level) {
        return LEVEL_CHANCES[level][idx];
    }
    public boolean payDurability(int damage) {
			/*if (dur <= 0) return false;
		int d = damage/((level*10) * 3 == 0 ? 1 :(level*10) * 3);
		if (!Game.isMode("creative")) dur -= d>4? 4: d<1? 1: d;*/
        if (dur <= (damage-1 < 0 ? 0 : damage-1)) return false;
        if (!Game.isMode("creative")) dur-=damage;
        return true;
    }
    @Override
    public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
        Player.boxAnim[0]=39;
        boolean success=false;
        if (tile == Tiles.get("water") && !player.isSwimming()) { // Make sure not to use it if swimming
            player.isFishing = true;
            player.fishingLevel = this.level;
            return true;
        }
        Player.boxAnim[1] = 40;
        if( Settings.getEntry("soundno").equals(true))Sound.no.play();
        return false;
    }

    @Override
    public boolean canAttack() { return false; }

    @Override
    public String getData() {
        return super.getData() + "_" + dur;
    }

    public boolean isDepleted() {
        return dur <= 0 && this.durability > 0;
    }


    public FishingRodItem clone() {
        FishingRodItem ti;
        {
            ti = new FishingRodItem(level);
        }
        ti.dur = dur;
        return ti;
    }
}
