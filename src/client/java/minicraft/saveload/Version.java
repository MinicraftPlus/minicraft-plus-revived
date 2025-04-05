package minicraft.saveload;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

public class Version implements Comparable<Version> {
	private int make, major, minor, dev;
	private boolean valid = true;

	// Toggle if Version is Special (e.g.: infdev is different of normal dev)
	private boolean isSpecial, curIsSpecial = true;
	protected String specialPrefix = "infdev";

	public Version(String version) {
		this(version, true);
	}

	private Version(String version, boolean printError) {
		String[] nums = version.split("\\.");
		try {
			if (nums.length > 0) make = Integer.parseInt(nums[0]);
			else make = 0;

			if (nums.length > 1) major = Integer.parseInt(nums[1]);
			else major = 0;

			String min;
			if (nums.length > 2) min = nums[2];
			else min = "";

			if (min.contains("-")) {
				String[] mindev = min.split("-");
				minor = Integer.parseInt(mindev[0]);
				isSpecial = mindev[1].contains(specialPrefix);
				if (isSpecial) dev = Integer.parseInt(mindev[1].replace("pre", "").replace(specialPrefix, ""));
				else dev = Integer.parseInt(mindev[1].replace("pre", "").replace("dev", ""));
			} else {
				if (!min.equals("")) minor = Integer.parseInt(min);
				else minor = 0;
				dev = 0;
			}
		} catch (NumberFormatException ex) {
			if (printError) Logger.tag("Version").error("INVALID version number: \"" + version + "\"");
			valid = false;
		} catch (Exception ex) {
			if (printError) ex.printStackTrace();
			valid = false;
		}
	}

	public boolean isSpecial() {
		return isSpecial;
	}

	public boolean isValid() {
		return valid;
	}

	public static boolean isValid(String version) {
		return new Version(version, false).isValid();
	}

	/**
	 * The returned value of this method (-1, 0, or 1) is determined by whether this object is less than, equal to, or greater than the specified object, ov.
	 * (this.compareTo(new Version("1.0.0") < 0 is the same as this < 1.0.0)
	 * @param ov The version to compare to.
	 */
	public int compareTo(@NotNull Version ov) {
		return compareTo(ov, false);
	}

	/**
	 * The returned value of this method (-1, 0, or 1) is determined by whether this object is less than, equal to, or greater than the specified object, ov.
	 * (this.compareTo(new Version("1.0.0") < 0 is the same as this < 1.0.0)
	 * @param ov The version to compare to.
	 * @param ignoreDev If we should ignore dev versions in this comparison.
	 */
	public int compareTo(@NotNull Version ov, boolean ignoreDev) {
		if (make != ov.make) return Integer.compare(make, ov.make);
		if (major != ov.major) return Integer.compare(major, ov.major);
		if (minor != ov.minor) return Integer.compare(minor, ov.minor);
		if (!ignoreDev) {
			if (dev != ov.dev) {
				if (dev == 0) return 1; //0 is the last "dev" version, as it is not a dev.
				if (ov.dev == 0) return -1;
				return Integer.compare(dev, ov.dev);
			}
		}

		return 0; // The versions are equal.
	}

	public boolean isDev() {
		return dev != 0;
	}

	@Override
	public String toString() {
		return make + "." + major + "." + minor + (!curIsSpecial ? (dev == 0 ? "" : "-dev" + dev) : (dev == 0 ? "" : "-" + specialPrefix + dev));
	}

	public int[] toArray() {
		return new int[] { make, major, minor, dev };
	}
}
