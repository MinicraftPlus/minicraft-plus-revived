//respawn mod +dillyg10+
package com.mojang.ld22.screen;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.sound.Sound;
import com.mojang.ld22.Game; 

public class LoadingMenu extends Menu implements ActionListener {
	private Menu parent;
	Timer t = new Timer(400, this);
	public static int percentage = 0;
	
	public LoadingMenu() {
		
	}
	
	public void tick() {
		t.start();
	}
	
	public void actionPerformed(ActionEvent e) {
		game.resetstartGame();
		game.setMenu(null);
		t.stop();
	}
	
	public void render(Screen screen) {
		int col = Color.get(0, 300, 300, 300);
		int coll = Color.get(0, 555, 555, 555);
		screen.clear(0);
		
		Font.draw("Loading...", screen, 110, screen.h - 105, col);
		//Font.draw("This should take 4 seconds or less", screen, 10, screen.h - 185, coll);
		//Font.draw("If not then restart because it froze", screen, 0, screen.h - 175, coll);
		Font.draw(percentage + "%", screen, centertext(percentage + "%"), screen.h - 85, col);
	}
}