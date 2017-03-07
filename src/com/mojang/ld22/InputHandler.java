package com.mojang.ld22;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InputHandler implements MouseListener, KeyListener {
	
	/*
	UNFINISHED BUISNESS
		convert all references to InputHandler (pretty much always called input)
		from "input.[key-name].[bool]" to "input.getKey([key-name]).[bool]".
		
		Also, the getKey() method of InputHandler should also be made to, then.
		This might also mean that none of the keys or maps have to be public -- only the getKey() method.
	
	*/
	
	public InputHandler(Game game) {
		game.addKeyListener(this);
		game.addMouseListener(this);
	}
	
	public class Key {
		public int presses, absorbs;
		public boolean down, clicked;
		
		public Key() {}
		
		public void toggle(boolean pressed) {
			if (pressed != down) {
				down = pressed;
			}
			if (pressed) {
				presses++;
			}
		}
		
		public void tick() {
			if (absorbs < presses) {
				absorbs++;
				clicked = true;
			} else {
				clicked = false;
			}
		}
	}
	
	
	public class Mouse {
		
		public int pressesd, absorbsd;
		public boolean click, down;
		
		public Mouse() {
			mouse.add(this);
		}
		
		public void toggle(boolean clickd) {
			if (clickd != down) {
				down = clickd;
			}
			if (clickd) {
				pressesd++;
			}
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
	
	public List<Mouse> mouse = new ArrayList<Mouse>();
	public Mouse one = new Mouse();
	public Mouse two = new Mouse();
	public Mouse tri = new Mouse();
	
	
	//public List<Key> keys = new ArrayList<Key>();
	public HashMap<String,Key> keymap = new HashMap<String,Key>();
	public String lastKeyTyped = "";
	
	keymap.add("A", new Key());
	keymap.add("B", new Key());
	keymap.add("C", new Key());
	keymap.add("D", new Key());
	keymap.add("E", new Key());
	keymap.add("F", new Key());
	keymap.add("G", new Key());
	keymap.add("H", new Key());
	keymap.add("I", new Key());
	keymap.add("J", new Key());
	keymap.add("K", new Key());
	keymap.add("L", new Key());
	keymap.add("M", new Key());
	keymap.add("N", new Key());
	keymap.add("O", new Key());
	keymap.add("P", new Key());
	keymap.add("Q", new Key());
	keymap.add("R", new Key());
	keymap.add("S", new Key());
	keymap.add("T", new Key());
	keymap.add("U", new Key());
	keymap.add("V", new Key());
	keymap.add("W", new Key());
	keymap.add("X", new Key());
	keymap.add("Y", new Key());
	keymap.add("Z", new Key());
	
	keymap.add("A1", new Key());
	keymap.add("A2", new Key());
	keymap.add("A3", new Key());
	keymap.add("A4", new Key());
	keymap.add("A5", new Key());
	keymap.add("A6", new Key());
	keymap.add("A7", new Key());
	keymap.add("A8", new Key());
	keymap.add("A9", new Key());
	keymap.add("A0", new Key());
	keymap.add("F2", new Key());
	keymap.add("F3", new Key());
	
	keymap.add("ENTER", new Key());
	keymap.add("DELETE", new Key());
	keymap.add("SPACE", new Key());
	keymap.add("BACKSPACE", new Key());
	
	//later, a seperate map/array of keys for the action keys should be used, so that
		//multiple keys inputs can be specified..? Actually, why would I do that?
	
	//would still be nice, though, as an easy way to differenciate which are the Action Keys.
	keymap.add("UP", keymap.get("W"));
	keymap.add("DOWN", keymap.get("S"));
	keymap.add("LEFT", keymap.get("A"));
	keymap.add("RIGHT", keymap.get("D"));
	keymap.add("ATTACK", keymap.get("C"));
	keymap.add("MENU", keymap.get("X"));
	keymap.add("CRAFT", keymap.get("Z"));
	keymap.add("PAUSE", keymap.get("ESCAPE"));
	keymap.add("SETHOME", keymap.get("H"));
	keymap.add("HOME", keymap.get("1"));
	keymap.add("MODE", new Key());
	keymap.add("SURVIVAL", new Key());
	keymap.add("CREATIVE", new Key());
	keymap.add("HARDCORE", new Key());
	keymap.add("FPS", keymap.get("TAB"));
	keymap.add("OPTIONS", keymap.get("O"));
	keymap.add("SOUNDON", keymap.get("M"));
	keymap.add("DAYTIME", new Key());
	keymap.add("NIGHTTIME", new Key());
	
	public void releaseAll() {
		Key[] keys = keymap.values().toArray(new Key[0]);
		for (int i = 0; i < keys.size(); i++) {
			keys.get(i).down = false;
		}
	}
	
		Key[] keys = keymap.values().toArray(new Key[0]);
	public void tick() {
		for (int i = 0; i < keys.size(); i++) {
			keys.get(i).tick();
		}
	}
	
	public void keyPressed(KeyEvent ke) {
		toggle(ke, true);
	}
	
	public void keyReleased(KeyEvent ke) {
		toggle(ke, false);
	}
	
	public void keyTyped(KeyEvent ke) {
		lastKeyTyped = String.valueOf(ke.getKeyChar());
	}
	
	private void toggle(KeyEvent ke, boolean pressed) {
		/*
		if (ke.getKeyCode() == KeyEvent.VK_NUMPAD8) up.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_NUMPAD2) down.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_NUMPAD4) left.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_NUMPAD6) right.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_W) up.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_S) down.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_A) left.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_D) right.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_UP) up.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_DOWN) down.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_LEFT) left.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_RIGHT) right.toggle(pressed);
		*//*
		if (ke.getKeyCode() == KeyEvent.VK_TAB) menu.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_ALT) menu.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_ALT_GRAPH) menu.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_SPACE) attack.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_CONTROL) attack.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_NUMPAD0) attack.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_INSERT) attack.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_ENTER) menu.toggle(pressed);
		//if (ke.getKeyCode() == KeyEvent.VK_ENTER) enter.toggle(pressed);//generic
		if (ke.getKeyCode() == KeyEvent.VK_Q) craft.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_NUMPAD1) craft.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_E) craft.toggle(pressed);
		
		if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) pause.toggle(pressed);
		//if (ke.getKeyCode() == KeyEvent.VK_N) pause.toggle(pressed);
		
		if (ke.getKeyCode() == KeyEvent.VK_Z) craft.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_X) menu.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_C) attack.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_R) r.toggle(pressed);//generic
		if (ke.getKeyCode() == KeyEvent.VK_T) t.toggle(pressed);//generic
		if (ke.getKeyCode() == KeyEvent.VK_1) sethome.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_H) home.toggle(pressed);
		*//*
		if (ke.getKeyCode() == KeyEvent.VK_G) mode.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_1) survival.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_2) creative.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_3) hardcore.toggle(pressed);
		
		if (ke.getKeyCode() == KeyEvent.VK_F3) hardcore.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_O) options.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_S) soundOn.toggle(pressed);
		
		if (ke.getKeyCode() == KeyEvent.VK_S) soundOn.toggle(pressed);
		
		if (ke.getKeyCode() == KeyEvent.VK_2) dayTime.toggle(pressed);
		if (ke.getKeyCode() == KeyEvent.VK_3) nightTime.toggle(pressed);
		*//*if (ke.getKeyCode() == KeyEvent.VK_I) i.toggle(pressed);//generic
		if (ke.getKeyCode() == KeyEvent.VK_W) w.toggle(pressed);//generic
		if (ke.getKeyCode() == KeyEvent.VK_L) l.toggle(pressed);//generic
		if (ke.getKeyCode() == KeyEvent.VK_S) s.toggle(pressed);//generic
		
		if (ke.getKeyCode() == KeyEvent.VK_SPACE) space.toggle(pressed);//generic
		if (ke.getKeyCode() == KeyEvent.VK_BACK_SPACE) backspace.toggle(pressed);//generic
		if (ke.getKeyCode() == KeyEvent.VK_F2) f2.toggle(pressed);//generic
		if (ke.getKeyCode() == KeyEvent.VK_F3) f3.toggle(pressed);//generic
		*/
		try {
			keymap.get(ke.getKeyText(ke.getKeyCode())).toggle(pressed);
		} catch(Exception ex) {
			ex.printStackTrace();
			keymap.add(ke.getKeyText(ke.getKeyCode()), new Key());
		}
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
