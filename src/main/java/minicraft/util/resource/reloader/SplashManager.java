package minicraft.util.resource.reloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import minicraft.util.MyUtils;
import minicraft.util.resource.Resource;
import minicraft.util.resource.ResourceManager;

public class SplashManager extends SyncResourceReloader {
	private static final Random random = new Random();
	private static final List<String> splashes = new ArrayList<>();

	@Override
	public void reload(ResourceManager manager) {
		splashes.clear();

		for (Resource res : manager.getResources("assets/texts/splashes.json")) {
			System.out.println(res.getName());
			try (BufferedReader reader = res.getAsReader()) {
				JSONObject obj = new JSONObject(MyUtils.readAsString(reader));

				if (obj.optBoolean("replace")) {
					splashes.clear();
				}

				JSONArray arr = obj.getJSONArray("splashes");

				for (Object o : arr) {
					splashes.add(String.valueOf(o));
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String getRandom() {
		LocalDateTime time = LocalDateTime.now();

		if (splashes.size() == 0) {
			return "Sadge";
		}

		if (time.getMonth() == Month.DECEMBER) {
			if (time.getDayOfMonth() == 19) return "Happy birthday Minicraft!";
			if (time.getDayOfMonth() == 25) return "Happy XMAS!";
		}

		return splashes.get(random.nextInt(splashes.size() - 1) + 1);
	}
}
