package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.entity.Arrow;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.FireSpark;
import minicraft.entity.furniture.KnightStatue;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.item.Items;
import minicraft.item.PotionType;
import minicraft.level.tile.Tile;

public class ObsidianKnight extends EnemyMob {

    private static MobSprite[][][] sprites;
    static {
        sprites = new MobSprite[1][4][2];
            MobSprite[][] list = MobSprite.compileMobSpriteAnimations(32, 10);
            sprites[0] = list;
    }



    public static ObsidianKnight entity = null;
    private int nextPhase = this.maxHealth-1100;
    public static boolean beaten = false; // If the boss was beaten
    public static boolean active = false; // If the boss is active

    private static int phase = 0; // The phase of the boss. {0, 1}
    private static int attackPhaseCooldown = 0; // Cooldown between attacks

    private AttackPhase attackPhase = AttackPhase.Attacking;
    private enum AttackPhase { Attacking, Dashing, Walking; } // Using fire sparks in attacking.
    private static final AttackPhase[] ATTACK_PHASES = AttackPhase.values();
    public boolean phaseTriggered;
    private float hpLevel=health/maxHealth;
    private int dashTime = 0;
    private int dashCooldown = 1000;

    private int attackDelay = 0;
    private int attackTime = 0;
    private int attackLevel = 0; // Attack level is set to 0, as the default.

    void spikes(boolean active,int x,int y){
        byte state = (byte)(active ? 1 : 0);
        for(int xi = (x >> 4) - 11;xi<(x >> 4) + 11;xi++)
         for(int yi = (y >> 4) - 11;yi<(y >> 4) + 11;yi++){
             if (level.getTile(xi,yi).name.contains("SPIKE"))level.setData(xi,yi, state);
             if (level.getTile(xi,yi).name.contains("OBSIDIAND DOOR"))level.setData(xi,yi,0);
         }
    }
    
    void phase(){
        if( phaseTriggered) {
            spikes(this.health!=0 && this.health<=1100,this.x,this.y);
            String[] quotes={"Fire cult never dies","GRRRRR!!","The Void is your grave!","It's over... for you","I won't surrender"};
            Sound.statuePh2.play(); // Play boss-rise/awakening sound.
            Game.notifications.add(quotes[random.nextInt(quotes.length)]);
            phaseTriggered=false;
        }
    }
    /**
     * Constructor for the ObsidianKnight.
     */
    public ObsidianKnight(int health) {
        super(1, sprites, 3300, false, 16 * 8, -1, 10, 50);




        active = true;
        this.health = health;
        speed = 1;
        walkTime = 3;
        this.setRealmId(1);
        entity = this;

        //World.obvLevels[World.lvlIdx(-4)].regenerateBossRoom();
    }

    public boolean canBurn() { return false; }
    public boolean canBeAffectedByLava() { return false; }


    @Override
    public void tick() {
        super.tick();
        if (getClosestPlayer().isRemoved()) {
            active = false;
            KnightStatue ks = new KnightStatue(this.health);
            level.add(ks, x, y, false,this.getRealmId());
            spikes(false,this.x,this.y);
            this.remove();
        }
        if(health<=nextPhase && this.health>0){
            phaseTriggered=true;
            phase();
        }
        nextPhase=(int)Math.floor((health-1)/1100)*1100;
        if(this.health > 2200)phase =0;
        else if(this.health <= 2200 && this.health>1100)phase = 1;
     else phase = 2;

        //Achieve phase2

        if (Game.isMode("Creative")) return; // Should not attack if player is in creative

        if (attackPhaseCooldown == 0) {
            AttackPhase newPhase;
            do {
                newPhase = ATTACK_PHASES[random.nextInt(ATTACK_PHASES.length)];
            } while (newPhase == attackPhase);
            attackPhase = newPhase;
            attackPhaseCooldown = 500;
        } else {
            attackPhaseCooldown--;
        }

        if (attackPhase == AttackPhase.Attacking) {
            Player player = getClosestPlayer();
            if (attackDelay > 0) {
                xmov = ymov = 0;
                int dir = (attackDelay - 35) / 4 % 4; // The direction of attack.
                dir = (dir * 2 % 4) + (dir / 2); // Direction attack changes
                if (attackDelay < 35)
                    dir = 0; // Direction is reset, if attackDelay is less than 45; prepping for attack.

                this.dir = Direction.getDirection(dir);
                attackDelay--;
                if (attackDelay == 0) {
                    if (health < maxHealth / 2)
                        attackLevel = 1; // If at 1000 health (50%) or lower, attackLevel = 1
                    if (health < maxHealth / 10)
                        attackLevel = 2; // If at 200 health (10%) or lower, attackLevel = 2
                    attackTime = 120; // attackTime set to 120 (2 seconds, at default 60 ticks/sec)
                }

                return; // Skips the rest of the code (attackDelay must have been > 0)
            }

            // Send out sparks
            if (attackTime > 0) {
                xmov = ymov = 0;
                attackTime--;
                int attackDir; // The degree of attack. {0, 45, 90, 135, 180, -45, -90, -135}
                double atan2 = Math.toDegrees(Math.atan2(player.y - y, player.x - x));
                if (atan2 > 157.5 || atan2 < -157.5) attackDir = 270;
                else if (atan2 > 112.5) attackDir = 135;
                else if (atan2 > 67.5) attackDir = 90;
                else if (atan2 > 22.5) attackDir = 45;
                else if (atan2 < -112.5) attackDir = -135;
                else if (atan2 < -67.5) attackDir = -90;
                else if (atan2 < -22.5) attackDir = -45;
                else attackDir = 0;
                double speed = 1 + attackLevel * 0.2 + attackTime / 10 * 0.01; // speed is dependent on the attackType. (higher attackType, faster speeds)
                // The range of attack is 90 degrees. With little random factor.
                int phi = attackDir - 36 + (attackTime % 5) * 18 + random.nextInt(7) - 3;
                level.add(new FireSpark(this, Math.cos(Math.toRadians(phi)) * speed, Math.sin(Math.toRadians(phi)) * speed)); // Adds a spark entity with the cosine and sine of dir times speed.
                return; // Skips the rest of the code (attackTime was > 0; ie we're attacking.)
            }

            if (player != null && randomWalkTime == 0) { // If there is a player around, and the walking is not random
                int xd = player.x - x; // The horizontal distance between the player and the Obsidian Knight.
                int yd = player.y - y; // The vertical distance between the player and the Obsidian Knight.
                if (xd * xd + yd * yd < 16 * 16 * 2 * 2) {
                    /// Move away from the player if less than 2 blocks away

                    this.xmov = 0; // Velocity
                    this.ymov = 0;

                    // These four statements basically just find which direction is away from the player:
                    if (xd < 0) this.xmov = +1;
                    if (xd > 0) this.xmov = -1;
                    if (yd < 0) this.ymov = +1;
                    if (yd > 0) this.ymov = -1;

                } else if (xd * xd + yd * yd > 16 * 16 * 15 * 15) {// 15 squares away
                    /// Drags the Obsidian Knight to the player, maintaining relative position.
                    double hypot = Math.sqrt(xd * xd + yd * yd);
                    int newxd = (int) (xd * Math.sqrt(16 * 16 * 15 * 15) / hypot);
                    int newyd = (int) (yd * Math.sqrt(16 * 16 * 15 * 15) / hypot);
                    x = player.x - newxd;
                    y = player.y - newyd;
                }

                xd = player.x - x; // Recalculate these two
                yd = player.y - y;
                // If a random number, 0-3, equals 0, and the player is less than 50 blocks away, and attackDelay and attackTime equal 0...
                if (random.nextInt(4) == 0 && xd * xd + yd * yd < 50 * 50 && attackDelay == 0 && attackTime == 0) {
                    attackDelay = 60 * 2; // ...then set attackDelay to 120 (2 seconds at default 60 ticks/sec)
                }
            }
            // AttackPhase.Walking is handled by Mob.java like normal mob.
        } else if (phase >= 1) { // AttackPhase.Dashing is handled here only in second& third phase. Otherwise, it is handled as same as AttackPhase.Walking.
            if (attackPhase == AttackPhase.Dashing) {
                if (dashCooldown < 1) {
                    dashTime =  phase *20 + 20;
                    dashCooldown = 250;
                    this.speed = 2;
                } else {
                    dashCooldown--;
                }
                if (dashTime == 0) {
                    this.speed = 1;
                    dashCooldown--; // We want cooldown
                }

                if (dashTime > 0) {
                    dashTime--;
                    speed = 2;
                    level.add(new FireSpark(this, 0, 0)); // Fiery trail
                }
            }
        }
    }

   @Override
    public void doHurt(int damage, Direction attackDir) {
        float mult = (float) (this.phase == 0 ? 0.6 : (this.phase == 1 ? 0.8 : 1));
       boolean crit= !getClosestPlayer().potionEffects.containsKey(PotionType.Weak) && random.nextInt(22)>20 ;
        super.doHurt((int)(this.health-(damage *mult)<this.nextPhase ? this.health-this.nextPhase  : (damage* mult)), attackDir,this.phase == 2 ? crit : false);
        if(this.health< nextPhase)this.health=this.nextPhase;
        if (attackDelay == 0 && attackTime == 0) {
            attackDelay = 60 * 2;
        }
    }

    @Override
    public void render(Screen screen) {
        super.render(screen);
        this.lvlSprites[0]  =MobSprite.compileMobSpriteAnimations(32, (10 + (phase *2)));
       renderHPPercent(screen);
    }

    @Override
    protected void touchedBy(Entity entity) {
        if (entity instanceof Player) {
            // If the entity is the Player, then deal them 2 damage points.
            ((Player)entity).hurt(this, 2 + phase);
            if (attackPhase == AttackPhase.Dashing) {
                dashTime = Math.max(dashTime - 10, 0);
            }
        }
    }

    /** What happens when the obsidian knight dies */
    @Override
    public void die() {
        spikes(false,this.x,this.y);
        Player[] players = level.getPlayers();
        if (players.length > 0) { // If the player is still here
            for (Player p: players) {
                if(p.getRealmId() == this.getRealmId())
                p.addScore(300000); // Give the player 300K points. //only to these who are in the same realm rn
            }
            dropItem(15, 25, Items.get("shard"));
            dropItem(1, 1, Items.get("Obsidian Heart")); // Drop it's precious item.
        }
        getClosestPlayer().potionEffects.put(PotionType.FireMark, 36000);
        Sound.bossDeath.play();

        //Analytics.AirWizardDeath.ping();
        Updater.notifyAll(Localization.getLocalized("Obsidian knight:Defeated!"));


        // If this is the first time we beat the obsidian knight.
      /*  if (!beaten) {
            AchievementsDisplay.setAchievement("minicraft.achievement.obsidianknight", true);

            //Analytics.FirstAirWizardDeath.ping();
        }*/

        beaten = true;
        active = false;
        entity = null;

        super.die(); // Calls the die() method in EnemyMob.java
    }

  public int getMaxLevel() { return 1; }
}
