package minicraft.core.io;

import minicraft.util.Logging;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

public class Localization {

	public static final Locale DEFAULT_LOCALE = Locale.US;
	public static final Locale DEBUG_LOCALE = Locale.ROOT; // This locale is used for debugging;

	public static boolean isDebugLocaleEnabled = false;
	public static boolean unlocalizedStringTracing = false;

	private static final HashMap<Locale, HashSet<String>> knownUnlocalizedStrings = new HashMap<>();
	private static final HashMap<String, String> localization = new HashMap<>();

	private static Locale selectedLocale = DEFAULT_LOCALE;
	private static final HashMap<Locale, ArrayList<String>> unloadedLocalization = new HashMap<>();
	private static final HashMap<Locale, LocaleInformation> localeInfo = new HashMap<>();

	/**
	 * Get the provided key's localization for the currently selected language.
	 *
	 * @param key       The key to localize.
	 * @param arguments The additional arguments to format the localized string.
	 * @return A localized string.
	 */
	@NotNull
	public static String getLocalized(String key, Object... arguments) {
		if (key.matches("^ *$")) return key; // Blank, or just whitespace
		if (selectedLocale == DEBUG_LOCALE) return key;

		try {
			Double.parseDouble(key);
			return key; // This is a number; don't try to localize it
		} catch (NumberFormatException ignored) {
		}

		String localString = localization.get(key);

		if (localString == null) {
			if (!knownUnlocalizedStrings.containsKey(selectedLocale))
				knownUnlocalizedStrings.put(selectedLocale, new HashSet<>());
			if (!knownUnlocalizedStrings.get(selectedLocale).contains(key)) {
				Logger.tag("LOC").trace(unlocalizedStringTracing ? new Throwable("Tracing") : null, "{}: '{}' is unlocalized.", selectedLocale.toLanguageTag(), key);
				knownUnlocalizedStrings.get(selectedLocale).add(key);
			}
		}

		if (localString != null) {
			localString = String.format(getSelectedLocale(), localString, arguments);
		}

		return (localString == null ? key : localString);
	}

	/**
	 * Gets the currently selected locale.
	 *
	 * @return A locale object.
	 */
	public static Locale getSelectedLocale() {
		return selectedLocale;
	}

	/**
	 * Get the currently selected locale, but as a full name without the country code.
	 *
	 * @return A string with the name of the language.
	 */
	@NotNull
	public static LocaleInformation getSelectedLanguage() {
		return localeInfo.get(selectedLocale);
	}

	/**
	 * Gets a  list of all the known locales.
	 *
	 * @return A list of locales.
	 */
	@NotNull
	public static LocaleInformation[] getLocales() {
		return localeInfo.values().toArray(new LocaleInformation[0]);
	}

	/**
	 * Changes the selected language and loads it.
	 * If the provided language doesn't exist, it loads the default locale.
	 *
	 * @param newLanguage The language-country code of the language to load.
	 */
	public static void changeLanguage(@NotNull String newLanguage) {
		changeLanguage(Locale.forLanguageTag(newLanguage));
	}

	/**
	 * @see #changeLanguage(String)
	 */
	public static void changeLanguage(@NotNull Locale newLanguage) {
		selectedLocale = newLanguage;
		loadLanguage();
	}

	/**
	 * This method gets the currently selected locale and loads it if it exists. If not, it loads the default locale.
	 * The loaded file is then parsed, and all the entries are added to a hashmap.
	 */
	public static void loadLanguage() {
		Logging.RESOURCEHANDLER_LOCALIZATION.trace("Loading language...");
		localization.clear();

		if (selectedLocale == DEBUG_LOCALE) return; // DO NOT load any localization for debugging.

		// Check if selected localization exists.
		if (!unloadedLocalization.containsKey(selectedLocale))
			selectedLocale = DEFAULT_LOCALE;

		// Attempt to load the string as a json object.
		JSONObject json;
		for (String text : unloadedLocalization.get(selectedLocale)) {
			json = new JSONObject(text);
			for (String key : json.keySet()) {
				localization.put(key, json.getString(key));
			}
		}

		// Language fallback
		if (!selectedLocale.equals(DEFAULT_LOCALE)) {
			for (String text : unloadedLocalization.get(DEFAULT_LOCALE)) { // Getting default localization.
				json = new JSONObject(text);
				for (String key : json.keySet()) {
					if (!localization.containsKey(key)) { // The default localization is added only when the key is not existed.
						localization.put(key, json.getString(key));
					}
				}
			}
		}
	}

	public static void resetLocalizations() {
		// Clear array with localization files.
		unloadedLocalization.clear();
		localeInfo.clear();
		if (isDebugLocaleEnabled) { // Adding the debug locale as an option.
			localeInfo.put(DEBUG_LOCALE, new LocaleInformation(DEBUG_LOCALE, "Debug", null));
		}
	}

	public static class LocaleInformation {
		public final Locale locale;
		public final String name;
		public final String region;

		public LocaleInformation(Locale locale, String name, String region) {
			this.locale = locale;
			this.name = name;
			this.region = region;
		}

		@Override
		public String toString() {
			if (region == null) return name;
			return String.format("%s (%s)", name, region);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) return false;
			if (obj instanceof LocaleInformation)
				return locale.equals(((LocaleInformation) obj).locale) &&
					name.equals(((LocaleInformation) obj).name) &&
					region.equals(((LocaleInformation) obj).region);
			return false;
		}
	}

	public static void addLocale(Locale loc, LocaleInformation info) {
		if (!localeInfo.containsKey(loc)) localeInfo.put(loc, info);
	}

	public static void addLocalization(Locale loc, String json) {
		if (!localeInfo.containsKey(loc)) return; // Only add when Locale Information is exist.
		if (!unloadedLocalization.containsKey(loc))
			unloadedLocalization.put(loc, new ArrayList<>());
		unloadedLocalization.get(loc).add(json);
	}
}
