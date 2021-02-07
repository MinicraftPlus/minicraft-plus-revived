package minicraft.saveload;

import org.jetbrains.annotations.NotNull;

public class Version implements Comparable<Version> {
	private int make, major, minor, dev;
	private boolean valid = true;
	
	public Version(String version) { this(version, true); }
	private Version(String version, boolean printError) {
		String[] nums = version.split("\\.");
		try {
			if(nums.length > 0) make = Integer.parseInt(nums[0]);
			else make = 0;
			
			if(nums.length > 1) major = Integer.parseInt(nums[1]);
			else major = 0;
			
			String min;
			if(nums.length > 2) min = nums[2];
			else min = "";
			
			if(min.contains("-")) {
				String[] mindev = min.split("-");
				minor = Integer.parseInt(mindev[0]);
				dev = Integer.parseInt(mindev[1].replace("pre", "").replace("dev", ""));
			} else {
				if(!min.equals("")) minor = Integer.parseInt(min);
				else minor = 0;
				dev = 0;
			}
		} catch(NumberFormatException ex) {
			if(printError) System.err.println("INVALID version number: \"" + version + "\"");
			valid = false;
		} catch(Exception ex) {
			if(printError) ex.printStackTrace();
			valid = false;
		}
	}
	
	public boolean isValid() { return valid; }
	public static boolean isValid(String version) { return new Version(version, false).isValid(); }
	
	// the returned value of this method (-1, 0, or 1) is determined by whether this object is less than, equal to, or greater than the specified object.
	public int compareTo(@NotNull Version ov) {
		if(make != ov.make) return Integer.compare(make, ov.make);
		if(major != ov.major) return Integer.compare(major, ov.major);
		if(minor != ov.minor) return Integer.compare(minor, ov.minor);
		if(dev != ov.dev) {
			if(dev == 0) return 1; //0 is the last "dev" version, as it is not a dev.
			if(ov.dev == 0) return -1;
			return Integer.compare(dev, ov.dev);
		}
		return 0; // the versions are equal.
	}
	
	@Override
	public String toString() { return make + "." + major + "." + minor + (dev == 0 ? "" : "-dev" + dev); }
}
