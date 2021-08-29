package minicraft.screen.entry;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.IOException;
import java.net.URI;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.screen.BookDisplay;
import minicraft.screen.Menu;
import minicraft.screen.RelPos;
import minicraft.screen.TempDisplay;

public class LinkEntry extends SelectEntry {
	
	private static boolean checkedDesktop = false;
	private static Desktop desktop = null;
	private static boolean canBrowse = false;
	
	private static final String openMsg = "Opening with browser...";
	
	private final int color;
	
	// note that if the failMsg should be localized, such must be done before passing them as parameters, for this class will not do it since, by default, the failMsg contains a url.
	
	public LinkEntry(int color, String urlText) { this(color, urlText, urlText, false); }
	public LinkEntry(int color, String text, String url) { this(color, text, url, true); }
	public LinkEntry(int color, String text, String url, String failMsg) { this(color, text, url, failMsg, true); }
	
	public LinkEntry(int color, String text, String url, boolean localize) { this(color, text, url, Localization.getLocalized("Go to") + ": " + url, localize); }
	public LinkEntry(int color, String text, String url, String failMsg, boolean localize) {
		super(text, () -> {
			if (!checkedDesktop) {
				checkedDesktop = true;
				if (Desktop.isDesktopSupported()) {
					desktop = Desktop.getDesktop();
					canBrowse = desktop.isSupported(Action.BROWSE);
				}
			}
			
			if(canBrowse) {
				// try to open the download link directly from the browser.
				try {
					URI uri = URI.create(url);
					Game.setMenu(new TempDisplay(3000, false, true, new Menu.Builder(true, 0, RelPos.CENTER, new StringEntry(Localization.getLocalized(openMsg))).createMenu()));
					desktop.browse(uri);
				} catch (IOException e) {
					System.err.println("Could not parse LinkEntry url \"" + url + "\" into valid URI:");
					e.printStackTrace();
					canBrowse = false;
				}
			}
			
			if (!canBrowse) {
				Game.setMenu(new BookDisplay(failMsg, false));
			}
			
		}, localize);
		
		this.color = color;
	}
	
	@Override
	public int getColor(boolean isSelected) { return color; }
}
