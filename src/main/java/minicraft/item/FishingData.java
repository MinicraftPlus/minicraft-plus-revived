package minicraft.item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import minicraft.saveload.Load;

public class FishingData {

    public static final List<String> fishData = getData("fish");

    public static final List<String> toolData = getData("tool");

    public static final List<String> junkData = getData("junk");

    public static final List<String> rareData = getData("rare");

    public static List<String> getData(String name) {
        List<String> data;
        try {
            data = Load.loadFile("/resources/fishing/" + name + "_loot.txt");
        } catch (IOException e) {
            e.printStackTrace();
            data = new ArrayList<>();
        }
        return data;
    }
}
