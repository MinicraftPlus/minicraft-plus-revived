package minicraft.core;

import minicraft.core.CrashHandler.ErrorInfo.ErrorType;
import minicraft.core.io.ClipboardHandler;
import minicraft.network.Analytics;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Future;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.tinylog.Logger;

import kong.unirest.Empty;
import kong.unirest.HttpResponse;

public class CrashHandler {
	public static void crashHandle(Thread thread, Throwable throwable) { crashHandle(throwable); }
	public static void crashHandle(Throwable throwable) { crashHandle(throwable, new ErrorInfo(true)); }
	/** This handles application crashing errors by giving notification to the user clearly.<br>
	 * The user can only exit the program. */
	public static void crashHandle(Throwable throwable, ErrorInfo info) {
		throwable.printStackTrace();

		StringWriter string = new StringWriter();
		PrintWriter printer = new PrintWriter(string);
		throwable.printStackTrace(printer);

		Future<HttpResponse<Empty>> ping = Analytics.Crashes.ping();

		// Ensure ping finishes before program closes.
		if (GraphicsEnvironment.isHeadless() && ping != null) {
			try {
				ping.get();
			} catch (Exception ignored) {}
			return;
		}

		Logger.error("Crash: " + info.type.message + ": " + info.title + (info.message != null ? ": " + info.message : ""));
		JDialog dialog = new JDialog(Initializer.frame, "Crash: " + info.type.message, true);
		JLabel icon = new JLabel(UIManager.getIcon(info.serious ? "OptionPane.errorIcon" : "OptionPane.warningIcon"));
		icon.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		JPanel boxPanel1 = new JPanel();
		boxPanel1.setLayout(new BoxLayout(boxPanel1, BoxLayout.Y_AXIS));
		boxPanel1.add(icon);
		dialog.getContentPane().add(boxPanel1, BorderLayout.WEST);
		JLabel title = new JLabel(info.title);
		title.setFont(title.getFont().deriveFont(20.0f));
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		JPanel gridPanel = new JPanel(new GridBagLayout());
		JPanel boxPanel2 = new JPanel();
		boxPanel2.setLayout(new BoxLayout(boxPanel2, BoxLayout.Y_AXIS));
		gridPanel.add(boxPanel2);
		boxPanel2.add(title);
		if (info.message != null && info.message.length() > 0) {
			JLabel msg = new JLabel(info.message);
			msg.setFont(title.getFont().deriveFont(12.5f).deriveFont(Font.PLAIN));
			msg.setAlignmentX(JLabel.CENTER_ALIGNMENT);
			boxPanel2.add(msg);
		}

		gridPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
		dialog.getContentPane().add(gridPanel, BorderLayout.NORTH);
		dialog.getContentPane().add(getCrashPanel(info, getErrorScrollPane(string.toString()), dialog, string.toString()));
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);

		// Ensure ping finishes before program closes.
		if (ping != null) {
			try {
				ping.get();
			} catch (Exception ignored) {
			}
		}

		Logger.error("Application closes due to the crash.");
		System.exit(info.type.exitCode);
	}

	public static void errorHandle(Throwable throwable) { errorHandle(throwable, new ErrorInfo()); }
	public static void errorHandle(Throwable throwable, ErrorInfo info) { errorHandle(throwable, info, null); }
	/** This handles application crashing errors by giving notification to the user clearly.<br>
	 * The user can ignore the error, continue handling the error or exit the program (only in serious errors or error reports). */
	public static void errorHandle(Throwable throwable, ErrorInfo info, Action handling) {
		throwable.printStackTrace();

		StringWriter string = new StringWriter();
		PrintWriter printer = new PrintWriter(string);
		throwable.printStackTrace(printer);

		Future<HttpResponse<Empty>> ping = Analytics.Crashes.ping();

		// Ensure ping finishes before program closes.
		if (GraphicsEnvironment.isHeadless() && ping != null) {
			try {
				ping.get();
			} catch (Exception ignored) {}
			return;
		}

		Logger.error(info.type.message + ": " + info.title + (info.message != null ? ": " + info.message : ""));
		JDialog dialog = new JDialog(Initializer.frame, "Error: " + info.type.message, true);
		JLabel icon = new JLabel(info.serious ? UIManager.getIcon("OptionPane.errorIcon") : info.type == ErrorType.REPORT ? UIManager.getIcon("OptionPane.informationIcon") : UIManager.getIcon("OptionPane.warningIcon"));
		icon.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		JPanel boxPanel1 = new JPanel();
		boxPanel1.setLayout(new BoxLayout(boxPanel1, BoxLayout.Y_AXIS));
		boxPanel1.add(icon);
		dialog.getContentPane().add(boxPanel1, BorderLayout.WEST);
		JLabel title = new JLabel(info.title);
		title.setFont(title.getFont().deriveFont(20.0f));
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		JPanel gridPanel = new JPanel(new GridBagLayout());
		JPanel boxPanel2 = new JPanel();
		boxPanel2.setLayout(new BoxLayout(boxPanel2, BoxLayout.Y_AXIS));
		gridPanel.add(boxPanel2);
		boxPanel2.add(title);
		if (info.message != null && info.message.length() > 0) {
			JLabel msg = new JLabel(info.message);
			msg.setFont(title.getFont().deriveFont(12.5f).deriveFont(Font.PLAIN));
			msg.setAlignmentX(JLabel.CENTER_ALIGNMENT);
			boxPanel2.add(msg);
		}

		gridPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
		dialog.getContentPane().add(gridPanel, BorderLayout.NORTH);
		dialog.getContentPane().add(getErrorPanel(info, getErrorScrollPane(string.toString()), dialog, string.toString(), handling, ping));
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	private static JScrollPane getErrorScrollPane(String stackTrace) {
		JTextArea errorDisplay = new JTextArea(stackTrace);
		errorDisplay.setEditable(false);
		return new JScrollPane(errorDisplay);
	}
	private static JPanel getCrashPanel(ErrorInfo info, JScrollPane errorPane, JDialog dialog, String stackTrace) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(errorPane);
		JPanel buttonPanel = new JPanel();

		JButton copyButton = new JButton("Copy Error");
		ClipboardHandler clip = new ClipboardHandler();
		copyButton.addActionListener(e -> clip.setClipboardContents(info.type.message + ": " + info.title + (info.message != null ? ": " + info.message : "") + "\n" + stackTrace));
		buttonPanel.add(copyButton);

		JButton exitButton = new JButton("Exit Program");
		exitButton.addActionListener(e -> {
			dialog.setVisible(false);
			dialog.dispose();
		});
		dialog.getRootPane().setDefaultButton(exitButton);
		buttonPanel.add(exitButton);

		panel.add(buttonPanel, BorderLayout.SOUTH);
		return panel;
	}
	private static JPanel getErrorPanel(ErrorInfo info, JScrollPane errorPane, JDialog dialog, String stackTrace, Action callback, Future<HttpResponse<Empty>> ping) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(errorPane);
		JPanel buttonPanel = new JPanel();

		JButton copyButton = new JButton("Copy Error");
		ClipboardHandler clip = new ClipboardHandler();
		copyButton.addActionListener(e -> clip.setClipboardContents(info.type.message + ": " + info.title + (info.message != null ? ": " + info.message : "") + "\n" + stackTrace));
		buttonPanel.add(copyButton);

		if (info.serious || info.type == ErrorType.REPORT) {
			JButton exitButton = new JButton("Exit Program");
			exitButton.addActionListener(e -> {
				dialog.setVisible(false);
				dialog.dispose();

				// Ensure ping finishes before program closes.
				if (ping != null) {
					try {
						ping.get();
					} catch (Exception ignored) {
					}
				}

				Logger.error("Application closes due to the error.");
				System.exit(info.type.exitCode);
			});

			buttonPanel.add(exitButton);
		}

		JButton ignoreButton = new JButton("Ignore");
		ignoreButton.addActionListener(e -> {
			dialog.setVisible(false);
			dialog.dispose();
		});
		buttonPanel.add(ignoreButton);

		JButton continueButton = new JButton("Continue");
		continueButton.addActionListener(e -> {
			dialog.setVisible(false);
			dialog.dispose();
			callback.act();
		});
		dialog.getRootPane().setDefaultButton(continueButton);
		buttonPanel.add(continueButton);

		panel.add(buttonPanel, BorderLayout.SOUTH);
		return panel;
	}

	public static class ErrorInfo {
		public final String title;
		public final ErrorType type;
		public final String message;
		public final boolean serious;

		public ErrorInfo() { this(false); }
		public ErrorInfo(boolean crashing) { this(crashing ? "General Application Crash" : "General Application Error",
			crashing ? ErrorType.DEFAULT : ErrorType.REPORT); }
		public ErrorInfo(String topic) { this(topic, ErrorType.DEFAULT); }
		public ErrorInfo(String topic, ErrorType type) { this(topic, type, type.exitCode != 0); }
		public ErrorInfo(String topic, ErrorType type, boolean serious) { this(topic, type, serious, null); }
		public ErrorInfo(String topic, ErrorType type, String message) { this(topic, type, type.exitCode < 0, message); }
		public ErrorInfo(String topic, ErrorType type, boolean serious, String message) {
			this.title = topic;
			this.type = type;
			this.message = message;
			this.serious = serious;
		}

		public static enum ErrorType {
			DEFAULT (-1, "Unhandled error"),
			UNEXPECTED (-2, "Unexpected error"),
			UNHANDLEABLE (-3, "Unhandleable error"),
			SERIOUS (1, "Serious error"),
			HANDLED (0, "Handled error"),
			REPORT (0, "Error report"),
			;

			public final int exitCode;
			public final String message;

			ErrorType(int exitCode, String message) {
				this.exitCode = exitCode;
				this.message = message;
			}
		}
	}
}
