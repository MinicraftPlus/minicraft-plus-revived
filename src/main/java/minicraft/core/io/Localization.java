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
import minicraft.screen.TexturePackDisplay;

import org.json.JSONObject;
import org.tinylog.Logger;

public class Localization {
	
	private static final HashSet<String> knownUnlocalizedStrings = new HashSet<>();
	
	private static final HashMap<String, String> localization = new HashMap<>();
	private static String selectedLanguage = "english";
	
	private static final HashMap<String, Locale> locales = new HashMap<>();
	private static final HashMap<String, String> localizationFiles = new HashMap<>();
	
	private static String[] loadedLanguages = getLanguagesFromDirectory();
	
	static {
		if (loadedLanguages == null)
			loadedLanguages = new String[] { selectedLanguage };
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
	
	public static Locale getSelectedLocale() { return locales.getOrDefault(selectedLanguage, Locale.getDefault()); }
	
	@NotNull
	public static String getSelectedLanguage() { return selectedLanguage; }
	
	public static void changeLanguage(String newLanguage) {
		localization.clear();
		selectedLanguage = newLanguage;
		loadLanguageFile(selectedLanguage);
	}

	private static void loadLanguageFile(String fileName) {
		String fileText = getFileAsString(fileName);

		JSONObject json = new JSONObject(fileText);
		for (String key : json.keySet()) {
			localization.put(key, json.getString(key));
		}
	}
	
	@NotNull
	private static String getFileAsString(String fileName) {
		// Find path of the selected language, and check for errors.
		String location = localizationFiles.get(fileName);
		if (location == null) {
			Logger.error("Could not find locale with name: {}.", fileName);
			return "";
		}

		// Load an InputStream from the location provided, and check for errors.
		// Using getResourceAsStream since we're publishing this as a jar file.
		InputStream locStream = null;
		if(location.startsWith("/resources")) {
			locStream = Game.class.getResourceAsStream(location);
		} else {
			try {
				String[] path = location.split("/");

				ZipFile zipFile = new ZipFile(new File(TexturePackDisplay.getLocation(), path[0] + ".zip"));
	
				HashMap<String, ZipEntry> localizations = TexturePackDisplay.generateResourceTree(zipFile).get("localization"); 
	
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
						String data = name.replace("resources/localization/", "").replace(".json", "");
						String lang = data.substring(0, data.indexOf('_'));
						languages.add(lang);
						localizationFiles.put(lang, '/' + name);
						locales.put(lang, Locale.forLanguageTag(data.substring(data.indexOf('_')+1)));
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

		getLanguagesFromResourcePacks(languages);

		return languages.toArray(new String[0]);
	}
	
	// This is only here so we can run the game in our ide.
	// This will not work if we're running the game from a jar file.
	@java.lang.Deprecated
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
				String data = filename.replace(".json", "");
				String lang = data.substring(0, data.indexOf('_'));

				languages.add(lang);
				localizationFiles.put(lang, "/resources/localization/" + filename);
				locales.put(lang, Locale.forLanguageTag(data.substring(data.indexOf('_')+1)));
			}
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
			// Nothing to do in this menu if we can't load the languages so we
			// just return to the title menu.
			return null;
		}

		getLanguagesFromResourcePacks(languages);
		
		return languages.toArray(new String[languages.size()]);
	}

	private static void getLanguagesFromResourcePacks(ArrayList<String> languages) {
		for (String fileName : Objects.requireNonNull(TexturePackDisplay.getLocation().list())) {
			try {
				ZipFile zipFile = new ZipFile(new File(TexturePackDisplay.getLocation(), fileName));
	
				HashMap<String, HashMap<String, ZipEntry>> resources = TexturePackDisplay.generateResourceTree(zipFile); 
	
				// Load textures
				HashMap<String, ZipEntry> localizations = resources.get("localization");
				for(String locale: localizations.keySet()) {
					ZipEntry localization = localizations.get(locale);

					String data = locale.replace(".json", "");
					String lang = data.substring(0, data.indexOf('_'));


					languages.add(lang);
					localizationFiles.put(lang, localization.getName());
					locales.put(lang, Locale.forLanguageTag(data.substring(data.indexOf('_')+1)));

					// locales.put(lang, Locale.forLanguageTag(data.substring(data.indexOf('_')+1)));
		
					// InputStream inputEntry = zipFile.getInputStream(localization);
		
					// Load the file as a BufferedReader.
					// BufferedReader reader = new BufferedReader(new InputStreamReader(inputEntry, StandardCharsets.UTF_8));
		
					// String text = String.join("\n", reader.lines().toArray(String[]::new));
				}
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
				// Logger.error("Could not load texture pack with name {} at {}.", Objects.requireNonNull(menus[0].getCurEntry()).toString(), location);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}
}