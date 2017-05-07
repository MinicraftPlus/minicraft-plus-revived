//new class, no comments.
package minicraft.entity;

import java.awt.event.*;
import java.util.*;
import javax.swing.Timer;
import minicraft.gfx.Color;
import minicraft.level.tile.Tile;
import minicraft.sound.Sound;

public class Tnt extends Furniture implements ActionListener {
	private static int MAX_FUSE_TIME = 60;
	private static int BLAST_RADIUS = 30;
	private static int BLAST_RADIUSTWO = 2000;
	private static int BLAST_DAMAGE = 30;
	
	private static int color = Color.get(-1, 200, 300, 555);
	
	protected int dir = 0;
	private int lvl;
	private int counter = 0;
	private int fuseTime = 0;
	private int ftik = 0;
	private boolean fuseLit = false;
	private boolean running = true;
	private boolean flashing = false;
	public Tile tile;

	Timer t = new Timer(500, this);
	Timer s = new Timer(50, this);
	Timer flash = new Timer(5, this);

	public Tnt() {
		super("Tnt", color, 7, 3, 2);
		/*
		col0 = Color.get(-1, 200, 300, 444);
		col1 = Color.get(-1, 200, 300, 555);
		col2 = Color.get(-1, 100, 200, 444);
		col3 = Color.get(-1, 000, 100, 333);
		*//*
		col = Color.get(-1, 200, 300, 555);
		sprite = 7;
		xr = 3;
		yr = 2;
		*/
	}

	public void tick() {
		super.tick();
		
		if (flashing) {
			ftik++;
			
			int colFctr = 100*(ftik/5) + 200;
			col = Color.get(-1, colFctr, colFctr+100, 555);
			/*if (ftik < 5) {
				col0 = Color.get(-1, 200, 300, 555);
				col1 = Color.get(-1, 200, 300, 555);
				col2 = Color.get(-1, 200, 300, 555);
				col3 = Color.get(-1, 200, 300, 555);
			}
			else if (ftik < 10) {
				col0 = Color.get(-1, 300, 400, 555);
				col1 = Color.get(-1, 300, 400, 555);
				col2 = Color.get(-1, 300, 400, 555);
				col3 = Color.get(-1, 300, 400, 555);
			}
			else if (ftik < 14) {
				col0 = Color.get(-1, 400, 500, 555);
				col1 = Color.get(-1, 400, 500, 555);
				col2 = Color.get(-1, 400, 500, 555);
				col3 = Color.get(-1, 400, 500, 555);
			}*/
			if(ftik >= 15) {
				if(minicraft.Game.debug) System.out.println("BLOW UP!");
				s.start();
				ftik = 0;
				flashing = false;
			}
		}

		if (fuseTime == 0) {
			if (fuseLit) {
				// blow up
				int pdx = Math.abs(level.player.x - x);
				int pdy = Math.abs(level.player.y - y);
				if (pdx < BLAST_RADIUSTWO && pdy < BLAST_RADIUSTWO) {
					float pd = (float) Math.sqrt(pdx * pdx + pdy * pdy);
					int dmg = (int) (BLAST_DAMAGE * (1 - (pd / BLAST_RADIUS))) + 1;
					level.player.hurt(this, dmg, 0);
					level.player.payStamina(dmg * 2);
					Sound.explode.play();

					// figure out which tile the mob died on
					int xt = x >> 4;
					int yt = (y - 2) >> 4;

					level.setTile(xt, yt, Tile.explode, 0);
					level.setTile(xt - 1, yt, Tile.explode, 0);
					level.setTile(xt + 1, yt, Tile.explode, 0);
					level.setTile(xt, yt - 1, Tile.explode, 0);
					level.setTile(xt, yt + 1, Tile.explode, 0);
					level.setTile(xt - 1, yt - 1, Tile.explode, 0);
					level.setTile(xt + 1, yt + 1, Tile.explode, 0);
					level.setTile(xt + 1, yt - 1, Tile.explode, 0);
					level.setTile(xt - 1, yt + 1, Tile.explode, 0);
					t.start();

					super.remove();
				} else {
					fuseTime = 0;
					fuseLit = false;
				}
			}
		} else {
			fuseTime--;
		}
	}

	public void actionPerformed(ActionEvent e) {
		int xt = x >> 4;
		int yt = (y - 2) >> 4;
		if (e.getSource() == t) {
			level.setTile(xt, yt, Tile.hole, 0);
			level.setTile(xt - 1, yt, Tile.hole, 0);
			level.setTile(xt + 1, yt, Tile.hole, 0);
			level.setTile(xt, yt - 1, Tile.hole, 0);
			level.setTile(xt, yt + 1, Tile.hole, 0);
			level.setTile(xt - 1, yt - 1, Tile.hole, 0);
			level.setTile(xt + 1, yt + 1, Tile.hole, 0);
			level.setTile(xt + 1, yt - 1, Tile.hole, 0);
			level.setTile(xt - 1, yt + 1, Tile.hole, 0);
			t.stop();
		}
		if (e.getSource() == flash) {
			flashing = true;
			flash.stop();
		}
		if (e.getSource() == s) {
			fuseTime = MAX_FUSE_TIME;
			fuseLit = true;
			s.stop();
		}
	}

	public boolean canWool() {
		return true;
	}

	protected void touchedBy(Entity entity) {
		if (entity instanceof Player) {
			if (fuseTime == 0) {
				Sound.fuse.play();
				flash.start();
			}
			entity.hurt(level.getTile(x >> 4, y >> 4), x, y, 1);
		}
	}
}
