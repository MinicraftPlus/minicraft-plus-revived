package minicraft.core;

public final class MyUtils {
	
	private MyUtils() {}
	
	public static int clamp(int val, int min, int max) {
		if(val > max) return max;
		if(val < min) return min;
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
	
	public static <T> T fromNetworkStatus(T offline, T client, T server) {
		if(Game.isValidServer())
			return server;
		if(Game.isValidClient())
			return client;
		return offline;
	}
}
