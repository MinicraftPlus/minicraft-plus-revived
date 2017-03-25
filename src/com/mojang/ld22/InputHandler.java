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
	
	/** Note: Yay! I made HUGE revisions to this class, so I get to make the comments!
		...and I actually know what I'm talking about. ;)
			-Chris J
	*/
		
	/**
		This class handles key presses; this also implements MouseListener... but I have no idea why.
		It's not used in any way. Ever. As far as I know. Anyway, here are a few tips about this class:
		
		-This class must instantiated to be used; and it's pretty much always called "input" in the code.
		
		-The keys are stored in two arrays, one for physical keyboard keys(called "keyboard"), and one for "keys" you make-up (called "keymap") to represent different actions ("virtual keys", you could say).
		
		-All the Keys in the keyboard array are generated automatically as you ask for them in the code (if they don't already exist), so there's no need to define anything in the keyboard array here.
			--Note: this shouldn't matter, but keys that are not asked for or defined as values here in keymap will be ignored when it comes to key presses.
		
		-All the "virtual keys" in keymap "map" to a Key object in the keyboard array; that is to say,
			keymap contains a HashMap of string keys, to string values. The keys are the names of the actions,
			and the values are the names of the keyboard keys you will physically press.
		
		-This class supports modifier keys (I think; it's recent) by giving a modified key ("shift-a", called "compound keys" from here out) its own seperate Key object from "shift" and "a". While they do intersect a bit, by using the each component Key object's down property to determine their own, they are more or less seperate.
			--To specify a compound key, write "MOD1-MOD2-KEY", that is, "SHIFT-ALT-D" or "ALT-F", with a "-" between the keys. ALWAYS put the actual trigger key last, after all modifiers.
			--For the purposes of this class's terminology, Compound Keys are considered to be "physical keys".
		
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
		keymap.put("SETHOME", "SHIFT-H"); // set your home.
		keymap.put("HOME", "H"); // go to set home.
		//keymap.put("DAYTIME", "2"); //sort of makes day happen.
		//keymap.put("NIGHTTIME", "3"); //sort of makes night happen.
		keymap.put("SURVIVAL", "5");
		keymap.put("CREATIVE", "6");
		
		//keymap.put("SOUNDON", "M"); //toggles sound on and off... well, it should...
		
		keymap.put("POTIONEFFECTS", "F2"); // toggle potion effect display
		//keymap.put("FPSDISP", "F3"); // toggle fps display
		keymap.put("INFO", "I"); // toggle player stats display
		
		/*//don't NEED this...
		for(String key: keymap.values())
			keyboard.put(key, new Key());
		*/
		game.addKeyListener(this); //add key listener to game
		game.addMouseListener(this); //add mouse listener to game (though it's never used)
	}
	
	/** Processes each key one by one, in keyboard. */
	public void tick() {
		synchronized ("lock") {
			//Key[] keys = keyboard.values().toArray(new Key[0]); // Get an array of all the Key objects
			for (Key key: keyboard.values()) key.tick(); //call tick() for each one.
		}
	}
	
	//The Key class.
	public class Key {
		//presses = how many times the Key has been pressed.
		//absorbs = how many key presses have been processed.
		private int presses, absorbs;
		//down = if the key is currently physically being held down.
		//clicked = if the key is still being processed at the current tick.
		public boolean down, clicked;
		//sticky = true if presses reaches 3, and the key continues to be held down.
		private boolean sticky;
		
		public Key() {} // probably would be auto-created anyway.
		
		/** toggles the key down or not down. */
		public void toggle(boolean pressed) {
			down = pressed; // set down to the passed in value; the if statement is probably unnecessary...
			if (pressed && !sticky) presses++; //add to the number of total presses.
		}
		
		/** Processes the key presses. */
		public void tick() {
			if (absorbs < presses) { // If there are more key presses to process...
				absorbs++; //process them!
				clicked = true; // make clicked true, since key presses are still being processed.
			} else { // All key presses so far for this key have been processed.
				if (!sticky) sticky = presses > 3;
				else sticky = down;
				clicked = sticky ? down : false; // set clicked to false, since we're done processing; UNLESS the key has been held down for a bit, and hasn't yet been released.
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
	
	/** This is used to stop all of the actions when the game is out of focus. */
	public void releaseAll() {
		//Map.Entry<String,Key>[] mappings = keymap.entrySet().toArray(new Map.Entry[0]);
		//Key[] keys = keyboard.values().toArray(new Key[0]);
		for (Key key: keyboard.values()) {
			//if(Game.debug) System.out.println(i+1+": " + mappings[i].getKey() + " - " + mappings[i].getValue());
			key.down = false;
		}
	}
	
	/// this is meant for changing the default keys. Call it from the options menu, or something.
	public void setKey(String keymapKey, String keyboardKey) {
		if (keymapKey != null) //the keyboardKey can be null, I suppose, if you want to disable a key...
			keymap.put(keymapKey, keyboardKey);
	}
	
	/** Simply returns the mapped value of key in keymap. */
	public String getMapping(String actionKey) {
		actionKey = actionKey.toUpperCase();
		if(keymap.containsKey(actionKey))
			return keymap.get(actionKey);
		
		return "NO_KEY";
	}
	
	/// THIS is pretty much the only way you want to be interfacing with this class; it has all the auto-create and protection functions and such built-in.
	public Key getKey(String keytext) {
		// if the passed-in key is blank, or null, then return null.
		if (keytext == null || keytext.length() == 0) return null;
		
		Key key; // make a new key to return at the end
		keytext = keytext.toUpperCase(); // prevent errors due to improper "casing"
		
		synchronized ("lock") {
			// if the passed-in key matches one in keymap, then replace it with it's match, a key in keyboard.
			if (keymap.containsKey(keytext))
				keytext = keymap.get(keytext); // converts action name to physical key name
			
			if (keyboard.containsKey(keytext))
				key = keyboard.get(keytext); // gets the key object from keyboard, if if exists.
			else {
				// If the specified key does not yet exist in keyboard, then create a new Key, and put it there.
				key = new Key(); //make new key
				keyboard.put(keytext, key); //add it to keyboard
				
				if(Game.debug) System.out.println("Added new key: \'" + keytext + "\'"); //log to console that a new key was added to the keyboard
			}
		}
		return key; // return the Key object.
	}
	
	/// this gets a key from key text, w/o adding to the key list.
	private Key getPhysKey(String keytext) {
		keytext = keytext.toUpperCase();
		if (keyboard.containsKey(keytext))
			return keyboard.get(keytext);
		else
			return new Key(); //won't matter where I'm calling it.
	}
	
	private void checkCompoundKeys(String mod) {
		mod = mod.toUpperCase();
		for(String keyname: keyboard.keySet()) {
			if(!keyname.contains("-")) continue; // only check compound keys
			if(!keyname.contains(mod)) continue; // only check those with this modifier
			
			// we have to check that all the keys are pressed to set it to true
			boolean pressed = true;
			for (String compkey: keyname.split("-")) {
				if (getKey(compkey).down == false) {
					pressed = false;
					break;
				}
			}
			getKey(keyname).toggle(pressed);
			//this next statement should make it so that if you release a modifier key, the normal key is released as well; I thought it would be nice to keep it from pressing the different key if you happened to release the modifier before the main one.
			if(pressed == false) getKey(keyname.substring(keyname.lastIndexOf("-")+1)).toggle(false);
		}
	}
	
	/** Used by Save.java, to save user key preferences. */
	public String[] getKeyPrefs() {
		ArrayList<String> keystore = new ArrayList<String>(); //make a list for keys
		//Map.Entry<String, String>[] keysets = keymap.entrySet().toArray(new Map.Entry[0]); // get a list of the mappings in keymap, which stores key preferences
		//for (int i = 0; i < keysets.length; i++) //go though each mapping
		for (String keyname: keymap.keySet()) //go though each mapping
			keystore.add(keyname + ";" + keymap.get(keyname)); //add the mapping values as one string, seperated by a semicolon.

		return keystore.toArray(new String[0]); //return the array of encoded key preferences.
	}
	
	//called by KeyListener Event methods, below. Only accesses keyboard Keys.
	private void toggle(KeyEvent ke, boolean pressed) {
		String keytext = ke.getKeyText(ke.getKeyCode()).toLowerCase(); //because I'm lazy
		
		// this works okay... unless I release a modifier key first!
		if(keytext != "shift" && keytext != "alt" && keytext != "ctrl") {
			if(getPhysKey("shift").down) keytext = "shift-"+keytext;
			if(getPhysKey("alt").down) keytext = "alt-"+keytext;
			if(getPhysKey("ctrl").down) keytext = "ctrl-"+keytext;
			
			//user presses should not generate new keys; only code requests.
			getPhysKey(keytext).toggle(pressed); // toggle a normal key
		}
		else {
			getPhysKey(keytext).toggle(pressed); // toggle a modifier key
			checkCompoundKeys(keytext); // check that compound keys using this modifier are still set to appropriate values.
		}
	}
	
	/// Event methods, many to satisfy interface requirements...
	public void keyPressed(KeyEvent ke) { toggle(ke, true); }
	public void keyReleased(KeyEvent ke) { toggle(ke, false); }
	public void keyTyped(KeyEvent ke) {
		//stores the last character typed
		lastKeyTyped = String.valueOf(ke.getKeyChar());
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
	
	//called by MouseListener methods.
	private void click(MouseEvent e, boolean clickd) {
		if (e.getButton() == MouseEvent.BUTTON1) one.toggle(clickd);
		if (e.getButton() == MouseEvent.BUTTON2) two.toggle(clickd);
		if (e.getButton() == MouseEvent.BUTTON3) tri.toggle(clickd);
	}
	
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	
	public void mousePressed(MouseEvent e) { click(e, true); }
	public void mouseReleased(MouseEvent e) { click(e, false); }
}
