package minicraft.level.tile.entity;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.screen.SignDisplayMenu;
import minicraft.util.DamageSource;
import org.jetbrains.annotations.Nullable;

public class SignTileEntity extends Entity {
	public SignTileEntity() {
		super(8, 8);
	}

	@Override
	public void render(Screen screen) {}

	@Override
	public boolean isSolid() {
		return false;
	}

	@Override
	public boolean isAttackable(Entity source, @Nullable Item item, Direction attackDir) {
		return false;
	}

	@Override
	public boolean isAttackable(Tile source, Level level, int x, int y, Direction attackDir) {
		return false;
	}

	@Override
	public boolean isUsable() {
		return false;
	}

	@Override
	protected void handleDamage(DamageSource source, Direction attackDir, int damage) {}

	@Override
	public boolean hurt(DamageSource source, Direction attackDir, int damage) {
		return false;
	}

	@Override
	public void tick() {
		int xt = x >> 4, yt = y >> 4;
		if (Game.player.x >> 4 == xt && Game.player.y >> 4 == yt) {
			if (Renderer.signDisplayMenu == null || Renderer.signDisplayMenu.differsFrom(level.depth, xt, yt)) {
				Renderer.signDisplayMenu = new SignDisplayMenu(level, xt, yt);
			}
		} else {
			if (Renderer.signDisplayMenu != null && Renderer.signDisplayMenu.matches(level.depth, xt, yt)) {
				Renderer.signDisplayMenu = null;
			}
		}
	}
}
