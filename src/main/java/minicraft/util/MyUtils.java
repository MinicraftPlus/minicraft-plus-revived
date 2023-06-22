package minicraft.util;

public final class MyUtils {

	private MyUtils() {}

	public static int clamp(int val, int min, int max) { // This method could be used for some codes.
		if (val > max) return max;
		return Math.max(val, min);
	}

	@SuppressWarnings("unused") // Reserved
	public static int randInt(int max) { return randInt(0, max); }
	public static int randInt(int min, int max) {
		return (int) (Math.random() * (max - min + 1)) + min;
	}

	@SuppressWarnings("unused") // Reserved; it seems that we do not often care about this.
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

	/** @deprecated Multiplayer removed. */
	@SuppressWarnings("unused")
	@Deprecated
	public static <T> T fromNetworkStatus(T offline, T client, T server) {
		return offline;
	}
}
