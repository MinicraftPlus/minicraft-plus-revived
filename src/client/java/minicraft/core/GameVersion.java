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
			// Versions are not saved in earlier versions, so they are ignored.
			case "1.9.1": return 100;
			case "1.9.2": return 101;
			case "1.9.3-dev1": return 102;
			case "1.9.3-dev2": return 103;
			case "1.9.3": return 104;
			case "1.9.4-dev1": return 105;
			case "1.9.4-dev2": return 106;
			case "1.9.4-dev3": return 107;
			case "1.9.4-dev4": return 108;
			case "1.9.4-dev5": return 109;
			case "1.9.4-dev6": return 110;
			case "1.9.4-dev7": return 111;
			case "1.9.4": return 112;
			case "2.0.0-dev1": return 113;
			case "2.0.0-dev2": return 114;
			case "2.0.0-dev3": return 115;
			case "2.0.0": return 116;
			case "2.0.1-dev1": return 117;
			case "2.0.1-dev2": return 118;
			case "2.0.1-dev3": return 119;
			case "2.0.1-dev4": return 120;
			case "2.0.1-dev5": return 121;
			case "2.0.1-dev6": return 122;
			case "2.0.1-dev7": return 123;
			case "2.0.1-dev8": return 124;
			case "2.0.1-dev9": return 125;
			case "2.0.1": return 126;
			case "2.0.2-dev1": return 127;
			case "2.0.2-dev2": return 128;
			case "2.0.2-dev3": return 129;
			case "2.0.2": return 130;
			case "2.0.3-dev1": return 131;
			case "2.0.3-dev2": return 132;
			case "2.0.3-dev3": return 133;
			case "2.0.3-dev4": return 134;
			case "2.0.3-dev5": return 135;
			case "2.0.3-dev6": return 136;
			case "2.0.3": return 137;
			case "2.0.4-dev1": return 138;
			case "2.0.4-dev2": return 139;
			case "2.0.4-dev3": return 140;
			case "2.0.4-dev4": return 141;
			case "2.0.4-dev5": return 142;
			case "2.0.4-dev6": return 143;
			case "2.0.4-dev7": return 144;
			case "2.0.4-dev8": return 145;
			case "2.0.4-dev9": return 146;
			case "2.0.4": return 147;
			case "2.0.5-dev1": return 148;
			case "2.0.5-dev2": return 149;
			case "2.0.5-dev3": return 150;
			case "2.0.5-dev4": return 151;
			case "2.0.5-dev5": return 152;
			case "2.0.5-dev6": return 153;
			case "2.0.5": return 154;
			case "2.0.6-dev1": return 155;
			case "2.0.6-dev2": return 156;
			case "2.0.6-dev3": return 157;
			case "2.0.6-dev4": return 158;
			case "2.0.6": return 159;
			case "2.0.7-dev1": return 160;
			case "2.0.7-dev2": return 161;
			case "2.0.7-dev3": return 162;
			case "2.0.7-dev4": return 163;
			case "2.0.7": return 164;
			case "2.1.0-dev1": return 165;
			case "2.1.0-dev2": return 166;
			case "2.1.0-dev3": return 167;
			case "2.1.0": return 168;
			case "2.1.1": return 169;
			case "2.1.2": return 170;
			case "2.1.3": return 171;
			case "2.2.0-dev1": return 172;
			case "2.2.0-dev2": return 173;
			case "2.2.0-dev3": return 174;
			case "2.2.0-dev4": return 175;
			case "2.2.0-dev5": return 176;
			case "2.2.0-dev6": return 177;
			case "2.2.0-dev7": return 178;
			case "2.2.0": return 179;
			default: throw new AssertionError(String.format("Missing handle for version \"%s\"", version));
		}
	}
}
