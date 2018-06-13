package minicraft.gfx;

import minicraft.core.Updater;

public class Ellipses {
	private int ePos = 0;
	private int eposTick = 0;
	
	public String updateAndGet() {
		StringBuilder dots = new StringBuilder();
		for(int i = 0; i < 3; i++) {
			if (ePos == i)
				dots.append(".");
			else
				dots.append(" ");
		}
		
		eposTick++;
		if(eposTick >= Updater.normSpeed) {
			eposTick = 0;
			ePos++;
		}
		if(ePos >= 3) ePos = 0;
		
		return dots.toString();
	}
}
