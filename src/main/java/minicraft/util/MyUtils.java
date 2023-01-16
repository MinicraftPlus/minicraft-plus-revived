package minicraft.util;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class MyUtils {

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
