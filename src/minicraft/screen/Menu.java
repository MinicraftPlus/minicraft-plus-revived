package minicraft.screen;

import java.awt.Dimension;
import java.awt.Point;

import minicraft.gfx.Color;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;
import minicraft.screen.entry.ListEntry;

public class Menu {
	
	private ListEntry[] entries;
	private int spacing = 0;
	private Point anchor = new Point(Screen.w/2, Screen.h/2);
	private RelPos lineCentering = RelPos.CENTER;
	private Dimension size = null;
	
	private String title = "";
	private int titleColor;
	private RelPos titleCentering = RelPos.TOP; // if this is RelPos.CENTER, the title probably shouldn't be drawn.  
	
	private boolean hasFrame = true;
	private int frameFillColor, frameEdgeColor;
	
	private boolean selectable = false;
	
	private int maxDispLen = 0;
	private float padding = 0;
	private boolean wrap = false;
	
	
	private Menu(ListEntry[] entries) {
		this.entries = entries;
	}
	
	
	
	
	
	
	private void renderFrame(Screen screen) {
		if(!hasFrame) return;
		
		Rectangle bounds = new Rectangle(anchor, size);
		
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
		private int titleCol = 550, frameFillCol = 5, frameEdgeStroke = 1, frameEdgeFill = 445;
		
		public Builder(int entrySpacing, ListEntry... entries) {
			menu = new Menu(entries);
		}
		
		public Builder setTitle(String title, RelPos centering, int color) { return setTitle(title, centering, color, false); }
		public Builder setTitle(String title, RelPos centering, int color, boolean fullColor) {
			menu.title = title;
			menu.titleCentering = centering;
			
			setTitleColor = fullColor;
			if(fullColor) // this means that the color is the full 4 parts, abcd. Otherwise, it is assumed it is only the main component, the one that matters.
				menu.titleColor = color;
			else
				titleCol = color;
			
			return this;
		}
		
		public Builder setAnchor(Point p) { menu.anchor = p; return this; }
		
		public Builder setCentering(RelPos menuPos, RelPos linePos) {
			if(menuPos == null || linePos == null)
				throw new NullPointerException("menu and/or line centering cannot be null");
			
			menuCentering = menuPos;
			menu.lineCentering = linePos;
			return this;
		}
		
		public Builder setSize(int width, int height) { return setSize(new Dimension(width, height)); }
		public Builder setSize(Dimension size) { menu.size = size; return this; }
		
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
		
		public Builder setScrollPolicies(int maxDispLen, float padding, boolean wrap) {
			menu.maxDispLen = maxDispLen;
			menu.padding = padding;
			menu.wrap = wrap;
			return this;
		}
		
		// this is only meant to be called once. Afterwards, the menu returned is null.
		public Menu createMenu() {
			
			if(this.menu == null)
				throw new UnsupportedOperationException("Cannot create multiple menus from one Builder instance. To create multiple menus with the same data, save another instance with copy() before calling createMenu() the first time.");
			
			// set default selectability
			if(!setSelectable) {
				for(ListEntry entry: menu.entries) {
					menu.selectable = menu.selectable || entry.isSelectable();
					if(menu.selectable)
						break;
				}
			}
			
			// set default size
			if(menu.size == null) {
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
				else if(menu.title.length() > 0 && menu.titleCentering != RelPos.CENTER) {
					RelPos c = menu.titleCentering;
					if(c != RelPos.LEFT && c != RelPos.RIGHT)
						height += SpriteSheet.boxWidth;
					if(c != RelPos.TOP && c != RelPos.BOTTOM)
						width += SpriteSheet.boxWidth;
				}
				
				menu.size = new Dimension(width, height);
			}
			
			
			// based on the menu centering, and the anchor, determine the upper-left point from which to draw the menu.
			int menuPos = menuCentering.positioningIndex();
			int mx = menu.anchor.x - menu.size.width/2 * (menuPos % 3);
			int my = menu.anchor.y - menu.size.height/2 * (menuPos / 3);
			menu.anchor = new Point(mx, my); // reset to a value that is actually useful to the menu
			
			
			// set default max display length
			if(menu.maxDispLen <= 0) {
				if(menu.anchor.y + menu.size.height <= Screen.h) // the determined height isn't too big to fit
					menu.maxDispLen = menu.entries.length;
				else { // the total height is greater than the screen height, so go entry by entry and find out how many fit
					int height = menu.anchor.y;
					for(int i = 0; i < menu.entries.length; i++) {
						height += menu.entries[i].getHeight();
						
						if(height > Screen.h) {
							menu.maxDispLen = Math.max(1, i); // the minimum entries to display is 1.
							break;
						}
						
						if(i < menu.entries.length-1)
							height += menu.spacing;
					}
					
					if(menu.maxDispLen <= 0) // never ran out of space
						menu.maxDispLen = menu.entries.length; // all entries fit
				}
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
			
			// done setting defaults; now, set local menu to null, and return the set-up menu. 
			
			Menu menu = this.menu;
			this.menu = null;
			return menu;
		}
		
		// returns a new Builder instance, that can be further modified to create another menu.
		public Builder copy() {
			if(menu == null)
				throw new UnsupportedOperationException("cannot copy Builder instance after creating menu; copy() must be called before createMenu().");
			
			Builder b = new Builder(menu.spacing, menu.entries);
			b.menu.anchor = menu.anchor;
			b.menu.lineCentering = menu.lineCentering;
			b.menu.size = menu.size;
			b.menu.title = menu.title;
			b.menu.titleCentering = menu.titleCentering;
			b.menu.hasFrame = menu.hasFrame;
			b.menu.selectable = b.menu.selectable;
			b.menu.maxDispLen = menu.maxDispLen;
			b.menu.padding = menu.padding;
			b.menu.wrap = menu.wrap;
			
			b.menuCentering = menuCentering;
			b.setSelectable = setSelectable;
			b.setTitleColor = setTitleColor;
			b.titleCol = titleCol;
			b.frameFillCol = frameFillCol;
			b.frameEdgeStroke = frameEdgeStroke;
			b.frameEdgeFill = frameEdgeFill;
			
			return b;
		}
	}
}
