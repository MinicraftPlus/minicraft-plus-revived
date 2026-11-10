package minicraft.util;

public class Hash2 {
	static final long X_MULTIPLIER = 16993;
	static final long Y_MULTIPLIER = 21929;
	static final long CONST_OFFSET = 17986579807203984l;
	static final long XOR = 8579006662848621798l;

	public static long hash2(long seed, long x, long y) {
		return (CONST_OFFSET + seed + X_MULTIPLIER*x + Y_MULTIPLIER*y) ^ XOR;
	}

	public static double hash2d01(long seed, long x, long y) {
		double range = Math.pow(2, 52);
		return (hash2(seed, x, y) % range) / range;
	}
}
