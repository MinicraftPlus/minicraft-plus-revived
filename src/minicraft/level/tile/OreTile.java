package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.Mob;
import minicraft.entity.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.ResourceItem;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.item.resource.Resource;
import minicraft.level.Level;
import minicraft.screen.ModeMenu;

/// this is all the spikey stuff (except "cloud cactus")
public class OreTile extends Tile {
	private OreType type;
	private int color, oreColor;
	
	public static enum OreType {
        IRON, LAPIS, GOLD, GEM
    }
	
	public OreTile(int id, OreType oreType, int col) {
		super(id);
        type = oreType;
        oreColor = col;
	}

	public void render(Screen screen, Level level, int x, int y) {
		color = (oreColor & 0xffffff00) + Color.get(DirtTile.dCol(level.depth));
		screen.render(x * 16 + 0, y * 16 + 0, 17 + 1 * 32, color, 0);
		screen.render(x * 16 + 8, y * 16 + 0, 18 + 1 * 32, color, 0);
		screen.render(x * 16 + 0, y * 16 + 8, 17 + 2 * 32, color, 0);
		screen.render(x * 16 + 8, y * 16 + 8, 18 + 2 * 32, color, 0);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}

	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
		int playHurt;
		if (ModeMenu.creative) playHurt = random.nextInt(4);
		else {
			playHurt = 0;
		}
		hurt(level, x, y, playHurt);
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {

		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.pickaxe) {
				if (player.payStamina(6 - tool.level)) {
					hurt(level, xt, yt, 1);
					return true;
				}
			}
		}
		return false;
	}

        public ResourceItem getOre(){
            switch(type){
                case IRON: return new ResourceItem(Resource.ironOre);
                case LAPIS: return new ResourceItem(Resource.lapisOre);
                case GOLD: return new ResourceItem(Resource.goldOre);
                case GEM: return new ResourceItem(Resource.gem);
                default: return new ResourceItem(Resource.ironOre);
            }
        }
        
        
	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + 1;
		int oreH;
		if (ModeMenu.creative) oreH = 1;
		else {
			oreH = random.nextInt(10) + 3;
		}
		level.add(new SmashParticle(x * 16 + 8, y * 16 + 8));
		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.get(-1, 500, 500, 500)));
		if (dmg > 0) {
			int count = random.nextInt(2);
			if (damage >= oreH) {
				level.setTile(x, y, Tile.dirt, 0);
				count += 2;
			} else {
				level.setData(x, y, damage);
			}
			for (int i = 0; i < count; i++) {
				level.add(new ItemEntity(getOre(), x * 16 + random.nextInt(10) + 3, y * 16 + random.nextInt(10) + 3));
			}
		}
	}

	public void bumpedInto(Level level, int x, int y, Entity entity) {}
}
