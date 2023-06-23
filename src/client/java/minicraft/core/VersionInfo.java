package minicraft.core;

import minicraft.saveload.Version;
import org.json.JSONObject;

public class VersionInfo {

	public final Version version;
	public final String releaseUrl;
	public final String releaseName;

	public VersionInfo(JSONObject releaseInfo) {
		String versionTag = releaseInfo.getString("tag_name").substring(1); // Cut off the "v" at the beginning
		version = new Version(versionTag);

		releaseUrl = releaseInfo.getString("html_url");

		releaseName = releaseInfo.getString("name");
	}

	public VersionInfo(Version version, String releaseUrl, String releaseName) {
		this.version = version;
		this.releaseUrl = releaseUrl;
		this.releaseName = releaseName;
	}
}
