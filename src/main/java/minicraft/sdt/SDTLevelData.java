package minicraft.sdt;

import minicraft.core.Game;
import minicraft.saveload.Version;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import me.nullicorn.nedit.*;
import me.nullicorn.nedit.type.NBTCompound;

public class SDTLevelData extends SDT {
    public SDTLevelData(byte[] b) {
        NBTCompound nbt;
        try {
            nbt = NBTReader.read(new ByteArrayInputStream(b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



// public class SDTLevelData extends SDT {
//     public Version DataVersion;
//     public static Version Version = Game.levelSDTversion;
//     private SDTLevelDataUnit[] data;
//     public final int w;
//     public final int h;
//     public SDTLevelData(JSONObject json) {
//         DataVersion = new Version(json.getString("DataVersion"));
//         w = json.getInt("w");
//         h = json.getInt("h");
//         data = new SDTLevelDataUnit[w*h];
//         JSONArray ts = json.getJSONArray("data");
//         for (int x = 0; x<w; x++) {
//             JSONArray xts = ts.getJSONArray(x);
//             for (int y = 0; y<h; y++) {
//                 data[x*w+y] = new SDTLevelDataUnit(xts.getJSONObject(y));
//             }
//         }
//     }
//     public SDTLevelData(int w, int h, SDTLevelDataUnit[] unit) {
//         DataVersion = Version;
//         this.w = w;
//         this.h = h;
//         data = unit;
//     }
//     public SDTLevelData(int w, int h) {
//         SDTLevelDataUnit[] data = new SDTLevelDataUnit[w*h];
//         for (int i = 0; i<data.length; i++) data[i] = new SDTLevelDataUnit(i, i);
//         SDTLevelData();
//     }
// }
