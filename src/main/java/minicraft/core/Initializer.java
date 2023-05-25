package minicraft.core;

import minicraft.core.io.FileHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.util.Logging;
import minicraft.util.TinylogLoggingProvider;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import org.tinylog.provider.ProviderRegistry;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

public class Initializer extends Game {
	private Initializer() {}

	/**
	 * Reference to actual frame, also it may be null.
	 */
	static JFrame frame;
	static LogoSplashCanvas logoSplash = new LogoSplashCanvas();
	static int fra, tik; // These store the number of frames and ticks in the previous second; used for fps, at least.

	public static JFrame getFrame() { return frame; }
	public static int getCurFps() { return fra; }

	static void parseArgs(String[] args) {
		// Parses command line arguments
		@Nullable
		String saveDir = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("--savedir") && i + 1 < args.length) {
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
			} else if (args[i].equalsIgnoreCase("--debug-level")) {
				Logging.logLevel = true;
			} else if (args[i].equalsIgnoreCase("--debug-filelog-full")) {
				Logging.fileLogFull = true;
			} else if (args[i].equalsIgnoreCase("--debug-locale")) {
				Localization.isDebugLocaleEnabled = true;
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
			if (currentDisplay == null) nsPerTick /= Updater.gamespeed;
			unprocessed += (now - lastTick) / nsPerTick; // Figures out the unprocessed time between now and lastTick.
			lastTick = now;
			while (unprocessed >= 1) { // If there is unprocessed time, then tick.
				ticks++;
				Updater.tick(); // Calls the tick method (in which it calls the other tick methods throughout the code.
				unprocessed--;
			}

			now = System.nanoTime();
			if (now >= lastRender + 1E9D / MAX_FPS) {
				frames++;
				lastRender = now;
				Renderer.render();
			}

			if (System.currentTimeMillis() - lastTimer1 > 1000) { //updates every 1 second
				long interval = System.currentTimeMillis() - lastTimer1;
				lastTimer1 = System.currentTimeMillis(); // Adds a second to the timer

				fra = (int) Math.round(frames * 1000D / interval); // Saves total frames in last second
				tik = (int) Math.round(ticks * 1000D / interval); // Saves total ticks in last second
				frames = 0; // Resets frames
				ticks = 0; // Resets ticks; ie, frames and ticks only are per second
			}
		}
	}


	// Creates and displays the JFrame window that the game appears in.
	static void createAndDisplayFrame() {
		Renderer.canvas.setMinimumSize(new java.awt.Dimension(1, 1));
		Renderer.canvas.setPreferredSize(Renderer.getWindowSize());
		Renderer.canvas.setBackground(Color.BLACK);
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
		private final Image logo;

		{
			try {
				logo = ImageIO.read(Objects.requireNonNull(Initializer.class.getResourceAsStream("/assets/textures/gui/title.png")));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		private int transparency = 255;
		private volatile boolean display = false;
		private volatile boolean inAnimation = false;
		private volatile boolean interruptWhenAnimated = false;

		public Thread renderer = new Thread(() -> {
			long lastTick = System.nanoTime();
			do {
				if (System.nanoTime() - lastTick >= 1E7) { // 10ms/tick
					repaint();
					lastTick = System.nanoTime();
				}

				if (interruptWhenAnimated && !inAnimation) break;
			} while (!Initializer.logoSplash.renderer.isInterrupted());
		}, "Logo Splash Screen Renderer");

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			final int w = g.getClipBounds().width;
			final int h = g.getClipBounds().height;

			// Drawing background
			// Drawing gradient border with width 100
			Graphics2D g2d = (Graphics2D) g;
			g2d.setPaint(new GradientPaint(100, 100, Color.WHITE, 0, 100, Color.GREEN)); // Left
			g2d.fillRect(0, 100, 100, h - 200);
			g2d.setPaint(new GradientPaint(w - 100, 100, Color.WHITE, w, 100, Color.GREEN)); // Right
			g2d.fillRect(w - 100, 100, 100, h - 200);
			g2d.setPaint(new GradientPaint(100, 100, Color.WHITE, 100, 0, Color.GREEN)); // Top
			g2d.fillRect(100, 0, w - 200, 100);
			g2d.setPaint(new GradientPaint(100, h - 100, Color.WHITE, 100, h, Color.GREEN)); // Bottom
			g2d.fillRect(100, h - 100, w - 200, 100);
			float[] fractions = new float[] { 0.0f, 1.0f };
			Color[] colors = new Color[] { Color.WHITE, Color.GREEN };
			g2d.setPaint(new RadialGradientPaint(new Rectangle(0, 0, 200, 200), fractions, colors, MultipleGradientPaint.CycleMethod.NO_CYCLE)); // Top Left
			g2d.fillRect(0, 0, 100, 100);
			g2d.setPaint(new RadialGradientPaint(new Rectangle(w - 200, 0, 200, 200), fractions, colors, MultipleGradientPaint.CycleMethod.NO_CYCLE)); // Top Right
			g2d.fillRect(w - 100, 0, 100, 100);
			g2d.setPaint(new RadialGradientPaint(new Rectangle(0, h - 200, 200, 200), fractions, colors, MultipleGradientPaint.CycleMethod.NO_CYCLE)); // Bottom Left
			g2d.fillRect(0, h - 100, 100, 100);
			g2d.setPaint(new RadialGradientPaint(new Rectangle(w - 200, h - 200, 200, 200), fractions, colors, MultipleGradientPaint.CycleMethod.NO_CYCLE)); // Bottom Right
			g2d.fillRect(w - 100, h - 100, 100, 100);
			// Drawing center white area
			g.setColor(Color.WHITE);
			g.fillRect(100, 100, w - 200, h - 200);

			// Green Border Fading effect
			g.setColor(new Color(255, 255, 255, Math.max(Math.min(255 - (int) (Math.pow(Math.cos(transparency/255.0 * Math.PI/2), 2) * 255), 255), 0)));
			g.fillRect(0, 0, w, h);

			// Drawing the centered logo
			if (transparency < 255) g.drawImage(logo, w/2 - logo.getWidth(frame)*2, h/2 - logo.getHeight(frame)*2, logo.getWidth(frame)*4, logo.getHeight(frame)*4, frame);

			// Overall Fading effect
			g.setColor(new Color(0, 0, 0, Math.max(Math.min(255 - (int) (Math.cos(transparency/255.0 * Math.PI/2) * 255), 255), 0)));
			g.fillRect(0, 0, w, h);

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

		public  void setDisplay(boolean display) {
			while (inAnimation) {} // Waiting for animation to finish.
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
