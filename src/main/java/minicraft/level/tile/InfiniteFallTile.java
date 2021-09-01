package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.entity.Arrow;
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

	public boolean tick(Level level, int xt, int yt) { return false; }

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e instanceof AirWizard || e instanceof Arrow || e instanceof Player && ( ((Player) e).suitOn || Game.isMode("creative") );
	}
}
