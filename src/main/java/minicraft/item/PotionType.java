package minicraft.item;

import minicraft.core.Game;
import minicraft.core.World;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.level.Level;

public enum PotionType {
	None (Color.get(1, 22, 22, 137), 0),
	
	Speed (Color.get(1, 23, 46, 23), 4200) {
		public boolean toggleEffect(Player player, boolean addEffect) {
			player.moveSpeed += (double)( addEffect ? 1 : (player.moveSpeed > 1 ? -1 : 0) );
			return true;
		}
	},
	
	Light (Color.get(1, 183, 183, 91), 6000),
	Swim (Color.get(1, 17, 17, 85), 4800),
	Energy (Color.get(1, 172, 80, 57), 8400),
	Regen (Color.get(1, 168, 54, 146), 1800),
	
	Health (Color.get(1, 161, 46, 69), 0) {
		public boolean toggleEffect(Player player, boolean addEffect) {
			if(addEffect) player.heal(5);
			return true;
		}
	},
	
	Time (Color.get(1, 102), 1800),
	Lava (Color.get(1, 129, 37, 37), 7200),
	Shield (Color.get(1, 65, 65, 157), 5400),
	Haste (Color.get(1, 106, 37, 106), 4800),
	
	Escape (Color.get(1, 85, 62, 62), 0) {
		public boolean toggleEffect(Player player, boolean addEffect) {
			if (addEffect) {
				int playerDepth = player.getLevel().depth;
				
				if (playerDepth == 0) {
					if (!Game.isValidServer()) {
						// player is in overworld
						String note = "You can't escape from here!";
						Game.notifications.add(note);
					}
					return false;
				}
				
				int depthDiff = playerDepth > 0 ? -1 : 1;
				
				World.scheduleLevelChange(depthDiff, () -> {
					Level plevel = World.levels[World.lvlIdx(playerDepth + depthDiff)];
					if (plevel != null && !plevel.getTile(player.x >> 4, player.y >> 4).mayPass(plevel, player.x >> 4, player.y >> 4, player))
						player.findStartPos(plevel, false);
				});
			}
			return true;
		}
	};
	
	public int dispColor, duration;
	public String name;
	
	PotionType(int col, int dur) {
		dispColor = col;
		duration = dur;
		if(this.toString().equals("None")) name = "Potion";
		else name = this + " Potion";
	}
	
	public boolean toggleEffect(Player player, boolean addEffect) {
		return duration > 0; // If you have no duration and do nothing, then you can't be used.
	}
	
	public boolean transmitEffect() {
		return true; // Any effect which could be duplicated and result poorly should not be sent to the server.
		// For the case of the Health potion, the player health is not transmitted separately until after the potion effect finishes, so having it send just gets the change there earlier.
	}
	
	public static final PotionType[] values = PotionType.values();
}
