package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.io.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.screen.entry.StringEntry;

import org.jetbrains.annotations.NotNull;

public class BookDisplay extends Display {
	
	private static final int DISPLAY_WIDTH = 256;
	private static final int DISPLAY_HEIGHT = 128;
	
	static final int TEXT_AREA_WIDTH = DISPLAY_WIDTH - 12;
	static final int TEXT_AREA_HEIGHT = DISPLAY_HEIGHT - 24;
	static final int LINE_SPACING = 2;
	
	private final Menu.Builder builder;
	
	int page;
	String[] pages;
	
	private final int pageNumberDisplayIdx;
	
	private final boolean hasTitlePage;
	
	public BookDisplay(@NotNull String text) { this(text, false); }
	public BookDisplay(@NotNull String book, boolean hasTitlePage) {
		this.hasTitlePage = hasTitlePage;
		
		pages = book.split("\0", -1);
		
		pageNumberDisplayIdx = pages.length;
		
		menus = new Menu[pages.length + (hasTitlePage ? 2 : 1)];
		
		builder =  new Menu.Builder(false, 3, RelPos.CENTER).setSize(DISPLAY_WIDTH, DISPLAY_HEIGHT).setPositioning(new Point(Renderer.WIDTH/2, Renderer.HEIGHT/2), RelPos.CENTER).setFrame(443, 3, 443);
		
		for (int p = 0; p < pages.length; p++) {
			menus[p] = builder.createMenu();
		}
		
		menus[pageNumberDisplayIdx] = updatePageNumber();
		
		page = 0;
	}
	
	protected int getPageCount() {
		return pages.length;
	}
	
	private Menu updatePageNumber() {
		return builder
			.setSize(64, 24)
			.setPositioning(new Point(48, 28), RelPos.CENTER)
			.setEntries(new StringEntry(
				(page + (hasTitlePage ? 0 : 1)) + "/" + (getPageCount() - (hasTitlePage ? 1 : 0)),
				Color.BLACK
			))
			.createMenu();
	}
	
	@Override
	public void tick(InputHandler input) {
		if (input.getKey("cursor-left").clicked) turnPage(-1);
		if (input.getKey("cursor-right").clicked) turnPage(1);
		if (input.getKey("exit").clicked)
			Game.exitMenu();
	}
	
	void turnPage(int direction) {
		if (!(page + direction < 0 || page + direction > getPageCount() - 1)) {
			page += direction;
		}
		
		menus[pageNumberDisplayIdx] = updatePageNumber();
	}
	
	@Override
	public void render(Screen screen) {
		FontStyle fontStyle;
		
		if (hasTitlePage && page == 0) {
			fontStyle = new FontStyle(Color.WHITE).setShadowType(Color.BLACK, true);
			// if(Game.debug) System.out.println("showing title");
		} else {
			menus[pageNumberDisplayIdx].render(screen);
			fontStyle = new FontStyle(Color.BLACK).setXPos(24).setYPos(44);
		}
		
		menus[page].render(screen);
		
		Font.drawParagraph(pages[page], screen, TEXT_AREA_WIDTH, TEXT_AREA_HEIGHT, fontStyle, LINE_SPACING);
	}
}
