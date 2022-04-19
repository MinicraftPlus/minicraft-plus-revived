package minicraft.sdt;

import minicraft.item.Item;
import minicraft.item.StackableItem;
import minicraft.saveload.Version;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

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
        for (int i = 0; i<is.size(); i++) items.add(is.getCompound(i));
    }
    public static NBTCompound toNBT(Version v, ArrayList<Item> inventory) {
        NBTCompound res = new NBTCompound();
        res.put("DataVersion", v.toString());
        NBTList dt = new NBTList(TagType.COMPOUND);
        for (Item item : inventory) {
            NBTCompound itemdata = new NBTCompound();
            itemdata.put("name", item.getName());
            itemdata.put("data", item.data);
            if (item instanceof StackableItem) itemdata.put("count", ((StackableItem)item).count);
            dt.add(itemdata);
        }
        res.put("items", dt);
        return res;
    }
}