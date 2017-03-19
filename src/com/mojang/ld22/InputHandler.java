package com.mojang.ld22;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputHandler implements MouseListener, KeyListener {
	//note: there needs to be an options menu for changing the key controls.
	
	/** Note: He he! I made HUGE revisions to this class, so I get to make the comments!
		Mostly. I'll use whatever's helpful. -Chris J
	*/
	
	/**
		This class handles key presses; this also implements MouseListener... but I have no idea why.
		It's not used in any way. Ever. As far as I know. Anyway, here are a few tips about this class:
		
		-This class must instantiated to be used; and it's pretty much always called "input" in the code.
		
		-The keys are stored in two arrays, one for physical keyboard keys(called "keyboard"), and one for "keys" you make-up (called "keymap") to represent different actions ("virtual keys", you could say).
		
		-All the Keys in the keyboard array are generated automatically as you press them / ask for them (if they don't already exist), so there's no need to define anything in the keyboard array in the code.
		
		-All the "virtual keys" in keymap "map" to a Key object in the keyboard array; that is to say,
			keymap contains a HashMap of string keys, to string values. The keys are the name of your actions,
			and the values are the name of the keyboard keys you're actually going to be using.
		
		-To get whether a key is pressed or not, use input.getKey("key"), where "key" is the name of the key, either physical or virtual. If virtual, all it does is then fetch the corrosponding key from keyboard anyway; but it allows one to change the controls while still making the same key requests in the code.
	*/

	private HashMap<String, String> keymap; // The symbolic map of actions to physical key names.
	private HashMap<String, Key> keyboard; // The actual map of key names to Key objects.
	public String lastKeyTyped = ""; // Used for things like typing world names.
	
	// mouse stuff that's never used
	public List<Mouse> mouse = new ArrayList<Mouse>();
	public Mouse one = new Mouse();
	public Mouse two = new Mouse();
	public Mouse tri = new Mouse();

	public InputHandler(Game game) {
		keymap = new HashMap<String, String>(); //stores custom key name with physical key name in keyboard.
		keyboard = new HashMap<String, Key>(); //stores physical keyboard keys; auto-generated :D

		keymap.put("UP", "UP"); //up action references up arrow key
		keymap.put("DOWN", "DOWN"); //move down action references down arrow key
		keymap.put("LEFT", "LEFT"); //move left action references left arrow key
		keymap.put("RIGHT", "RIGHT"); //move right action references right arrow key
		keymap.put("ATTACK", "C"); //attack action references "C" key
		keymap.put("MENU", "X"); //and so on... menu does various things.
		keymap.put("CRAFT", "Z"); // open/close personal crafting window.
		keymap.put("PAUSE", "ESCAPE"); // pause the game.
		keymap.put("SETHOME", "H"); // set your home.
		keymap.put("HOME", "1"); // go to set home.
		//keymap.put("MODE", ""); //useless? (unreachable)
		//keymap.put("SURVIVAL", ""); //useless? (unreachable)
		//keymap.put("CREATIVE", ""); //useless? (unreachable)
		//keymap.put("HARDCORE", ""); //useless? (unreachable)
		keymap.put("FPS", "TAB"); //SUPPOSED to toggle fps display, but I don't think it does...
		keymap.put("OPTIONS", "O"); //displays some options?
		keymap.put("SOUNDON", "M"); //toggles sound on and off.
		keymap.put("DAYTIME", "2"); //sort of makes day happen, in creative mode.
		keymap.put("NIGHTTIME", "3"); //sort of makes night happen, in creative mode.
		
		game.addKeyListener(this); //add key listener to game
		game.addMouseListener(this); //add mouse listener to game (though it's never used)
	}
	
	/** Processes each key one by one, in keyboard. */
	public void tick() {
		Key[] keys = keyboard.values().toArray(new Key[0]); // Get an array of all the Key objects
		for (int i = 0; i < keys.length; i++) keys[i].tick(); //call tick() for each one.
	}
	
	/** This is used to stop all of the actions when the game is out of focus. */
	public void releaseAll() {
		//Map.Entry<String,Key>[] mappings = keymap.entrySet().toArray(new Map.Entry[0]);
		Key[] keys = keyboard.values().toArray(new Key[0]);
		for (int i = 0; i < keys.length; i++) {
			//System.out.println(i+1+": " + mappings[i].getKey() + " - " + mappings[i].getValue());
			keys[i].down = false;
		}
	}
	
	/// this is meant for changing the default keys. Call it from the options menu, or something.
	public void setKey(String keymapKey, String keyboardKey) {
		if (keymapKey != null) //the keyboardKey can be null, I suppose, if you want to disable a key...
		keymap.put(keymapKey, keyboardKey);
	}
	
	/// THIS is pretty much the only way you want to be interfacing with this class; it has all the auto-create and protection functions and such built-in.
	public Key getKey(String keytext) {
		// if the passed-in key is blank, or null, then return null.
		if (keytext == null || keytext.length() == 0) return null;
		
		Key key; // make a new key to return at the end
		keytext = keytext.toUpperCase(); // prevent errors due to improper "casing"
		
		// if the passed-in key matches one in keymap, then replace it with it's match, a key in keyboard.
		if (keymap.containsKey(keytext))
			keytext = keymap.get(keytext); // converts action name to physical key name
		
		if (keyboard.containsKey(keytext))
			key = keyboard.get(keytext); // gets the key object from keyboard, if if exists.
		else {
			// If the specified key does not yet exist in keyboard, then create a new Key, and put it there.
			key = new Key(); //make new key
			keyboard.put(keytext, key); //add it to keyboard
			//System.out.println("Added new key: \'" + keytext + "\'"); //log to console that a new key was added to the keyboard
		}
		
		return key; // return the Key object.
	}
	
	//The Key class.
	public class Key {
		//presses = how many times the Key has been pressed.
		//absorbs = how many key presses have been processed.
		public int presses, absorbs;
		//down = if the key is currently physically being held down.
		//clicked = if the key is still being processed at the current tick.
		public boolean down, clicked;
		
		public Key() {} // probably would be auto-created anyway.
		
		/** toggles the key down or not down. */
		public void toggle(boolean pressed) {
			if (pressed != down) down = pressed; // set down to the passed in value; the if statement is probably unnecessary...
			if (pressed) presses++; //add to the number of total presses.
		}
		
		/** Processes the key presses. */
		public void tick() {
			if (absorbs < presses) { // If there are more key presses to process...
				absorbs++; //process them!
				clicked = true; // make clicked true, since key presses are still being processed.
			} else { // All key presses so far for this key have been processed.
				clicked = false; // set clicked to false, since we're done processing.
				//reset the presses and absorbs, to ensure they don't get too high, or something:
				presses = 0;
				absorbs = 0;
			}
		}
		
		//custom toString() method, I used it for debugging.
		public String toString() {
			return "down:" + down + "; clicked:" + clicked + "; presses=" + presses + "; absorbs=" + absorbs;
		}
	}
	
	//Mouse class! That...never really ever gets used... and so shall not be commented...
	public class Mouse {
		public int pressesd, absorbsd; //d=down?
		public boolean click, down;

		public Mouse() {
			mouse.add(this);
		}

		public void toggle(boolean clickd) {
			if (clickd != down) down = clickd;
			if (clickd) pressesd++;
		}

		public void tick() {
			if (absorbsd < pressesd) {
				absorbsd++;
				click = true;
			} else {
				click = false;
			}
		}
	}
	
	//for Keys; called by KeyListener methods.
	private void toggle(KeyEvent ke, boolean pressed) {
		String keytext = ke.getKeyText(ke.getKeyCode());
		getKey(keytext).toggle(pressed);
	}
	
	//called by MouseListener methods.
	private void click(MouseEvent e, boolean clickd) {
		if (e.getButton() == MouseEvent.BUTTON1) one.toggle(clickd);
		if (e.getButton() == MouseEvent.BUTTON2) two.toggle(clickd);
		if (e.getButton() == MouseEvent.BUTTON3) tri.toggle(clickd);
	}
	
	/** Used by Save.java, to save user key preferences. */
	public String[] getKeyPrefs() {
		ArrayList<String> keystore = new ArrayList<String>(); //make a list for keys
		Map.Entry<String, String>[] keysets = keymap.entrySet().toArray(new Map.Entry[0]); // get a list of the mappings in keymap, which stores key preferences
		for (int i = 0; i < keysets.length; i++) //go though each mapping
			keystore.add(keysets[i].getKey() + ";" + keysets[i].getValue()); //add the mapping values as one string, seperated by a semicolon.

		return keystore.toArray(new String[0]); //return the array of encoded key preferences.
	}
	
	/// Event methods, many to satisfy interface requirements...
	
	public void keyPressed(KeyEvent ke) {
		toggle(ke, true);
	}

	public void keyReleased(KeyEvent ke) {
		toggle(ke, false);
	}

	public void keyTyped(KeyEvent ke) {
		//stores the last character typed
		lastKeyTyped = String.valueOf(ke.getKeyChar());
	}

	public void mouseClicked(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {
		click(e, true);
	}

	public void mouseReleased(MouseEvent e) {
		click(e, false);
	}
}
