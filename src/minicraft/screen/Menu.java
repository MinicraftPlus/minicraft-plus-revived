package minicraft.screen;

import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;
import minicraft.Sound;
import minicraft.screen.entry.ListEntry;

/*** The Menu class is now basically the SelectMenu class... but better. ;)  On another note, perhaps... I *could* make this class not abstract, but I don't really want random menus being generated on the fly, so I'll stick with a class per menu type. **/
public abstract class Menu extends Display {
	
	protected ListEntry[] options;
	protected int selected;
	
	private int highlightColor, offColor;
	
	private int spacing;
	private FontStyle style;
	
	/*protected Menu(String[] options, Rectangle frame, int colOn, int colOff) {
		this(options, (new Rectangle[] {frame}), colOn, colOff);
	}*/
	protected Menu(ListEntry[] options) {
		this(options, Color.get(-1, 555), Color.get(-1, 222));
	}
	protected Menu(ListEntry[] options, int colOn, int colOff) {
		super();
		this.options = options;
		style = new FontStyle();
		//setTextStyle(new FontStyle(Color.get(-1, 333)));
		
		selected = 0;
		highlightColor = colOn;
		offColor = colOff;
	}
	
	/** handles selection changes. There is no exit function, becuase not all menus have one, for example the TitleMenu. */
	public void tick() {
		int prevSel = selected;
		
		if(input.getKey("up").clicked) selected--;
		if(input.getKey("down").clicked) selected++;
		
		if(selected >= options.length) selected = 0;
		if(selected < 0) selected = options.length - 1;
		
		if(prevSel != selected) {
			Sound.craft.play(); // play a sound
			//options[prevSel] = options[prevSel].replace("\\A> (.*) <\\z", "\\1"); // remove the angle brackets
			//onSelectionChange(prevSel, selected); // do any other behavior (including adding new angle brackets)
		}
		
		options[selected].tick(input);
	}
	
	public void render(Screen screen) {
		//style.drawParagraphLine(options, lineIndex, spacing, screen);
		int ySave = style.getYPos();
		for(int i = 0; i < options.length; i++) {
			style.setColor(isHighlighted(i) ? highlightColor : offColor);
			style.setupParagraphLine((new String[options.length]), i, spacing);
			options[i].render(screen, style);
		}
	}
	
	protected void setTextStyle(FontStyle style) { this.style = style; }
	protected void setLineSpacing(int spacing) { this.spacing = spacing; }
	
	/** Is used to determine whether the current line should be highlighted. */
	protected boolean isHighlighted(int idx) {
		return idx == selected;
	}
	
	/** This was made with expansion in mind, mostly. It's sort of like an API / event method. It gets called whenever the selected option changes. I can see it being helpful for scrolling menus. */
	/*protected void onSelectionChange(int prevSelection, int newSelection) {
		options[newSelection] = "> "+options[newSelection]+" <"; // adds the new angle brackets. This is done here to allow the options string and selection index to be modified by subclasses, before the brackets are put in.
	}*/
	
	
	// TODO this needs to go. It looks so efficient... but I want to make it so it's never used.
	/*public void renderItemList(Screen screen, int xo, int yo, int x1, int y1,
	  List<? extends ListItem> listItems, int selected) {
		boolean renderCursor = true;
		if (selected < 0) { // this is set to negative purposely for the express reason of disabling the cursor.
			selected = -selected - 1;
			renderCursor = false;
		}
		int w = x1 - xo;
		int h = y1 - yo - 1;
		int i0 = 0;
		int i1 = listItems.size();
		if (i1 > h) i1 = h;
		int io = selected - h / 2;
		if (io > listItems.size() - h) io = listItems.size() - h; /// this is what stops the inventory from scrolling when you reach the bottom.
		if (io < 0) io = 0;
		
		for (int i = i0; i < i1; i++) {
			listItems.get(i + io).renderInventory(screen, (xo+1) * 8, (yo+1 + i) * 8);
		}
		
		if (renderCursor) {
			int yy = selected + 1 - io + yo;
			Font.draw(">", screen, (xo + 0) * 8, yy * 8, Color.get(-1, 555));
			Font.draw("<", screen, (xo + w) * 8, yy * 8, Color.get(-1, 555));
		}
	}*/
}
