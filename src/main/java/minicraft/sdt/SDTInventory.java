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

public class SDTInventory extends SDT {
    public Version DataVersion;
    public static Version VERSION = new Version("0.0.1")/*Game.levelSDTversion*/;
    public ArrayList<NBTCompound> items = new ArrayList<>();
    public SDTInventory(byte[] b) {
        NBTCompound nbt = null;
        try {
            nbt = NBTReader.read(new ByteArrayInputStream(b));
        } catch (IOException e) {
            e.printStackTrace();
        }
        DataVersion = new Version(nbt.getString("DataVersion"));
        NBTList is = nbt.getList("items");
        for (int i = 0; i<is.size(); i++) items.add(is.getCompound(i);
    }
    public NBTCompound toNBT() {
        NBTCompound res = new NBTCompound();
        res.put("DataVersion", DataVersion.toString());
        res.put("w", w);
        res.put("h", h);
        NBTList dt = new NBTList(TagType.COMPOUND);
        dt.addAll(Arrays.asList(data));
        res.put("data", dt);
        return res;
    }
}