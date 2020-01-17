package minicraft.core.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import minicraft.core.Game;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Localization {
	
	private static HashSet<String> knownUnlocalizedStrings = new HashSet<>();
	
	private static HashMap<String, String> localization = new HashMap<>();
	private static String selectedLanguage = "english";
	
	private static HashMap<String, Locale> locales = new HashMap<>();
	private static HashMap<String, String> localizationFiles = new HashMap<>();
	
	private static String[] loadedLanguages = getLanguagesFromDirectory();
	
	static {
		if(loadedLanguages == null)
			loadedLanguages = new String[] {selectedLanguage};
		
		loadSelectedLanguageFile();
	}
	
	@NotNull
	public static String getLocalized(String string) {
		if(string.matches("^[ ]*$")) return string; // blank, or just whitespace
		
		try {
			double num = Double.parseDouble(string);
			return string; // this is a number; don't try to localize it
		} catch(NumberFormatException ignored) {}
		
		String localString = localization.get(string);
		
		// System.out.println(string +" to "+localString);
		
		if (Game.debug && localString == null) {
			if(!knownUnlocalizedStrings.contains(string))
				System.out.println("The string \"" + string + "\" is not localized, returning itself instead.");
			knownUnlocalizedStrings.add(string);
		}
		
		return (localString == null ? string : localString);
	}
	
	public static Locale getSelectedLocale() { return locales.getOrDefault(selectedLanguage, Locale.getDefault()); }
	
	@NotNull
	public static String getSelectedLanguage() { return selectedLanguage; }
	
	public static void changeLanguage(String newLanguage) {
		selectedLanguage = newLanguage;
		loadSelectedLanguageFile();
	}
	
	private static void loadSelectedLanguageFile() {
		String fileText = getFileAsString();
		
		// System.out.println("file:");
		// System.out.println(fileText);
		
		String currentKey = "";
		
		for (String line : fileText.split("\r\n|\n|\r")) {
			// # at the start of a line means the line is a comment.
			if (line.startsWith("#")) continue;
			if (line.matches("^[ ]*$")) continue;
			
			if (currentKey.equals("")) {
				currentKey = line;
			} else {
				localization.put(currentKey, line);
				currentKey = "";
			}
		}
	}
	
	@NotNull
	private static String getFileAsString() {
    BufferedReader reader = new BufferedReader(new InputStreamReader(Game.class.getResourceAsStream(localizationFiles.getOrDefault(selectedLanguage, "/resources/localization/english_en-us.mcpl")), StandardCharsets.UTF_8));


		return String.join("\n", reader.lines().toArray(String[]::new));
		// Using getResourceAsStream since we're publishing this as a jar file.
	}
	
	@NotNull
	public static String[] getLanguages() { return loadedLanguages; }
	
	// Couldn't find a good way to find all the files in a directory when the program is
	// exported as a jar file so I copied this. Thanks!
	// https://stackoverflow.com/questions/1429172/how-do-i-list-the-files-inside-a-jar-file/1429275#1429275
	@Nullable
	private static String[] getLanguagesFromDirectory() {
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
					// we're running this from an ide (at least for eclipse)
					if (e == null) {
						if (reads > 0) break;
						else {
							return getLanguagesFromDirectoryUsingIDE();
						}
					}
					reads++;
					String name = e.getName();
					if (name.startsWith("resources/localization/") && name.endsWith(".mcpl")) {
						String data = name.replace("resources/localization/", "").replace(".mcpl", "");
						String lang = data.substring(0, data.indexOf('_'));
						languages.add(lang);
						localizationFiles.put(lang, '/'+name);
						locales.put(lang, Locale.forLanguageTag(data.substring(data.indexOf('_')+1)));
					}
				}
			}
			else {
			  /* Fail... */
				System.out.println("failed to get code source.");
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return languages.toArray(new String[languages.size()]);
	}
	
	// This is only here so we can run the game in our ide.
	// This will not work if we're running the game from a jar file.
	@java.lang.Deprecated
	@Nullable
	private static String[] getLanguagesFromDirectoryUsingIDE() {
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
			return null;
		}
		
		return languages.toArray(new String[languages.size()]);
	}
}
