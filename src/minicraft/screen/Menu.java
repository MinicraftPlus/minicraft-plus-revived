package minicraft.screen;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import minicraft.InputHandler;
import minicraft.Sound;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;
import minicraft.screen.entry.ListEntry;

public class Menu {
	
	private ArrayList<ListEntry> entries = new ArrayList<>();
	
	private int spacing = 0;
	private Rectangle bounds = null;
	private RelPos linePos = RelPos.CENTER;
	private int lineY; // don't need to copy
	
	private String title = "";
	private int titleColor;
	private Point titlePos = null; // standard point is anchor, with anchor.x + SpriteSheet.boxWidth
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
	private Menu(Menu m) {
		entries.addAll(m.entries);
		spacing = m.spacing;
		bounds = m.bounds == null ? null : new Rectangle(m.bounds);
		linePos = m.linePos;
		title = m.title;
		titleColor = m.titleColor;
		titlePos = m.titlePos;
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
		recalcEntryPos();
		
		if(padding < 0) padding = 0;
		if(padding > 1) padding = 1;
		this.padding = Math.round(padding * displayLength / 2);
		
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
		
		dispSelection = Math.min(dispSelection, displayLength-1);
		dispSelection = Math.max(0, dispSelection);
		
		offset = selection - dispSelection;
		doScroll();
	}
	
	public int getSelection() { return selection; }
	public int getDispSelection() { return dispSelection; }
	
	public boolean isSelectable() { return selectable; }
	public boolean shouldRender() { return shouldRender; }
	
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
		dispSelection += delta;
		
		if(dispSelection < 0) dispSelection = 0;
		dispSelection = dispSelection % displayLength;
		
		doScroll();
	}
	
	private void doScroll() {
		// check if dispSelection is past padding point, and if so, bring it back in
		
		// for scrolling up
		while(dispSelection < padding && offset > 0) {
			offset--;
			dispSelection++;
		}
		
		// for scrolling down
		while(displayLength - dispSelection < padding && offset + displayLength < this.entries.size()) {
			offset++;
			dispSelection--;
		}
	}
	
	public void render(Screen screen) {
		renderFrame(screen);
		
		// render the title
		if(title.length() > 0) {
			if (drawVertically) {
				for (int i = 0; i < title.length(); i++) {
					Font.draw(title.substring(i, i + 1), screen, titlePos.x, titlePos.y + i * Font.textHeight(), titleColor);
				}
			} else
				Font.draw(title, screen, titlePos.x, titlePos.y, titleColor);
		}
		
		// render the options
		int y = lineY;
		for(int i = offset; i < Math.min(offset+displayLength, entries.size()); i++) {
			ListEntry entry = entries.get(i);
			// TODO this isn't working
			int lineX = linePos.positionSubRect(new Dimension(entry.getWidth(), entry.getHeight()), new Dimension(bounds.getWidth(), entry.getHeight()), new Point(bounds.getCenter().x, y+entry.getHeight()/2)).x;
			entry.render(screen, lineX, y, i == selection);
			if(i == selection) {
				// draw the arrows
				Font.draw("> ", screen, lineX-Font.textWidth("> "), y, ListEntry.COL_SLCT);
				Font.draw(" <", screen, lineX+entry.getWidth(), y, ListEntry.COL_SLCT);
			}
			
			y += entry.getHeight() + spacing;
		}
	}
	
	private void recalcEntryPos() {
		/*if(entries.length == 0) {
			lineY = 0;
			return;
		}*/
		
		lineY = linePos.getRect(new Dimension(bounds.getWidth(), bounds.getHeight()), bounds.getCenter()).getTop();
	}
	
	
	public void updateSelectedEntry(ListEntry newEntry) {
		entries.set(selection, newEntry);
	}
	
	public void removeSelectedEntry() {
		entries.remove(selection);
		
		if(selection >= entries.size())
			selection = entries.size() - 1;
		else if(selection < 0)
			selection = 0;
		
		recalcEntryPos();
	}
	
	
	
	private void renderFrame(Screen screen) {
		if(!hasFrame) return;
		
		for (int y = bounds.getTop(); y <= bounds.getBottom(); y += SpriteSheet.boxWidth) { // loop through the height of the bounds
			for (int x = bounds.getLeft(); x <= bounds.getRight(); x += SpriteSheet.boxWidth) { // loop through the width of the bounds
				
				boolean xend = x == bounds.getLeft() || x == bounds.getRight();
				boolean yend = y == bounds.getTop() || y == bounds.getBottom();
				int spriteoffset = (xend && yend ? 0 : (yend ? 1 : 2)); // determines which sprite to use
				int mirrors = ( x == bounds.getRight() ? 1 : 0 ) + ( y == bounds.getBottom() ? 2 : 0 ); // gets mirroring
				
				int color = xend || yend ? frameEdgeColor : frameFillColor;//sideColor; // gets the color; slightly different in upper right corner, and middle is all blue.
				
				screen.render(x, y, spriteoffset + 13*32, color, mirrors);
			}
		}
	}
	
	
	
	/// This needs to be in the Menu class, to have access to the private constructor and fields.
	
	public static class Builder {
		
		private Menu menu;
		
		private RelPos menuCentering = RelPos.CENTER;
		private boolean setSelectable = false;
		
		private boolean fullTitleColor = false, setTitleColor = false;
		private RelPos titleCentering = RelPos.TOP;
		private int titleCol = 550, frameFillCol = 5, frameEdgeStroke = 1, frameEdgeFill = 445;
		
		private Dimension size = null;
		private Point anchor = new Point(Screen.w/2, Screen.h/2);
		
		public Builder(boolean hasFrame, int entrySpacing, ListEntry... entries) { this(hasFrame, entrySpacing, Arrays.asList(entries)); }
		public Builder(boolean hasFrame, int entrySpacing, List<ListEntry> entries) {
			menu = new Menu();
			setEntries(entries);
			menu.hasFrame = hasFrame;
			menu.spacing = entrySpacing;
		}
		
		public Builder setEntries(ListEntry... entries) { return setEntries(Arrays.asList(entries)); }
		public Builder setEntries(List<ListEntry> entries) {
			menu.entries.clear();
			menu.entries.addAll(entries);
			return this;
		}
		
		public Builder setSpacing(int spacing) { menu.spacing = spacing; return this; }
		
		public Builder setTitle(String title) {
			boolean setColor = setTitleColor;
			setTitle(title, 555, false);
			setTitleColor = setColor;
			return this;
		}
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
		
		public Builder setTitlePos(RelPos centering) {
			menu.titlePos = null;
			titleCentering = centering;
			return this;
		}
		public Builder setTitlePos(Point pos) {
			menu.titlePos = pos;
			return this;
		}
		
		public Builder setAnchor(int x, int y) { anchor = new Point(x, y); return this; }
		public Builder setAnchor(Point p) { anchor = p; return this; }
		
		public Builder setCentering(RelPos menuPos, RelPos linePos) {
			if(menuPos == null || linePos == null)
				throw new NullPointerException("menu and/or line centering cannot be null");
			
			menu.bounds = null;
			menuCentering = menuPos;
			menu.linePos = linePos;
			return this;
		}
		
		public Builder setSize(int width, int height) { return setSize(new Dimension(width, height)); }
		public Builder setSize(Dimension d) { size = d; return this; }
		
		public Builder setBounds(Rectangle rect) {
			menu.bounds = rect;
			size = new Dimension(menu.bounds.getWidth(), menu.bounds.getHeight());
			anchor = menu.bounds.getCenter();
			return this;
		}
		
		public Builder setFrame(boolean hasFrame) {
			menu.hasFrame = hasFrame;
			if(!setTitleColor) titleCol = hasFrame ? 550 : 555;
			return this;
		}
		public Builder setFrame(int fillCol, int edgeStroke, int edgeFill) {
			menu.hasFrame = true;
			
			// these are not full colors, only the component that matters.
			frameFillCol = fillCol;
			frameEdgeStroke = edgeStroke;
			frameEdgeFill = edgeFill;
			
			return this;
		}
		
		public Builder setSelectable(boolean selectable) {
			setSelectable = true;
			menu.selectable = selectable;
			return this;
		}
		
		public Builder setShouldRender(boolean render) { menu.shouldRender = render; return this; }
		
		public Builder setScrollPolicies(float padding, boolean wrap) { return setScrollPolicies(0, padding, wrap); }
		public Builder setScrollPolicies(int numDisplayedEntries, float padding, boolean wrap) {
			menu.displayLength = numDisplayedEntries;
			menu.padding = padding;
			menu.wrap = wrap;
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
			
			// set default size
			if(size == null) {
				int width = 0;
				int height = 0;
				for(ListEntry entry: menu.entries) {
					width = Math.max(width, entry.getWidth());
					height += entry.getHeight() + menu.spacing;
				}
				height -= menu.spacing; // extra one at the end
				
				if(menu.hasFrame) {
					width += SpriteSheet.boxWidth * 2;
					height += SpriteSheet.boxWidth * 2;
				}
				else if(menu.title.length() > 0 && titleCentering != RelPos.CENTER) {
					RelPos c = titleCentering;
					if(c != RelPos.LEFT && c != RelPos.RIGHT)
						//noinspection SuspiciousNameCombination
						height += SpriteSheet.boxWidth;
					if(c != RelPos.TOP && c != RelPos.BOTTOM)
						width += SpriteSheet.boxWidth;
				}
				
				size = new Dimension(width, height);
			}
			
			
			// based on the menu centering, and the anchor, determine the upper-left point from which to draw the menu.
			if(menu.bounds == null)
				menu.bounds = menuCentering.getRect(size, anchor); // reset to a value that is actually useful to the menu
			
			// set default max display length
			if(menu.displayLength <= 0) {
				//if(menu.bounds.getBottom() <= Screen.h) // the determined height isn't too big to fit
				//	menu.maxDispLen = menu.entries.length;
				//else { // the total height is greater than the screen height, so go entry by entry and find out how many fit
					int height = 0;
					for(int i = 0; i < menu.entries.size(); i++) {
						height += menu.entries.get(i).getHeight();
						
						if(height > menu.bounds.getHeight()) {
							menu.displayLength = Math.max(1, i); // the minimum entries to display is 1.
							break;
						}
						
						if(i < menu.entries.size()-1)
							height += menu.spacing;
					}
					
					if(menu.displayLength <= 0) { // never ran out of space
						int avgHeight = 0;
						if(menu.entries.size() > 0)
							avgHeight = height / menu.entries.size();
						else
							avgHeight = Font.textHeight();
						
						menu.displayLength = (menu.bounds.getHeight()+menu.spacing) / avgHeight; // all entries fit
					}
				//}
			}
			
			
			// set the menu title color
			if(menu.title.length() > 0 && !fullTitleColor) { // the full title color must be set
				if(menu.hasFrame) // make it match the frame color
					menu.titleColor = Color.get(frameFillCol, titleCol);
				else // make it transparent
					menu.titleColor = Color.get(-1, titleCol);
			}
			
			// set the menu frame colors
			if(menu.hasFrame) {
				menu.frameFillColor = Color.get(frameFillCol, frameFillCol);
				menu.frameEdgeColor = Color.get(-1, frameEdgeStroke, frameFillCol, frameEdgeFill);
			}
			
			// set the title anchor based on the centering
			
			// basically, use RelPos.apply, but change the dimension so that:
			/* - width = menu width if title is on left or right, title width otherwise
			/* - height = menu height if title is on top or bottom, title height otherwise
			 * 
			 * after the anchor calculation...
			 * 
			 * - if on right side:
			 * -- if top or bottom, subtract title width
			 * -- otherwise, subtract width of one character
			 * 
			 * - if on bottom: subtract height of title
			 */
			if(menu.titlePos == null) {
				if(titleCentering == null) titleCentering = RelPos.TOP;
				
				if(titleCentering == RelPos.LEFT || titleCentering == RelPos.RIGHT)
					menu.drawVertically = true;
				
				Dimension titleDim = menu.drawVertically ?
					new Dimension(Font.textHeight(), Font.textWidth(menu.title)) :
					new Dimension(Font.textWidth(menu.title), Font.textHeight());
				
				menu.titlePos = titleCentering.positionSubRect(titleDim, size, menu.bounds.getCenter());
			}
			
			
			// done setting defaults/values; return the new menu 
			
			menu.init(); // any setup the menu does by itself right before being finished.
			return menu;
		}
		
		// returns a new Builder instance, that can be further modified to create another menu.
		private Builder copy() {
			Builder b = new Builder(menu.hasFrame, menu.spacing, menu.entries);
			
			b.menu = new Menu(menu);
			
			b.anchor = anchor == null ? null : new Point(anchor);
			b.size = size == null ? null : new Dimension(size);
			b.menuCentering = menuCentering;
			b.setSelectable = setSelectable;
			b.titleCentering = titleCentering;
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
