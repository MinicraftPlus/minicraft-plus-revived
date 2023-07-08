package minicraft.item;

import minicraft.saveload.Load;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class FishingData {

    public static final List<String> fishData = getData("fish");

    public static final List<String> toolData = getData("tool");

    public static final List<String> junkData = getData("junk");

    public static final List<String> rareData = getData("rare");

    public static List<String> getData(String name) {
        List<String> data;
        try {
            data = Collections.unmodifiableList(Load.loadFile("/resources/data/fishing/" + name + "_loot.txt"));
        } catch (IOException e) {
            e.printStackTrace();
            data = Collections.emptyList();
        }
        return data;
    }
}
