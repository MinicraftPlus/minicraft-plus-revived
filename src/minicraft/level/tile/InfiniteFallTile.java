package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.entity.Entity;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;

public class InfiniteFallTile extends Tile {
	
	protected InfiniteFallTile(String name) {
		super(name, (Sprite)null);
	}

	public void render(Screen screen, Level level, int x, int y) {}

	public void tick(Level level, int xt, int yt) {}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e instanceof AirWizard || e instanceof Player && ((Player) e).skinon || e instanceof Player && Game.isMode("creative");
	}
}
