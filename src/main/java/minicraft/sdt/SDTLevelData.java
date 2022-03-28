package minicraft.sdt;

// import minicraft.core.Game;
import minicraft.saveload.Version;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.nullicorn.nedit.*;
import me.nullicorn.nedit.type.NBTCompound;
import me.nullicorn.nedit.type.NBTList;
import me.nullicorn.nedit.type.TagType;

public class SDTLevelData extends SDT {
    public Version DataVersion;
    public static Version Version = new Version("0.0.1")/*Game.levelSDTversion*/;
    public final int w;
    public final int h;
    public NBTCompound[] NBTdata;
    public SDTLevelData(byte[] b) {
        NBTCompound nbt = null;
        try {
            nbt = NBTReader.read(new ByteArrayInputStream(b));
        } catch (IOException e) {
            e.printStackTrace();
        }
        DataVersion = new Version(nbt.getString("DataVersion"));
        w = nbt.getInt("w", 0);
        h = nbt.getInt("h", 0);
        List<NBTCompound> l = new ArrayList<>();
        nbt.getList("data").iterator().forEachRemaining(o -> l.add((NBTCompound)o));
        NBTdata = Arrays.copyOf(l.toArray(), l.size(), NBTCompound[].class);
    }
    public SDTLevelData(int wl, int hl) {
        w = wl;
        h = hl;
        DataVersion = Version;
        NBTdata = new NBTCompound[w*h];
        for (int x = 0; x<w; x++) {
            for (int y = 0; y<h; y++) {
                NBTdata[x*w+y] = createDataUnit(x, y);
            }
        }
    }
    private static NBTCompound createDataUnit(int x, int y) {
        NBTCompound c = new NBTCompound();
        c.put("x", x);
        c.put("y", y);
        c.put("properties", new NBTCompound());
        return c;
    }
    public NBTCompound toNBT() {
        NBTCompound res = new NBTCompound();
        res.put("DataVersion", DataVersion.toString());
        res.put("w", w);
        res.put("h", h);
        NBTList dt = new NBTList(TagType.COMPOUND);
        dt.addAll(Arrays.asList(NBTdata));
        res.put("data", dt);
        return res;
    }
}