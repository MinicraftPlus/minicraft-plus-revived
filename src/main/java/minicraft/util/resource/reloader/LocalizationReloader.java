package minicraft.util.resource.reloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import minicraft.core.io.Localization;
import minicraft.util.Logging;
import minicraft.util.MyUtils;
import minicraft.util.resource.Resource;
import minicraft.util.resource.ResourceManager;
import minicraft.util.resource.SyncReloadableResourceManager;

public class LocalizationReloader implements SyncReloadableResourceManager.SyncReloader {
	@Override
	public void reload(ResourceManager manager) {
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
}
