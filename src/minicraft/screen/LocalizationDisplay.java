package minicraft.screen;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import minicraft.core.Action;
import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Screen;
import minicraft.screen.entry.ArrayEntry;
import minicraft.screen.entry.SelectEntry;

public class LocalizationDisplay extends Display {
	
	private ArrayList<String> languages;
	
	public LocalizationDisplay() {
		super(true);
		
		languages = getLanguagesFromDirectory();
		languages.remove(Localization.selectedLanguage);
		languages.add(0, Localization.selectedLanguage);
		
		Menu.Builder builder = new Menu.Builder(false, 6, RelPos.LEFT,
			new ArrayEntry<>("Language", languages.toArray()),
			new SelectEntry("Select", new ChangeLanguageListener())
		);
		
		builder.setTitle("Languages");
		
		menus = new Menu[1];
		menus[0] = builder.createMenu();
	}
	
	@Override
	public void tick(InputHandler input) {
		super.tick(input);
	}
	
	@Override
	public void render(Screen screen) {
		super.render(screen);
	}
	
	// Couldn't find a good way to find all the files in a directory when the program is
	// exported as a jar file so I copied this. Thanks!
	// https://stackoverflow.com/questions/1429172/how-do-i-list-the-files-inside-a-jar-file/1429275#1429275
	private ArrayList<String> getLanguagesFromDirectory() {
		ArrayList<String> languages = new ArrayList<>();
		
		try {
			CodeSource src = Game.class.getProtectionDomain().getCodeSource();
			if (src != null) {
				URL jar = src.getLocation();
				ZipInputStream zip = new ZipInputStream(jar.openStream());
				int reads = 0;
				while(true) {
					ZipEntry e = zip.getNextEntry();
					
					// e is either null if there are no entries left, or if
					// we're running this from an ide (atleast for eclipse)
					if (e == null) {
						if (reads > 0) break;
						else {
							return getLanguagesFromDirectoryUsingIDE();
						}
					}
					reads++;
					String name = e.getName();
					if (name.startsWith("resources/localization/") && name.contains(".mcpl")) {
						languages.add(name.replace("resources/localization/", "").replace(".mcpl", ""));
					}
				}
			}
			else {
			  /* Fail... */
				System.out.println("fail");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return languages;
	}
	
	// This is only here so we can run the game in our ide.
	// This will not work if we're running the game from a jar file.
	@java.lang.Deprecated
	private ArrayList<String> getLanguagesFromDirectoryUsingIDE() {
		ArrayList<String> languages = new ArrayList<>();
		
		try {
			URL fUrl = Game.class.getResource("/resources/localization/");
			Path folderPath = Paths.get(fUrl.toURI());
			DirectoryStream<Path> dir = Files.newDirectoryStream(folderPath);
			for (Path p : dir) {
				String filename = p.getFileName().toString();
				languages.add(filename.replace(".mcpl", ""));
			}
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
			// Nothing to do in this menu if we can't load the languages so we
			// just return to the title menu.
			Game.exitMenu();
		}
		
		return languages;
	}
	
	private class ChangeLanguageListener implements Action {
		@Override
		@SuppressWarnings("unchecked")
		public void act() {
			Menu menu = menus[0];
			ArrayEntry<String> entry = (ArrayEntry<String>) menu.getEntries()[0];
			String newLanguage = entry.getValue();
			
			Localization.changeLanguage(newLanguage);
		}
	}
}
