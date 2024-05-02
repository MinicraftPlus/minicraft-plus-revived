package minicraft.core;

import java.time.LocalDateTime;

public final class GameVersion {
	public final String id;
	public final int dataVersion;
	public final int packResourceVersion, packDataVersion;
	public final boolean stable;
	public final boolean useEditor;
	public final String seriesId;
	public final LocalDateTime buildTime;

	public GameVersion(String id, int dataVersion, int packResourceVersion, int packDataVersion, boolean stable,
	                   boolean useEditor, String seriesId, LocalDateTime buildTime) {
		this.id = id;
		this.dataVersion = dataVersion;
		this.packResourceVersion = packResourceVersion;
		this.packDataVersion = packDataVersion;
		this.stable = stable;
		this.useEditor = useEditor;
		this.seriesId = seriesId;
		this.buildTime = buildTime;
	}

	public static int mapOldVersionToDataVersion(String version) { // This should be better than using a JSON file.
		switch (version) {
			case "": return 100;
			default: throw new AssertionError(String.format("Missing handle for version \"%s\"", version));
		}
	}
}
