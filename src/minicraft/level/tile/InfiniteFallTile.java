package minicraft.level.tile;

import minicraft.entity.AirWizard;
import minicraft.entity.Entity;
import minicraft.entity.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.gfx.ConnectorSprite;
import minicraft.level.Level;

public class InfiniteFallTile extends Tile {
	protected static void addInstances() {
		Tiles.add(new InfiniteFallTile("Infinite Fall"));
	}
	
	private InfiniteFallTile(String name) {
		super(name, (Sprite)null);
	}

	public void render(Screen screen, Level level, int x, int y) {}

	public void tick(Level level, int xt, int yt) {}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e instanceof AirWizard?true:e instanceof Player && Player.skinon;
	}
}
