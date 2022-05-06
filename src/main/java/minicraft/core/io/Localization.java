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

import org.json.JSONException;
import org.json.JSONObject;
import org.tinylog.Logger;

public class Localization {

	public static final Locale DEFAULT_LOCALE = Locale.US;

	private static final HashSet<String> knownUnlocalizedStrings = new HashSet<>();
	private static HashMap<String, String> localization = new HashMap<>();
	private static final HashMap<Locale, HashMap<String, String>> defaultLocalizations = new HashMap<>();

	private static Locale selectedLocale = DEFAULT_LOCALE;
	private static final HashMap<Locale, HashMap<String, String>> localizations = new HashMap<>();

	static {
		/**
		 * Gets a list of paths to where the localization files are located in the jar file, and adds them to the "localizationFiles" HashMap.
		 * https://stackoverflow.com/questions/1429172/how-do-i-list-the-files-inside-a-jar-file/1429275#1429275
		 */
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
						if (reads == 0) getDefaultLocalizationFilesUsingIDE();
						localizations.putAll(defaultLocalizations);
						break;
					}
					reads++;
					String name = e.getName();
					if (name.startsWith("resources/localization/") && name.endsWith(".json")) {
						try {
							String data = name.replace("resources/localization/", "").replace(".json", "");
							Locale lang = Locale.forLanguageTag(data.substring(data.indexOf('_')+1));

							defaultLocalizations.put(lang, loadLocalizationFromJSON(new JSONObject(String.join("\n", new BufferedReader(
								new InputStreamReader(Game.class.getResourceAsStream("/" + name), StandardCharsets.UTF_8)
							).lines().toArray(String[]::new)))));
						} catch (StringIndexOutOfBoundsException ex) {
							Logger.error("Could not load localization with path: {}", name);
						}
					}
				}
			} else {
				Logger.error("Failed to get code source.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	 * Gets a  list of all the known locales.
	 * @return A list of locales.
	 */
	@NotNull
	public static Locale[] getLocales() { return localizations.keySet().toArray(new Locale[0]); }

	/**
	 * Gets a list of all the known locales.
	 * @return A list of language tags.
	 */
	@NotNull
	public static String[] getLocalesAsString() {
		ArrayList<String> locs = new ArrayList<>();
		for (Locale loc : localizations.keySet()) {
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
	private static void loadLanguage() {
		Logger.trace("Loading language...");

		// Check if selected localization exists.
		if (localizations.get(selectedLocale) == null)
			selectedLocale = DEFAULT_LOCALE;
		localization = localizations.get(selectedLocale);
	}

	public static String loadJSONFromZipEntry(ZipFile zip, ZipEntry entry) {
		try {
			return String.join("\n", new BufferedReader(new InputStreamReader(zip.getInputStream(entry), StandardCharsets.UTF_8)).lines().toArray(String[]::new));
		} catch (IOException e) {
			e.printStackTrace();
			Logger.error("Failed on loading localization {} from {}", entry, zip);
			return "";
		}
	}

	private static HashMap<String, String> loadLocalizationFromJSON(JSONObject json) {
		HashMap<String, String> locs = new HashMap<>();
		for (String k : json.keySet()) {
			locs.put(k, json.getString(k));
		}
		return locs;
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

					defaultLocalizations.put(lang, loadLocalizationFromJSON(new JSONObject(String.join("\n", new BufferedReader(
						new InputStreamReader(Game.class.getResourceAsStream("/resources/localization/" + filename), StandardCharsets.UTF_8)
					).lines().toArray(String[]::new)))));
				} catch (StringIndexOutOfBoundsException e) {
					Logger.error("Could not load localization file with path: {}", p);
				}
			}
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * Used by resource packs to reload the default localization files. It clears all references to loaded files, and adds the default locales anew.
	 */
	public static void reloadLocalizationFiles() {
		// Clear array with localization files.
		localizations.clear();

		// Reload all default localization files.
		localizations.putAll(defaultLocalizations);
	}

	/**
	 * Used by resource packs to replace and add initialized localization files.
	 * @param locs The paths to the different localization files. It is relative to the loaded pack, and not an absolute path.
	 * @param mergeDefault If the localization merges with the default localization
	 */
	public static void updateLocalizationFiles(HashMap<String, String> locs, boolean mergeDefault) {
		for (String langName : locs.keySet()) {
			String data = langName.replace(".json", "");

			try {
				// Convert language tag into locale.
				Locale lang = Locale.forLanguageTag(data.substring(data.indexOf('_') + 1));

				addLocalization(lang, loadLocalizationFromJSON(new JSONObject(locs.get(langName))), !mergeDefault);
			} catch (StringIndexOutOfBoundsException e) {
				Logger.error("Title of localization file {} is invalid.", langName);
			}
		}

		// After we have added the paths to localizationFiles, reload the currently selected language.
		loadLanguage();
	}

	/**
	 * Add/Refresh localization
	 * @param lang The language to add
	 * @param locs The localizations with the language
	 * @param replace Replace if neccessary
	 */
	private static void addLocalization(Locale lang, HashMap<String, String> locs, boolean replace) {
		if (replace) localizations.put(lang, locs);
		else
			if (localizations.containsKey(lang)) localizations.get(lang).putAll(locs);
			else localizations.put(lang, locs);
	}
}
