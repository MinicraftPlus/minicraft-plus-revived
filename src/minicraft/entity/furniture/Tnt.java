package minicraft.entity.furniture;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;

public class Tnt extends Furniture implements ActionListener {
	private static int FUSE_TIME = 90;
	private static int BLAST_RADIUS = 32;
	//private static int BLAST_RADIUSTWO = 2000;
	private static int BLAST_DAMAGE = 30;
	
	private static int color = Color.get(-1, 200, 300, 555);
	
	private int ftik = 0;
	private boolean fuseLit = false;
	private Timer explodeTimer;
	private Level levelSave;
	
	public Tnt() {
		super("Tnt", new Sprite(14, 8, 2, 2, color), 3, 2);
		fuseLit = false;
		ftik = 0;
		
		explodeTimer = new Timer(300, this);
	}

	public void tick() {
		super.tick();
		
		if (fuseLit) {
			ftik++;
			
			if(ftik >= FUSE_TIME) {
				// blow up
				List<Entity> entitiesInRange = level.getEntitiesInRect(new Rectangle(x, y, BLAST_RADIUS*2, BLAST_RADIUS*2, Rectangle.CENTER_DIMS));
				
				for(Entity e: entitiesInRange) {
					float dist = (float) Math.hypot(e.x - x, e.y - y);
					int dmg = (int) (BLAST_DAMAGE * (1 - (dist / BLAST_RADIUS))) + 1;
					if(e instanceof Mob)
						((Mob)e).hurt(this, dmg);
					if(e instanceof Tnt) {
						Tnt tnt = (Tnt) e;
						if (!tnt.fuseLit) {
							tnt.fuseLit = true;
							Sound.fuse.play();
							tnt.ftik = FUSE_TIME * 2 / 3;
						}
					}
				}
				
				Sound.explode.play();
				
				int xt = x >> 4;
				int yt = (y - 2) >> 4;
				
				level.setAreaTiles(xt, yt, 1, Tiles.get("explode"), 0);
				
				levelSave = level;
				explodeTimer.start();
				super.remove();
			}
		}
	}
	
	public void render(Screen screen) {
		if(fuseLit) {
			int colFctr = 100*((ftik%15)/5) + 200;
			col = Color.get(-1, colFctr, colFctr+100, 555);
		}
		super.render(screen);
	}
	
	public void actionPerformed(ActionEvent e) {
		explodeTimer.stop();
		int xt = x >> 4;
		int yt = (y - 2) >> 4;
		levelSave.setAreaTiles(xt, yt, 1, Tiles.get("hole"), 0);
		levelSave = null;
	}
	
	@Override
	public boolean interact(Player player, Item heldItem, Direction attackDir) {
		if (!fuseLit) {
			fuseLit = true;
			Sound.fuse.play();
			return true;
		}
		
		return false;
	}
	
	protected String getUpdateString() {
		String updates = super.getUpdateString() + ";";
		updates += "fuseLit,"+fuseLit+
		";ftik,"+ftik;
		
		return updates;
	}
	
	protected boolean updateField(String field, String val) {
		if(super.updateField(field, val)) return true;
		switch(field) {
			case "fuseLit": fuseLit = Boolean.parseBoolean(val); return true;
			case "ftik": ftik = Integer.parseInt(val); return true;
		}
		
		return false;
	}
}
