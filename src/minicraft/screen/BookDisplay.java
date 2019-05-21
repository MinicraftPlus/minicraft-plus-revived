package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.io.InputHandler;
import minicraft.gfx.*;
import minicraft.screen.entry.StringEntry;

public class BookDisplay extends Display {

    int height = 128;
    int width = 256;

    String defaultBook = "";

    Menu.Builder builder;

    private int page;
    private String[] pages;

    private int pageCountIdx;

    private boolean titlePage;

    public BookDisplay(String book) { this(book, false); }
    public BookDisplay(String book, boolean hasTitlePage) { this(book, hasTitlePage, false); }
    public BookDisplay(String book, boolean hasTitlePage, boolean editbale) {

        if (book == null) {
            book = defaultBook;
        }
        pages = book.split("\0");

        pageCountIdx = pages.length;

        titlePage = hasTitlePage;

        menus = new Menu[pages.length + (hasTitlePage ? 2 : 1)];

        builder =  new Menu.Builder(false, 3, RelPos.CENTER).setSize(width, height).setPositioning(new Point(Renderer.WIDTH/2, Renderer.HEIGHT/2), RelPos.CENTER).setFrame(443, 3, 443);

        for (int p = 0; p < pages.length; p++) {
            menus[p] = builder.createMenu();
        }

        menus[pageCountIdx] = updatePageCount();

        page = 0;
    }

    public Menu updatePageCount() {
        return builder.setSize(64, 24).setPositioning(new Point(48, 28), RelPos.CENTER).setEntries(new StringEntry(Integer.toString(page + (titlePage ? 0 : 1)) + "/" + Integer.toString(pages.length - (titlePage ? 1 : 0)), Color.BLACK)).createMenu();
    }

    private void turnPage(int direction) {
        if (page + direction < 0 || page + direction > pages.length - 1) {
            // don't do anything
        } else {
            page += direction;
        }

        menus[pageCountIdx] = updatePageCount();
    }

    @Override
    public void tick(InputHandler input) {
        if (input.getKey("exit").clicked)
            Game.exitMenu();
        if (input.getKey("left").clicked) turnPage(-1);
        if (input.getKey("right").clicked) turnPage(1);
    }

    @Override
    public void render(Screen screen) {
        FontStyle fontStyle;

        if (titlePage && page == 0) {
            fontStyle = new FontStyle(Color.WHITE).setShadowType(Color.BLACK, true);
        } else {
            menus[pages.length].render(screen);
            fontStyle = new FontStyle(Color.BLACK).setXPos(24).setYPos(44);
        }

        menus[page].render(screen);

        Font.drawParagraph(pages[page], screen, width - 12, height - 24, fontStyle, 2);
    }
}
