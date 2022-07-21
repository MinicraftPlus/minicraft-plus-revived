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

import minicraft.core.CrashHandler;
import minicraft.core.Game;
import minicraft.screen.ResourcePackDisplay;

import org.json.JSONException;
import org.json.JSONObject;
import org.tinylog.Logger;

public class Localization {

	public static final Locale DEFAULT_LOCALE = Locale.US;

	private static final HashSet<String> knownUnlocalizedStrings = new HashSet<>();
	private static final HashMap<String, String> localization = new HashMap<>();

	private static Locale selectedLocale = DEFAULT_LOCALE;
	private static final HashMap<Locale, String> localizationFiles = new HashMap<>();

	static {
		getDefaultLocalizationFiles();
	}

	/**
	 * Get the provided key's localization for the currently selected language.
	 * @param key The key to localize.
	 * @return A localized string.
	 */
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

	/**
	 * Gets the currently selected locale.
	 * @return A locale object.
	 */
	public static Locale getSelectedLocale() { return selectedLocale; }

	/**
	 * Get the currently selected locale, but as a full name without the country code.
	 * @return A string with the name of the language.
	 */
	@NotNull
	public static String getSelectedLanguage() {
		String data = localizationFiles.get(selectedLocale);
		return data.substring(data.lastIndexOf("/")+1, data.indexOf('_'));
	}

	/**
	 * Gets a list of all the known languages.
	 * @return A list of languages.
	 */
	@NotNull
	public static String[] getLanguages() {
		ArrayList<String> locs = new ArrayList<>();
		for (String loc : localizationFiles.values()) {
			locs.add(loc.substring(loc.lastIndexOf("/") + 1, loc.indexOf('_')));
		}

		return locs.toArray(new String[0]);
	}

	/**
	 * Gets a  list of all the known locales.
	 * @return A list of locales.
	 */
	@NotNull
	public static Locale[] getLocales() { return localizationFiles.keySet().toArray(new Locale[0]); }

	/**
	 * Gets a list of all the known locales.
	 * @return A list of language tags.
	 */
	@NotNull
	public static String[] getLocalesAsString() {
		ArrayList<String> locs = new ArrayList<>();
		for (Locale loc : localizationFiles.keySet()) {
			locs.add(loc.toLanguageTag());
		}

		return locs.toArray(new String[0]);
	}

	/**
	 * Changes the selected language and loads it.
	 * If the provided language doesn't exist, it loads the default locale.
	 * @param newLanguage The language-country code of the language to load.
	 */
	public static void changeLanguage(@NotNull String newLanguage) {
		selectedLocale = Locale.forLanguageTag(newLanguage);

		loadLanguage();
	}

	/**
	 * This method gets the currently selected locale and loads it if it exists. If not, it loads the default locale.
	 * The loaded file is then parsed, and all the entries are added to a hashmap.
	 */
	public static void loadLanguage() {
		Logger.trace("Loading language...");
		localization.clear();

		// Check if selected localization exists.
		if (localizationFiles.get(selectedLocale) == null)
			selectedLocale = DEFAULT_LOCALE;

		// Convert the file into a string we can parse.
		String fileText = loadLocalizationFile(selectedLocale);

		// Attempt to load the string as a json object.
		JSONObject json;
		try {
			json = new JSONObject(fileText);
		} catch (JSONException e) {
			// If it is the default locale, the game is too broken to run, so we should just quit.
			if (selectedLocale == DEFAULT_LOCALE) {
				CrashHandler.crashHandle(e, new CrashHandler.ErrorInfo("Localization Could not be Loaded", CrashHandler.ErrorInfo.ErrorType.SERIOUS, "The default locale contains broken json."));
			} else {
				// If the default locale isn't loaded, retry with the default locale.
				Logger.error("The locale we attempted to load contains invalid json. Retrying with default locale.");
				selectedLocale = DEFAULT_LOCALE;
				loadLanguage();
			}
			return;
		}

		// Put all loc strings in a key-value set.
		for (String key : json.keySet()) {
			localization.put(key, json.getString(key));
		}
	}

	/**
	 * Gets the path of the provided locale by checking if it exists in localizationFiles. Then loads the file as a string.
	 * @param locale The locale to load.
	 * @return A sting object with the text of the file. If locale doesn't exist it returns an empty string.
	 */
	@NotNull
	private static String loadLocalizationFile(Locale locale) {
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

				ZipFile zipFile = new ZipFile(new File(ResourcePackDisplay.getFolderLocation(), path[0]));

				HashMap<String, ZipEntry> localizations = ResourcePackDisplay.getPackFromZip(zipFile).get("localization");

				ZipEntry localization = localizations.get(path[path.length - 1]);

				locStream = zipFile.getInputStream(localization);
			} catch (IllegalStateException | IOException | NullPointerException e) {
				CrashHandler.errorHandle(e);
			}
		}

		if (locStream == null) {
			Logger.error("Error opening localization file at: {}.", location);
			return "";
		}

		Logger.trace("Loading localization file from {}.", location);

		// Load the file as a BufferedReader.
		BufferedReader reader = new BufferedReader(new InputStreamReader(locStream, StandardCharsets.UTF_8));

		return String.join("\n", reader.lines().toArray(String[]::new));
	}

	/**
	 * Gets a list of paths to where the localization files are located in the jar file, and adds them to the "localizationFiles" HashMap.
	 * https://stackoverflow.com/questions/1429172/how-do-i-list-the-files-inside-a-jar-file/1429275#1429275
	 */
	private static void getDefaultLocalizationFiles() {
		try {
			CodeSource src = Game.class.getProtectionDomain().getCodeSource();
			if (src != null) {
				URL jar = src.getLocation();
				ZipInputStream zip = new ZipInputStream(jar.openStream());
				int reads = 0;
				while (true) {
					ZipEntry e = zip.getNextEntry();

					// e is either null if there are no entries left, or if
					// we're running this from an ide
					if (e == null) {
						if (reads > 0) break;
						else {
							getDefaultLocalizationFilesUsingIDE();
							return;
						}
					}
					reads++;
					String name = e.getName();
					if (name.startsWith("resources/localization/") && name.endsWith(".json")) {
						try {
							String data = name.replace("resources/localization/", "").replace(".json", "");
							Locale lang = Locale.forLanguageTag(data.substring(data.indexOf('_')+1));

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
			CrashHandler.errorHandle(e);
			return;
		}
	}

	/**
	 * Gets a list of paths to where the localization files are located on your disk, and adds them to the "localizationFiles" HashMap.
	 * The path is relative to the "resources" folder.
	 * Will not work if we are running this from a jar.
	 */
	private static void getDefaultLocalizationFilesUsingIDE() {
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

					localizationFiles.put(lang, "/resources/localization/" + filename);
				} catch (StringIndexOutOfBoundsException e) {
					Logger.error("Could not load localization file with path: {}", p);
				}
			}
		} catch (IOException | URISyntaxException e) {
			CrashHandler.errorHandle(e);
			return;
		}
	}

	/**
	 * Used by resource packs to reload the default localization files. It clears all references to loaded files, and adds the default locales anew.
	 */
	public static void reloadLocalizationFiles() {
		// Clear array with localization files.
		localizationFiles.clear();

		// Reload all default localization files.
		getDefaultLocalizationFiles();
	}

	/**
	 * Used by resource packs to replace and add initialized localization files.
	 * @param paths The paths to the different localization files. It is relative to the loaded pack, and not an absolute path.
	 */
	public static void updateLocalizationFiles(String[] paths) {
		for (String path : paths) {
			String data = path.replace(".json", "");

			try {
				// Convert language tag into locale.
				Locale lang = Locale.forLanguageTag(data.substring(data.indexOf('_') + 1));


				localizationFiles.put(lang, path);
			} catch (StringIndexOutOfBoundsException e) {
				Logger.error("Title of localization file {} is invalid.", path);
			}
		}

		// After we have added the paths to localizationFiles, reload the currently selected language.
		loadLanguage();
	}
}
