package minicraft.core;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

import javax.imageio.ImageIO;
import javax.swing.*;

import minicraft.core.io.FileHandler;
import minicraft.util.Logging;
import minicraft.util.TinylogLoggingProvider;

import org.jetbrains.annotations.Nullable;
import org.tinylog.provider.ProviderRegistry;

public class Initializer extends Game {
	private Initializer() {}

	/**
	 * Reference to actual frame, also it may be null.
	 */
	static JFrame frame;
	static LogoSplashCanvas logoSplash = new LogoSplashCanvas();
	static int fra, tik; // These store the number of frames and ticks in the previous second; used for fps, at least.

	public static int getCurFps() { return fra; }

	static void parseArgs(String[] args) {
		// Parses command line arguments
		@Nullable
		String saveDir = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("--debug")) {
				debug = true;
			} else if (args[i].equalsIgnoreCase("--savedir") && i + 1 < args.length) {
				i++;
				saveDir = args[i];
			} else if (args[i].equalsIgnoreCase("--fullscreen")) {
				Updater.FULLSCREEN = true;
			} else if (args[i].equalsIgnoreCase("--debug-log-time")) {
				Logging.logTime = true;
			} else if (args[i].equalsIgnoreCase("--debug-log-thread")) {
				Logging.logThread = true;
			} else if (args[i].equalsIgnoreCase("--debug-log-trace")) {
				Logging.logTrace = true;
			} else if (args[i].equalsIgnoreCase("--debug-filelog-full")) {
				Logging.fileLogFull = true;
			}
		}
		((TinylogLoggingProvider) ProviderRegistry.getLoggingProvider()).init();

		FileHandler.determineGameDir(saveDir);
	}

	/** This is the main loop that runs the game. It:
	 *	-keeps track of the amount of time that has passed
	 *	-fires the ticks needed to run the game
	 *	-fires the command to render out the screen.
	 */
	static void run() {
		long lastTick = System.nanoTime();
		long lastRender = System.nanoTime();
		double unprocessed = 0;
		int frames = 0;
		int ticks = 0;
		long lastTimer1 = System.currentTimeMillis();

		while (running) {
			long now = System.nanoTime();
			double nsPerTick = 1E9D / Updater.normSpeed; // Nanosecs per sec divided by ticks per sec = nanosecs per tick
			if (display == null) nsPerTick /= Updater.gamespeed;
			unprocessed += (now - lastTick) / nsPerTick; // Figures out the unprocessed time between now and lastTick.
			lastTick = now;
			while (unprocessed >= 1) { // If there is unprocessed time, then tick.
				ticks++;
				Updater.tick(); // Calls the tick method (in which it calls the other tick methods throughout the code.
				unprocessed--;
			}

			if ((now - lastRender) / 1.0E9 > 1.0 / MAX_FPS) {
				frames++;
				lastRender = System.nanoTime();
				Renderer.render();
			}

			if (System.currentTimeMillis() - lastTimer1 > 1000) { //updates every 1 second
				lastTimer1 += 1000; // Adds a second to the timer

				fra = frames; // Saves total frames in last second
				tik = ticks; // Saves total ticks in last second
				frames = 0; // Resets frames
				ticks = 0; // Resets ticks; ie, frames and ticks only are per second
			}
		}
	}


	// Creates and displays the JFrame window that the game appears in.
	static void createAndDisplayFrame() {
		Renderer.canvas.setMinimumSize(new java.awt.Dimension(1, 1));
		Renderer.canvas.setPreferredSize(Renderer.getWindowSize());
		Renderer.canvas.setBackground(Color.WHITE);
		logoSplash.setMinimumSize(new java.awt.Dimension(1, 1));
		logoSplash.setPreferredSize(Renderer.getWindowSize());
		JFrame frame = Initializer.frame = new JFrame(NAME);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout()); // Sets the layout of the window
		frame.add(logoSplash, BorderLayout.CENTER);
		frame.pack(); // Squishes everything into the preferredSize.

		try {
			BufferedImage logo = ImageIO.read(Game.class.getResourceAsStream("/resources/logo.png")); // Load the window logo
			frame.setIconImage(logo);
		} catch (IOException e) {
			CrashHandler.errorHandle(e);
		}

		frame.setLocationRelativeTo(null); // The window will pop up in the middle of the screen when launched.

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
			public void windowClosed(WindowEvent e) { Logging.GAMEHANDLER.debug("Window closed"); }
			public void windowClosing(WindowEvent e) {
				Logging.GAMEHANDLER.info("Window closing");
				quit();
			}
		});

		frame.setVisible(true);
		logoSplash.setDisplay(true);
		logoSplash.renderer.start();
	}

	private static class LogoSplashCanvas extends JPanel {
		private Image logo;

		{
			try {
				logo = ImageIO.read(Objects.requireNonNull(Initializer.class.getResourceAsStream("/assets/textures/gui/title.png")));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		private int transparency = 255;
		private boolean display = false;
		private boolean inAnimation = false;
		private boolean interruptWhenAnimated = false;

		public Thread renderer = new Thread(() -> {
			do {
				repaint();
				if (interruptWhenAnimated && !inAnimation) break;
			} while (!Initializer.logoSplash.renderer.isInterrupted());
		}, "Logo Splash Screen Renderer");

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (transparency < 255) g.drawImage(logo, getWidth()/2 - logo.getWidth(frame)*2, getHeight()/2 - logo.getHeight(frame)*2, logo.getWidth(frame)*4, logo.getHeight(frame)*4, frame);
			if (transparency > 0) {
				g.setColor(new Color(255, 255, 255, transparency));
				g.fillRect(0, 0, g.getClipBounds().width, g.getClipBounds().height);
			}

			if (inAnimation) {
				if (display) {
					if (transparency > 0) transparency -= 5;
					else inAnimation = false;
				} else {
					if (transparency < 255) transparency += 5;
					else inAnimation = false;
				}
			}
		}

		public void setDisplay(boolean display) {
			this.display = display;
			inAnimation = true;
		}
	}

	/** Remove the logo splash screen and start canvas rendering. */
	static void startCanvasRendering() {
		logoSplash.setDisplay(false);
		logoSplash.interruptWhenAnimated = true;
		try {
			logoSplash.renderer.join();
		} catch (InterruptedException ignored) {}
		logoSplash.renderer.interrupt();
		frame.remove(logoSplash);
		frame.add(Renderer.canvas, BorderLayout.CENTER); // Adds the game (which is a canvas) to the center of the screen.
		frame.pack();
		frame.revalidate();
		logoSplash = null; // Discard the canvas.
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
		} catch (Exception ex) {
			exceptionStr = "Unavailable";
		}
		return exceptionStr;
	}
}
