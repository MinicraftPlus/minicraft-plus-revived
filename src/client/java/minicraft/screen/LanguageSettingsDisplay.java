package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class LanguageSettingsDisplay extends Display {
	private Map.Entry<ArrayList<ListEntry>, Integer> getEntries() {
		Localization.LocaleInformation[] locales = Localization.getLocales();
		ArrayList<Localization.LocaleInformation> list = new ArrayList<>(Arrays.asList(locales));
		list.sort((a, b) -> { // Debug language is always on top.
			if (a.locale.equals(Localization.DEBUG_LOCALE)) return -1;
			if (b.locale.equals(Localization.DEBUG_LOCALE)) return 1;
			return a.toString().compareTo(b.toString());
		});
		ArrayList<ListEntry> entries = new ArrayList<>();
		int count = 0;
		int index = 0;
		// Getting the list of entries by the list of available languages.
		for (Localization.LocaleInformation locale : list) {
			boolean selected = Localization.getSelectedLanguage() == locale;
			if (selected) index = count;
			entries.add(new SelectEntry(locale.toString(), () -> languageSelected(locale), false) {
				@Override
				public int getColor(boolean isSelected) {
					if (selected) return isSelected ? Color.GREEN : Color.tint(Color.GRAY, 1, true);
					return super.getColor(isSelected);
				}
			});
			count++;
		}

		return new AbstractMap.SimpleEntry<>(entries, index);
	}

	public LanguageSettingsDisplay() {
		super(true);
		Map.Entry<ArrayList<ListEntry>, Integer> entries = getEntries();
		menus = new Menu[]{
			new Menu.Builder(false, 2, RelPos.CENTER, entries.getKey())
				.setTitle("minicraft.displays.language_settings.title")
				.setSelectable(true)
				.setPositioning(new Point(Screen.w / 2, 10), RelPos.BOTTOM)
				.setSize(Screen.w, Screen.h - 30)
				.setSelection(entries.getValue())
				.createMenu()
		};
	}

	private static void languageSelected(Localization.LocaleInformation locale) {
		Localization.changeLanguage(locale.locale);
		Game.exitDisplay();
	}
}
