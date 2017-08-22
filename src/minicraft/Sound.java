package minicraft;

import java.applet.Applet;
import java.applet.AudioClip;
import minicraft.screen.OptionsMenu;

public class Sound {
	//creates sounds from their respective files
	public static final Sound playerHurt = new Sound("/resources/playerhurt.wav");
	public static final Sound playerDeath = new Sound("/resources/death.wav");
	public static final Sound monsterHurt = new Sound("/resources/monsterhurt.wav");
	public static final Sound test = new Sound("/resources/test.wav");
	public static final Sound pickup = new Sound("/resources/pickup.wav");
	public static final Sound bossdeath = new Sound("/resources/bossdeath.wav");
	public static final Sound craft = new Sound("/resources/craft.wav");
	public static final Sound fuse = new Sound("/resources/fuse.wav");
	public static final Sound explode = new Sound("/resources/explode.wav");

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
			//creates a naew thread (string of events)
			new Thread(() -> {
				//if (OptionsMenu.isSoundAct)
				clip.play(); // plays the sound clip when called
			}).start(); //runs the thread
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
