package com.mojang.ld22;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class InputHandler implements MouseListener, KeyListener {
	//note: there needs to be an options menu for changing the key controls.
	
	private HashMap<String, String> keymap;
	private HashMap<String, Key>keyboard;
	public String lastKeyTyped = "";
	     
	public List<Mouse> mouse = new ArrayList<Mouse>();
	public Mouse one = new Mouse();
	public Mouse two = new Mouse();
	public Mouse tri = new Mouse();
	
	public InputHandler(Game game) {
		keymap = new HashMap<String, String>(); //stores custom key name with physical key name in keyboard.
		keyboard = new HashMap<String, Key>(); //stores physical keyboard keys; auto-generated :D
		
		keymap.put("UP", "UP");
		keymap.put("DOWN", "DOWN");
		keymap.put("LEFT", "LEFT");
		keymap.put("RIGHT", "RIGHT");
		keymap.put("ATTACK", "C");
		keymap.put("MENU", "X");
		keymap.put("CRAFT", "Z");
		keymap.put("PAUSE", "ESCAPE");
		keymap.put("SETHOME", "H");
		keymap.put("HOME", "1");
		//keymap.put("MODE", ""); //useless? (unreachable)
		//keymap.put("SURVIVAL", ""); //useless? (unreachable)
		//keymap.put("CREATIVE", ""); //useless? (unreachable)
		//keymap.put("HARDCORE", ""); //useless? (unreachable)
		keymap.put("FPS", "TAB");
		keymap.put("OPTIONS", "O");
		keymap.put("SOUNDON", "M");
		keymap.put("DAYTIME", "2"); //useless? (unreachable)
		keymap.put("NIGHTTIME", "3"); //useless? (unreachable)
		
		game.addKeyListener(this);
		game.addMouseListener(this);
	}
	
	public void tick() {
		Key[] keys = keyboard.values().toArray(new Key[0]);
		for (int i = 0; i < keys.length; i++)
			keys[i].tick();
	}
	
	public void releaseAll() {
		//Map.Entry<String,Key>[] mappings = keymap.entrySet().toArray(new Map.Entry[0]);
		Key[] keys = keyboard.values().toArray(new Key[0]);
		for (int i = 0; i < keys.length; i++) {
			//System.out.println(i+1+": " + mappings[i].getKey() + " - " + mappings[i].getValue());
			keys[i].down = false;
		}
	}
	
	//this is meant for changing the default keys.
	public void setKey(String keymapKey, String keyboardKey) {
		if(keymapKey != null) //the keyboardKey can be null, I suppose, if you want to disable a key...
			keymap.put(keymapKey, keyboardKey);
	}
	
	public Key getKey(String keytext) {
		if(keytext == null || keytext.length() == 0)
			return null;
		
		Key key;
		keytext = keytext.toUpperCase();
		
		if(keymap.containsKey(keytext))
			keytext = keymap.get(keytext); //converts action name to physical key name
				
		if(keyboard.containsKey(keytext))
			key = keyboard.get(keytext);
		else {
			key = new Key();
			keyboard.put(keytext, key);
			System.out.println("Added new key: \'" + keytext + "\'");
		}
		
		return key;
	}
	
	
	public class Key {
		public int presses, absorbs;
		public boolean down, clicked;
		
		public Key() {}
		
		public void toggle(boolean pressed) {
			if (pressed != down)
				down = pressed;
			if (pressed)
				presses++;
		}
		
		public void tick() {
			if (absorbs < presses) {
				absorbs++;
				clicked = true;
			} else {
				clicked = false;
			}
		}
		
		public String toString() {
			return "down:"+down+"; clicked:"+clicked+"; presses="+presses+"; absorbs="+absorbs;
		}
	}
	
	public class Mouse {
		public int pressesd, absorbsd; //d=down?
		public boolean click, down;
		
		public Mouse() {
			mouse.add(this);
		}
		
		public void toggle(boolean clickd) {
			if (clickd != down)
				down = clickd;
			if (clickd)
				pressesd++;
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
	
	
	private void toggle(KeyEvent ke, boolean pressed) {
		String keytext = ke.getKeyText(ke.getKeyCode());
		getKey(keytext).toggle(pressed);
	}
	
	private void click(MouseEvent e, boolean clickd) {
		if (e.getButton() == MouseEvent.BUTTON1) one.toggle(clickd);
		if (e.getButton() == MouseEvent.BUTTON2) two.toggle(clickd);
		if (e.getButton() == MouseEvent.BUTTON3) tri.toggle(clickd);
	}
	
	public String[] getKeyPrefs() {
		ArrayList<String> keystore = new ArrayList<String>();
		Map.Entry<String,String>[] keysets = keymap.entrySet().toArray(new Map.Entry[0]);
		for(int i = 0; i < keysets.length; i++)
			keystore.add(keysets[i].getKey()+";"+keysets[i].getValue());
		
		return keystore.toArray(new String[0]);
	}
	
	public void keyPressed(KeyEvent ke) { toggle(ke, true); }
	public void keyReleased(KeyEvent ke) { toggle(ke, false); }
	public void keyTyped(KeyEvent ke) {
		lastKeyTyped = String.valueOf(ke.getKeyChar());
	}
	
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) { click(e, true); }
	public void mouseReleased(MouseEvent e) { click(e, false); }
}
