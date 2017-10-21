package minicraft.core;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class Sound {
	//creates sounds from their respective files
	public static final Sound playerHurt = new Sound("/resources/playerhurt.wav");
	public static final Sound playerDeath = new Sound("/resources/death.wav");
	public static final Sound monsterHurt = new Sound("/resources/monsterhurt.wav");
	public static final Sound bossDeath = new Sound("/resources/bossdeath.wav");
	public static final Sound fuse = new Sound("/resources/fuse.wav");
	public static final Sound explode = new Sound("/resources/explode.wav");
	public static final Sound pickup = new Sound("/resources/pickup.wav");
	public static final Sound craft = new Sound("/resources/craft.wav");
	public static final Sound back = new Sound("/resources/craft.wav");
	public static final Sound select = new Sound("/resources/select.wav");
	public static final Sound confirm = new Sound("/resources/confirm.wav");
	
	private Clip clip; // Creates a audio clip to be played
	
	public static void init() {} // a way to initialize the class without actually doing anything
	
	private Sound(String name) {
		if(!Game.HAS_GUI) return;
		
		try {
			clip = (Clip)AudioSystem.getLine(new Line.Info(Clip.class));
			clip.open(AudioSystem.getAudioInputStream(getClass().getResource(name)));
			
			clip.addLineListener(e -> {
				if(e.getType() == LineEvent.Type.STOP) {
					clip.flush();
					clip.setFramePosition(0);
				}
			});
			
		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void play() {
		if (!(boolean)Settings.get("sound") || clip == null) return;
		
		if(clip.isRunning() || clip.isActive())
			clip.stop();
		
		clip.start();
	}
	
	public void loop(boolean start) {
		if (!(boolean)Settings.get("sound") || clip == null) return;
		
		if(start)
			clip.loop(Clip.LOOP_CONTINUOUSLY);
		else
			clip.stop();
	}
	
	/*
	public static class Music {
		
		
		
		public Music(String name) {
			AudioInputStream stream = AudioSystem.getAudioInputStream()
		}
		
	}*/
}
