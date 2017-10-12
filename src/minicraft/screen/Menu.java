package minicraft.screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sun.istack.internal.NotNull;
import minicraft.InputHandler;
import minicraft.Sound;
import minicraft.gfx.Color;
import minicraft.gfx.Dimension;
import minicraft.gfx.Font;
import minicraft.gfx.Insets;
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.ListEntry;

public class Menu {
	
	private ArrayList<ListEntry> entries = new ArrayList<>();
	
	private int spacing = 0;
	private Rectangle bounds = null;
	private Rectangle entryBounds = null;
	private RelPos entryPos = RelPos.CENTER; // the x part of this is re-applied per entry, while the y part is calculated once using the cumulative height of all entries and spacing.
	
	private String title = "";
	private int titleColor;
	private Point titleLoc = null; // standard point is anchor, with anchor.x + SpriteSheet.boxWidth
	private boolean drawVertically = false;
	
	private boolean hasFrame;
	private int frameFillColor, frameEdgeColor;
	
	private boolean selectable = false;
	boolean shouldRender = true;
	
	private int displayLength = 0;
	private float padding = 0;
	private boolean wrap = false;
	
	// menu selection vars
	private int selection = 0;
	private int dispSelection = 0;
	private int offset = 0;
	
	
	private Menu() {}
	protected Menu(Menu m) {
		entries.addAll(m.entries);
		spacing = m.spacing;
		bounds = m.bounds == null ? null : new Rectangle(m.bounds);
		entryBounds = m.entryBounds == null ? null : new Rectangle(m.entryBounds);
		entryPos = m.entryPos;
		title = m.title;
		titleColor = m.titleColor;
		titleLoc = m.titleLoc;
		drawVertically = m.drawVertically;
		hasFrame = m.hasFrame;
		frameFillColor = m.frameFillColor;
		frameEdgeColor = m.frameEdgeColor;
		selectable = m.selectable;
		shouldRender = m.shouldRender;
		displayLength = m.displayLength;
		padding = m.padding;
		wrap = m.wrap;
		selection = m.selection;
		dispSelection = m.dispSelection;
		offset = m.offset;
	}
	
	public void init() {
		//recalcEntryPos();
		
		if(padding < 0) padding = 0;
		if(padding > 1) padding = 1;
		this.padding = (float)Math.ceil(padding * displayLength / 2);
		
		if(entries.size() == 0) {
			selection = 0;
			dispSelection = 0;
			offset = 0;
			return;
		}
		
		selection = Math.min(selection, entries.size()-1);
		selection = Math.max(0, selection);
		
		if(!entries.get(selection).isSelectable()) {
			int prevSel = selection;
			do {
				selection++;
				if (selection < 0) selection = entries.size() - 1;
				selection = selection % entries.size();
			} while (!entries.get(selection).isSelectable() && selection != prevSel);
		}
		
		dispSelection = selection;
		dispSelection = Math.min(dispSelection, displayLength-1);
		dispSelection = Math.max(0, dispSelection);
		
		doScroll();
	}
	
	void setSelection(int idx) {
		this.selection = idx;
		if(selection >= entries.size())
			selection = entries.size() - 1;
		else if(selection < 0)
			selection = 0;
	}
	int getSelection() { return selection; }
	int getDispSelection() { return dispSelection; }
	
	ListEntry getCurEntry() { return entries.get(selection); }
	int getNumOptions() { return entries.size(); }
	
	Rectangle getBounds() {
		return new Rectangle(bounds);
	}
	String getTitle() { return title; }
	
	boolean isSelectable() { return selectable; }
	boolean shouldRender() { return shouldRender; }
	
	@SuppressWarnings("SameParameterValue")
	void translate(int xoff, int yoff) {
		bounds.translate(xoff, yoff);
		entryBounds.translate(xoff, yoff);
	}
	
	public void tick(InputHandler input) {
		if(!selectable || entries.size() == 0) return;
		
		int prevSel = selection;
		if(input.getKey("up").clicked) selection--;
		if(input.getKey("down").clicked) selection++;
		
		int delta = selection - prevSel;
		selection = prevSel;
		if(delta == 0) {
			entries.get(selection).tick(input); // only ticks the entry on a frame where the selection cursor has not moved.
			return;
		} else
			Sound.select.play();
		
		do {
			selection += delta;
			if (selection < 0) selection = entries.size() - 1;
			selection = selection % entries.size();
		} while(!entries.get(selection).isSelectable() && selection != prevSel);
		
		// update offset and selection displayed
		dispSelection += selection - prevSel;
		
		if(dispSelection < 0) dispSelection = 0;
		if(dispSelection >= displayLength) dispSelection = displayLength - 1;
		
		doScroll();
	}
	
	//private boolean pressed(InputHandler input, String key) { return input.getKey(key).clicked; }
	
	private void doScroll() {
		// check if dispSelection is past padding point, and if so, bring it back in
		
		int offset = getSelection() - dispSelection;
		
		// for scrolling up
		while(dispSelection < padding && offset > 0) {
			offset--;
			dispSelection++;
		}
		
		// for scrolling down
		while(displayLength - dispSelection <= padding && offset + displayLength < this.entries.size()) {
			offset++;
			dispSelection--;
		}
		
		this.offset = offset;
	}
	
	public void render(Screen screen) {
		renderFrame(screen);
		
		// render the title
		if(title.length() > 0) {
			if (drawVertically) {
				for (int i = 0; i < title.length(); i++) {
					Font.draw(title.substring(i, i + 1), screen, titleLoc.x, titleLoc.y + i * Font.textHeight(), titleColor);
				}
			} else
				Font.draw(title, screen, titleLoc.x, titleLoc.y, titleColor);
		}
		
		// render the options
		int y = entryBounds.getTop();
		for(int i = offset; i < Math.min(offset+displayLength, entries.size()); i++) {
			ListEntry entry = entries.get(i);
			
			if(!(entry instanceof BlankEntry)) {
				Point pos = entryPos.positionRect(new Dimension(entry.getWidth(), ListEntry.getHeight()), new Rectangle(entryBounds.getLeft(), y, entryBounds.getWidth(), ListEntry.getHeight(), Rectangle.CORNER_DIMS));
				entry.render(screen, pos.x, pos.y, i == selection);
				if (i == selection && entry.isSelectable()) {
					// draw the arrows
					Font.draw("> ", screen, pos.x - Font.textWidth("> "), y, ListEntry.COL_SLCT);
					Font.draw(" <", screen, pos.x + entry.getWidth(), y, ListEntry.COL_SLCT);
				}
			}
			
			y += ListEntry.getHeight() + spacing;
		}
	}
	
	/*private void recalcEntryPos() {
		if(entries.length == 0) {
			lineY = 0;
			return;
		}
		int height = 0;
		for(ListEntry entry: entries)
			height += ListEntry.getHeight() + spacing;
		
		if(height > 0)
			height -= spacing;
		
		lineY = entryPos.positionRect(new Dimension(bounds.getWidth(), height), bounds).y;
	}*/
	
	
	public void updateSelectedEntry(ListEntry newEntry) {
		entries.set(selection, newEntry);
	}
	
	public void removeSelectedEntry() {
		entries.remove(selection);
		
		if(selection >= entries.size())
			selection = entries.size() - 1;
		else if(selection < 0)
			selection = 0;
		
		//recalcEntryPos();
	}
	
	
	
	private void renderFrame(Screen screen) {
		if(!hasFrame) return;
		
		int bottom = bounds.getBottom()-SpriteSheet.boxWidth;
		int right = bounds.getRight()-SpriteSheet.boxWidth;
		
		for (int y = bounds.getTop(); y <= bottom; y += SpriteSheet.boxWidth) { // loop through the height of the bounds
			for (int x = bounds.getLeft(); x <= right; x += SpriteSheet.boxWidth) { // loop through the width of the bounds
				
				boolean xend = x == bounds.getLeft() || x == right;
				boolean yend = y == bounds.getTop() || y == bottom;
				int spriteoffset = (xend && yend ? 0 : (yend ? 1 : 2)); // determines which sprite to use
				int mirrors = ( x == right ? 1 : 0 ) + ( y == bottom ? 2 : 0 ); // gets mirroring
				
				int color = xend || yend ? frameEdgeColor : frameFillColor;//sideColor; // gets the color; slightly different in upper right corner, and middle is all blue.
				
				screen.render(x, y, spriteoffset + 13*32, color, mirrors);
				
				if(x < right && x + SpriteSheet.boxWidth > right)
					x = right - SpriteSheet.boxWidth;
			}
			
			if(y < bottom && y + SpriteSheet.boxWidth > bottom)
				y = bottom - SpriteSheet.boxWidth;
		}
	}
	
	
	
	/// This needs to be in the Menu class, to have access to the private constructor and fields.
	
	public static class Builder {
		
		private static final Point center = new Point(Screen.w/2, Screen.h/2);
		
		private Menu menu;
		
		private boolean setSelectable = false;
		
		@NotNull private RelPos titlePos = RelPos.TOP;
		//@NotNull private RelPos titlePos = RelPos.LEFT;
		private boolean fullTitleColor = false, setTitleColor = false;
		private int titleCol = 550, frameFillCol = 5, frameEdgeStroke = 1, frameEdgeFill = 445;
		
		@NotNull private Point anchor = center;
		@NotNull private RelPos menuPos = RelPos.CENTER;
		private Dimension menuSize = null;
		
		public Builder(boolean hasFrame, int entrySpacing, RelPos entryPos, ListEntry... entries) { this(hasFrame, entrySpacing, entryPos, Arrays.asList(entries)); }
		public Builder(boolean hasFrame, int entrySpacing, RelPos entryPos, List<ListEntry> entries) {
			menu = new Menu();
			setEntries(entries);
			menu.hasFrame = hasFrame;
			menu.spacing = entrySpacing;
			menu.entryPos = entryPos;
		}
		
		public Builder setEntries(ListEntry... entries) { return setEntries(Arrays.asList(entries)); }
		public Builder setEntries(List<ListEntry> entries) {
			menu.entries.clear();
			menu.entries.addAll(entries);
			return this;
		}
		
		public Builder setPositioning(Point anchor, RelPos menuPos) {
			this.anchor = anchor == null ? new Point() : anchor;
			this.menuPos = menuPos == null ? RelPos.BOTTOM_RIGHT : menuPos;
			return this;
		}
		
		public Builder setSize(int width, int height) { menuSize = new Dimension(width, height); return this; }
		public Builder setMenuSize(Dimension d) { menuSize = d; return this; } // can be used to set the size to null
		
		public Builder setBounds(Rectangle rect) {
			menuSize = rect.getSize();
			setPositioning(rect.getCenter(), RelPos.CENTER); // because the anchor represents the center of the rectangle.
			return this;
		}
		
		public Builder setDisplayLength(int numEntries) { menu.displayLength = numEntries; return this; }
		
		
		public Builder setTitlePos(RelPos rp) { titlePos = (rp == null ? RelPos.TOP : rp); return this; }
		
		public Builder setTitle(String title) { menu.title = title; return this; }
		
		public Builder setTitle(String title, int color) { return setTitle(title, color, false); }
		public Builder setTitle(String title, int color, boolean fullColor) {
			menu.title = title;
			
			fullTitleColor = fullColor;
			setTitleColor = true;
			if(fullColor) // this means that the color is the full 4 parts, abcd. Otherwise, it is assumed it is only the main component, the one that matters.
				menu.titleColor = color;
			else
				titleCol = color;
			
			return this;
		}
		
		public Builder setFrame(boolean hasFrame) { menu.hasFrame = hasFrame; return this; }
		
		public Builder setFrame(int fillCol, int edgeStroke, int edgeFill) {
			setFrame(true);
			// these are not full colors, only the components that matter.
			frameFillCol = fillCol;
			frameEdgeStroke = edgeStroke;
			frameEdgeFill = edgeFill;
			
			return this;
		}
		
		public Builder setScrollPolicies(float padding, boolean wrap) {
			menu.padding = padding;
			menu.wrap = wrap;
			return this;
		}
		
		public Builder setShouldRender(boolean render) { menu.shouldRender = render; return this; }
		
		public Builder setSelectable(boolean selectable) {
			setSelectable = true;
			menu.selectable = selectable;
			return this;
		}
		
		public Builder setSelection(int sel) { menu.selection = sel; return this; }
		public Builder setSelection(int sel, int dispSel) {
			menu.selection = sel;
			menu.dispSelection = dispSel;
			return this;
		}
		
		public Menu createMenu() {
			// this way, I don't have to reference all the variables to a different var.
			return copy().createMenu(this);
		}
		private Menu createMenu(Builder b) {
			if(b == this)
				return copy().createMenu(this);
			
			// set default selectability
			if(!setSelectable) {
				for(ListEntry entry: menu.entries) {
					menu.selectable = menu.selectable || entry.isSelectable();
					if(menu.selectable)
						break;
				}
			}
			
			// check the centering of the title, and find the dimensions of the title's display space.
			
			menu.drawVertically = titlePos == RelPos.LEFT || titlePos == RelPos.RIGHT;
			
			Dimension titleDim = menu.drawVertically ?
				new Dimension(Font.textHeight()*2, Font.textWidth(menu.title)) :
				new Dimension(Font.textWidth(menu.title), Font.textHeight()*2);
			
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
			if(menu.hasFrame)
				border = new Insets(SpriteSheet.boxWidth); // add frame insets
			else {
				border = new Insets();
				
				// add title insets
				if (menu.title.length() > 0 && titlePos != RelPos.CENTER) {
					RelPos c = titlePos;
					int space = SpriteSheet.boxWidth * 2;
					//noinspection SuspiciousNameCombination
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
			
			if(menu.isSelectable()) {
				// add spacing for selection cursors
				border.left += SpriteSheet.boxWidth * 2;
				border.right += SpriteSheet.boxWidth * 2;
			}
			
			// I have anchor and menu's relative position to it, and may or may not have size.
			Dimension entrySize;
			
			if(menuSize == null && menu.entries.size() == 0) {
				menuSize = new Dimension(border.left + border.right, border.top + border.bottom);
				entrySize = new Dimension();
			} else if(menuSize == null) {
				int width = 0;
				for(ListEntry entry: menu.entries) {
					int entryWidth = entry.getWidth();
					if(menu.isSelectable() && !entry.isSelectable())
						entryWidth = Math.max(0, entryWidth - SpriteSheet.boxWidth * 4);
					width = Math.max(width, entryWidth);
				}
				
				if(menu.displayLength > 0) { // has been set; use to determine entry bounds
					int height = (ListEntry.getHeight() + menu.spacing) * menu.displayLength - menu.spacing;
					
					entrySize = new Dimension(width, height);
				}
				else {
					// no set size; just keep going to the edges of the screen
					
					int maxHeight;
					if(menuPos.yIndex == 0) // anchor is lowest down coordinate (highest y value)
						maxHeight = anchor.y;
					else if(menuPos.yIndex == 2)
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
			}
			else // menuSize was set manually
				entrySize = border.subtractFrom(menuSize);
			
			
			// set default max display length (needs size first)
			if(menu.displayLength <= 0 && menu.entries.size() > 0)
				menu.displayLength = (entrySize.height + menu.spacing) / (ListEntry.getHeight() + menu.spacing);
				
			// based on the menu centering, and the anchor, determine the upper-left point from which to draw the menu.
			menu.bounds = new Rectangle(menuPos.positionRect(menuSize, anchor), menuSize); // reset to a value that is actually useful to the menu
			
			menu.entryBounds = border.subtractFrom(menu.bounds);
			
			menu.titleLoc = titlePos.positionRect(titleDim, menu.bounds);
			
			if(titlePos.xIndex == 0 && titlePos.yIndex != 1)
				menu.titleLoc.x += SpriteSheet.boxWidth;
			if(titlePos.xIndex == 2 && titlePos.yIndex != 1)
				menu.titleLoc.x -= SpriteSheet.boxWidth;
			
			// set the menu title color
			if(menu.title.length() > 0) {
				if(fullTitleColor)
					menu.titleColor = titleCol;
				else {
					if (!setTitleColor) titleCol = menu.hasFrame ? 550 : 555;
					menu.titleColor = Color.get((menu.hasFrame ? frameFillCol : -1), titleCol); // make it match the frame color, or be transparent
				}
			}
			
			// set the menu frame colors
			if(menu.hasFrame) {
				menu.frameFillColor = Color.get(frameFillCol, frameFillCol);
				menu.frameEdgeColor = Color.get(-1, frameEdgeStroke, frameFillCol, frameEdgeFill);
			}
			
			// done setting defaults/values; return the new menu 
			
			menu.init(); // any setup the menu does by itself right before being finished.
			return menu;
		}
		
		// returns a new Builder instance, that can be further modified to create another menu.
		private Builder copy() {
			Builder b = new Builder(menu.hasFrame, menu.spacing, menu.entryPos, menu.entries);
			
			b.menu = new Menu(menu);
			
			b.anchor = anchor == null ? null : new Point(anchor);
			b.menuSize = menuSize == null ? null : new Dimension(menuSize);
			b.menuPos = menuPos;
			b.setSelectable = setSelectable;
			b.titlePos = titlePos;
			b.fullTitleColor = fullTitleColor;
			b.setTitleColor = setTitleColor;
			b.titleCol = titleCol;
			b.frameFillCol = frameFillCol;
			b.frameEdgeStroke = frameEdgeStroke;
			b.frameEdgeFill = frameEdgeFill;
			
			return b;
		}
	}
}
