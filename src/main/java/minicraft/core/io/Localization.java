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
import org.jetbrains.annotations.Nullable;

import minicraft.core.Game;
import minicraft.screen.ResourcePackDisplay;

import org.json.JSONObject;
import org.tinylog.Logger;

public class Localization {
	
	private static final HashSet<String> knownUnlocalizedStrings = new HashSet<>();
	private static final HashMap<String, String> localization = new HashMap<>();

	private static Locale selectedLanguage = Locale.US;
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
	
	public static Locale getSelectedLocale() { return selectedLanguage; }
	
	@NotNull
	public static String getSelectedLanguage() {
		String data = localizationFiles.get(selectedLanguage);
		return data.substring(data.lastIndexOf("/")+1, data.indexOf('_'));
	}

	public static void changeLanguage(@NotNull String newLanguage) {
		localization.clear();
		selectedLanguage = Locale.forLanguageTag(newLanguage);
		loadLocFromFile(selectedLanguage);
	}

	private static void loadLocFromFile(Locale locale) {
		String fileText = getFileAsString(locale);

		JSONObject json = new JSONObject(fileText);
		for (String key : json.keySet()) {
			localization.put(key, json.getString(key));
		}
	}
	
	@NotNull
	public static String[] getLanguages() {
		ArrayList<String> locs = new ArrayList<>();
		for (String loc : localizationFiles.values()) {
			locs.add(loc.substring(loc.lastIndexOf("/")+1, loc.indexOf('_')));
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
				while (true) {
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
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return languages.toArray(new String[0]);
	}
	
	// This is only here, so we can run the game in our ide.
	// This will not work if we're running the game from a jar file.
	@Nullable
	private static String[] getLanguagesFromDirectoryUsingIDE() {
		ArrayList<String> languages = new ArrayList<>();
		
		try {
			URL fUrl = Game.class.getResource("/resources/localization/");
			if (fUrl == null) {
				Logger.error("Could not find localization folder.");
				return null;
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
					Logger.error("Could not load localization with path: {}", p);
				}
			}
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
			// Nothing to do in this menu if we can't load the languages, so we
			// just return to the title menu.
			return null;
		}
		
		return languages.toArray(new String[0]);
	}

	public static void getLanguagesFromResourcePack(ZipFile zipFile) {
		HashMap<String, HashMap<String, ZipEntry>> resources = ResourcePackDisplay.getPackFromZip(zipFile);
		try {
			HashMap<String, ZipEntry> localizations = resources.get("localization");

			for (String locale : localizations.keySet()) {
				ZipEntry localization = localizations.get(locale);

				String data = locale.replace(".json", "");
				Locale lang = Locale.forLanguageTag(data.substring(data.indexOf('_') + 1));

				localizationFiles.put(lang, localization.getName());
			}
		} catch (NullPointerException ignored) {}
	}
}