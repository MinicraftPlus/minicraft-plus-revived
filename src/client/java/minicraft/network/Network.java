package minicraft.network;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import minicraft.core.Action;
import minicraft.core.Game;
import minicraft.saveload.Version;
import minicraft.core.VersionInfo;
import minicraft.entity.Entity;
import minicraft.level.Level;
import minicraft.util.Logging;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import kong.unirest.json.JSONArray;

import java.util.Random;

public class Network extends Game {
	private Network() {
	}

	private static final Random random = new Random();

	private static VersionInfo latestVersion = null;


	@Nullable
	public static VersionInfo getLatestVersion() {
		return latestVersion;
	}

	public static void findLatestVersion(Action callback) {
		new Thread(() -> {
			Logging.NETWORK.debug("Fetching release list from GitHub..."); // Fetch the latest version from GitHub
			try {
				HttpResponse<JsonNode> response = Unirest.get("https://api.github.com/repositories/83168941/releases").asJson();
				if (response.getStatus() != 200) {
					Logging.NETWORK.error("Version request returned status code " + response.getStatus() + ": " + response.getStatusText());
					Logging.NETWORK.error("Response body: " + response.getBody());
					latestVersion = new VersionInfo(VERSION, "", "");
				} else {
					JSONArray versions = response.getBody().getArray();
					Version highestVersion = new Version("0.0.0");
					int idx = 0;
					for (int i = 10; 0 >= i; i--)
					{
						kong.unirest.json.JSONObject versionJson = versions.getJSONObject(i);
						Version version = new Version(versionJson.getString("tag_name").substring(1));
						if (version.compareTo(highestVersion) > 0)
						{
							highestVersion = version;
							idx = i;
						}
					}
					latestVersion = new VersionInfo(new JSONObject(versions.getJSONObject(idx).toString()));
				}
			} catch (UnirestException e) {
				e.printStackTrace();
				latestVersion = new VersionInfo(VERSION, "", "");
			}

			callback.act(); // finished.
		}).start();
	}

	@Nullable
	public static Entity getEntity(int eid) {
		for (Level level : levels) {
			if (level == null) continue;
			for (Entity e : level.getEntityArray())
				if (e.eid == eid)
					return e;
		}

		return null;
	}

	public static int generateUniqueEntityId() {
		int eid;
		int tries = 0; // Just in case it gets out of hand.
		do {
			tries++;
			if (tries == 1000)
				Logging.NETWORK.info("Note: Trying 1000th time to find valid entity id...(Will continue)");

			eid = random.nextInt();
		} while (!idIsAvailable(eid));

		return eid;
	}

	public static boolean idIsAvailable(int eid) {
		if (eid == 0) return false; // This is reserved for the main player... kind of...
		if (eid < 0) return false; // ID's must be positive numbers.

		for (Level level : levels) {
			if (level == null) continue;
			for (Entity e : level.getEntityArray()) {
				if (e.eid == eid)
					return false;
			}
		}

		return true;
	}


}
