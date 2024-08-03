package minicraft.item;

import minicraft.core.Game;
import minicraft.core.World;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.level.Level;

public enum PotionType {
	Awkward(Color.get(1, 41, 51, 255), 0),

	Speed(Color.get(1, 105, 209, 105), 4200) {
		public boolean toggleEffect(Player player, boolean addEffect) {
			player.moveSpeed += (double) (addEffect ? 1 : (player.moveSpeed > 1 ? -1 : 0));
			return true;
		}
	},

	Light(Color.get(1, 183, 183, 91), 6000),
	Swim(Color.get(1, 51, 51, 255), 4800),
	Energy(Color.get(1, 237, 110, 78), 8400),
	Regen(Color.get(1, 219, 70, 189), 1800),

	Health(Color.get(1, 194, 56, 84), 0) {
		public boolean toggleEffect(Player player, boolean addEffect) {
			if (addEffect) player.heal(5);
			return true;
		}
	},

	Time(Color.get(1, 163), 1800),
	Lava(Color.get(1, 199, 58, 58), 7200),
	Shield(Color.get(1, 84, 84, 204), 5400),
	Haste(Color.get(1, 201, 71, 201), 4800),

	Escape(Color.get(1, 222, 162, 162), 0) {
		public boolean toggleEffect(Player player, boolean addEffect) {
			if (addEffect) {
				int playerDepth = player.getLevel().depth;

				if (playerDepth == 0) {
					// player is in overworld
					Game.notifications.add("You can't escape from here!");
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
		name = this + " Potion";
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
