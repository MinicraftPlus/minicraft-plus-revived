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
	private Rectangle bounds = new Rectangle(Screen.w/2, Screen.h/2, 0, 0);
	private RelPos linePos = RelPos.CENTER;
	private int lineY;
	
	private String title = "";
	private int titleColor;
	private Point titlePos = null; // standard point is anchor, with anchor.x + SpriteSheet.boxWidth
	private boolean drawVertically = false;
	
	private boolean hasFrame = true;
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
	
	public void init() {
		recalcEntryPos();
		
		if(padding < 0) padding = 0;
		if(padding > 1) padding = 1;
		this.padding = Math.round(padding * displayLength / 2);
		
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
			int lineX = linePos.getRect(entry.getWidth(), 0, bounds.getCenter().x, y).getLeft();
			entry.render(screen, lineX, lineY, i == selection);
			if(i == selection) {
				// draw the arrows
				Font.draw("> ", screen, lineX-Font.textWidth("> "), y, ListEntry.COL_SLCT);
				Font.draw(" <", screen, lineX+entry.getWidth(), y, ListEntry.COL_SLCT);
			}
			
			y += entry.getHeight();
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
		
		private boolean setTitleColor = false;
		private RelPos titleCentering = RelPos.TOP;
		private int titleCol = 550, frameFillCol = 5, frameEdgeStroke = 1, frameEdgeFill = 445;
		
		private boolean setBounds = false;
		private Dimension size = null;
		private Point anchor = null;
		
		public Builder(int entrySpacing, ListEntry... entries) { this(entrySpacing, Arrays.asList(entries)); }
		public Builder(int entrySpacing, List<ListEntry> entries) {
			menu = new Menu();
			setEntries(entries);
			menu.spacing = entrySpacing;
		}
		
		public Builder setEntries(ListEntry... entries) { return setEntries(Arrays.asList(entries)); }
		public Builder setEntries(List<ListEntry> entries) {
			menu.entries.clear();
			menu.entries.addAll(entries);
			return this;
		}
		
		public Builder setTitle(String title) { return setTitle(title, 555, false); }
		public Builder setTitle(String title, int color) { return setTitle(title, color, false); }
		public Builder setTitle(String title, int color, boolean fullColor) {
			menu.title = title;
			
			setTitleColor = fullColor;
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
			
			setBounds = false;
			menuCentering = menuPos;
			menu.linePos = linePos;
			return this;
		}
		
		public Builder setSize(int width, int height) { return setSize(new Dimension(width, height)); }
		public Builder setSize(Dimension d) { size = d; return this; }
		
		public Builder setBounds(Rectangle rect) {
			menu.bounds = rect;
			setBounds = true;
			size = new Dimension(menu.bounds.getWidth(), menu.bounds.getHeight());
			anchor = menu.bounds.getCenter();
			return this;
		}
		
		public Builder setFrame(boolean hasFrame) { menu.hasFrame = hasFrame; return this; }
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
			if(size == null && !setBounds) {
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
			if(!setBounds)
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
						if(height > 0)
							avgHeight = height / menu.entries.size();
						
						menu.displayLength = (menu.bounds.getHeight()+menu.spacing) / avgHeight; // all entries fit
					}
				//}
			}
			
			
			// set the menu title color
			if(menu.title.length() > 0 && !setTitleColor) { // the full title color must be set
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
				
				Dimension titleDim = new Dimension(Font.textWidth(menu.title), Font.textHeight());
				// set the right dims to use RelPos.getRect, which positions the title
				if(titleCentering.xPos != 1) // is on left or right
					titleDim.width = menu.bounds.getWidth();
				if(titleCentering.yPos != 1) // is on top or bottom
					titleDim.height = menu.bounds.getHeight();
				
				if(titleCentering == RelPos.LEFT || titleCentering == RelPos.RIGHT) {
					menu.drawVertically = true;
					titleDim.height = Font.textWidth(menu.title);
				}
				
				Rectangle rect = titleCentering.getRect(titleDim, menu.bounds.getCenter());
				Point titlePos = new Point(rect.getLeft(), rect.getTop());
				
				// correct for not drawing past right border
				if(titleCentering.xPos == 2) { // on right
					if(titleCentering.yPos == 1) // center right
						titlePos.x -= Font.textWidth(" ");
					else // top right or bottom right
						titlePos.x -= Font.textWidth(menu.title);
				}
				
				// correct for not drawing past bottom border
				if(titleCentering.yPos == 2) // on bottom
					titlePos.y -= Font.textHeight();
				
				menu.titlePos = titlePos;
			}
			
			
			// done setting defaults/values; return the new menu 
			
			menu.init(); // any setup the menu does by itself right before being finished.
			return menu;
		}
		
		// returns a new Builder instance, that can be further modified to create another menu.
		private Builder copy() {
			Builder b = new Builder(menu.spacing, menu.entries);
			
			b.menu.linePos = menu.linePos;
			b.menu.title = menu.title;
			b.menu.hasFrame = menu.hasFrame;
			b.menu.selectable = menu.selectable;
			b.menu.displayLength = menu.displayLength;
			b.menu.padding = menu.padding;
			b.menu.wrap = menu.wrap;
			
			b.anchor = anchor == null ? null : new Point(anchor);
			b.size = size == null ? null : new Dimension(size);
			b.menuCentering = menuCentering;
			b.setSelectable = setSelectable;
			b.titleCentering = titleCentering;
			b.setTitleColor = setTitleColor;
			b.titleCol = titleCol;
			b.frameFillCol = frameFillCol;
			b.frameEdgeStroke = frameEdgeStroke;
			b.frameEdgeFill = frameEdgeFill;
			
			return b;
		}
	}
}
