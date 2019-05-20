package minicraft.screen;

import minicraft.level.tile.Tiles;

public enum MapData {

    GRASS(Tiles.get("Grass").id, 50),
    DIRT(Tiles.get("Dirt").id, 321),
    FLOWER(Tiles.get("Flower").id, 50),
    HOLE(Tiles.get("Hole").id, 222),
    WATER(Tiles.get("Water").id, 5),
    LAVA(Tiles.get("Lava").id, 511),
    ROCK(Tiles.get("Rock").id, 333),
    HARD_ROCK(Tiles.get("Hard Rock").id, 202),
    // don't reveal ores on the map by making them look the same as rocks
    IRON_ORE(Tiles.get("Iron Ore").id, 333),
    LAPIS_ORE(Tiles.get("Lapis").id, 333),
    GOLD_ORE(Tiles.get("Gold Ore").id, 333),
    GEM_ORE(Tiles.get("Gem Ore").id, 333),
    JUNGLE(Tiles.get("Jungle").id, 210),
    BAMBOO(Tiles.get("Bamboo").id, 210),
    SPIKES(Tiles.get("Spikes").id, 232),
    TREE(Tiles.get("Tree").id, 30),
    SAND(Tiles.get("Sand").id, 550),
    CACTUS(Tiles.get("Cactus").id, 550),
    STAIRS_UP(Tiles.get("Stairs Up").id, 303),
    STAIRS_DOWN(Tiles.get("Stairs Down").id, 303),
    WOOD_FLOOR(Tiles.get("Wood Planks").id, 430),
    WOOD_WALL(Tiles.get("Wood Wall").id, 540),
    WOOD_DOOR(Tiles.get("Wood Door").id, 540),
    STONE_FLOOR(Tiles.get("Stone Bricks").id, 444),
    STONE_WALL(Tiles.get("Stone Wall").id, 555),
    STONE_DOOR(Tiles.get("Stone Door").id, 555),
    OBSIDIAN_FLOOR(Tiles.get("Obsidian").id, 203),
    OBSIDIAN_WALL(Tiles.get("Obsidian Wall").id, 304),
    OBSIDIAN_DOOR(Tiles.get("Obsidian Door").id, 304),
    WOOL(Tiles.get("Wool").id, 555),
    RED_WOOL(Tiles.get("Red Wool").id, 400),
    YELLOW_WOOL(Tiles.get("Yellow Wool").id, 440),
    GREEN_WOOL(Tiles.get("Green Wool").id, 40),
    BLUE_WOOL(Tiles.get("Blue Wool").id, 5),
    BLACK_WOOL(Tiles.get("Black Wool").id, 0),
    FARMLAND(Tiles.get("Farmland").id, 422),
    WHEAT(Tiles.get("Wheat").id, 350),
    INFINITE_FALL(Tiles.get("Infinite Fall").id, 20),
    CLOUD(Tiles.get("Cloud").id, 444),
    CLOUD_CACTUS(Tiles.get("Cloud Cactus").id, 555);

    public int tileID;
    public int color;

    MapData(int id, int color) {
        tileID = id;
        this.color = color;
    }

}
