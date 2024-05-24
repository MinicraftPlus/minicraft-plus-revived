package minicraft.network;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import minicraft.core.Action;
import minicraft.core.Game;
import minicraft.core.VersionInfo;
import minicraft.entity.Entity;
import minicraft.level.Level;
import minicraft.util.Logging;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

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
				HttpResponse<JsonNode> response = Unirest.get("https://api.github.com/repos/chrisj42/minicraft-plus-revived/releases").asJson();
				if (response.getStatus() != 200) {
					Logging.NETWORK.error("Version request returned status code " + response.getStatus() + ": " + response.getStatusText());
					Logging.NETWORK.error("Response body: " + response.getBody());
					latestVersion = new VersionInfo(VERSION, "", "");
				} else {
					latestVersion = new VersionInfo(new JSONObject(response.getBody().getArray().getJSONObject(0).toString()));
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
