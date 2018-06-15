package minicraft.entity.particle;

import minicraft.gfx.Color;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;

public class TextParticle extends Particle {
	private String msg; // Message of the text particle
	private double xa, ya, za; // x,y,z acceleration
	private double xx, yy, zz; // x,y,z coordinates
	
	private FontStyle style;
	
	/**
	 * Creates a text particle which shows a message on the screen.
	 * 
	 * @param msg Message to display
	 * @param x X map position
	 * @param y Y map position
	 * @param col Text color
	 */
	public TextParticle(String msg, int x, int y, int col) {
		super(x, y, msg.length(), 60, null);
		
		style = new FontStyle(col).setShadowType(Color.BLACK, false);
		this.msg = msg;
		xx = x; //assigns x pos
		yy = y; //assigns y pos
		zz = 2; //assigns z pos to be 2
		
		//assigns x,y,z acceleration:
		xa = random.nextGaussian() * 0.3;
		ya = random.nextGaussian() * 0.2;
		za = random.nextFloat() * 0.7 + 2;
	}

	@Override
	public void tick() {
		super.tick();
		
		//move the particle according to the acceleration
		xx += xa;
		yy += ya;
		zz += za;
		if (zz < 0) {
			//if z pos if less than 0, alter accelerations...
			zz = 0;
			za *= -0.5;
			xa *= 0.6;
			ya *= 0.6;
		}
		za -= 0.15;  // za decreases by 0.15 every tick.
		//truncate x and y coordinates to integers:
		x = (int) xx;
		y = (int) yy;
	}
	
	@Override
	public void render(Screen screen) {
		//if(!msg.contains("Thanks")) {
		style.setXPos(x - msg.length() * 4).setYPos(y - (int)zz).draw(msg, screen);
		/*} else { // special, for "Thanks for Playing!" message? Second message is right below...
			String msg1 = msg.substring(0, 19);
			String msg2 = msg.substring(19, msg.length());
			Font.draw(msg1, screen, x - msg.length() * 4, y - (int)zz - 8, color, Color.BLACK);
			Font.draw(msg2, screen, x - msg.length() * 4, y - (int)zz, color, Color.BLACK);
		}*/
	}
	
	/**
	 * Returns the message and color divied by the character :.
	 * @return string representation of the particle
	 */
	public String getData() {
		return msg+":"+style.getColor();
	}
}
