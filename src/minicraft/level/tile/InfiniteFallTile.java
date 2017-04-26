package minicraft.level.tile;

import minicraft.entity.AirWizard;
import minicraft.entity.Entity;
import minicraft.entity.Player;
import minicraft.gfx.Screen;
import minicraft.level.Level;

public class InfiniteFallTile extends Tile {
	public InfiniteFallTile(int id) {
		super(id);
	}

	public void render(Screen screen, Level level, int x, int y) {}

	public void tick(Level level, int xt, int yt) {}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e instanceof AirWizard?true:e instanceof Player && Player.skinon;
	}
}
