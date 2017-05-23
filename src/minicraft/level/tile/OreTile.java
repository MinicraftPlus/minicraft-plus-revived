package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.entity.Mob;
import minicraft.entity.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.screen.ModeMenu;

/// this is all the spikey stuff (except "cloud cactus")
public class OreTile extends Tile {
	private Sprite sprite;
	private OreType type;
	
	public static enum OreType {
        Iron (Items.get("Iron Ore"), Color.get(-1, 100, 322, 544)),
		Lapis (Items.get("Lapis"), Color.get(-1, 005, 115, 115)),
		Gold (Items.get("Gold Ore"), Color.get(-1, 110, 440, 553)),
		Gem (Items.get("Gem"), Color.get(-1, 101, 404, 545));
		
		private Item drop;
		public final int color;
		
		private OreType(Item drop, int color) {
			this.drop = drop;
			this.color = color;
		}
		
		protected Item getOre() {
			return drop.clone();
		}
    }
	
	protected OreTile(OreType o) {
		super((o == OreTile.OreType.Lapis ? "Lapis" : o.name() + " Ore"), new Sprite(17, 1, 2, 2, o.color));
        this.type = o;
		this.sprite = super.sprite;
	}

	public void render(Screen screen, Level level, int x, int y) {
		sprite.color = (type.color & 0xffffff00) + Color.get(DirtTile.dCol(level.depth));
		sprite.render(screen, x*16, y*16);
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
			if (tool.type == ToolType.Pickaxe) {
				if (player.payStamina(6 - tool.level)) {
					hurt(level, xt, yt, 1);
					return true;
				}
			}
		}
		return false;
	}
	
    public Item getOre() {
        return type.getOre();
    }
    
	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + 1;
		int oreH;
		if (ModeMenu.creative) oreH = 1;
		else {
			oreH = random.nextInt(10) + 3;
		}
		level.add(new SmashParticle(x * 16, y * 16));
		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.get(-1, 500)));
		if (dmg > 0) {
			int count = random.nextInt(2);
			if (damage >= oreH) {
				level.setTile(x, y, Tiles.get("dirt"));
				count += 2;
			} else {
				level.setData(x, y, damage);
			}
			level.dropItem(x*16+8, y*16+8, count, type.getOre());
		}
	}

	public void bumpedInto(Level level, int x, int y, Entity entity) {
		/// this was used at one point to hurt the player if they touched the ore; that's probably why the sprite is so spikey-looking.
	}
}
