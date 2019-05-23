package minicraft.screen;

import java.util.Arrays;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.gfx.Font;
import minicraft.item.BookItem;

public class BookEditableDisplay extends BookDisplay {
	
	private static final int MAX_PAGE_COUNT = 30;
	
	private BookItem bookItem;
	
	private int pageCount;
	
	private static String extendPageCount(String bookText) {
		int pageCount = bookText.split("\0", -1).length;
		if(pageCount >= MAX_PAGE_COUNT) return bookText;
		String[] extra = new String[MAX_PAGE_COUNT - pageCount];
		Arrays.fill(extra, "\0");
		return bookText + String.join("", extra);
	}
	
	public BookEditableDisplay(BookItem bookItem) {
		super(extendPageCount(bookItem.getText()), bookItem.hasTitlePage);
		
		this.bookItem = bookItem;
		
		this.pageCount = bookItem.getText().split("\0").length;
		turnPage(-1); // just updates the page count
	}
	
	@Override
	protected int getPageCount() {
		return Math.min(MAX_PAGE_COUNT, Math.max(pageCount, page+1)+1);
	}
	
	/*private void addChar(char character) {
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
	}*/
	
	@Override
	public void tick(InputHandler input) {
		if (input.getKey("exit").clicked) {
			bookItem.setText(String.join("\0", Arrays.copyOfRange(pages, 0, pageCount)));
			Game.exitMenu();
			return;
		}
		
		if (input.getKey("cursor-left").clicked) turnPage(-1);
		if (input.getKey("cursor-right").clicked) turnPage(1);
		
		String pageText = input.addKeyTyped(pages[page], null);
		if(input.getKey("enter").clicked)
			pageText += "\n"; // allow newlines
		
		final int oldLen = pages[page].length();
		final int newLen = pageText.length();
		
		if(oldLen == newLen) return;
		
		// determine if this last change can fit on the current page
		String[] lines = Font.getLines(pageText, TEXT_AREA_WIDTH, TEXT_AREA_HEIGHT, LINE_SPACING, true);
		boolean hasOverflow = lines[lines.length-1].length() > 0;
		// check if change occurred, and there is enough space on the page to write it
		if(newLen > oldLen && hasOverflow) {
			// change does not fit; attempt to move it to next page
			if(page >= MAX_PAGE_COUNT - 1 || page < pageCount-1)
				return; // change does not fit because max page limit reached or there is text on the next page
			
			// recreate this page with the last word removed
			String prevPage = pageText.substring(0, pageText.length() - lines[lines.length-1].length());
			pages[page] = prevPage; // save the page without the overflow text
			turnPage(1); // move to next page
			pages[page] = lines[lines.length-1]; // write the overflow to the next page
		}
		else {
			// change fits on page
			pages[page] = pageText;
			if(oldLen == 0 && pageCount <= page)
				pageCount = page + 1; // text has been set, bring page count to current page
			else if(newLen == 0 && pageCount == page + 1 && page > 0) {
				// last non-blank page has been made blank, lower page count until non-blank page found
				do pageCount--;
				while(pageCount > 1 && pages[pageCount-1].length() == 0);
			}
		}
	}
	
	/*private String getBookText() {
		
		String bookToSave = "";
		
		for (String currentPage: pages) {
			bookToSave += currentPage + "\0";
		}
		
		return bookToSave;
	}*/
}
