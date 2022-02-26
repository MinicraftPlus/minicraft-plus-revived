package minicraft.sdt;

import org.json.JSONObject;

import minicraft.core.Game;
import minicraft.saveload.Version;

public class SDTLevel extends SDT {
    public Version DataVersion;
    public static Version Version = Game.levelSDTversion;
    public SDTLevel(JSONObject json) {

    }
}
