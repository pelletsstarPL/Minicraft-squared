package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.BlackSpark;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.item.Items;
import minicraft.item.PotionType;
import minicraft.level.Level;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.level.LevelGen;
import minicraft.level.tile.Tiles;
import minicraft.network.Analytics;
import minicraft.saveload.Save;

public class NightWizard extends EnemyMob {
    private static MobSprite[][][] sprites;
    private static MobSprite[][] sleepSprite;
    static {
        sprites = new MobSprite[2][4][2];
        for (int i = 0; i < 2; i++) {
            MobSprite[][] list  = MobSprite.compileMobSpriteAnimations(8 + (i* 8), 36);
            sprites[i] = list;
        }
    }

    public static boolean beaten = false;
    public boolean canBeAffectedByLava() { return false; }
    public static int revenge=0;
    private int attackDelay = 0;
    private int attackTime = 0;
    private int attackType = 0;

    /**
     * Constructor for the NightWizard.
     */
    public NightWizard(int lvl) {
        super(lvl, sprites, (lvl>1 ? 3150 : 2400), false, 16 * 9, -1, 10, 50);

        speed = (Updater.getTime()== Updater.Time.Night ? 2 : 0);
        walkTime = 2;
    }

    public boolean canSwim() { return true; }

    public boolean canWool() { return false; }

    @Override
    public void tick() {
        if (Updater.getTime() == Updater.Time.Night) {
            if(Updater.isbloody)this.lvl=2;
            else this.lvl=1;
            this.speed=2;
            this.speedS=2;
            if (lvl<2 && health > maxHealth) health = maxHealth; //overheal bug fix
            super.tick();
            Player player = getClosestPlayer();
            double speedMult = player.isWithin(96, this) && player.potionEffects.containsKey(PotionType.Time) ? 2 : player.potionEffects.containsKey(PotionType.AntiTime) ? 0.25 : 1;
            if (Updater.tickCount == 48601) {//when the night starts

                Sound.nightactivate.play(); // Play boss-rise/awakening sound.
                Game.notifications.add("Night wizard has awakened.");
                if (Updater.isbloody)
                    Game.notifications.add("..in bloody realm");
                if (health < maxHealth) this.heal((int) (maxHealth * 0.66));
            }

            if (Game.isMode("Creative")) return; // Should not attack if player is in creative
            if (Updater.tickCount % (2000*(Updater.isbloody ? 0.75 : 1)) == 0) {

                int xd = Renderer.player.x - x; // The horizontal distance between the player and the night wizard.
                int yd = Renderer.player.y - y; // The vertical distance between the player and the night wizard.
                double hypot = Math.sqrt(xd * xd + yd * yd);
                if (xd * xd + yd * yd > 16 * 16 * 15 * 15) {
                    int newxd = (int) (xd * Math.sqrt(9 * 9 * 15 * 15) / hypot);
                    int newyd = (int) (yd * Math.sqrt(9 * 9 * 15 * 15) / hypot);
                    if (Renderer.player.x - newxd < 20) x += 30;
                    if (Renderer.player.y - newyd < 20) y += 30;
                    if (Renderer.player.x - newxd < level.w - 20) x -= 30;
                    if (Renderer.player.y - newyd < level.h - 20) y -= 30;
                    x = Renderer.player.x - newxd;
                    y = Renderer.player.y - newyd;
                }

                Sound.nightbuff.play(); // Play boss-death sound.
                Game.notifications.add(Updater.isbloody ? "Darkness will take your blood." : "This is your last night.");
                if(lvl>1)this.heal(100);
                for (int i = 0; i < 100; i++) {
                    if (i % 100 == 0) {
                        attackTime = 200;
                        while (attackTime > 0.01) {
                            xmov = ymov = 0;

                            attackTime *= 0.9998; // attackTime will decrease by 7% every time.
                            double dir = attackTime * 0.25 * (attackTime % 2 * 2 - 1); // Assigns a local direction variable from the attack time.
                            double speed = (1.7) + attackType * 0.2; // speed is dependent on the attackType. (higher attackType, faster speeds)
                            level.add(new BlackSpark(this, (Math.cos(dir) * speed) / speedMult, (Math.sin(dir) * speed) / speedMult)); // Adds a spark entity with the cosine and sine of dir times speed.
                        }
                    }
                }

                if (level.mobCount + 60 < level.maxMobCount) {
                    //Level.removeAllEnemies();
                    for (int i = 0; i <= 60; i++) {
                        int rnd = random.nextInt(100);
                        int nx = random.nextInt(level.w) * 16 + 8, ny = random.nextInt(level.h) * 16 + 8;
                        Level level = this.level;
                        while (!level.getTile(nx / 16, ny / 16).mayPass(level, nx / 16, ny / 16, this) || level.getTile(nx, ny) == Tiles.get("water") || level.getTile(nx, ny) == Tiles.get("lava") || level.getTile(nx, ny) == Tiles.get("ice")) {
                            nx = random.nextInt(level.w) * 16 + 8;
                            ny = random.nextInt(level.h) * 16 + 8; //reroll
                        }
                        if (rnd < 2)
                            level.add((new Wraith((revenge + 1) > 4 + (lvl>1 ? 1 : 0) ? 4 + (lvl>1 ? 1 : 0) : (revenge + 1) + (lvl>1 ? 1 : 0))), nx, ny);
                        else if (rnd >= 2 && rnd <= 40)
                            level.add((new Slime((revenge + 1) > 4 + (lvl>1 ? 1 : 0) ? 4 + (lvl>1 ? 1 : 0) : (revenge + 1) + (lvl>1 ? 1 : 0))), nx, ny);
                        else if (rnd <= 75)
                            level.add((new Zombie((revenge + 1) > 4 + (lvl>1 ? 1 : 0) ? 4 + (lvl>1 ? 1 : 0) : (revenge + 1) + (lvl>1 ? 1 : 0))), nx, ny);
                        else if (rnd >= 85)
                            level.add((new Skeleton((revenge + 1) > 4 + (lvl>1 ? 1 : 0) ? 4 + (lvl>1 ? 1 : 0) : (revenge + 1) + (lvl>1 ? 1 : 0))), nx, ny);
                    }
                }
            }
            if (Updater.getTime() == Updater.Time.Night) {
                if (health > maxHealth * 0.75) revenge = 1;
                else if (health > maxHealth * 0.5) revenge = 2;
                else if (health > maxHealth * 0.25) revenge = 3;
            }
            this.speed = 2;

            if (attackDelay > 0) {
                xmov = ymov = 0;
                int dir = (attackDelay - 45) / 4 % 4; // The direction of attack.
                dir = (dir * 2 % 4) + (dir / 2); // Direction attack changes
                if (attackDelay < 45)
                    dir = 0; // Direction is reset, if attackDelay is less than 45; prepping for attack.

                this.dir = Direction.getDirection(dir);

                attackDelay -= player.potionEffects.containsKey(PotionType.AntiTime) ? 2 : player.potionEffects.containsKey(PotionType.Time) ? (Updater.tickCount % 2 == 0 ? 1 : 0) : 1;
                if (attackDelay <= 0) {
                    //attackType = 0; // Attack type is set to 0, as the default.
                    if (health < maxHealth / 2) attackType = 1; // If at 1500 health (50%) or lower, attackType = 1
                    if (health < maxHealth / 10) attackType = 2; // If at 300 health (10%) or lower, attackType = 2
                    attackTime = 60 * (2); // attackTime set to 120 or 180 (2 or 3 seconds, at default 60 ticks/sec)
                }
                return; // Skips the rest of the code (attackDelay must have been > 0)
            }

            // Send out sparks
            if (attackTime > 0) {
                xmov = ymov = 0;
                attackTime *= 0.84; // attackTime will decrease by 7% every time.
                double dir = attackTime * 0.25 * (attackTime % 2 * 2 - 1); // Assigns a local direction variable from the attack time.
                double speed = (1.1) + attackType * 0.2; // speed is dependent on the attackType. (higher attackType, faster speeds)
                level.add(new BlackSpark(this, (Math.cos(Math.round(Math.random()) == 1 ? dir : -dir) * speed) / speedMult, (Math.sin(Math.round(Math.random()) == 1 ? dir : -dir) * speed) / speedMult)); // Adds a spark entity with the cosine and sine of dir times speed.
                return; // Skips the rest of the code (attackTime was > 0; ie we're attacking.)
            }


            if (player != null && randomWalkTime == 0) { // If there is a player around, and the walking is not random
                int xd = player.x - x; // The horizontal distance between the player and the night wizard.
                int yd = player.y - y; // The vertical distance between the player and the night wizard.
                if (xd * xd + yd * yd < 16 * 16 * 2 * 2) {
                    /// Move away from the player if less than 2 blocks away

                    this.xmov = 0; // Accelerations
                    this.ymov = 0;

                    // These four statements basically just find which direction is away from the player:
                    if (xd < 0) this.xmov = +1;
                    if (xd > 0) this.xmov = -1;
                    if (yd < 0) this.ymov = +1;
                    if (yd > 0) this.ymov = -1;
                } else if (xd * xd + yd * yd > 16 * 16 * 15 * 15) {// 15 squares away

                    /// Drags the night to the player, maintaining relative position.
					/*double hypot = Math.sqrt(xd * xd + yd * yd);
					int newxd = (int) (xd * Math.sqrt(16 * 16 * 15 * 15) / hypot);
					int newyd = (int) (yd * Math.sqrt(16 * 16 * 15 * 15) / hypot);
					x = player.x - newxd;
					y = player.y - newyd;*/
                }

                xd = player.x - x; // Recalculate these two
                yd = player.y - y;
                if (random.nextInt(4) == 0 && xd * xd + yd * yd < 50 * 50 && attackDelay == 0 && attackTime == 0) { // If a random number, 0-3, equals 0, and the player is less than 50 blocks away, and attackDelay and attackTime equal 0...
                    attackDelay = 60 * 2; // ...then set attackDelay to 120 (2 seconds at default 60 ticks/sec)
                }
                if (y / 16 > level.h) y = level.h - (30 * 16);
                if (x / 16 > level.w) x = level.w - (30 * 16);
                if (x / 16 < 0) x = 30 * 16;
                if (y / 16 < 0) y = 30 * 16;
            }
        } else {
            this.speed = 0;
            this.speedS = 0;
            this.burningDuration = 0;
        }
    }
    @Override
    public void doHurt(int damage, Direction attackDir, boolean canCrit) {
        int chance=random.nextInt(4);

        if (Updater.getTime()!= Updater.Time.Night){
            Game.notifications.add("Can be only damaged at night");
        }else {
            if(chance==2){
                int xd = Renderer.player.x - x; // The horizontal distance between the player and the night wizard.
                int yd = Renderer.player.y - y; // The vertical distance between the player and the night wizard.
                double hypot = Math.sqrt(xd * xd + yd * yd);
                int newxd = (int) (xd * Math.sqrt(-(random.nextInt(10)+5) * (-(random.nextInt(10)+5) * (-(random.nextInt(10)+5) * (-(random.nextInt(10)+5) / hypot)))));
                int newyd = (int) (yd * Math.sqrt(-(random.nextInt(10)+5) * (-(random.nextInt(10)+5) * (-(random.nextInt(10)+5) * (-(random.nextInt(10)+5) / hypot)))));
                if(this.x - newxd<20)x+=30;
                else if(this.x + newxd>level.w-20)x-=30;
                else x += newyd;
                if(this.y - newyd<20)y+=30;
                else if(this.y + newxd>level.h-20)y-=30;
                else y += newyd;

                double dir = (random.nextInt(150)+1) * 0.25 * ((random.nextInt(150)+1) % 2 * 2 - 1); // Assigns a local direction variable from the attack time.
                double speed = (1.1) + attackType * 0.2; // speed is dependent on the attackType. (higher attackType, faster speeds)
                level.add(new BlackSpark(this, Math.cos(Math.round(Math.random())==1 ? dir : -dir) * speed, Math.sin(Math.round(Math.random())==1 ? dir : -dir) * speed)); // Adds a spark entity with the cosine and sine of dir times speed.
                return; // Skips the rest of the code (attackTime was > 0; ie we're attacking.)
            }
            boolean crit= !getClosestPlayer().potionEffects.containsKey(PotionType.Weak) && random.nextInt(22)>20;
            super.doHurt(damage, attackDir,crit);
            crit=false;
        }
        if (attackDelay <= 0 && attackTime == 0) {
            attackDelay = 60 * 2;
        }
    }

    @Override
    public void render(Screen screen) {
        super.render(screen);
        if (Updater.getTime()!= Updater.Time.Night) {
            // apply invulnerable sprite
            lvlSprites[lvl-1] = (MobSprite.compileMobSpriteAnimations(8, 38));
        }else{
            lvlSprites[lvl-1]=sprites[lvl-1];
        }
        int textcol = Color.get(1, 0, 204, 0);
        int textcol2 = Color.get(1, 0, 51, 0);
        int percent = health / (maxHealth / 100);
        String h = percent + "%";

        if (percent < 1) h = "1%";

        if (percent < 16) {
            textcol = Color.get(1, 204, 0, 0);
            textcol2 = Color.get(1, 51, 0, 0);
        }
        else if (percent < 51) {
            textcol = Color.get(1, 204, 204, 9);
            textcol2 = Color.get(1, 51, 51, 0);
        }
        int textwidth = Font.textWidth(h);
        if(Updater.getTime()== Updater.Time.Night) {
            Font.draw(h, screen, (x - textwidth / 2) + 1, y - 17, textcol2);
            Font.draw(h, screen, (x - textwidth / 2), y - 18, textcol);
        }
    }

    @Override
    protected void touchedBy(Entity entity) {
        if (Updater.getTime()== Updater.Time.Night) {
            if (entity instanceof Player) {
                // If the entity is the Player, then deal them 1 or 2 damage points.
                ((Player) entity).hurt(this, (2));
            }
        }
    }

    /** What happens when the night wizard dies */

    public void die() {
        revenge=0;
        for(int i=0;i<100;i++) {
            if(i%100==0) {
                attackTime = 200;
                while (attackTime > 0.001) {
                    xmov = ymov = 0;
                    attackTime *= 0.9998; // attackTime will decrease by 7% every time.
                    double dir = attackTime * 0.25 * (attackTime % 2 * 2 - 1); // Assigns a local direction variable from the attack time.
                    double speed = (1.1) + attackType * 0.2; // speed is dependent on the attackType. (higher attackType, faster speeds)
                    level.add(new BlackSpark(this, Math.cos(dir) * speed, Math.sin(dir) * speed)); // Adds a spark entity with the cosine and sine of dir times speed.
                }
            }
        }
        Player[] players = level.getPlayers();
        if (players.length > 0) { // If the player is still here
            for (Player p: players)
                p.addScore((lvl>1 ? 600000 : 120000)); // Give the player 120k pts or 600k if bloodmoon
                if(lvl>1){

                    dropItem(30, 30, Items.get("Blood shard"));
                    dropItem(1,1, Items.get("Night Armor"));
                }
        }

        Sound.bossDeath.play(); // Play boss-death sound.

        Updater.notifyAll("Night Wizard "+(Updater.isbloody ? "II :" : ":")+ "Defeated!");
        Game.notifications.add(lvl>1 ? "\nAn armor lies on the ground." : "");
        beaten = true;

        super.die(); // Calls the die() method in EnemyMob.java
    }

    public int getMaxLevel() { return 1; }
}
