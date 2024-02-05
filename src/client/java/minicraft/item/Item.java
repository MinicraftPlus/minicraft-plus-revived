package minicraft.item;

import minicraft.core.io.Localization;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.StringEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Item {

	/* Note: Most of the stuff in the class is expanded upon in StackableItem/PowerGloveItem/FurnitureItem/etc */

	private final String name;
	public LinkedSprite sprite;

	public boolean used_pending = false; // This is for multiplayer, when an item has been used, and is pending server response as to the outcome, this is set to true so it cannot be used again unless the server responds that the item wasn't used. Which should basically replace the item anyway, soo... yeah. this never gets set back.

	protected Item(String name) {
		sprite = SpriteLinker.missingTexture(SpriteType.Item);
		this.name = name;
	}

	protected Item(String name, LinkedSprite sprite) {
		this.name = name;
		this.sprite = sprite;
	}

	/**
	 * Renders an item on the HUD
	 */
	public void renderHUD(Screen screen, int x, int y, int fontColor) {
		String dispName = getDisplayName();
		screen.render(x, y, sprite);
		Font.drawBackground(" " + dispName, screen, x + 8, y, fontColor);
	}

	/**
	 * Determines what happens when the player interacts with a tile
	 */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		return false;
	}

	/**
	 * Returning true causes this item to be removed from the player's active item slot
	 */
	public boolean isDepleted() {
		return false;
	}

	/**
	 * Returns if the item can attack mobs or not
	 */
	public boolean canAttack() {
		return false;
	}

	/**
	 * Sees if an item equals another item
	 */
	public boolean equals(Item item) {
		return item != null && item.getClass().equals(getClass()) && item.name.equals(name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * This returns a copy of this item, in all necessary detail.
	 */
	@NotNull
	public abstract Item copy();

	@Override
	public String toString() {
		return name + "-Item";
	}

	/**
	 * Gets the necessary data to send over a connection. This data should always be directly input-able into Items.get() to create a valid item with the given properties.
	 */
	public String getData() {
		return name;
	}

	/**
	 * Gets the description used for display item information.
	 */
	public ItemDescription getDescription() {
		return new ItemDescription.Builder(Localization.getLocalized(getName())).create();
	}

	public static class ItemDescription {
		private final String displayName;
		private final List<String> description;
		private final List<String> attributes;

		private ItemDescription(String displayName, List<String> description, List<String> attributes) {
			this.displayName = displayName;
			this.description = description;
			this.attributes = attributes;
		}

		public static class Builder {
			private final String displayName;
			private final ArrayList<String> description = new ArrayList<>();
			private final ArrayList<String> attributes = new ArrayList<>();

			public Builder(String displayName) {
				this.displayName = displayName;
			}

			public Builder appendDescription(String line) {
				description.add(line);
				return this;
			}

			public Builder appendAttribute(String line) {
				attributes.add(line);
				return this;
			}

			public ItemDescription create() {
				return new ItemDescription(displayName, description, attributes);
			}
		}

		public List<ListEntry> toEntries() { return toEntries(null); }
		public List<ListEntry> toEntries(@Nullable List<String> lore) {
			ArrayList<ListEntry> entries = new ArrayList<>();
			entries.add(new StringEntry(new Localization.LocalizationString(displayName)));
			for (String l : description)
				entries.add(new StringEntry(new Localization.LocalizationString(false, l), Color.LIGHT_GRAY));
			if (lore != null) for (String l : lore)
				entries.add(new StringEntry(new Localization.LocalizationString(false, l), Color.GRAY));
			for (String l : attributes)
				entries.add(new StringEntry(new Localization.LocalizationString(l), Color.WHITE));
			return entries;
		}
	}

	public final String getName() {
		return name;
	}

	// Returns the String that should be used to display this item in a menu or list.
	public String getDisplayName() { // instant localization
		return Localization.getLocalized(getName());
	}

	public boolean interactsWithWorld() {
		return true;
	}
}
