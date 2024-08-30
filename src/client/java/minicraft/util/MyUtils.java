package minicraft.util;

import org.jetbrains.annotations.NotNull;

public final class MyUtils {

	private MyUtils() {
	}

	public static int clamp(int val, int min, int max) {
		if (val > max) return max;
		if (val < min) return min;
		return val;
	}

	public static int randInt(int max) {
		return randInt(0, max);
	}

	public static int randInt(int min, int max) {
		return (int) (Math.random() * (max - min + 1)) + min;
	}

	public static String plural(int num, String word) {
		String p = num == 1 ? "" : "s";
		return num + " " + word + p;
	}

	/**
	 * <p>Capitalizes all the delimiter separated words in a String.
	 * Only the first letter of each word is changed. To convert the
	 * rest of each word to lowercase at the same time,
	 * use {@link #capitalizeFully(String)}.</p>
	 *
	 * <p>The delimiters represent a set of characters understood to separate words.
	 * The first string character and the first non-delimiter character after a
	 * delimiter will be capitalized. </p>
	 *
	 * <p>A <code>null</code> input String returns <code>null</code>.
	 * Capitalization uses the unicode title case, normally equivalent to
	 * upper case.</p>
	 *
	 * <pre>
	 * WordUtils.capitalize(null, *)            = null
	 * WordUtils.capitalize("", *)              = ""
	 * WordUtils.capitalize(*, new char[0])     = *
	 * WordUtils.capitalize("i am fine", null)  = "I Am Fine"
	 * WordUtils.capitalize("i aM.fine", {'.'}) = "I aM.Fine"
	 * </pre>
	 *
	 * Source: modified and copied from Apache commons-lang WordUtils#capitalize
	 *
	 * @param str  the String to capitalize, may be null
	 * @return capitalized String, <code>null</code> if null String input
	 */
   public static String capitalize(@NotNull String str) {
		if (str.isEmpty()) {
			return str;
		}
		int strLen = str.length();
		StringBuffer buffer = new StringBuffer(strLen);
		boolean capitalizeNext = true;
		for (int i = 0; i < strLen; i++) {
			char ch = str.charAt(i);
			if (Character.isWhitespace(ch)) {
				buffer.append(ch);
				capitalizeNext = true;
			} else if (capitalizeNext) {
				buffer.append(Character.toTitleCase(ch));
				capitalizeNext = false;
			} else {
				buffer.append(ch);
			}
		}
		return buffer.toString();
   }

	/**
	 * <p>Converts all the delimiter separated words in a String into capitalized words,
	 * that is each word is made up of a titlecase character and then a series of
	 * lowercase characters. </p>
	 *
	 * <p>The delimiters represent a set of characters understood to separate words.
	 * The first string character and the first non-delimiter character after a
	 * delimiter will be capitalized. </p>
	 *
	 * <p>A <code>null</code> input String returns <code>null</code>.
	 * Capitalization uses the unicode title case, normally equivalent to
	 * upper case.</p>
	 *
	 * <pre>
	 * WordUtils.capitalizeFully(null, *)            = null
	 * WordUtils.capitalizeFully("", *)              = ""
	 * WordUtils.capitalizeFully(*, null)            = *
	 * WordUtils.capitalizeFully(*, new char[0])     = *
	 * WordUtils.capitalizeFully("i aM.fine", {'.'}) = "I am.Fine"
	 * </pre>
	 *
	 * Source: modified and copied from Apache commons-lang WordUtils#capitalizeFully
	 *
	 * @param str  the String to capitalize, may be null
	 * @return capitalized String, <code>null</code> if null String input
	 */
	public static String capitalizeFully(@NotNull String str) {
		if (str.isEmpty()) {
			return str;
		}
		str = str.toLowerCase();
		return capitalize(str);
	}

	public static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ignored) {
		}
	}

	@Deprecated
	/** @deprecated Multiplayer removed. */
	public static <T> T fromNetworkStatus(T offline, T client, T server) {
		return offline;
	}
}
