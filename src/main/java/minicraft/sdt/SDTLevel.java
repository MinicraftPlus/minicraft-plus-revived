package minicraft.sdt;

import minicraft.saveload.Version;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import me.nullicorn.nedit.*;
import me.nullicorn.nedit.type.NBTCompound;
import me.nullicorn.nedit.type.NBTList;
import me.nullicorn.nedit.type.TagType;

/**
 * It seems that SDT for Level is useless
 */
public class SDTLevel extends SDT {
    public Version DataVersion;
    public static Version VERSION = new Version("0.0.1")/*Game.levelSDTversion*/;
    public final int w;
    public final int h;
    public short[] tiles;
    public SDTLevel(byte[] b) {
        NBTCompound nbt = null;
        try {
            nbt = NBTReader.read(new ByteArrayInputStream(b));
        } catch (IOException e) {
            e.printStackTrace();
        }
        DataVersion = new Version(nbt.getString("DataVersion"));
        w = nbt.getInt("w", 0);
        h = nbt.getInt("h", 0);
        tiles = new short[w*h];
        NBTList t = nbt.getList("tiles");
        for (int i = 0; i<t.size(); i++) tiles[i] = t.getShort(i);
    }
    public SDTLevel(int wl, int hl) {
        w = wl;
        h = hl;
        DataVersion = VERSION;
        tiles = new short[w*h];
    }
    public static NBTCompound toNBT(Version v, int w, int h, short[] tiles) {
        NBTCompound res = new NBTCompound();
        res.put("DataVersion", v.toString());
        res.put("w", w);
        res.put("h", h);
        NBTList dt = new NBTList(TagType.SHORT);
        for (short s : tiles) dt.add(s);
        res.put("tiles", dt);
        return res;
    }
}