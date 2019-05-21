package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.io.InputHandler;
import minicraft.gfx.*;
import minicraft.screen.entry.StringEntry;

public class BookDisplay extends Display {

    int height = 128;
    int width = 256;

    private String alphabet = "abcdefghijklmnopqrstuvwxyz1234567890";
    private String alphabetShifted = "ABCDEFGHIJKLMNOPQRSTUVWXYZ!@  %^  ()? ";

    String defaultBook = "";

    Menu.Builder builder;

    private int page;
    private String[] pages;

    private int pageCountIdx;

    private boolean titlePage;

    private boolean editable;

    public BookDisplay(String book) { this(book, false); }
    public BookDisplay(String book, boolean hasTitlePage) { this(book, hasTitlePage, false); }
    public BookDisplay(String book, boolean hasTitlePage, boolean editable) {

        if (!editable && book == null) {
            book = defaultBook;
        }

        if (editable) {
            pages = new String[30];

            for (int p = 0; p < 30; p++) {
                pages[p] = "";
            }
        } else if (book != null) {
            pages = book.split("\0");
        }

        pageCountIdx = pages.length;

        titlePage = hasTitlePage;

        this.editable = editable;

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
        if (!(page + direction < 0 || page + direction > pages.length - 1)) {
            page += direction;
        }

        menus[pageCountIdx] = updatePageCount();
    }

    private void addChar(char character) {
        if (pages[page].length() + 1 < 270) {
            pages[page] += character;
        } else if (page < 28) {
                turnPage(1);
                addChar(character);
        }
    }

    private void removeChar() {
        if (pages[page].length() - 1 >= 0) {
            pages[page] = pages[page].substring(0, pages[page].length()-1);
        }
    }

    @Override
    public void tick(InputHandler input) {
        if (editable) {
            if (input.getKey("exit").clicked)
                Game.exitMenu();
            // Use the button variants so people can still type 'A' and 'D'
            if (input.getKey("left" + (editable ? "-button" : "")).clicked) turnPage(-1);
            if (input.getKey("right" + (editable ? "-button" : "")).clicked) turnPage(1);

            for (int a = 0; a < alphabet.length(); a++) {
                if (input.getKey(Character.toString(alphabet.toCharArray()[a])).clicked) {
                    addChar(alphabet.toCharArray()[a]);
                } else if (input.getKey("shift-" + Character.toString(alphabet.toCharArray()[a])).clicked) {
                    addChar(alphabetShifted.toCharArray()[a]);
                }
            }

            // special characters
            if (input.getKey("backspace").clicked) removeChar();
            if (input.getKey("space").clicked) addChar(' ');
            if (input.getKey("period").clicked) addChar('.');
            if (input.getKey("shift-period").clicked) addChar('>');
            if (input.getKey("comma").clicked) addChar(',');
            if (input.getKey("shift-comma").clicked) addChar('<');
            if (input.getKey("slash").clicked) addChar('/');
            if (input.getKey("back_slash").clicked) addChar('\\');
            if (input.getKey("minus").clicked) addChar('-');
            if (input.getKey("equals").clicked) addChar('=');
            if (input.getKey("add").clicked || input.getKey("shift-equals").clicked) addChar('+');
            if (input.getKey("semicolon").clicked) addChar(';');
            if (input.getKey("shift-semicolon").clicked) addChar(':');
            if (input.getKey("quote").clicked) addChar('\'');
            if (input.getKey("shift-quote").clicked) addChar('\"');
        } else {
            if (input.getKey("exit").clicked || input.getKey("menu").clicked)
                Game.exitMenu();

            if (input.getKey("left").clicked) turnPage(-1);
            if (input.getKey("right").clicked) turnPage(1);
        }
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
