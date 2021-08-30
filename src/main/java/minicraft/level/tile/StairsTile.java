package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.PowerGloveItem;
import minicraft.level.Level;

public class StairsTile extends Tile {
	private static Sprite down = new Sprite(21, 0, 2, 2, 1, 0);
	private static Sprite up = new Sprite(19, 0, 2, 2, 1, 0);
	
	protected StairsTile(String name, boolean leadsUp) {
		super(name, leadsUp?up:down);
		maySpawn = false;
	}
	
	@Override
	public void render(Screen screen, Level level, int x, int y) {
		sprite.render(screen, x * 16, y * 16, 0, DirtTile.dCol(level.depth));
	}

	@Override
	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		super.interact(level, xt, yt, player, item, attackDir);

		// Makes it so you can remove the stairs if you are in creative and debug mode.
		if (item instanceof PowerGloveItem && Game.isMode("Creative") && Game.debug) {
			level.setTile(xt, yt, Tiles.get("Grass"));
			Sound.monsterHurt.play();
			return true;
		} else {
			return false;
		}
	}
}
