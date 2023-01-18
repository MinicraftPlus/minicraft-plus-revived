package minicraft.util;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class MyUtils {
	/**
	 * Used by the ResourceReloader so that when closing the game/application the reloading can be stopped/shutdown.
	 * Otherwise, it would continue running until it finished. We don't want to anger gamers.
	 */
	public static final ExecutorService RELOADER_WORKER = Executors.newSingleThreadExecutor();

	public static void shutdownWorkers() {
		RELOADER_WORKER.shutdown();

		boolean terminated;
		try {
			terminated = RELOADER_WORKER.awaitTermination(3, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			terminated = false;
		}

		if (!terminated) {
			RELOADER_WORKER.shutdownNow();
		}
	}

	private MyUtils() {}

	public static int clamp(int val, int min, int max) {
		if (val > max) return max;
		if (val < min) return min;
		return val;
	}

	public static int randInt(int max) { return randInt(0, max); }
	public static int randInt(int min, int max) {
		return (int) (Math.random() * (max - min + 1)) + min;
	}

	public static String plural(int num, String word) {
		String p = num == 1 ? "" : "s";
		return num + " " + word + p;
	}

	public static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch(InterruptedException ignored) {
		}
	}

	public static String normalizeToPosix(String path) {
		return path.replace(File.separator, "/");
	}

	public static String readAsString(BufferedReader reader) {
		return String.join("\n", reader.lines().toArray(String[]::new));
	}

	public static <T> List<T> reverseList(List<T> list) {
		if (list.size() == 0) return list;

		List<T> l = new ArrayList<>();

		for (int i = list.size() - 1; i >= 0; --i) {
			l.add(list.get(i));
		}

		return l;
	}

	@Deprecated
	/** @deprecated Multiplayer removed. */
	public static <T> T fromNetworkStatus(T offline, T client, T server) {
		return offline;
	}
}
