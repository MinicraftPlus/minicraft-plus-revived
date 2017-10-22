package minicraft.core;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import minicraft.level.tile.Tiles;
import minicraft.saveload.Load;
import minicraft.screen.MultiplayerDisplay;
import minicraft.screen.TitleDisplay;
import minicraft.screen.WorldSelectDisplay;

class Initializer extends Game {
	private Initializer() {}
	
	static int fra, tik; //these store the number of frames and ticks in the previous second; used for fps, at least.
	
	/** The main method! *
	 *
	 * This is the main loop that runs the  It:
	 *	-sets up the frame
	 *  -initializes variables	
	 *	-keeps track of the amount of time that has passed
	 *	-fires the ticks needed to run the game
	 *	-fires the command to render out the screen.
	 */
	
	public static void main(String[] args) {
		
		/*Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				String exceptionTrace = "Exception in thread " + t + ":P\n";
				exceptionTrace += getExceptionTrace(e);
				System.err.println(exceptionTrace);
				javax.swing.JOptionPane.showInternalMessageDialog(null, exceptionTrace, "Fatal Error", javax.swing.JOptionPane.ERROR_MESSAGE);
			}
		});*/
		
		
		boolean debug = false;
		boolean autoclient = false;
		boolean autoserver = false;
		
		/// parses command line arguments
		String saveDir = FileHandler.systemGameDir;
		for(int i = 0; i < args.length; i++) {
			if(args[i].equals("--debug"))
				debug = true;
			if(args[i].equals("--savedir") && i+1 < args.length) {
				i++;
				saveDir = args[i];
			}
			if(args[i].equals("--localclient"))
				autoclient = true;
			if(args[i].equals("--server")) {
				autoserver = true;
				if(i+1 < args.length) {
					i++;
					WorldSelectDisplay.setWorldName(args[i], true);
				}
			}
		}
		Game.debug = debug;
		HAS_GUI = !autoserver;
		
		//if(HAS_GUI) {
			/*SplashScreen splash = SplashScreen.getSplashScreen();
			if (splash != null) {
				// TODO maybe in the future, I can make it so that the window is not displayed until the title menu is first rendered?
			}*/
		//}
		
		FileHandler.determineGameDir(saveDir);
		
		if(HAS_GUI) {
			
		}
		
		Network.autoclient = autoclient; // this will make the game automatically jump to the MultiplayerMenu, and attempt to connect to localhost.
		
		input = new InputHandler(Renderer.canvas);
		
		gameOver = false;
		
		running = true;
		
		
		Tiles.initTileList();
		
		Sound.init();
		
		World.resetGame(); // "half"-starts a new game, to set up initial variables
		player.eid = 0;
		new Load(true); // this loads any saved preferences.
		
		
		if(autoclient)
			setMenu(new MultiplayerDisplay( "localhost"));
		else if(!HAS_GUI)
			Network.startMultiplayerServer();//setMenu(null);//new WorldSelectMenu());
		else
			setMenu(new TitleDisplay()); //sets menu to the title screen.
		
		
		run();
		
		
		if (debug) System.out.println("main game loop ended; terminating application...");
		System.exit(0);
	}
	
	private static void run() {
		long lastTime = System.nanoTime();
		long lastRender = System.nanoTime();
		double unprocessed = 0;
		int frames = 0;
		int ticks = 0;
		long lastTimer1 = System.currentTimeMillis();
		
		//main game loop? calls tick() and render().
		if(!HAS_GUI)
			(new ConsoleReader()).start();
		while (running) {
			long now = System.nanoTime();
			double nsPerTick = 1E9D / Updater.normSpeed; // nanosecs per sec divided by ticks per sec = nanosecs per tick
			if(menu == null) nsPerTick /= Updater.gamespeed;
			unprocessed += (now - lastTime) / nsPerTick; //figures out the unprocessed time between now and lastTime.
			lastTime = now;
			boolean shouldRender = true;
			while (unprocessed >= 1) { // If there is unprocessed time, then tick.
				//if(debug) System.out.println("ticking...");
				ticks++;
				Updater.tick(); // calls the tick method (in which it calls the other tick methods throughout the code.
				unprocessed--;
				shouldRender = true; // sets shouldRender to be true... maybe tick() could make it false?
			}
			
			try {
				Thread.sleep(2); // makes a small pause for 2 milliseconds
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (shouldRender && (now - lastRender) / 1.0E9 > 1.0 / MAX_FPS) {
				frames++;
				lastRender = System.nanoTime();
				Renderer.render();
			}
			
			if (System.currentTimeMillis() - lastTimer1 > 1000) { //updates every 1 second
				lastTimer1 += 1000; // adds a second to the timer
				
				fra = frames; //saves total frames in last second
				tik = ticks; //saves total ticks in last second
				frames = 0; //resets frames
				ticks = 0; //resets ticks; ie, frames and ticks only are per second
			}
		}
	}
	
	private static void createAndDisplayFrame() {
		Renderer.canvas.setMinimumSize(new java.awt.Dimension(1, 1));
		Renderer.canvas.setPreferredSize(Renderer.getWindowSize());
		JFrame frame = new JFrame(NAME);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout()); // sets the layout of the window
		frame.add(Renderer.canvas, BorderLayout.CENTER); // Adds the game (which is a canvas) to the center of the screen.
		frame.pack(); //squishes everything into the preferredSize.
		
		try {
			BufferedImage logo = ImageIO.read(Game.class.getResourceAsStream("/resources/logo.png"));
			frame.setIconImage(logo);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		frame.setLocationRelativeTo(null); // the window will pop up in the middle of the screen when launched.
		
		frame.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				float w = frame.getWidth() - frame.getInsets().left - frame.getInsets().right;
				float h = frame.getHeight() - frame.getInsets().top - frame.getInsets().bottom;
				Renderer.SCALE = Math.min(w / Renderer.WIDTH, h / Renderer.HEIGHT);
			}
		});
		
		frame.addWindowListener(new WindowListener() {
			public void windowActivated(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowOpened(WindowEvent e) {}
			public void windowClosed(WindowEvent e) {System.out.println("window closed");}
			public void windowClosing(WindowEvent e) {
				System.out.println("window closing");
				if(isConnectedClient())
					client.endConnection();
				if(isValidServer())
					server.endConnection();
				
				quit();
			}
		});
		
		frame.setVisible(true);
	}
	
	
	/**
	 * Provides a String representation of the provided Throwable's stack trace
	 * that is extracted via PrintStream.
	 *
	 * @param throwable Throwable/Exception from which stack trace is to be
	 *	extracted.
	 * @return String with provided Throwable's stack trace.
	 */
	public static String getExceptionTrace(final Throwable throwable) {
		final java.io.ByteArrayOutputStream bytestream = new java.io.ByteArrayOutputStream();
		final java.io.PrintStream printStream = new java.io.PrintStream(bytestream);
		throwable.printStackTrace(printStream);
		String exceptionStr;
		try {
			exceptionStr = bytestream.toString("UTF-8");
		}
		catch(Exception ex) {
			exceptionStr = "Unavailable";
		}
		return exceptionStr;
	}
}
