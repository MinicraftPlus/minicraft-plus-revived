package minicraft.entity;

import minicraft.Settings;
import minicraft.Game;
import minicraft.gfx.Color;

public class DeathChest extends Chest {
	
	public int time; // time passed (used for death chest despawn)
	int redtick = 0; // this is used to determine the shade of red when the chest is about to expire.
	boolean reverse; // what direction the red shade (redtick) is changing.
	
	public DeathChest() {
		super("Death Chest", Color.get(-1, 220, 331, 552));
		
		/// set the expiration time based on the world difficulty.
		if (Settings.get("diff").equals("easy")) {
			time = 18000;
		} else if (Settings.get("diff").equals("norm")) {
			time = 6000;
		} else if (Settings.get("diff").equals("hard")) {
			time = 1200;
		}
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
			col = Color.get(-1, expcol, expcol+100, expcol+200);
			
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
	
	protected String getUpdateString() {
		String updates = super.getUpdateString() + ";";
		updates += "time,"+time;
		
		return updates;
	}
	
	protected boolean updateField(String field, String val) {
		if(super.updateField(field, val)) return true;
		switch(field) {
			case "time":
				time = Integer.parseInt(val);
				return true;
		}
		
		return false;
	}
}
