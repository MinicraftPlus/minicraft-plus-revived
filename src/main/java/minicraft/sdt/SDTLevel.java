// package minicraft.sdt;

// import org.json.JSONArray;
// import org.json.JSONObject;

// import minicraft.core.Game;
// import minicraft.saveload.Version;

// public class SDTLevel extends SDT {
//     public Version DataVersion;
//     public static Version Version = Game.levelSDTversion;
//     private SDTLevelUnit[][] tiles;
//     public final int w;
//     public final int h;
//     public SDTLevel(JSONObject json) {
//         DataVersion = new Version(json.getString("DataVersion"));
//         w = json.getInt("w");
//         h = json.getInt("h");
//         tiles = new SDTLevelUnit[w][h];
//         JSONArray ts = json.getJSONArray("tiles");
//         for (int x = 0; x<w; x++) {
//             JSONArray xts = ts.getJSONArray(x);
//             for (int y = 0; y<h; y++) {
//                 tiles[x][y] = new SDTLevelUnit(xts.getJSONObject(y));
//             }
//         }
//     }
//     public SDTLevelUnit getUnit(int x, int y) {
//         return tiles[x][y];
//     }
// }
