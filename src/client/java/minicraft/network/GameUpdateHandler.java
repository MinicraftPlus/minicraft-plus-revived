package minicraft.network;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import minicraft.core.Game;
import minicraft.core.VersionInfo;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.gfx.Color;
import minicraft.util.DisplayString;
import minicraft.util.Logging;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameUpdateHandler {
	/*
	 * Game Update Check
	 *
	 * Ideally, update check should be done by Minicraft Launcher, but it is available only when it is implemented.
	 * I think 2.3 should be expected for the removal of the update checker, which means the Launcher
	 * would be implemented by 2.3?
	 * Probably, we should start having versions that can be separated from the trunk road like how Minecraft does, by 2.3.
	 * Currently, our game still sticks with latest version at the moment, which is not friendly
	 * for sticking with older versions.
	 */

	/*
	 * Update Check Status
	 *
	 * No check performed
	 *   failureMessage == null && latestUpdate == null
	 * Last check failed
	 *   failureMessage != null && latestUpdate == null
	 * Last check succeeded
	 *   failureMessage == null && latestUpdate != null
	 * Illegal state (ignored)
	 *   failureMessage != null && latestUpdate != null
	 */

	private final ExecutorService executorService =
		Executors.newSingleThreadExecutor(r -> new Thread(r, "Game Update Checker"));
	private String failureMessage = null;
	private UpdateMeta latestUpdate = null;
	private boolean checkDid = false;

	public GameUpdateHandler() {}

	private static class UpdateMeta {
		public final VersionInfo versionInfo;
		public final boolean inDev;
		public final DisplayString message;
		public final int color;

		public UpdateMeta(VersionInfo versionInfo) {
			this.versionInfo = versionInfo;
			inDev = versionInfo.version.isDev();
			if (versionInfo.version.compareTo(Game.VERSION) <= 0) {
				message = new Localization.HookedLocalizedBufArgString(
					"minicraft.displays.title.update_checker.display.status.latest");
				color = Color.DARK_GRAY;
			} else { // > Game.VERSION
				message = new Localization.HookedLocalizedBufArgString(
					"minicraft.displays.title.update_checker.display.status.new_available", versionInfo.version,
					Game.input.getMapping("U"));
				color = inDev ? Color.DIMMED_BLUE : Color.DIMMED_GREEN;
			}
		}
	}

	@Nullable
	public VersionInfo getLatestVersion() {
		return latestUpdate == null || latestUpdate.versionInfo.version.compareTo(Game.VERSION) <= 0 ? null :
			latestUpdate.versionInfo;
	}

	public String getStatusMessage() {
		return failureMessage == null ? latestUpdate == null ?
			Localization.getLocalized("minicraft.displays.title.update_checker.display.status.checking") :
			latestUpdate.message.toString() : Localization.getLocalized(
				"minicraft.displays.title.update_checker.display.status.failed", failureMessage);
	}

	public int getStatusMessageColor() {
		return failureMessage == null ? latestUpdate == null ?
			Color.DARK_GRAY : latestUpdate.color : Color.DIMMED_RED;
	}

	public boolean anyCheckDid() {
		return checkDid;
	}

	public void checkForUpdate() {
		String setting = (String) Settings.get("updatecheck");
		if (!setting.equals("minicraft.settings.update_check.disabled")) {
			boolean fullOnly = setting.equals("minicraft.settings.update_check.full_only");
			checkDid = true;
			executorService.submit(() -> {
				Logging.NETWORK.debug("Fetching release list from GitHub..."); // Fetch the latest version from GitHub
				try {
					HttpResponse<JsonNode> response = Unirest.get(
						"https://api.github.com/repos/MinicraftPlus/minicraft-plus-revived/releases").asJson();
					if (response.getStatus() == 200) {
						failureMessage = null;
						VersionInfo versionInfo;
						int idx = 0;
						do {
							versionInfo = new VersionInfo(
								new JSONObject(response.getBody().getArray().getJSONObject(idx++).toString()));
						} while (fullOnly && versionInfo.version.isDev());
						latestUpdate = new UpdateMeta(versionInfo);
					} else {
						Logging.NETWORK.error("Version request returned status code {}: {}",
							response.getStatus(), response.getStatusText());
						Logging.NETWORK.error("Response body: {}", response.getBody());
						latestUpdate = null;
						failureMessage = Localization.getLocalized(
							"minicraft.displays.title.update_checker.display.status.failed", response.getStatusText());
					}
				} catch (UnirestException e) {
					Logging.NETWORK.error(e, "Unable to perform update check");
					latestUpdate = null;
					failureMessage = Localization.getLocalized(
						"minicraft.displays.title.update_checker.display.status.failed.unknown");
				}
			});
		}
	}
}
