package minicraft.item;

import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.screen.EditableBookDisplay;
import minicraft.util.BookData.EditableBookData;

public class EditableBookItem extends Item {

	private EditableBookData book;

	public EditableBookItem(EditableBookData book, Sprite sprite) {
		super("Editable Book", sprite);
		this.book = book;
		this.sprite = sprite;
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		Game.setDisplay(new EditableBookDisplay(book));
		return true;
	}

	@Override
	public String getData() {
		return super.getData() + ("_" + book.title + "\0" + book.content);
	}

	@Override
	public String getDescription() {
		return super.getDescription() + ("\n" + book.title);
	}

	public EditableBookItem clone() {
		return new EditableBookItem(book, sprite);
	}
}