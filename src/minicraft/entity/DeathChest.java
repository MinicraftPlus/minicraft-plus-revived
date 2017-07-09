package minicraft.entity;

import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.screen.OptionsMenu;

public class DeathChest extends Chest {
	
	public int time; // time passed (used for death chest despawn)
	int redtick = 0; // this is used to determine the shade of red when the chest is about to expire.
	boolean reverse; // what direction the red shade (redtick) is changing.
	
	public DeathChest() {
		super("Death Chest", Color.get(-1, 220, 331, 552));
		
		/// set the expiration time based on the world difficulty.
		if (OptionsMenu.diff == OptionsMenu.easy) {
			time = 18000;
		} else if (OptionsMenu.diff == OptionsMenu.norm) {
			time = 6000;
		} else if (OptionsMenu.diff == OptionsMenu.hard) {
			time = 1200;
		}
		/*
		if (canLight()) {
			col0 = Color.get(-1, 220, 331, 552);
			col1 = Color.get(-1, 220, 331, 552);
			col2 = Color.get(-1, 220, 331, 552);
			col3 = Color.get(-1, 220, 331, 552);
		} else {
			col0 = Color.get(-1, 110, 220, 441);
			col1 = Color.get(-1, 220, 331, 552);
			col2 = Color.get(-1, 110, 220, 441);
			col3 = Color.get(-1, 0, 110, 330);
		}
		*/
		//col = Color.get(-1, 220, 331, 552);
		//sprite = 1;
	}
	
	// for death chest time count, I imagine.
	public void tick() {
		super.tick();
		name = "Death Chest:" + time / Game.normSpeed + "S"; // add the current
		
		if (inventory.invSize() < 1) {
			remove();
		}

		if (time < 3600) { // if there is less than 3600 ticks left... (1 min @ 60tiks/sec)
			redtick += reverse ? -1 : 1; // inc/dec-rement redtick, changing the red shading.
			
			// set the chest color based on redtick's value
			int expcol = 100 * (redtick / 5 + 1);
			//if (redtick < 5) {
				col = Color.get(-1, expcol, expcol+100, expcol+200);
			//} else if (redtick < 10) {
				//col = Color.get(-1, 200, 300, 400);
			//} else {
				//col = Color.get(-1, 300, 400, 500);
			//}
			
			/// these two statements keep the red color oscillating.
			if (redtick > 13) {
				reverse = true;
			}
			if (redtick < 0) {
				reverse = false;
			}
		}

		if (time > 0) {
			time--; // decrement the time if it is not already zero.
		}

		if (time == 0) {
			remove(); // remove the death chest when the time expires.
		}
	}
	
	public void take(Player player) {} // can't grab a death chest.
}
