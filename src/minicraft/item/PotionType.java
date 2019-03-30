package minicraft.item;

import minicraft.core.Game;
import minicraft.core.World;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.RemotePlayer;

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

				if(playerDepth >= 0) {
					// player is in overworld
					// TODO: still consumes potion even on failure
					String note = "You can't escape from here!";
					if(!Game.isValidServer())
						Game.notifications.add(note); // add the notification telling you
					else if(player instanceof RemotePlayer)
						Game.server.getAssociatedThread((RemotePlayer)player).sendNotification(note, 0);
					return false;
				}

				World.scheduleLevelChange(1);
				player.findStartPos(Game.levels[playerDepth+4]);
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
		return true;
	}
	
	public static final PotionType[] values = PotionType.values();
}
