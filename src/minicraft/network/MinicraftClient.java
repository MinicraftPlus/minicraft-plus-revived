package minicraft.network;

import java.util.ArrayList;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import minicraft.Game;
import minicraft.InputHandler;

/// This class is only used by the client runtime; the server runtime doesn't touch it.
public class MinicraftClient extends Thread {
	
	public static String hostName = "localhost";
	
	public Socket socket = null;
	public boolean done = false;
	
	private PrintWriter out;
	private BufferedReader in;
	private String presses = "";
	
	public int[] pixels = null;
	
	public MinicraftClient(String hostName) {
		super("MinicraftClient");
		this.hostName = hostName;
	}
	
	public void run() {
		// this constantly checks for screen pixel updates, and then writes them to an array.
		System.out.println("starting client thread");
		
		try {
			socket = new Socket(hostName, MinicraftProtocol.PORT);
			
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + hostName);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " +
				hostName);
		}
		
		done = true;
		
		while(isConnected()) {
			try {
				if(in.ready()) {
					if (Game.debug) System.out.println("reading input (screen pixels) from host connection...");
					String str = in.readLine();
					if(str == null || str.length() == 0) continue;
					//System.out.println("screen pixels read from host: " + str);
					String[] pixelStrings = str.split(",");
					
					int[] newpixels = new int[pixelStrings.length];
					for(int i = 0; i < pixelStrings.length; i++) {
						//System.out.println("converting pixel: \"" + pixelStrings[i] + "\"");
						newpixels[i] = Integer.parseInt(pixelStrings[i]);
					}
					pixels = newpixels;
				}
			} catch (IOException ex) {
				System.err.println("Couldn't get pixels from the connection to " + hostName);
				ex.printStackTrace();
			}
		}
		
		if (Game.debug) System.out.println("client socket no longer connected; ending connection and terminating thread");
		
		endConnection();
	}
	
	public void cacheInput(String keyname) {
		presses += keyname + ",";
	}
	
	public void sendCachedInput() {
		if(!done || !isConnected()) return;
		
		if(presses.length() > 0) {
			if (Game.debug) System.out.println("sending key press data to server: " + presses);
			out.println(presses);
			presses = "";
		}
	}
	
	/*public void checkConnection() {
		//System.out.println("checking connection -- client side...");
		//out.write("");
		if(!isConnected()) endConnection();
	}*/
	
	public void endConnection() {
		if (Game.debug) System.out.println("closing client socket and ending connection");
		try {
			socket.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {}
	}
	
	public boolean isConnected() {
		return socket != null && !socket.isClosed() && socket.isConnected();
	}
}
