package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.furniture.Furniture;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSpriteSheet;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.PowerGloveItem;
import minicraft.level.Level;

public class StairsTile extends Tile {
	private static LinkedSpriteSheet down = new LinkedSpriteSheet(SpriteType.Tile, "stairs_down");
	private static LinkedSpriteSheet up = new LinkedSpriteSheet(SpriteType.Tile, "stairs_up");

	protected StairsTile(String name, boolean leadsUp) {
		super(name, leadsUp ? up : down);
		maySpawn = false;
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		sprite.getSprite().render(screen, x * 16, y * 16, 0, DirtTile.dCol(level.depth));
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return !(e instanceof Furniture);
	}

	@Override
	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		super.interact(level, xt, yt, player, item, attackDir);

		// Makes it so you can remove the stairs if you are in creative and debug mode.
		if (item instanceof PowerGloveItem && Game.isMode("minicraft.settings.mode.creative") && Game.debug) {
			level.setTile(xt, yt, Tiles.get("Grass"));
			Sound.monsterHurt.play();
			return true;
		} else {
			return false;
		}
	}
}
