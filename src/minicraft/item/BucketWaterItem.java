package minicraft.item;

import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.screen.ModeMenu;

public class BucketWaterItem extends Item {
	public int getColor() {
		return Color.get(-1, 222, 005, 555);
	}

	public int getSprite() {
		return 21 + 4 * 32;
	}

	public void renderIcon(Screen screen, int x, int y) {
		screen.render(x, y, getSprite(), getColor(), 0);
	}

	public void renderInventory(Screen screen, int x, int y) {
		screen.render(x, y, getSprite(), getColor(), 0);
		Font.draw(getName(), screen, x + 8, y, Color.get(-1, 555, 555, 555));
	}

	public String getName() {
		return "W.Bucket";
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if (tile == Tile.hole) {
			level.setTile(xt, yt, Tile.water, 0);
			Item item = ModeMenu.creative ? this : (new BucketItem());
			player.activeItem = item;
		}
		return true;
	}
}
