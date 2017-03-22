package com.mojang.ld22;

import com.mojang.ld22.Game;
import java.applet.Applet;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class GameApplet extends Applet {

	private static final long serialVersionUID = 1L;
	private Game game = new Game();
	URL location;
	public static boolean isApplet = false;
	public static String username = "Guest";


	public void init() {
		this.location = this.getDocumentBase();

		try {
			this.location = new URL("http://playminicraft.com/mod.php?m=minicraft-plus");
		} catch (MalformedURLException var5) {
			var5.printStackTrace();
		}

		isApplet = true;
		this.setLayout(new BorderLayout());
		this.setSize(288 * 3 + 20, 192 * 3 + 20);
		this.add(this.game, "Center");

		try {
			URLConnection yc = this.location.openConnection();
			yc.setDoOutput(true);
			yc.setDoInput(true);
			yc.setUseCaches(false);
			yc.setAllowUserInteraction(false);
			yc.setRequestProperty("Content-type", "text/xml; charset=UTF-8");
			OutputStream e = yc.getOutputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

			String inputLine;
			while((inputLine = in.readLine()) != null) {
				if(this.location.toString().startsWith("http") && inputLine.contains("nme=")) {
					username = inputLine.substring(inputLine.indexOf("nm") + 4, inputLine.lastIndexOf("&amp;"));
					if(username.length() == 0) {
						username = "Guest";
					}
				}
			}

			in.close();
			e.close();
		} catch (IOException var6) {
			var6.printStackTrace();
		}

	}

	public void start() {
		this.game.start();
	}

	public void stop() {
		this.game.stop();
	}
}
