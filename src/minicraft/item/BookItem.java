package minicraft.item;

import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.screen.BookMenu;

public class BookItem extends Item {
	private String[][] pages; // TODO this is not used yet; it could be, for editable books.
	
	public int getColor() {
		return Color.get(-1, 200, 531, 430);
	}

	public int getSprite() {
		return 14 + 4 * 32;
	}
	
	public void renderIcon(Screen screen, int x, int y) {
		screen.render(x, y, getSprite(), getColor(), 0);
	}
	
	public void renderInventory(Screen screen, int x, int y) {
		screen.render(x, y, getSprite(), getColor(), 0);
		Font.draw(getName(), screen, x + 8, y, Color.get(-1, 555));
	}
	
	public String getName() {
		return "Book";
	}
	
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		player.game.setMenu(new BookMenu());
		return true;
	}
}
