package minicraft.core.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.tinylog.Logger;

import minicraft.core.Game;
import minicraft.util.Logging;
import minicraft.util.MyUtils;
import minicraft.util.resource.Resource;
import minicraft.util.resource.reloader.ResourceReloader;
import minicraft.util.resource.reloader.SyncResourceReloader;

public class Localization {

	public static final Locale DEFAULT_LOCALE = Locale.US;

	private static final HashMap<Locale, HashSet<String>> knownUnlocalizedStrings = new HashMap<>();
	private static final HashMap<String, String> localization = new HashMap<>();

	private static Locale selectedLocale = DEFAULT_LOCALE;
	private static final HashMap<Locale, ArrayList<String>> unloadedLocalization = new HashMap<>();
	private static final HashMap<Locale, LocaleInformation> localeInfo = new HashMap<>();

	public static final ResourceReloader reloader = new SyncResourceReloader() {
		protected void reload(minicraft.util.resource.ResourceManager manager) {
			Localization.resetLocalizations();

			for (Resource res : manager.getResources("pack.json")) {
				try (BufferedReader reader = res.getAsReader()) {
					JSONObject obj = new JSONObject(MyUtils.readAsString(reader));

					for (String loc : obj.keySet()) {
						try {
							Locale locale = Locale.forLanguageTag(loc);
							JSONObject info = obj.getJSONObject(loc);
							Localization.addLocale(locale, new Localization.LocaleInformation(locale, info.getString("name"), info.getString("region")));
						} catch (JSONException e) {
							Logging.RESOURCEHANDLER_RESOURCEPACK.debug(e, "Invalid localization configuration in pack: {}", res.getResourcePack());
						}
					}
				} catch (JSONException | IOException e) {
					e.printStackTrace();
				}
			}

			for (Resource res : manager.getResources("assets/localization/", p -> p.endsWith(".json"))) {
				try (BufferedReader reader = res.getAsReader()) {
					String json = MyUtils.readAsString(reader);
					JSONObject obj = new JSONObject();

					for (String k : obj.keySet()) {
						obj.getString(k);
					}

					// Add verified localization.
					String name = res.getName();
					Localization.addLocalization(Locale.forLanguageTag(name.substring(0, name.length() - 5)), json);
				} catch (IOException e) {
					Logging.RESOURCEHANDLER_LOCALIZATION.debug(e, "Unable to load localization: {} in pack : {}", res.getName(), res.getResourcePackName());
				} catch (JSONException e) {
					Logging.RESOURCEHANDLER_LOCALIZATION.debug(e, "Invalid JSON format detected in localization: {} in pack : {}", res.getName(), res.getResourcePackName());
				}
			}
		}
	};

	/**
	 * Get the provided key's localization for the currently selected language.
	 * @param key The key to localize.
	 * @param arguments The additional arguments to format the localized string.
	 * @return A localized string.
	 */
	@NotNull
	public static String getLocalized(String key, Object... arguments) {
		if (key.matches("^[ ]*$")) return key; // Blank, or just whitespace

		try {
			Double.parseDouble(key);
			return key; // This is a number; don't try to localize it
		} catch(NumberFormatException ignored) {}

		String localString = localization.get(key);

		if (Game.debug && localString == null) {
			if (!knownUnlocalizedStrings.containsKey(selectedLocale)) knownUnlocalizedStrings.put(selectedLocale, new HashSet<>());
			if (!knownUnlocalizedStrings.get(selectedLocale).contains(key)) {
				Logger.tag("LOC").trace("{}: '{}' is unlocalized.", selectedLocale.toLanguageTag(), key);
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
	 * @return A locale object.
	 */
	public static Locale getSelectedLocale() { return selectedLocale; }

	/**
	 * Get the currently selected locale, but as a full name without the country code.
	 * @return A string with the name of the language.
	 */
	@NotNull
	public static LocaleInformation getSelectedLanguage() {
		return localeInfo.get(selectedLocale);
	}

	/**
	 * Gets a  list of all the known locales.
	 * @return A list of locales.
	 */
	@NotNull
	public static LocaleInformation[] getLocales() { return localeInfo.values().toArray(new LocaleInformation[0]); }

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
		Logging.RESOURCEHANDLER_LOCALIZATION.trace("Loading language...");
		localization.clear();

		// Check if selected localization exists.
		if (!unloadedLocalization.containsKey(selectedLocale))
			selectedLocale = DEFAULT_LOCALE;

		// Attempt to load the string as a json object.
		JSONObject json;
		for (String text : unloadedLocalization.get(selectedLocale)) {
			json = new JSONObject(text); // This JSON has been verified before.
			// Put all loc strings in a key-value set.
			for (String key : json.keySet()) {
				localization.put(key, json.getString(key));
			}
		}
	}

	public static void resetLocalizations() {
		// Clear array with localization files.
		unloadedLocalization.clear();
		localeInfo.clear();
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
			return String.format("%s (%s)", name, region);
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
