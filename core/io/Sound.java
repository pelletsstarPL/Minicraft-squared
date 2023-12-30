package minicraft.core.io;

import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import minicraft.core.Game;

public class Sound {
	static String baseExt = "/resources/sound/";

	// Creates sounds from their respective files
	/**KNIGHT STATUE
	 **/

	public static final Sound statuePh1 = new Sound(baseExt + "knightStatue/statuePh1.wav");
	public static final Sound statuePh2 = new Sound(baseExt + "knightStatue/statuePh2.wav");
	public static final Sound statuePh3 = new Sound(baseExt + "knightStatue/statuePh3.wav");

	/**MOB ACTIONS
			**/
	public static final Sound playerHurt = new Sound(baseExt + "mobActions/playerhurt.wav");
	public static final Sound playerDeath = new Sound(baseExt + "mobActions/death.wav");
	public static final Sound monsterHurt = new Sound(baseExt + "mobActions/monsterhurt.wav");
	public static final Sound bossDeath = new Sound(baseExt + "mobActions/bossdeath.wav");
	public static final Sound wraithDeath = new Sound(baseExt + "mobActions/wraithDeath.wav");
	public static final Sound nightactivate = new Sound(baseExt + "mobActions/nightwizactivate.wav");

	/**OTHER GAME ACTIONS
	 **/
	public static final Sound fuse = new Sound(baseExt + "fuse.wav");
	public static final Sound fuseChests = new Sound(baseExt + "fuseChests.wav");
	public static final Sound explode = new Sound(baseExt + "explode.wav");
	public static final Sound pickup = new Sound(baseExt + "pickup.wav");
	public static final Sound craft = new Sound(baseExt + "craft.wav");
	public static final Sound back = new Sound(baseExt + "craft.wav");
	public static final Sound place = new Sound(baseExt + "craft.wav");
	public static final Sound select = new Sound(baseExt + "select.wav");
	public static final Sound confirm = new Sound(baseExt + "confirm.wav");
	public static final Sound nightbuff = new Sound(baseExt + "nightbuff.wav");
	public static final Sound sprint = new Sound(baseExt + "powerup.wav");
	public static final Sound no = new Sound(baseExt + "no.wav");
	public static final Sound woosh = new Sound(baseExt + "woosh.wav");
	public static final Sound timeloop = new Sound(baseExt + "timeloop.wav");


	private Clip clip; // Creates a audio clip to be played
	
	public static void init() {} // A way to initialize the class without actually doing anything


	private Sound(String name) {

		if (!Game.HAS_GUI) return;
		
		try {
			URL url = getClass().getResource(name);
			
			DataLine.Info info = new DataLine.Info(Clip.class, AudioSystem.getAudioFileFormat(url).getFormat());
			
			if (!AudioSystem.isLineSupported(info)) {
				System.err.println("ERROR: Audio format of file " + name + " is not supported: " + AudioSystem.getAudioFileFormat(url));
				
				System.out.println("Supported audio formats:");
				System.out.println("-source:");
				Line.Info[] sinfo = AudioSystem.getSourceLineInfo(info);
				Line.Info[] tinfo = AudioSystem.getTargetLineInfo(info);
				for (int i = 0; i < sinfo.length; i++)
				{
					if (sinfo[i] instanceof DataLine.Info)
					{
						DataLine.Info dataLineInfo = (DataLine.Info) sinfo[i];
						AudioFormat[] supportedFormats = dataLineInfo.getFormats();
						for (AudioFormat af: supportedFormats)
							 System.out.println(af);
					}
				}
				System.out.println("-target:");
				for (int i = 0; i < tinfo.length; i++)
				{
					if (tinfo[i] instanceof DataLine.Info)
					{
						DataLine.Info dataLineInfo = (DataLine.Info) tinfo[i];
						AudioFormat[] supportedFormats = dataLineInfo.getFormats();
						for (AudioFormat af: supportedFormats)
							 System.out.println(af);
					}
				}
				
				return;
			}
			
			clip = (Clip)AudioSystem.getLine(info);
			clip.open(AudioSystem.getAudioInputStream(url));
			
			clip.addLineListener(e -> {
				if (e.getType() == LineEvent.Type.STOP) {
					clip.flush();
					clip.setFramePosition(0);
				}
			});
			
		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
			System.err.println("Could not load sound file " + name);
			e.printStackTrace();
		}
	}
	
	public void play() {
		if (!(boolean)Settings.get("sound") || clip == null) return;
		if (Game.isValidServer()) return;
		
		if (clip.isRunning() || clip.isActive())
			clip.stop();
		
		clip.start();
	}
	
	public void loop(boolean start) {
		if (!(boolean)Settings.get("sound") || clip == null) return;
		
		if (start)
			clip.loop(Clip.LOOP_CONTINUOUSLY);
		else
			clip.stop();
	}
}
