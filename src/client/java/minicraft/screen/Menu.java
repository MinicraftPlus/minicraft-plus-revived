package minicraft.screen;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.gfx.Color;
import minicraft.gfx.Dimension;
import minicraft.gfx.Font;
import minicraft.gfx.Insets;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.ItemEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectableStringEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class Menu {

	private static final int LIMIT_TYPING_SEARCHER = 22;

	@NotNull
	private final ArrayList<MenuListEntry> entries = new ArrayList<>();

	private int spacing = 0;
	private Rectangle bounds = null;
	private Rectangle entryBounds = null;
	private Rectangle entryRenderingBounds = null;
	private RelPos entryPos = RelPos.CENTER; // the x part of this is re-applied per entry, while the y part is calculated once using the cumulative height of all entries and spacing.

	private @Nullable Localization.LocalizationString title = null;
	private int titleColor;
	private Point titleLoc = null; // standard point is anchor, with anchor.x + SpriteSheet.boxWidth
	private boolean drawVertically = false;

	private boolean hasFrame;
	private boolean renderOutOfFrame = false;

	private boolean selectable = false;
	private boolean renderSelectionLevel = false;
	boolean shouldRender = true;

	private int displayLength = 0;
	private int padding = 0;
	private boolean wrap = false;

	// menu selection vars
	private int selection = 0;
	private int dispSelection = 0;
	private int offset = 0;

	/**
	 * If there's searcher bar in menu
	 */
	private boolean useSearcherBar = false;
	private boolean searcherBarActive = false;
	private List<Integer> listSearcher;
	private int listPositionSearcher;
	private int selectionSearcher;
	/**
	 * The actual typed word in searcher bar
	 */
	private String typingSearcher;

	private LinkedSprite hudSheet = new LinkedSprite(SpriteType.Gui, "hud");

	private Menu() {
	}

	protected Menu(Menu m) {
		spacing = m.spacing;
		bounds = m.bounds == null ? null : new Rectangle(m.bounds);
		entryBounds = m.entryBounds == null ? null : new Rectangle(m.entryBounds);
		entryRenderingBounds = m.entryRenderingBounds == null ? null : new Rectangle(m.entryRenderingBounds);
		entryPos = m.entryPos;
		// Requires parameters before this.
		setEntries(m.entries.stream().map(e -> e.delegate).toArray(ListEntry[]::new));
		title = m.title;
		titleColor = m.titleColor;
		titleLoc = m.titleLoc;
		drawVertically = m.drawVertically;
		hasFrame = m.hasFrame;
		selectable = m.selectable;
		renderSelectionLevel = m.renderSelectionLevel;
		shouldRender = m.shouldRender;
		displayLength = m.displayLength;
		padding = m.padding;
		wrap = m.wrap;
		selection = m.selection;
		dispSelection = m.dispSelection;
		offset = m.offset;
		renderOutOfFrame = m.renderOutOfFrame;

		useSearcherBar = m.useSearcherBar;
		selectionSearcher = 0;
		listSearcher = new ArrayList<>();
		listPositionSearcher = 0;
		typingSearcher = "";
	}

	public void init() {

		if (entries.size() == 0) {
			selection = 0;
			dispSelection = 0;
			offset = 0;
			return;
		}

		selection = Math.min(selection, entries.size() - 1);
		selection = Math.max(0, selection);

		if (!entries.get(selection).delegate.isSelectable()) {
			int prevSel = selection;
			do {
				selection++;
				if (selection < 0) selection = entries.size() - 1;
				selection = selection % entries.size();
			} while (!entries.get(selection).delegate.isSelectable() && selection != prevSel);
		}

		dispSelection = selection;
		dispSelection = Math.min(dispSelection, displayLength - 1);
		dispSelection = Math.max(0, dispSelection);

		doScroll();
	}

	void setSelection(int idx) {
		if (idx >= entries.size())
			idx = entries.size() - 1;

		if (idx < 0) idx = 0;

		updateEntrySelection(idx);

		doScroll();
	}

	int getSelection() {
		return selection;
	}

	int getDispSelection() {
		return dispSelection;
	}

	ListEntry[] getEntries() {
		return entries.stream().map(a -> a.delegate).toArray(ListEntry[]::new);
	}
	protected void setEntries(ListEntry[] entries) {
		this.entries.clear();
		Arrays.stream(entries)
			.forEach(entry -> this.entries.add(new MenuListEntry(entry, entryPos)));
	}

	protected void setEntries(List<ListEntry> entries) {
		this.entries.clear();
		entries.forEach(entry -> this.entries.add(new MenuListEntry(entry, entryPos)));
	}

	@Nullable ListEntry getCurEntry() {
		return entries.size() == 0 ? null : entries.get(selection).delegate;
	}

	int getNumOptions() {
		return entries.size();
	}

	Rectangle getBounds() {
		return new Rectangle(bounds);
	}

	Localization.LocalizationString getTitle() {
		return title;
	}

	boolean isSelectable() {
		return selectable;
	}

	boolean shouldRender() {
		return shouldRender;
	}

	public boolean isSearcherBarActive() {
		return searcherBarActive;
	}

	/**
	 * @noinspection SameParameterValue
	 */
	void translate(int xoff, int yoff) {
		bounds.translate(xoff, yoff);
		entryBounds.translate(xoff, yoff);
		entryRenderingBounds.translate(xoff, yoff);
		titleLoc.translate(xoff, yoff);
	}

	private void updateEntrySelection(int newSel) {
		if (selection == newSel) return;
		if (selection >= 0 && selection < entries.size())
			entries.get(selection).resetRelativeAnchorsSynced(entryPos); // Reset old selectable entry position
		selection = newSel;
	}

	public void tick(InputHandler input) {
		if (!selectable) {
			if (!entries.isEmpty()) entries.get(selection).tick(input);
			return;
		} else if (entries.size() == 0) {
			return;
		}

		int prevSel = selection;
		if (input.getMappedKey("ALT").isDown() && !entries.get(selection).delegate.isScrollingTickerSet()) {
			if (input.inputPressed("cursor-left"))
				entries.get(selection).translateX(MinicraftImage.boxWidth, true);
			if (input.inputPressed("cursor-right"))
				entries.get(selection).translateX(-MinicraftImage.boxWidth, true);
		} else {
			if (input.inputPressed("cursor-up")) updateEntrySelection(selection - 1);
			if (input.inputPressed("cursor-down")) updateEntrySelection(selection + 1);
			if (input.getMappedKey("shift+cursor-up").isClicked() && selectionSearcher == 0) selectionSearcher -= 2;
			if (input.getMappedKey("shift+cursor-down").isClicked() && selectionSearcher == 0) selectionSearcher += 2;
			if (prevSel != selection && selectionSearcher != 0) updateEntrySelection(prevSel);
		}

		int newSel = selection;
		if (useSearcherBar) {
			if (input.getMappedKey("searcher-bar").isClicked()) {
				searcherBarActive = !searcherBarActive;
				input.addKeyTyped("", null); // clear pressed key
			}

			if (!listSearcher.isEmpty() && selectionSearcher == 0) {
				int speed = input.getMappedKey("PAGE-UP").isClicked() ? -1 : input.getMappedKey("PAGE-DOWN").isClicked() ? 1 : 0;
				if (speed != 0) {
					int listPosition = listPositionSearcher + speed;
					if (listPosition < 0) {
						listPosition = listSearcher.size() - 1;
					}
					listPositionSearcher = listPosition % listSearcher.size();
					int position = listSearcher.get(listPositionSearcher);

					int difference = position - selection;
					selectionSearcher = difference > position ? -difference : difference;
				}
			}

			if (searcherBarActive) {
				String typingSearcher = input.addKeyTyped(this.typingSearcher, null);
				input.maskInput(k -> !k.equals("ENTER"));

				// check if word was updated
				if (typingSearcher.length() <= Menu.LIMIT_TYPING_SEARCHER && typingSearcher.length() != this.typingSearcher.length()) {
					this.typingSearcher = typingSearcher;
					listSearcher.clear();
					listPositionSearcher = 0;

					Iterator<MenuListEntry> entryIt = entries.iterator();
					boolean shouldSelect = true;
					for (int i = 0; entryIt.hasNext(); i++) {
						ListEntry entry = entryIt.next().delegate;

						String stringEntry = entry.toString().toLowerCase(Locale.ENGLISH);
						String typingString = typingSearcher.toLowerCase(Locale.ENGLISH);

						if (stringEntry.contains(typingString)) {
							if (shouldSelect) {
								int difference = i - selection;
								selectionSearcher = difference > i ? -difference : difference;

								shouldSelect = false;
							}

							listSearcher.add(i);
						}
					}
				}
			}

			if (selectionSearcher != 0) {
				boolean downDirection = selectionSearcher > 0;
				selectionSearcher += downDirection ? -1 : 1;
				newSel += downDirection ? 1 : -1;
			}
		}

		int delta = newSel - prevSel;
		updateEntrySelection(prevSel);
		if (delta == 0) {
			entries.get(selection).tick(input); // only ticks the entry on a frame where the selection cursor has not moved.
			return;
		} else
			Sound.play("select");

		newSel = selection;
		do {
			newSel += delta;
			if (newSel < 0) newSel = entries.size() - 1;
			newSel = newSel % entries.size();
		} while (!entries.get(newSel).delegate.isSelectable() && newSel != prevSel);
		updateEntrySelection(newSel);

		// update offset and selection displayed
		dispSelection += selection - prevSel;

		if (dispSelection < 0) dispSelection = 0;
		if (dispSelection >= displayLength) dispSelection = displayLength - 1;

		doScroll();
	}

	private void doScroll() {
		// check if dispSelection is past padding point, and if so, bring it back in

		dispSelection = selection - offset;
		int offset = this.offset;

		// for scrolling up
		while ((dispSelection < padding || !wrap && offset + displayLength > entries.size()) && (wrap || offset > 0)) {
			offset--;
			dispSelection++;
		}

		// for scrolling down
		while ((displayLength - dispSelection <= padding || !wrap && offset < 0) && (wrap || offset + displayLength < entries.size())) {
			offset++;
			dispSelection--;
		}

		// only useful when wrap is true
		if (offset < 0) offset += entries.size();
		if (offset > 0) offset = offset % entries.size();

		this.offset = offset;
	}

	public void render(Screen screen) { render(screen, true); }
	public void render(Screen screen, boolean selected) {
		renderFrame(screen, selected);

		// render the title
		if (title != null) {
			String title = this.title.toString();
			int spriteX = !renderSelectionLevel || selected ? 3 : 7;
			if (drawVertically) {
				for (int i = 0; i < title.length(); i++) {
					if (hasFrame)
						screen.render(null, titleLoc.x, titleLoc.y + i * Font.textHeight(), spriteX, 6, 0, hudSheet.getSheet());
					Font.draw(title.substring(i, i + 1), screen, titleLoc.x, titleLoc.y + i * Font.textHeight(), titleColor);
				}
			} else {
				for (int i = 0; i < title.length(); i++) {
					if (hasFrame)
						screen.render(null, titleLoc.x + i * 8, titleLoc.y, spriteX, 6, 0, hudSheet.getSheet());
					Font.draw(title.substring(i, i + 1), screen, titleLoc.x + i * 8, titleLoc.y, titleColor);
				}
			}
		}

		// render searcher bar
		if (searcherBarActive && useSearcherBar) {
			int spaceWidth = Font.textWidth(" ");
			int leading = typingSearcher.length() * spaceWidth / 2;
			// int xSearcherBar = titleLoc.x + title.length() * spaceWidth / 3 - title.length() / 2;
			int xSearcherBar = titleLoc.x - 16;
			if (title != null) xSearcherBar += title.toString().length() * 8 / 2;

			if (xSearcherBar - leading < 0) {
				leading += xSearcherBar - leading;
			}

			for (int i = 0; i < typingSearcher.length() + 4; i++) {
				if (hasFrame) {
					screen.render(null, xSearcherBar + spaceWidth * i - leading, titleLoc.y - 8, 3, 6, 0, hudSheet.getSheet());
				}

				Font.draw(String.format("> %s <", typingSearcher), screen, xSearcherBar - leading, titleLoc.y - 8, typingSearcher.length() < Menu.LIMIT_TYPING_SEARCHER ? Color.YELLOW : Color.RED);
			}
		}

		// render the options
		int y = entryBounds.getTop();
		boolean special = wrap && entries.size() < displayLength;
		if (special) {
			int diff = displayLength - entries.size(); // we have to account for this many entry heights.
			int extra = diff * (ListEntry.getHeight() + spacing) / 2;
			y += extra;
		}
		for (int i = offset; i < (wrap ? offset + displayLength : Math.min(offset + displayLength, entries.size())); i++) {
			if (special && i - offset >= entries.size()) break;

			int idx = i % entries.size();
			MenuListEntry entry = entries.get(idx);
			if (!(entry.delegate instanceof BlankEntry) && entry.delegate.isVisible()) {
				if (searcherBarActive && useSearcherBar) {
					entry.render(screen, y, idx == selection, typingSearcher, Color.YELLOW);
				} else {
					entry.render(screen, y, idx == selection);
				}
			}

			y += ListEntry.getHeight() + spacing;
		}
	}

	void updateSelectedEntry(ListEntry newEntry) {
		updateEntry(selection, newEntry);
	}

	void updateEntry(int idx, ListEntry newEntry) {
		if (idx >= 0 && idx < entries.size())
			entries.set(idx, new MenuListEntry(newEntry, entryPos));
	}

	public void removeSelectedEntry() {
		entries.remove(selection);

		if (selection >= entries.size())
			updateEntrySelection(entries.size() - 1);
		if (selection < 0)
			updateEntrySelection(0);

		doScroll();
	}

	public void setColors(Menu model) {
		titleColor = model.titleColor;
	}

	private void renderFrame(Screen screen, boolean selected) {
		if (!hasFrame) return;

		int bottom = bounds.getBottom() - MinicraftImage.boxWidth;
		int right = bounds.getRight() - MinicraftImage.boxWidth;
		int xOffset = !renderSelectionLevel || selected ? 0 : 4;

		for (int y = bounds.getTop(); y <= bottom; y += MinicraftImage.boxWidth) { // loop through the height of the bounds
			for (int x = bounds.getLeft(); x <= right; x += MinicraftImage.boxWidth) { // loop through the width of the bounds

				boolean xend = x == bounds.getLeft() || x == right;
				boolean yend = y == bounds.getTop() || y == bottom;
				int spriteoffset = (xend && yend ? 0 : (yend ? 1 : (xend ? 2 : 3))) + xOffset; // determines which sprite to use
				int mirrors = (x == right ? 1 : 0) + (y == bottom ? 2 : 0); // gets mirroring

				screen.render(null, x, y, spriteoffset, 6, mirrors, hudSheet.getSheet());

				if (x < right && x + MinicraftImage.boxWidth > right)
					x = right - MinicraftImage.boxWidth;
			}

			if (y < bottom && y + MinicraftImage.boxWidth > bottom)
				y = bottom - MinicraftImage.boxWidth;
		}
	}

	private Builder builder = null;

	public Builder builder() {
		return builder;
	}

	/** Acts as an internal wrapping decorator of a {@link ListEntry} for the {@link Menu} environment. */
	private class MenuListEntry extends Screen.EntryRenderingUnit {
		private class MenuEntryLimitingModel extends EntryLimitingModel {}
		private class ItemMenuEntryLimitingModel extends MenuEntryLimitingModel {
			@Override
			public int getLeftBound() {
				return super.getLeftBound() + 2 * MinicraftImage.boxWidth;
			}
		}

		private class MenuEntryXAccessor extends EntryXAccessor {}
		private class ItemMenuEntryXAccessor extends MenuEntryXAccessor {
			@Override
			public int getLeftBound(RelPos anchor) {
				return anchor.xIndex *
					(getDelegate().getWidth() - getEntryBounds().getWidth() + 2 * MinicraftImage.boxWidth) / 2;
			}
		}

		public final MenuEntryLimitingModel limitingModel;
		public final MenuEntryXAccessor accessor;
		public final ListEntry delegate;

		public MenuListEntry(ListEntry delegate, @NotNull RelPos anchor) {
			super(anchor);
			this.delegate = delegate;
			resetRelativeAnchorsSynced(anchor);
			limitingModel = delegate instanceof ItemEntry ? new ItemMenuEntryLimitingModel() : new MenuEntryLimitingModel();
			accessor = delegate instanceof ItemEntry ? new ItemMenuEntryXAccessor() : new MenuEntryXAccessor();
		}

		@Override
		public void resetRelativeAnchorsSynced(RelPos newAnchor) {
			super.resetRelativeAnchorsSynced(newAnchor);
			if (delegate instanceof ItemEntry)
				xPos = 2 * MinicraftImage.boxWidth;
		}

		@Override
		protected EntryLimitingModel getLimitingModel() {
			return limitingModel;
		}

		@Override
		protected EntryXAccessor getXAccessor() {
			return accessor;
		}

		protected Rectangle getEntryBounds() {
			return delegate.isSelectable() ? entryBounds : entryRenderingBounds;
		}

		@Override
		protected ListEntry getDelegate() {
			return delegate;
		}

		public void translateX(int displacement, boolean limit) {
			xPos += displacement;
			if (limit) {
				int w = delegate.getWidth();
				int minX;
				int maxX;
				if (w <= getEntryBounds().getWidth()) {
					minX = (entryAnchor.xIndex * w - containerAnchor.xIndex * getEntryBounds().getWidth()) / 2;
					maxX = ((2 - containerAnchor.xIndex) * getEntryBounds().getWidth() - (2 - entryAnchor.xIndex) * w) / 2;
				} else {
					minX = ((2 - containerAnchor.xIndex) * getEntryBounds().getWidth() - (2 - entryAnchor.xIndex) * w) / 2;
					maxX = (entryAnchor.xIndex * w - containerAnchor.xIndex * getEntryBounds().getWidth()) / 2;
				}
				if (xPos < minX) xPos = minX;
				if (xPos > maxX) xPos = maxX;
			}
		}

		@Override
		public void tick(InputHandler input) {
			delegate.hook(() -> { // For input entry
				xPos = 0; // Resets position
				int diff = xPos + (containerAnchor.xIndex - 2) * getEntryBounds().getWidth() / 2 +
					(2 - entryAnchor.xIndex) * delegate.getWidth() / 2;
				if (diff > 0) translateX(-diff, false); // Shifts if and only if the entry exceeds the area.
			});
			super.tick(input);
		}

		@Override
		protected boolean renderOutOfFrame() {
			return renderOutOfFrame;
		}

		protected void renderExtra(Screen screen, int x, int y, int entryWidth, boolean selected) {
			if (delegate.isScrollingTickerSet()) {
				entryWidth = Math.min(entryWidth, getEntryBounds().getWidth());
				x = getEntryBounds().getLeft();
			}

			if (delegate instanceof ItemEntry)
				((ItemEntry) delegate).renderIcon(screen, x, y);
			if (selected && delegate.isSelectable()) {
				Font.draw(null, "> ", screen, x - 2 * MinicraftImage.boxWidth, y, ListEntry.COL_SLCT);
				Font.draw(null, " <", screen, x + entryWidth, y, ListEntry.COL_SLCT);
			}
		}
	}

	// This needs to be in the Menu class, to have access to the private constructor and fields.
	public static class Builder {

		private static final Point center = new Point(Screen.w / 2, Screen.h / 2);

		private Menu menu;

		private boolean setSelectable = false;
		private float padding = 1;

		@NotNull
		private RelPos titlePos = RelPos.TOP;
		private boolean fullTitleColor = false, setTitleColor = false;
		private int titleCol = Color.YELLOW;

		@NotNull
		private Point anchor = center;
		@NotNull
		private RelPos menuPos = RelPos.CENTER;
		private Dimension menuSize = null;

		private boolean searcherBar;

		public Builder(boolean hasFrame, int entrySpacing, RelPos entryPos, ListEntry... entries) {
			this(hasFrame, entrySpacing, entryPos, Arrays.asList(entries));
		}

		public Builder(boolean hasFrame, int entrySpacing, RelPos entryPos, List<ListEntry> entries) {
			menu = new Menu();
			menu.hasFrame = hasFrame;
			menu.spacing = entrySpacing;
			menu.entryPos = entryPos;
			setEntries(entries);
		}

		public Builder setEntries(ListEntry... entries) {
			return setEntries(Arrays.asList(entries));
		}

		public Builder setEntries(List<ListEntry> entries) {
			menu.setEntries(entries);
			return this;
		}

		public Builder setPositioning(Point anchor, RelPos menuPos) {
			this.anchor = anchor == null ? new Point() : anchor;
			this.menuPos = menuPos == null ? RelPos.BOTTOM_RIGHT : menuPos;
			return this;
		}

		public Builder setSize(int width, int height) {
			menuSize = new Dimension(width, height);
			return this;
		}

		public Builder setMenuSize(Dimension d) {
			menuSize = d;
			return this;
		} // can be used to set the size to null

		public Builder setBounds(Rectangle rect) {
			menuSize = rect.getSize();
			setPositioning(rect.getCenter(), RelPos.CENTER); // because the anchor represents the center of the rectangle.
			return this;
		}

		public Builder setDisplayLength(int numEntries) {
			menu.displayLength = numEntries;
			return this;
		}


		public Builder setTitlePos(RelPos rp) {
			titlePos = (rp == null ? RelPos.TOP : rp);
			return this;
		}

		public Builder setTitle(Localization.LocalizationString title) {
			menu.title = title;
			return this;
		}

		public Builder setTitle(Localization.LocalizationString title, int color) {
			return setTitle(title, color, false);
		}

		public Builder setTitle(Localization.LocalizationString title, int color, boolean fullColor) {
			menu.title = title;

			fullTitleColor = fullColor;
			setTitleColor = true;
			if (fullColor) // this means that the color is the full 4 parts, abcd. Otherwise, it is assumed it is only the main component, the one that matters.
				menu.titleColor = color;
			else
				titleCol = color;

			return this;
		}

		public Builder setFrame(boolean hasFrame) {
			menu.hasFrame = hasFrame;
			return this;
		}


		public Builder setScrollPolicies(float padding, boolean wrap) {
			this.padding = padding;
			menu.wrap = wrap;
			return this;
		}

		public Builder setShouldRender(boolean render) {
			menu.shouldRender = render;
			return this;
		}

		public Builder setSelectable(boolean selectable) {
			setSelectable = true;
			menu.selectable = selectable;
			return this;
		}

		public Builder setShouldRenderSelectionLevel(boolean renderSelectionLevel) {
			menu.renderSelectionLevel = renderSelectionLevel;
			return this;
		}

		public Builder setSelection(int sel) {
			menu.selection = sel;
			return this;
		}

		public Builder setSelection(int sel, int dispSel) {
			menu.selection = sel;
			menu.dispSelection = dispSel;
			return this;
		}

		public Builder setSearcherBar(boolean searcherBar) {
			this.searcherBar = searcherBar;

			return this;
		}

		public Menu createMenu() {
			// this way, I don't have to reference all the variables to a different var.
			return copy().createMenu(this);
		}

		private Menu createMenu(Builder b) {
			if (b == this)
				return copy().createMenu(this);

			// set default selectability
			if (!setSelectable) {
				for (MenuListEntry entry : menu.entries) {
					menu.selectable = menu.selectable || entry.delegate.isSelectable();
					if (menu.selectable)
						break;
				}
			}

			recalculateFrame();

			menu.useSearcherBar = searcherBar;

			// done setting defaults/values; return the new menu

			menu.builder = this;
			menu.init(); // any setup the menu does by itself right before being finished.
			return menu;
		}

		public void recalculateFrame() {
			// check the centering of the title, and find the dimensions of the title's display space.

			menu.drawVertically = titlePos == RelPos.LEFT || titlePos == RelPos.RIGHT;

			String title = menu.title == null ? "" : menu.title.toString();
			Dimension titleDim = menu.drawVertically ?
				new Dimension(Font.textHeight() * 2, Font.textWidth(title)) :
				new Dimension(Font.textWidth(title), Font.textHeight() * 2);

			// find the area used by the title and/or frame, that can't be used by the entries

			/* Create an Insets instance, and do the following...
			 * - if the menu is selectable, add 2 buffer spaces on the left and right, for the selection arrows.
			 * - if the menu has a frame, then add one buffer space to all 4 sides
			 * - if the menu has a title AND a frame, do nothing.
			 * - if the menu has a title and NO frame, add two spaces to whatever side the title is on
			 *
			 * Remember to set the title pos one space inside the left/right bounds, so it doesn't touch the frame corner.
			 *
			 * Starting with the entry size figured out, add the insets to get the total size.
			 * Starting with the menu size set, subtract the insets to get the entry size.
			 */

			Insets border;
			if (menu.hasFrame)
				border = new Insets(MinicraftImage.boxWidth); // add frame insets
			else {
				border = new Insets();

				// add title insets
				if (title.length() > 0 && titlePos != RelPos.CENTER) {
					RelPos c = titlePos;
					int space = MinicraftImage.boxWidth * 2;
					if (c.yIndex == 0)
						border.top = space;
					else if (c.yIndex == 2)
						border.bottom = space;
					else if (c.xIndex == 0) // must be center left
						border.left = space;
					else if (c.xIndex == 2) // must be center right
						border.right = space;
				}
			}

			if (menu.isSelectable()) {
				// add spacing for selection cursors
				border.left += MinicraftImage.boxWidth * 2;
				border.right += MinicraftImage.boxWidth * 2;
			}

			if (menu.wrap && menu.displayLength > 0)
				menu.displayLength = Math.min(menu.displayLength, menu.entries.size());

			// I have anchor and menu's relative position to it, and may or may not have size.
			Dimension entrySize;

			if (menuSize == null) {
				int width = titleDim.width;
				for (MenuListEntry entry : menu.entries) {
					int entryWidth = entry.delegate.getWidth();
					if (menu.isSelectable() && !entry.delegate.isSelectable())
						entryWidth = Math.max(0, entryWidth - MinicraftImage.boxWidth * 4);
					width = Math.max(width, entryWidth);
				}

				if (!menu.hasFrame && menu.entryPos.xIndex == 1) width = Screen.w; // Reduces troubles simply :)
				if (menu.displayLength > 0) { // has been set; use to determine entry bounds
					int height = (ListEntry.getHeight() + menu.spacing) * menu.displayLength - menu.spacing;

					entrySize = new Dimension(width, height);
				} else {
					// no set size; just keep going to the edges of the screen

					int maxHeight;
					if (menuPos.yIndex == 0) // anchor is lowest down coordinate (highest y value)
						maxHeight = anchor.y;
					else if (menuPos.yIndex == 2)
						maxHeight = Screen.h - anchor.y;
					else // is centered; take the lowest value of the other two, and double it
						maxHeight = Math.min(anchor.y, Screen.h - anchor.y) * 2;

					maxHeight -= border.top + border.bottom; // reserve border space

					int entryHeight = menu.spacing + ListEntry.getHeight();
					int totalHeight = entryHeight * menu.entries.size() - menu.spacing;
					maxHeight = ((maxHeight + menu.spacing) / entryHeight) * entryHeight - menu.spacing;

					entrySize = new Dimension(width, Math.min(maxHeight, totalHeight));
				}

				menuSize = border.addTo(entrySize);
			} else // menuSize was set manually
				entrySize = border.subtractFrom(menuSize);


			// set default max display length (needs size first)
			if (menu.displayLength <= 0 && menu.entries.size() > 0)
				menu.displayLength = (entrySize.height + menu.spacing) / (ListEntry.getHeight() + menu.spacing);

			// based on the menu centering, and the anchor, determine the upper-left point from which to draw the menu.
			menu.bounds = menuPos.positionRect(menuSize, anchor, new Rectangle()); // reset to a value that is actually useful to the menu

			menu.entryBounds = border.subtractFrom(menu.bounds);

			if (menu.isSelectable()) {
				// remove spacing for selection cursors
				border.left -= MinicraftImage.boxWidth * 2;
				border.right -= MinicraftImage.boxWidth * 2;
			}

			menu.entryRenderingBounds = border.subtractFrom(menu.bounds);

			menu.titleLoc = titlePos.positionRect(titleDim, menu.bounds);

			if (titlePos.xIndex == 0 && titlePos.yIndex != 1)
				menu.titleLoc.x += MinicraftImage.boxWidth;
			if (titlePos.xIndex == 2 && titlePos.yIndex != 1)
				menu.titleLoc.x -= MinicraftImage.boxWidth;

			// set the menu title color
			if (title.length() > 0) {
				if (fullTitleColor)
					menu.titleColor = titleCol;
				else {
					if (!setTitleColor) titleCol = menu.hasFrame ? Color.YELLOW : Color.SILVER;
					menu.titleColor = titleCol; // make it match the frame color, or be transparent
				}
			}

			if (padding < 0) padding = 0;
			if (padding > 1) padding = 1;
			menu.padding = (int) Math.floor(padding * menu.displayLength / 2);
		}

		// returns a new Builder instance, that can be further modified to create another menu.
		public Builder copy() {
			Builder b = new Builder(menu.hasFrame, menu.spacing, menu.entryPos);

			b.menu = new Menu(menu);

			b.anchor = anchor == null ? null : new Point(anchor);
			b.menuSize = menuSize == null ? null : new Dimension(menuSize);
			b.menuPos = menuPos;
			b.setSelectable = setSelectable;
			b.padding = padding;
			b.titlePos = titlePos;
			b.fullTitleColor = fullTitleColor;
			b.setTitleColor = setTitleColor;
			b.titleCol = titleCol;
			b.searcherBar = searcherBar;

			return b;
		}
	}

	public String toString() {
		return String.format("%s-Menu[%s]", title, bounds);
	}
}
