package minicraft.entity;

import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;
import minicraft.Sound;

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
			
			int colFctr = 100*((ftik%15)/5) + 200;
			col = Color.get(-1, colFctr, colFctr+100, 555);
			
			if(ftik >= FUSE_TIME) {
				// blow up
				List<Entity> entitiesInRange = level.getEntitiesInRect(x - BLAST_RADIUS, x + BLAST_RADIUS, y - BLAST_RADIUS, y + BLAST_RADIUS);
				
				for(Entity e: entitiesInRange) {
					float dist = (float) Math.hypot(e.x - x, e.y - y);
					int dmg = (int) (BLAST_DAMAGE * (1 - (dist / BLAST_RADIUS))) + 1;
					e.hurt(this, dmg, 0);
					if(e instanceof Player)
						((Player)e).payStamina(dmg * 2);
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
	
	public void actionPerformed(ActionEvent e) {
		explodeTimer.stop();
		int xt = x >> 4;
		int yt = (y - 2) >> 4;
		levelSave.setAreaTiles(xt, yt, 1, Tiles.get("hole"), 0);
		levelSave = null;
	}
	
	public void hurt(Mob m, int dmg, int attackDir) {
		if (!fuseLit) {
			fuseLit = true;
			Sound.fuse.play();
		}
	}
}
