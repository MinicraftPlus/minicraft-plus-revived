package minicraft.sound;

import minicraft.screen.OptionsMenu;
import java.applet.Applet;
import java.applet.AudioClip;

public class Sound {
	//creates sounds from theri respective files
	public static final Sound playerHurt = new Sound("/playerhurt.wav");
	public static final Sound playerDeath = new Sound("/death.wav");
	public static final Sound monsterHurt = new Sound("/monsterhurt.wav");
	public static final Sound test = new Sound("/test.wav");
	public static final Sound pickup = new Sound("/pickup.wav");
	public static final Sound bossdeath = new Sound("/bossdeath.wav");
	public static final Sound craft = new Sound("/craft.wav");
	public static final Sound fuse = new Sound("/fuse.wav");
	public static final Sound explode = new Sound("/explode.wav");

	private AudioClip clip; // Creates a audio clip to be played

	private Sound(String name) {
		try {
			// tries to load the audio clip from the name you gave above.
			clip = Applet.newAudioClip(Sound.class.getResource(name));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void play() {
		if (!OptionsMenu.isSoundAct) return;
		try {
			new Thread() { //creates a naew thread (string of events)
				public void run() {
					//if (OptionsMenu.isSoundAct)
					clip.play(); // plays the sound clip when called
				}
			}.start(); //runs the thread
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
