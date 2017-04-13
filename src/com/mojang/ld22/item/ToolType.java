package com.mojang.ld22.item;

public class ToolType {
	// creates the various tool types; the number next to the name deals with the sprite's location.
	public static ToolType shovel = new ToolType("Shovel", 0);
	public static ToolType hoe = new ToolType("Hoe", 1);
	public static ToolType sword = new ToolType("Sword", 2);
	public static ToolType pickaxe = new ToolType("Pickaxe", 3);
	public static ToolType axe = new ToolType("Axe", 4);
	public static ToolType bow = new ToolType("Bow", 5);
	public static ToolType rod = new ToolType("Rod", 6, 15); // if there's a third number, it specifies durability.
	public static ToolType claymore = new ToolType("Claymore", 7);
	public static ToolType hatchet = new ToolType("Hatchet", 10);
	public static ToolType spade = new ToolType("Spade", 11);
	public static ToolType pick = new ToolType("Pick", 12);
	
	public final String name;
	public final int sprite; // sprite location on the spritesheet
	public final int durability;
	
	private ToolType(String name, int sprite, int dur) {
		this.name = name;
		this.sprite = sprite;
		durability = dur;
	}
	private ToolType(String name, int sprite) {
		this(name, sprite, -1); // durability defualts to -1 if not specified (means infinite durability)
	}
}
