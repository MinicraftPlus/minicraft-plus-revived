package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.entity.Arrow;
import minicraft.entity.Entity;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.item.Item;
import minicraft.level.Level;
import org.jetbrains.annotations.Nullable;

public class InfiniteFallTile extends Tile {

	protected InfiniteFallTile(String name) {
		super(name, (SpriteAnimation) null);
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
	}

	@Override
	public boolean tick(Level level, int xt, int yt) {
		return false;
	}

	@Override
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e instanceof AirWizard || e instanceof Arrow || e instanceof Player && Game.isMode("minicraft.settings.mode.creative");
	}

	@Override
	protected void handleDamage(Level level, int x, int y, Entity source, @Nullable Item item, int dmg) {}
}
