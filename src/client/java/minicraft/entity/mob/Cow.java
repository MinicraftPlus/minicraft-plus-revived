package minicraft.entity.mob;

import minicraft.core.io.Settings;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.item.Items;
import minicraft.level.tile.GrassTile;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class Cow extends PassiveMob {
	private static LinkedSprite[][] sprites = Mob.compileMobSpriteAnimations(0, 0, "cow");

	private boolean followingPlayer = false;

	/**
	 * Creates the cow with the right sprites and color.
	 */
	public Cow() {
		super(sprites, 5);
	}

	public void die() {
		int min = 0, max = 0;
		if (Settings.get("diff").equals("minicraft.settings.difficulty.easy")) {min = 1; max = 3;}
		if (Settings.get("diff").equals("minicraft.settings.difficulty.normal")) {min = 1; max = 2;}
		if (Settings.get("diff").equals("minicraft.settings.difficulty.hard")) {min = 0; max = 1;}

		dropItem(min, max, Items.get("leather"), Items.get("raw beef"));

		super.die();
	}

	@Override
	public void tick() {
		super.tick();
		if (random.nextInt(1000) == 0) { // Grazing without any benefits.
			Tile tile = level.getTile(x >> 4, y >> 4);
			// If tall grasses are present, these are consumed and then turn into grass tiles.
			if (tile instanceof GrassTile) {
				level.setTile(x >> 4, y >> 4, Tiles.get("dirt"));
			}
		}

		followingPlayer = false;
		if (AirWizard.beaten) { // When there is no curse
			Player player;
			double distance = 0;
			// If the player is close
			if ((player = level.getClosestPlayer(x, y)) != null && (distance = Math.hypot(x - player.x, y - player.y)) < 6 * 16) {
				// If the player is holding a wheat item
				if (player.activeItem != null && player.activeItem.getName().equalsIgnoreCase("Wheat")) {
					followingPlayer = true;
					xmov = 0; // Reset velocity
					ymov = 0;
				}
			}

			if (followingPlayer && distance > 1.5 * 16) {
				double theta = Math.atan2(player.y - y, player.x - x);
				int xMov = (int) (speed * 2.2 * Math.cos(theta));
				int yMov = (int) (speed * 2.2 * Math.sin(theta));
				move(xMov, yMov);
			}
		}
	}

	@Override
	protected boolean skipTick() {
		return !followingPlayer && super.skipTick(); // Skip tick if following a player
	}
}
