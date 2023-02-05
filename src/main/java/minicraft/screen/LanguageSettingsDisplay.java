package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LanguageSettingsDisplay extends Display {
	private Map.Entry<ArrayList<ListEntry> ,Integer> getEntries() {
		Localization.LocaleInformation[] locales = Localization.getLocales();
		ArrayList<Localization.LocaleInformation> list = new ArrayList<>(Arrays.asList(locales));
		list.sort((a, b) -> {
			if (a.locale.equals(Localization.DEBUG_LOCALE)) return -1;
			if (b.locale.equals(Localization.DEBUG_LOCALE)) return 1;
			return a.toString().compareTo(b.toString());
		});
		ArrayList<ListEntry> entries = new ArrayList<>();
		int count = 0;
		int index = 0;
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
		Map.Entry<ArrayList<ListEntry> ,Integer> entries = getEntries();
		menus = new Menu[] {
			new Menu.Builder(false, 2, RelPos.CENTER, entries.getKey())
				.setTitle("minicraft.displays.language_settings.title")
				.setSelectable(true)
				.setPositioning(new Point(Screen.w/2, 10), RelPos.BOTTOM)
				.setSize(Screen.w, Screen.h - 30)
				.setSelection(entries.getValue())
				.createMenu()
		};
	}

	private static final ExecutorService executorService = Executors.newCachedThreadPool();

	private static void languageSelected(Localization.LocaleInformation locale) {
		int total = Localization.getNumDefaultLocalization();
		int cur = Localization.getNumMatchedLocalization(locale.locale);
		if (cur < total) { // Not fully translated.
			ArrayList<PopupDisplay.PopupActionCallback> callbacks = new ArrayList<>();
			callbacks.add(new PopupDisplay.PopupActionCallback("select", popup -> {
				Localization.changeLanguage(locale.locale);
				executorService.submit(() -> {
					Game.exitDisplay();
					try {
						Thread.sleep(50);
					} catch (InterruptedException ignored) {}
					Game.exitDisplay();
				});
				return true;
			}));

			ArrayList<ListEntry> entries = new ArrayList<>();
			Collections.addAll(entries, StringEntry.useLines(Color.RED, false,
				Localization.getLocalized("minicraft.displays.language_settings.popup_display.confirm_warning", (total - cur)*100 / total)));
			Collections.addAll(entries, StringEntry.useLines(Color.RED,
				"minicraft.display.popup.enter_confirm", "minicraft.display.popup.escape_cancel"));
			Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig("minicraft.display.popup.title_confirm",
				callbacks, 4), entries.toArray(new ListEntry[0])));
		} else {
			Localization.changeLanguage(locale.locale);
			Game.exitDisplay();
		}
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);

		ArrayList<String> list = new ArrayList<>();
		Collections.addAll(list, Font.getLines(Localization.getLocalized("minicraft.displays.language_settings.display.accuracy"), Screen.w, Screen.h, 0));
		for (int i = 0; i < list.size(); i++)
			Font.drawCentered(list.get(i), screen, Screen.h - 8 * (list.size() - i), Color.GRAY);
	}
}
