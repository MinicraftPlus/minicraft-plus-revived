package minicraft.item;

import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.screen.ModeMenu;

public class BucketItem extends Item {

	public int getColor() {
		return Color.get(-1, 222, 333, 555);
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
		return "Bucket";
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		Item item = this;
		if (tile == Tile.water) {
			level.setTile(xt, yt, Tile.hole, 0);
			item = (new BucketWaterItem());
		}
		if (tile == Tile.lava) {
			level.setTile(xt, yt, Tile.hole, 0);
			item = (new BucketLavaItem());
		}
		
		if(ModeMenu.creative) item = this;
		player.activeItem = item;
		
		return true;
	}
}
