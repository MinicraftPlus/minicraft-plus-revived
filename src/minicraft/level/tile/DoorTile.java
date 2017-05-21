package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.entity.Mob;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.sound.Sound;

public class DoorTile extends Tile {
	private Sprite closedSprite = new Sprite(2, 24, 2, 2);
	private Sprite openSprite = new Sprite(0, 24, 2, 2);
	
	protected static void addInstances() {
		for(Material mat: Material.values())
			Tiles.add(new DoorTile(mat.name() + " Door", mat));
	}
	
	protected Material type;
	
	private DoorTile(String name, Material type) {
		super(name, (Sprite)null);
		this.type = type;
		switch(type) {
			case Wood:
				closedSprite.color = Color.get(320, 430, 210, 430);
				openSprite.color = Color.get(320, 430, 430, 210);
				break;
			case Stone:
				closedSprite.color = Color.get(444, 333, 222, 333);
				openSprite.color = Color.get(444, 333, 333, 222);
				break;
			case Obsidian:
				closedSprite.color = Color.get(203, 102, 203, 102);
				openSprite.color = Color.get(203, 102);
				break;
		}
		sprite = closedSprite;
	}
	/*
	public void render(Screen screen, Level level, int x, int y) {
		int col = Color.get(444, 333, 222, 333);
		
		screen.render(x * 16 + 0, y * 16 + 0, 2 + 24 * 32, col, 0);
		screen.render(x * 16 + 8, y * 16 + 0, 3 + 24 * 32, col, 0);
		screen.render(x * 16 + 0, y * 16 + 8, 2 + 25 * 32, col, 0);
		screen.render(x * 16 + 8, y * 16 + 8, 3 + 25 * 32, col, 0);
	}
	*/
	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Pickaxe) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tiles.get("Stone Bricks"), 0);
					level.dropItem(xt*16, yt*16, Items.get(type.name() + " Door"));
					Sound.monsterHurt.play();
					return true;
				}
			}
			/*if (tool.type == ToolType.pick) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tiles.get("Stone Bricks"), 0);
					level.add(
							new ItemEntity(
									Items.get("Stone Door"),
									xt * 16 + random.nextInt(10) + 3,
									yt * 16 + random.nextInt(10) + 3));
					Sound.monsterHurt.play();
					return true;
				}
			}*/
		}
		return false;
	}

	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
		if(source instanceof Player) {
			boolean closed = level.getData(x, y) == 0;
			level.setData(x, y, closed?1:0);
			sprite = closed?openSprite:closedSprite;
		}
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}
}
