package minicraft;

import javafx.scene.media.AudioClip;
import minicraft.screen.OptionsMenu;

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
	public static final Sound select = new Sound("/resources/select.wav");
	public static final Sound confirm = new Sound("/resources/confirm.wav");
	
	private AudioClip clip; // Creates a audio clip to be played
	
	private Sound(String name) {
		clip = new AudioClip(name);
	}

	public void play() {
		if (!OptionsMenu.isSoundAct) return;
		try {
			/*//creates a new thread (string of events)
			new Thread(() -> {
				//if (OptionsMenu.isSoundAct)
				//clip.stop();
				//clip.setFramePosition(0);
				clip.start();
				//clip.play(); // plays the sound clip when called
			}).start(); //runs the thread*/
			clip.play();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
