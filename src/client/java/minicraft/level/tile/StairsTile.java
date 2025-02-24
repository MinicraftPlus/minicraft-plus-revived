package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Entity;
import minicraft.entity.furniture.Furniture;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.level.Level;
import org.jetbrains.annotations.Nullable;

public class StairsTile extends Tile {
	private static SpriteAnimation down = new SpriteAnimation(SpriteType.Tile, "stairs_down");
	private static SpriteAnimation up = new SpriteAnimation(SpriteType.Tile, "stairs_up");

	protected StairsTile(String name, boolean leadsUp) {
		super(name, leadsUp ? up : down);
		maySpawn = false;
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		if (level.depth == 1)
			Tiles.get("cloud").render(screen, level, x, y);
		else
			Tiles.get("dirt").render(screen, level, x, y);
		sprite.render(screen, level, x, y);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return !(e instanceof Furniture);
	}

	@Override
	protected void handleDamage(Level level, int x, int y, Entity source, @Nullable Item item, int dmg) {}

	@Override
	public @Nullable Item take(Level level, int x, int y, Player player) {
		// Makes it so you can remove the stairs if you are in creative and debug mode.
		if (Game.isMode("minicraft.settings.mode.creative")) {
			level.setTile(x, y, Tiles.get("Grass"));
			Sound.play("monsterhurt");
		}
		return null;
	}
}
