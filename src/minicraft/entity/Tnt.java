package minicraft.entity;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Timer;
import minicraft.gfx.Color;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.sound.Sound;

public class Tnt extends Furniture implements ActionListener {
	private static int MAX_FUSE_TIME = 60;
	private static int BLAST_RADIUS = 30;
	private static int BLAST_RADIUSTWO = 2000;
	private static int BLAST_DAMAGE = 30;
	
	private static int color = Color.get(-1, 200, 300, 555);
	
	//protected int dir = 0;
	//private int lvl;
	//private int counter = 0;
	//private int fuseTime = 0;
	private int ftik = 0;
	private boolean fuseLit = false;
	//private boolean flashing = false;
	//public Tile tile;

	Timer t;
	//Timer s = new Timer(50, this);
	//Timer flash = new Timer(5, this);
	private Level levelSave;
	
	public Tnt() {
		super("Tnt", color, 7, 3, 2);
		//flashing = false;
		fuseLit = false;
		ftik = 0;
		
		t = new Timer(500, this);
	}

	public void tick() {
		super.tick();
		
		if (fuseLit) {
			ftik++;
			
			int colFctr = 100*((ftik%15)/5) + 200;
			col = Color.get(-1, colFctr, colFctr+100, 555);
			
			if(ftik >= 120) {
				//if(minicraft.Game.debug) System.out.println("BLOW UP!");
				//s.start();
				//ftik = 0;
				//flashing = false;
				
				// blow up
				int pdx = Math.abs(level.player.x - x);
				int pdy = Math.abs(level.player.y - y);
				if (pdx < BLAST_RADIUSTWO && pdy < BLAST_RADIUSTWO) {
					float pd = (float) Math.sqrt(pdx * pdx + pdy * pdy);
					int dmg = (int) (BLAST_DAMAGE * (1 - (pd / BLAST_RADIUS))) + 1;
					level.player.hurt(this, dmg, 0);
					level.player.payStamina(dmg * 2);
				}
				Sound.explode.play();
				
				int xt = x >> 4;
				int yt = (y - 2) >> 4;
				
				level.setAreaTiles(xt, yt, 1, Tile.explode, 0);
				/*
				level.setTile(xt, yt, Tile.explode, 0);
				level.setTile(xt - 1, yt, Tile.explode, 0);
				level.setTile(xt + 1, yt, Tile.explode, 0);
				level.setTile(xt, yt - 1, Tile.explode, 0);
				level.setTile(xt, yt + 1, Tile.explode, 0);
				level.setTile(xt - 1, yt - 1, Tile.explode, 0);
				level.setTile(xt + 1, yt + 1, Tile.explode, 0);
				level.setTile(xt + 1, yt - 1, Tile.explode, 0);
				level.setTile(xt - 1, yt + 1, Tile.explode, 0);
				*/
				t.start();
				
				levelSave = level;
				super.remove();
			}
		}
		/*
		if (fuseLit && fuseTime == 0) {
			
		} else if (fuseLit) {
			fuseTime--;
		}*/
	}
	
	public void actionPerformed(ActionEvent e) {
		int xt = x >> 4;
		int yt = (y - 2) >> 4;
		if (e.getSource() == t) {
			levelSave.setAreaTiles(xt, yt, 1, Tile.hole, 0);
			/*level.setTile(xt, yt, Tile.hole, 0);
			level.setTile(xt - 1, yt, Tile.hole, 0);
			level.setTile(xt + 1, yt, Tile.hole, 0);
			level.setTile(xt, yt - 1, Tile.hole, 0);
			level.setTile(xt, yt + 1, Tile.hole, 0);
			level.setTile(xt - 1, yt - 1, Tile.hole, 0);
			level.setTile(xt + 1, yt + 1, Tile.hole, 0);
			level.setTile(xt + 1, yt - 1, Tile.hole, 0);
			level.setTile(xt - 1, yt + 1, Tile.hole, 0);*/
			t.stop();
			levelSave = null;
		}/*
		if (e.getSource() == flash) {
			flashing = true;
			flash.stop();
		}
		if (e.getSource() == s) {
			fuseTime = MAX_FUSE_TIME;
			fuseLit = true;
			s.stop();
		}*/
	}
	
	public boolean canWool() {
		return true;
	}
	
	public void hurt(Mob m, int dmg, int attackDir) {
		if (!fuseLit) {
			fuseLit = true;
			Sound.fuse.play();
			//flash.start();
		}
		//entity.hurt(level.getTile(x >> 4, y >> 4), x, y, 1);
	}
}
