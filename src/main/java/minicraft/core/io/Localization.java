package minicraft.core.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.jetbrains.annotations.NotNull;

import minicraft.core.Game;
import minicraft.screen.ResourcePackDisplay;

import org.json.JSONObject;
import org.tinylog.Logger;

public class Localization {

	public static final Locale DEFAULT_LOCALE = Locale.US;

	private static final HashSet<String> knownUnlocalizedStrings = new HashSet<>();
	private static final HashMap<String, String> localization = new HashMap<>();

	private static Locale selectedLocale = DEFAULT_LOCALE;
	private static final HashMap<Locale, String> localizationFiles = new HashMap<>();

	static {
		getLanguagesFromDirectory();
	}
	
	@NotNull
	public static String getLocalized(String key) {
		if (key.matches("^[ ]*$")) return key; // Blank, or just whitespace
		
		try {
			Double.parseDouble(key);
			return key; // This is a number; don't try to localize it
		} catch(NumberFormatException ignored) {}
		
		String localString = localization.get(key);
		
		if (Game.debug && localString == null) {
			if (!knownUnlocalizedStrings.contains(key)) {
				Logger.tag("LOC").trace("'{}' is unlocalized.", key);
				knownUnlocalizedStrings.add(key);
			}
		}
		
		return (localString == null ? key : localString);
	}
	
	public static Locale getSelectedLocale() { return selectedLocale; }
	
	@NotNull
	public static String getSelectedLanguage() {
		String data = localizationFiles.get(selectedLocale);
		return data.substring(data.lastIndexOf("/")+1, data.indexOf('_'));
	}

	public static void changeLanguage(@NotNull String newLanguage) {
		selectedLocale = Locale.forLanguageTag(newLanguage);

		updateLanguage();
	}

	public static void updateLanguage() {
		localization.clear();

		// Check if selectedLanguage is empty.
		if (selectedLocale == null)
			selectedLocale = DEFAULT_LOCALE;

		// Check if language is loaded.
		if (localizationFiles.get(selectedLocale) == null)
			selectedLocale = DEFAULT_LOCALE;

		// Load the loc into localization.
		String fileText = getFileAsString(selectedLocale);

		// Attempt to load string as a json object.
		JSONObject json = new JSONObject(fileText);

		// Put all loc strings in a key-value set.
		for (String key : json.keySet()) {
			localization.put(key, json.getString(key));
		}
	}
	
	@NotNull
	public static String[] getLanguages() {
		ArrayList<String> locs = new ArrayList<>();
		for (String loc : localizationFiles.values()) {
			locs.add(loc.substring(loc.lastIndexOf("/") + 1, loc.indexOf('_')));
		}

		return locs.toArray(new String[0]);
	}

	@NotNull
	public static Locale[] getLocales() { return localizationFiles.keySet().toArray(new Locale[0]); }

	@NotNull
	public static String[] getLocalesString() {
		ArrayList<String> locs = new ArrayList<>();
		for (Locale loc : localizationFiles.keySet()) {
			locs.add(loc.toLanguageTag());
		}

		return locs.toArray(new String[0]);
	}

	@NotNull
	private static String getFileAsString(Locale locale) {
		// Find path of the selected language, and check for errors.
		String location = localizationFiles.get(locale);
		if (location == null) {
			Logger.error("Could not find locale with name: {}.", locale.toLanguageTag());
			return "";
		}

		// Load an InputStream from the location provided, and check for errors.
		// Using getResourceAsStream since we're publishing this as a jar file.
		InputStream locStream = null;
		if (location.startsWith("/resources")) {
			locStream = Game.class.getResourceAsStream(location);
		} else {
			try {
				String[] path = location.split("/");

				ZipFile zipFile = new ZipFile(new File(ResourcePackDisplay.getLocation(), path[0] + ".zip"));

				HashMap<String, ZipEntry> localizations = ResourcePackDisplay.getPackFromZip(zipFile).get("localization");

				ZipEntry localization = localizations.get(path[path.length - 1]);

				locStream = zipFile.getInputStream(localization);
			} catch (IllegalStateException | IOException | NullPointerException e) {
				e.printStackTrace();
			}
		}
		if (locStream == null) {
			Logger.error("Error opening localization file at: {}.", location);
			return "";
		}

		Logger.debug("Loading localization file from {}.", location);

		// Load the file as a BufferedReader.
		BufferedReader reader = new BufferedReader(new InputStreamReader(locStream, StandardCharsets.UTF_8));

		return String.join("\n", reader.lines().toArray(String[]::new));
	}

	// Couldn't find a good way to find all the files in a directory when the program is
	// exported as a jar file, so I copied this. Thanks!
	// https://stackoverflow.com/questions/1429172/how-do-i-list-the-files-inside-a-jar-file/1429275#1429275
	private static void getLanguagesFromDirectory() {
		ArrayList<String> languages = new ArrayList<>();
		
		try {
			CodeSource src = Game.class.getProtectionDomain().getCodeSource();
			if (src != null) {
				URL jar = src.getLocation();
				ZipInputStream zip = new ZipInputStream(jar.openStream());
				int reads = 0;
				while (true) {
					ZipEntry e = zip.getNextEntry();
					
					// e is either null if there are no entries left, or if
					// we're running this from an ide (at least for eclipse)
					if (e == null) {
						if (reads > 0) break;
						else {
							getLanguagesFromDirectoryUsingIDE();
							return;
						}
					}
					reads++;
					String name = e.getName();
					if (name.startsWith("resources/localization/") && name.endsWith(".json")) {
						try {
							String data = name.replace("resources/localization/", "").replace(".json", "");
							Locale lang = Locale.forLanguageTag(data.substring(data.indexOf('_')+1));

							languages.add(data.substring(0, data.indexOf('_')));
							localizationFiles.put(lang, '/' + name);
						} catch (StringIndexOutOfBoundsException ex) {
							Logger.error("Could not load localization with path: {}", name);
						}
					}
				}
			} else {
				Logger.error("Failed to get code source.");
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		languages.toArray(new String[0]);
	}
	
	// This is only here, so we can run the game in our ide.
	// This will not work if we're running the game from a jar file.
	private static void getLanguagesFromDirectoryUsingIDE() {
		ArrayList<String> languages = new ArrayList<>();
		
		try {
			URL fUrl = Game.class.getResource("/resources/localization/");
			if (fUrl == null) {
				Logger.error("Could not find localization folder.");
				return;
			}

			Path folderPath = Paths.get(fUrl.toURI());
			DirectoryStream<Path> dir = Files.newDirectoryStream(folderPath);
			for (Path p : dir) {
				String filename = p.getFileName().toString();
				try {
					String data = filename.replace(".json", "");
					Locale lang = Locale.forLanguageTag(data.substring(data.indexOf('_') + 1));

					languages.add(data.substring(0, data.indexOf('_')));
					localizationFiles.put(lang, "/resources/localization/" + filename);
				} catch (StringIndexOutOfBoundsException e) {
					Logger.error("Could not load localization file with path: {}", p);
				}
			}
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
			// Nothing to do in this menu if we can't load the languages, so we
			// just return to the title menu.
			return;
		}

		languages.toArray(new String[0]);
	}

	public static void addAndReplaceLanguages(String[] paths) {
		for (String path : paths) {
			String data = path.replace(".json", "");

			try {
				Locale lang = Locale.forLanguageTag(data.substring(data.indexOf('_') + 1));

				System.out.println(lang.toLanguageTag() + " and " + path);
				//localizationFiles.put(lang, path);
			} catch (StringIndexOutOfBoundsException e) {
				Logger.error("Title of loc file {} is invalid.", path);
			}
		}
	}

	public static void reloadLanguages() {
		// Clear array with localization files.
		localizationFiles.clear();

		// Reload all default localization files.
		getLanguagesFromDirectory();
	}
}