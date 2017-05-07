package minicraft.level.tile;

import minicraft.Game;
import minicraft.entity.ItemEntity;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.ResourceItem;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.item.resource.Resource;
import minicraft.level.Level;
import minicraft.sound.Sound;

public class GrassTile extends Tile {
	public GrassTile(int id) {
		super(id);
		connectsToGrass = true;
		maySpawn = true;
	}

	//public static int col0 = Color.get(131, 131, 141, 322);
	//public static int col00 = Color.get(131, 131, 141, 322);

	public static int col = Color.get(141, 141, 252, 321);
	public static int colt = Color.get(141, 141, 252, 321);
	/*
	public static int col2 = Color.get(30, 30, 141, 211);
	public static int col22 = Color.get(20, 30, 141, 211);

	public static int col3 = Color.get(20, 20, 30, 100);
	public static int col33 = Color.get(10, 20, 30, 100);
	*/
	public void render(Screen screen, Level level, int x, int y) {
		//if (Game.time == 0) {
			//int col = col0;
			int transitionColor = colt;

			boolean u = !level.getTile(x, y - 1).connectsToGrass;
			boolean d = !level.getTile(x, y + 1).connectsToGrass;
			boolean l = !level.getTile(x - 1, y).connectsToGrass;
			boolean r = !level.getTile(x + 1, y).connectsToGrass;

			if (!u && !l) {
				screen.render(x * 16 + 0, y * 16 + 0, 0, col, 0);
			} else
				screen.render(x * 16 + 0, y * 16 + 0, (l ? 11 : 12) + (u ? 0 : 1) * 32, transitionColor, 0);

			if (!u && !r) {
				screen.render(x * 16 + 8, y * 16 + 0, 1, col, 0);
			} else
				screen.render(x * 16 + 8, y * 16 + 0, (r ? 13 : 12) + (u ? 0 : 1) * 32, transitionColor, 0);

			if (!d && !l) {
				screen.render(x * 16 + 0, y * 16 + 8, 2, col, 0);
			} else
				screen.render(x * 16 + 0, y * 16 + 8, (l ? 11 : 12) + (d ? 2 : 1) * 32, transitionColor, 0);
			if (!d && !r) {
				screen.render(x * 16 + 8, y * 16 + 8, 3, col, 0);
			} else
				screen.render(x * 16 + 8, y * 16 + 8, (r ? 13 : 12) + (d ? 2 : 1) * 32, transitionColor, 0);
		//}
		/*if (Game.time == 1) {

			int col = col1;
			int transitionColor = col11;

			boolean u = !level.getTile(x, y - 1).connectsToGrass;
			boolean d = !level.getTile(x, y + 1).connectsToGrass;
			boolean l = !level.getTile(x - 1, y).connectsToGrass;
			boolean r = !level.getTile(x + 1, y).connectsToGrass;

			if (!u && !l) {
				screen.render(x * 16 + 0, y * 16 + 0, 0, col, 0);
			} else
				screen.render(x * 16 + 0, y * 16 + 0, (l ? 11 : 12) + (u ? 0 : 1) * 32, transitionColor, 0);

			if (!u && !r) {
				screen.render(x * 16 + 8, y * 16 + 0, 1, col, 0);
			} else
				screen.render(x * 16 + 8, y * 16 + 0, (r ? 13 : 12) + (u ? 0 : 1) * 32, transitionColor, 0);

			if (!d && !l) {
				screen.render(x * 16 + 0, y * 16 + 8, 2, col, 0);
			} else
				screen.render(x * 16 + 0, y * 16 + 8, (l ? 11 : 12) + (d ? 2 : 1) * 32, transitionColor, 0);
			if (!d && !r) {
				screen.render(x * 16 + 8, y * 16 + 8, 3, col, 0);
			} else
				screen.render(x * 16 + 8, y * 16 + 8, (r ? 13 : 12) + (d ? 2 : 1) * 32, transitionColor, 0);
		}
		if (Game.time == 2) {

			int col = col2;
			int transitionColor = col22;

			boolean u = !level.getTile(x, y - 1).connectsToGrass;
			boolean d = !level.getTile(x, y + 1).connectsToGrass;
			boolean l = !level.getTile(x - 1, y).connectsToGrass;
			boolean r = !level.getTile(x + 1, y).connectsToGrass;

			if (!u && !l) {
				screen.render(x * 16 + 0, y * 16 + 0, 0, col, 0);
			} else
				screen.render(x * 16 + 0, y * 16 + 0, (l ? 11 : 12) + (u ? 0 : 1) * 32, transitionColor, 0);

			if (!u && !r) {
				screen.render(x * 16 + 8, y * 16 + 0, 1, col, 0);
			} else
				screen.render(x * 16 + 8, y * 16 + 0, (r ? 13 : 12) + (u ? 0 : 1) * 32, transitionColor, 0);

			if (!d && !l) {
				screen.render(x * 16 + 0, y * 16 + 8, 2, col, 0);
			} else
				screen.render(x * 16 + 0, y * 16 + 8, (l ? 11 : 12) + (d ? 2 : 1) * 32, transitionColor, 0);
			if (!d && !r) {
				screen.render(x * 16 + 8, y * 16 + 8, 3, col, 0);
			} else
				screen.render(x * 16 + 8, y * 16 + 8, (r ? 13 : 12) + (d ? 2 : 1) * 32, transitionColor, 0);
		}
		if (Game.time == 3) {

			int col = col3;
			int transitionColor = col33;

			boolean u = !level.getTile(x, y - 1).connectsToGrass;
			boolean d = !level.getTile(x, y + 1).connectsToGrass;
			boolean l = !level.getTile(x - 1, y).connectsToGrass;
			boolean r = !level.getTile(x + 1, y).connectsToGrass;

			if (!u && !l) {
				screen.render(x * 16 + 0, y * 16 + 0, 0, col, 0);
			} else
				screen.render(x * 16 + 0, y * 16 + 0, (l ? 11 : 12) + (u ? 0 : 1) * 32, transitionColor, 0);

			if (!u && !r) {
				screen.render(x * 16 + 8, y * 16 + 0, 1, col, 0);
			} else
				screen.render(x * 16 + 8, y * 16 + 0, (r ? 13 : 12) + (u ? 0 : 1) * 32, transitionColor, 0);

			if (!d && !l) {
				screen.render(x * 16 + 0, y * 16 + 8, 2, col, 0);
			} else
				screen.render(x * 16 + 0, y * 16 + 8, (l ? 11 : 12) + (d ? 2 : 1) * 32, transitionColor, 0);
			if (!d && !r) {
				screen.render(x * 16 + 8, y * 16 + 8, 3, col, 0);
			} else
				screen.render(x * 16 + 8, y * 16 + 8, (r ? 13 : 12) + (d ? 2 : 1) * 32, transitionColor, 0);
		}*/
	}

	public void tick(Level level, int xt, int yt) {
		if (random.nextInt(40) != 0) return;

		int xn = xt;
		int yn = yt;

		if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
		else yn += random.nextInt(2) * 2 - 1;

		if (level.getTile(xn, yn) == Tile.dirt) {
			level.setTile(xn, yn, this, 0);
		}
		if (level.getTile(xn, yn) == Tile.lightdirt) {
			level.setTile(xn, yn, this, 0);
		}
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.shovel) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tile.dirt, 0);
					Sound.monsterHurt.play();
					if (random.nextInt(5) == 0) {
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.seeds),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.seeds),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						return true;
					}
				}
			}
			if (tool.type == ToolType.spade) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tile.dirt, 0);
					Sound.monsterHurt.play();
					if (random.nextInt(5) == 0) {
						return true;
					}
				}
			}
			if (tool.type == ToolType.hoe) {
				if (player.payStamina(4 - tool.level)) {
					Sound.monsterHurt.play();
					if (random.nextInt(5) == 0) {
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.seeds),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						return true;
					}
					level.setTile(xt, yt, Tile.farmland, 0);
					return true;
				}
			}
		}
		return false;
	}
}
