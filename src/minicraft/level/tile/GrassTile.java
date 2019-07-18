package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class GrassTile extends Tile {
	private static ConnectorSprite sprite = new ConnectorSprite(GrassTile.class, new Sprite(11, 0, 3, 3, Color.get(141, 141, 252, 321), 3), Sprite.dots(Color.get(141, 141, 252, 321)))
	{
		public boolean connectsTo(Tile tile, boolean isSide) {
			if(!isSide) return true;
			return tile.connectsToGrass;
		}
	};
	
	protected GrassTile(String name) {
		super(name, sprite);
		csprite.sides = csprite.sparse;
		connectsToGrass = true;
		maySpawn = true;
	}

	public void tick(Level level, int xt, int yt) {
		// TODO revise this method.
		if (random.nextInt(40) != 0) return;
		
		int xn = xt;
		int yn = yt;
		
		if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
		else yn += random.nextInt(2) * 2 - 1;

		if (level.getTile(xn, yn) == Tiles.get("dirt")) {
			level.setTile(xn, yn, this);
		}
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					level.setTile(xt, yt, Tiles.get("dirt"));
					Sound.monsterHurt.play();
					if (random.nextInt(5) == 0) { // 20% chance to drop seeds
						level.dropItem(xt*16+8, yt*16+8, 2, Items.get("seeds"));
					}
					return true;
				}
			}
			if (tool.type == ToolType.Hoe) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					level.setTile(xt, yt, Tiles.get("dirt"));
					Sound.monsterHurt.play();
					if (random.nextInt(5) != 0) { // 80% chance to drop seeds
						level.dropItem(xt*16+8, yt*16+8, Items.get("seeds"));
					}
					return true;
				}
			}
			if (tool.type == ToolType.Pickaxe) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					level.setTile(xt, yt, Tiles.get("path"));
					Sound.monsterHurt.play();
				}
			}
		}
		return false;
	}
}
