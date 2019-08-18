package minicraft.item;

import minicraft.core.Game;
import minicraft.core.World;
import minicraft.entity.mob.Player;
import minicraft.level.Level;

public enum PotionType {
	None (5, 0),
	
	Speed (10, 4200) {
		public boolean toggleEffect(Player player, boolean addEffect) {
			player.moveSpeed += (double)( addEffect ? 1 : (player.moveSpeed > 1 ? -1 : 0) );
			return true;
		}
	},
	
	Light (440, 6000),
	Swim (3, 4800),
	Energy (510, 8400),
	Regen (504, 1800),
	Health (501, 0) {
		public boolean toggleEffect(Player player, boolean addEffect) {
			if(addEffect) player.heal(5);
			return true;
		}
	},
	
	Time (222, 1800),
	Lava (400, 7200),
	Shield (115, 5400),
	Haste (303, 4800),
	
	Escape (211, 0) {
		public boolean toggleEffect(Player player, boolean addEffect) {
			if(addEffect) {
				int playerDepth = player.getLevel().depth;
				
				if(playerDepth == 0) {
					if(!Game.isValidServer()) {
						// player is in overworld
						String note = "You can't escape from here!";
						Game.notifications.add(note);
					}
					return false;
				}
				
				int depthDiff = playerDepth > 0 ? -1 : 1;
				
				World.scheduleLevelChange(depthDiff, () -> {
					Level plevel = World.levels[World.lvlIdx(playerDepth + depthDiff)];
					if(plevel != null && !plevel.getTile(player.x >> 4, player.y >> 4).mayPass(plevel, player.x >> 4, player.y >> 4, player))
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
		return duration > 0; // if you have no duration and do nothing, then you can't be used.
	}
	
	public boolean transmitEffect() {
		return true; // any effect which could be duplicated and result poorly should not be sent to the server.
		// for the case of the Health potion, the player health is not transmitted separately until after the potion effect finishes, so having it send just gets the change there earlier.
	}
	
	public static final PotionType[] values = PotionType.values();
}
