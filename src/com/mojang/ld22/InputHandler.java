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
	
	private HashMap<String, Key> keymap, keyboard;
	public String lastKeyTyped = "";
	     
	public List<Mouse> mouse = new ArrayList<Mouse>();
	public Mouse one = new Mouse();
	public Mouse two = new Mouse();
	public Mouse tri = new Mouse();
	
	public InputHandler(Game game) {
		keymap = new HashMap<String, Key>(); //stores custom keys
		keyboard = new HashMap<String, Key>(); //stores text of physical keyboard keys; auto-generated
		
		keymap.put("UP", getKey("W"));
		keymap.put("DOWN", getKey("S"));
		keymap.put("LEFT", getKey("A"));
		keymap.put("RIGHT", getKey("D"));
		keymap.put("ATTACK", getKey("C"));
		keymap.put("MENU", getKey("X"));
		keymap.put("CRAFT", getKey("Z"));
		keymap.put("PAUSE", getKey("ESCAPE"));
		keymap.put("SETHOME", getKey("H"));
		keymap.put("HOME", getKey("1"));
		keymap.put("MODE", new Key()); //useless? (unreachable)
		keymap.put("SURVIVAL", new Key()); //useless? (unreachable)
		keymap.put("CREATIVE", new Key()); //useless? (unreachable)
		keymap.put("HARDCORE", new Key()); //useless? (unreachable)
		keymap.put("FPS", getKey("TAB"));
		keymap.put("OPTIONS", getKey("O"));
		keymap.put("SOUNDON", getKey("M"));
		keymap.put("DAYTIME", new Key()); //useless? (unreachable)
		keymap.put("NIGHTTIME", new Key()); //useless? (unreachable)
		
		game.addKeyListener(this);
		game.addMouseListener(this);
	}
	
	public void tick() {
		Key[] keys = keymap.values().toArray(new Key[0]);
		for (int i = 0; i < keys.length; i++)
			keys[i].tick();
	}
	
	public void setKey(String keymapKey, String keyboardKey) {
		//this is meant for changing the default keys.
		//note: there needs to be a settings file for this... add it to save/load later.
		keymap.put(keymapKey, getKey(keyboardKey));
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
	
	public Key getKey(String keytext) {
		Key key;
		keytext = keytext.toUpperCase();
		
		if(keymap.containsKey(keytext))
			key = keymap.get(keytext);
		else if(keyboard.containsKey(keytext))
			key = keyboard.get(keytext);
		else {
			key = new Key();
			keyboard.put(keytext, key);
			System.out.println("Added new key: \'" + keytext + "\'");
		}
		
		return key;
	}
	
	public void releaseAll() {
		//Map.Entry<String,Key>[] mappings = keymap.entrySet().toArray(new Map.Entry[0]);
		Key[] keys = keymap.values().toArray(new Key[0]);
		for (int i = 0; i < keys.length; i++) {
			//System.out.println(i+1+": " + mappings[i].getKey() + " - " + mappings[i].getValue());
			keys[i].down = false;
		}
	}
	
	public void keyPressed(KeyEvent ke) { toggle(ke, true); }
	public void keyReleased(KeyEvent ke) { toggle(ke, false); }
	
	public void keyTyped(KeyEvent ke) {
		lastKeyTyped = String.valueOf(ke.getKeyChar());
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
	
	@Override
	public void mouseClicked(MouseEvent e) {
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public void mousePressed(MouseEvent e) {
 	  click(e, true);
	}
	 
	public void mouseReleased(MouseEvent e) {
 	  click(e, false);
	}
}
